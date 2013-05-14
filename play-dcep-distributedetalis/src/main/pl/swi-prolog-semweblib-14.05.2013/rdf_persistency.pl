/*  Part of SWI-Prolog

    Author:        Jan Wielemaker
    E-mail:        J.Wielemaker@vu.nl
    WWW:           http://www.swi-prolog.org
    Copyright (C): 2002-2012, University of Amsterdam
			      VU University Amsterdam

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

    As a special exception, if you link this library with other files,
    compiled with a Free Software compiler, to produce an executable, this
    library does not by itself cause the resulting executable to be covered
    by the GNU General Public License. This exception does not however
    invalidate any other reasons why the executable file might be covered by
    the GNU General Public License.
*/

:- module(rdf_persistency,
	  [ rdf_attach_db/2,		% +Directory, +Options
	    rdf_detach_db/0,		% +Detach current DB
	    rdf_current_db/1,		% -Directory
	    rdf_persistency/2,		% +DB, +Bool
	    rdf_flush_journals/1,	% +Options
	    rdf_journal_file/2,		% ?DB, ?JournalFile
	    rdf_db_to_file/2		% ?DB, ?FileBase
	  ]).
:- use_module(library(semweb/rdf_db)).
:- use_module(library(filesex)).
:- use_module(library(lists)).
:- use_module(library(uri)).
:- use_module(library(debug)).
:- use_module(library(error)).
:- use_module(library(thread)).
:- use_module(library(pairs)).
:- use_module(library(apply)).


/** <module> RDF persistency plugin

This  module  provides  persistency   for    rdf_db.pl   based   on  the
rdf_monitor/2 predicate to  track  changes   to  the  repository.  Where
previous  versions  used  autosafe  of  the  whole  database  using  the
quick-load format of rdf_db, this version is  based on a quick-load file
per source (4th argument of rdf/4), and journalling for edit operations.

The result is safe, avoids frequent small   changes to large files which
makes synchronisation and backup expensive and avoids long disruption of
the server doing the autosafe. Only loading large files disrupts service
for some time.

The persistent backup of the database is  realised in a directory, using
a lock file to avoid corruption due to concurrent access. Each source is
represented by two files, the latest snapshot   and a journal. The state
is restored by loading  the  snapshot   and  replaying  the journal. The
predicate rdf_flush_journals/1 can be used to create fresh snapshots and
delete the journals.

@tbd If there is a complete `.new'   snapshot  and no journal, we should
     move the .new to the plain snapshot name as a means of recovery.

@tbd Backup of each graph using one or two files is very costly if there
     are many graphs.  Although the currently used subdirectories avoid
     hitting OS limits early, this is still not ideal. Probably we
     should collect (small, older?) files and combine them into a single
     quick load file.  We could call this (similar to GIT) a `pack'.

@see	rdf_edit.pl
*/

:- volatile
	rdf_directory/1,
	rdf_lock/2,
	rdf_option/1,
	source_journal_fd/2,
	file_base_db/2.
:- dynamic
	rdf_directory/1,		% Absolute path
	rdf_lock/2,			% Dir, Lock
	rdf_option/1,			% Defined options
	source_journal_fd/2,		% DB, JournalFD
	file_base_db/2.			% FileBase, DB

:- meta_predicate
	no_agc(0).

:- predicate_options(rdf_attach_db/2, 2,
		     [ concurrency(positive_integer),
		       max_open_journals(positive_integer),
		       silent(oneof([true,false,brief])),
		       log_nested_transactions(boolean)
		     ]).

%%	rdf_attach_db(+Directory, +Options)
%
%	Start persistent operations using Directory   as  place to store
%	files.   There are several cases:
%
%		* Empty DB, existing directory
%		Load the DB from the existing directory
%
%		* Full DB, empty directory
%		Create snapshots for all sources in directory
%
%	Options:
%
%		* concurrency(+Jobs)
%		Number of threads to use for loading the initial
%		database.  If not provided it is the number of CPUs
%		as optained from the flag =cpu_count=.
%
%		* max_open_journals(+Count)
%		Maximum number of journals kept open.  If not provided,
%		the default is 10.  See limit_fd_pool/0.
%
%		* directory_levels(+Count)
%		Number of levels of intermediate directories for storing
%		the graph files.  Default is 2.
%
%		* silent(+BoolOrBrief)
%		If =true= (default =false=), do not print informational
%		messages.  Finally, if =brief= it will show minimal
%		feedback.
%
%		* log_nested_transactions(+Boolean)
%		If =true=, nested _log_ transactions are added to the
%		journal information.  By default (=false=), no log-term
%		is added for nested transactions.

rdf_attach_db(DirSpec, Options) :-
	absolute_file_name(DirSpec,
			   Directory,
			   [ access(write),
			     file_type(directory),
			     file_errors(fail)
			   ]), !,
	(   rdf_directory(Directory)
	->  true			% update settings?
	;   rdf_detach_db,
	    mkdir(Directory),
	    lock_db(Directory),
	    assert(rdf_directory(Directory)),
	    assert_options(Options),
	    stop_monitor,		% make sure not to register load
	    no_agc(load_db),
	    at_halt(rdf_detach_db),
	    start_monitor
	).
rdf_attach_db(DirSpec, Options) :-
	absolute_file_name(DirSpec,
			   Directory,
			   [ solutions(all)
			   ]),
	(   exists_directory(Directory)
	->  access_file(Directory, write)
	;   catch(make_directory(Directory), _, fail)
	), !,
	rdf_attach_db(Directory, Options).


assert_options([]).
assert_options([H|T]) :-
	(   option_type(H, Check)
	->  Check,
	    assert(rdf_option(H))
	;   true			% ignore options we do not understand
	),
	assert_options(T).

option_type(concurrency(X),		must_be(positive_integer, X)).
option_type(max_open_journals(X),	must_be(positive_integer, X)).
option_type(directory_levels(X),	must_be(positive_integer, X)).
option_type(silent(X),	       must_be(oneof([true,false,brief]), X)).
option_type(log_nested_transactions(X),	must_be(boolean, X)).


%%	no_agc(:Goal)
%
%	Run Goal with atom garbage collection   disabled. Loading an RDF
%	database creates large amounts  of  atoms   we  *know*  are  not
%	garbage.

no_agc(Goal) :-
	current_prolog_flag(agc_margin, Old),
	setup_call_cleanup(
	    set_prolog_flag(agc_margin, 0),
	    Goal,
	    set_prolog_flag(agc_margin, Old)).


%%	rdf_detach_db is det.
%
%	Detach from the  current  database.   Succeeds  silently  if  no
%	database is attached. Normally called at  the end of the program
%	through at_halt/1.

rdf_detach_db :-
	debug(halt, 'Detaching RDF database', []),
	stop_monitor,
	close_journals,
	(   retract(rdf_directory(Dir))
	->  debug(halt, 'DB Directory: ~w', [Dir]),
	    save_prefixes(Dir),
	    retractall(rdf_option(_)),
	    retractall(source_journal_fd(_,_)),
	    unlock_db(Dir)
	;   true
	).


%%	rdf_current_db(?Dir)
%
%	True if Dir is the current RDF persistent database.

rdf_current_db(Directory) :-
	rdf_directory(Dir), !,
	Dir = Directory.


%%	rdf_flush_journals(+Options)
%
%	Flush dirty journals.  Options:
%
%		* min_size(+KB)
%		Only flush if journal is over KB in size.
%
%	@tbd Provide a default for min_size?

rdf_flush_journals(Options) :-
	forall(rdf_graph(DB),
	       rdf_flush_journal(DB, Options)).

rdf_flush_journal(DB, Options) :-
	db_files(DB, _SnapshotFile, JournalFile),
	db_file(JournalFile, File),
	(   \+ exists_file(File)
	->  true
	;   memberchk(min_size(KB), Options),
	    size_file(JournalFile, Size),
	    Size / 1024 < KB
	->  true
	;   create_db(DB)
	).

		 /*******************************
		 *	       LOAD		*
		 *******************************/

%%	load_db is det.
%
%	Reload database from the directory specified by rdf_directory/1.
%	First we find all names graphs using find_dbs/1 and then we load
%	them.

load_db :-
	rdf_directory(Dir),
	get_time(Wall0),
	statistics(cputime, T0),
	load_prefixes(Dir),
	find_dbs(Dir, DBs),
	length(DBs, DBCount),
	verbosity(DBCount, Silent),
	make_goals(DBs, Silent, 1, DBCount, Goals),
	concurrency(Jobs),
	concurrent(Jobs, Goals, []),
	statistics(cputime, T1),
	get_time(Wall1),
	T is T1 - T0,
	Wall is Wall1 - Wall0,
	message_level(Silent, Level),
	print_message(Level, rdf(restore(attached(DBCount, T/Wall)))).


make_goals([], _, _, _, []).
make_goals([DB|T0], Silent, I,  Total,
	   [load_source(DB, Silent, I, Total)|T]) :-
	I2 is I + 1,
	make_goals(T0, Silent, I2, Total, T).

verbosity(_DBCount, Silent) :-
	rdf_option(silent(Silent)), !.
verbosity(_DBCount, brief).


%%	concurrency(-Jobs)
%
%	Number of jobs to run concurrently.

concurrency(Jobs) :-
	rdf_option(concurrency(Jobs)), !.
concurrency(Jobs) :-
	current_prolog_flag(cpu_count, Jobs),
	Jobs > 0, !.
concurrency(1).


%%	find_dbs(+Dir, -DBs:list(atom), -Depth) is det.
%
%	DBs is a  list  of  database   (named  graph)  names,  sorted in
%	increasing file-size. Small files  are   loaded  first  as these
%	typically contain the schemas and we   want  to avoid re-hashing
%	large databases due to added rdfs:subPropertyOf triples.
%
%	@param Depth is the depth at which all files have been found.

find_dbs(Dir, DBs) :-
	find_dbs(Dir, DBs, Depth),
	(   var(Depth)			% empty DB
	->  true
	;   length(DBs, Count),
	    LevelsRequired is floor(log(Count)/log(256)),
	    Depth < LevelsRequired
	->  print_message(informational, rdf(reindex(Count, LevelsRequired))),
	    reindex_db(Dir, LevelsRequired),
	    retractall(rdf_option(directory_levels(_))),
	    assertz(rdf_option(directory_levels(LevelsRequired)))
	;   retractall(rdf_option(directory_levels(_))),
	    assertz(rdf_option(directory_levels(Depth)))
	).

find_dbs(Dir, DBs, Depth) :-
	directory_files(Dir, Files),
	phrase(scan_db_files(Files, Dir, '.', 0), Scanned),
	sort(Scanned, ByDB),
	join_snapshot_and_journals(ByDB, BySize),
	keysort(BySize, SortedBySize),
	pairs_values(SortedBySize, DBs),
	(   maplist(depth(Depth), Scanned)
	->  true
	;   length(DBs, Count),
	    Levels is floor(log(Count)/log(256)),
	    print_message(warning, rdf(fix_index(Levels))),
	    reindex_db(Dir, Levels)
	).

depth(Depth, DB) :-
	arg(4, DB, Depth).

%%	scan_db_files(+Files, +Dir, +Prefix, +Depth)// is det.
%
%	Produces a list of db(DB,  Size,   File)  for all recognised RDF
%	database files.  File is relative to the database directory Dir.

scan_db_files([], _, _, _) -->
	[].
scan_db_files([Nofollow|T], Dir, Prefix, Depth) -->
	{ nofollow(Nofollow) }, !,
	scan_db_files(T, Dir, Prefix, Depth).
scan_db_files([File|T], Dir, Prefix, Depth) -->
	{ file_name_extension(Base, Ext, File),
	  db_extension(Ext), !,
	  rdf_db_to_file(DB, Base),
	  directory_file_path(Prefix, File, DBFile),
	  directory_file_path(Dir, DBFile, AbsFile),
	  size_file(AbsFile, Size)
	},
	[ db(DB, Size, DBFile, Depth) ],
	scan_db_files(T, Dir, Prefix, Depth).
scan_db_files([D|T], Dir, Prefix, Depth) -->
	{ directory_file_path(Prefix, D, SubD),
	  directory_file_path(Dir, SubD, AbsD),
	  exists_directory(AbsD),
	  \+ read_link(AbsD, _, _), !,	% Do not follow links
	  directory_files(AbsD, SubFiles),
	  SubDepth is Depth + 1
	},
	scan_db_files(SubFiles, Dir, SubD, SubDepth),
	scan_db_files(T, Dir, Prefix, Depth).
scan_db_files([_|T], Dir, Prefix, Depth) -->
	scan_db_files(T, Dir, Prefix, Depth).

nofollow(.).
nofollow(..).

db_extension(trp).
db_extension(jrn).

join_snapshot_and_journals([], []).
join_snapshot_and_journals([db(DB,S0,_,_)|T0], [S-DB|T]) :- !,
	same_db(DB, T0, T1, S0, S),
	join_snapshot_and_journals(T1, T).

same_db(DB, [db(DB,S1,_,_)|T0], T, S0, S) :- !,
	S2 is S0+S1,
	same_db(DB, T0, T, S2, S).
same_db(_, L, L, S, S).


:- public load_source/4.		% called through make_goals/5

%%	load_source(+DB, +Silent, +Nth, +Total) is det.
%
%	Load triples and reload  journal   from  the  indicated snapshot
%	file.
%
%	@param Silent One of =false=, =true= or =brief=

load_source(DB, Silent, Nth, Total) :-
	message_level(Silent, Level),
	db_files(DB, SnapshotFile, JournalFile),
	rdf_retractall(_,_,_,DB),
	statistics(cputime, T0),
	print_message(Level, rdf(restore(Silent, source(DB, Nth, Total)))),
	db_file(SnapshotFile, AbsSnapShot),
	(   exists_file(AbsSnapShot)
	->  print_message(Level, rdf(restore(Silent, snapshot(SnapshotFile)))),
	    rdf_load_db(AbsSnapShot)
	;   true
	),
	(   exists_db(JournalFile)
	->  print_message(Level, rdf(restore(Silent, journal(JournalFile)))),
	    load_journal(JournalFile, DB)
	;   true
	),
	statistics(cputime, T1),
	T is T1 - T0,
	(   rdf_statistics(triples_by_file(DB, Count))
	->  true
	;   Count = 0
	),
	print_message(Level, rdf(restore(Silent,
					 done(DB, T, Count, Nth, Total)))).

message_level(true, silent) :- !.
message_level(_, informational).


		 /*******************************
		 *	   LOAD JOURNAL		*
		 *******************************/

%%	load_journal(+File:atom, +DB:atom) is det.
%
%	Process transactions from the RDF journal File, adding the given
%	named graph.

load_journal(File, DB) :-
	open_db(File, read, In, []),
	call_cleanup((  read(In, T0),
			process_journal(T0, In, DB)
		     ),
		     close(In)).

process_journal(end_of_file, _, _) :- !.
process_journal(Term, In, DB) :-
	(   process_journal_term(Term, DB)
	->  true
	;   throw(error(type_error(journal_term, Term), _))
	),
	read(In, T2),
	process_journal(T2, In, DB).

process_journal_term(assert(S,P,O), DB) :-
	rdf_assert(S,P,O,DB).
process_journal_term(assert(S,P,O,Line), DB) :-
	rdf_assert(S,P,O,DB:Line).
process_journal_term(retract(S,P,O), DB) :-
	rdf_retractall(S,P,O,DB).
process_journal_term(retract(S,P,O,Line), DB) :-
	rdf_retractall(S,P,O,DB:Line).
process_journal_term(update(S,P,O,Action), DB) :-
	(   rdf_update(S,P,O,DB, Action)
	->  true
	;   print_message(warning, rdf(update_failed(S,P,O,Action)))
	).
process_journal_term(start(_), _).	% journal open/close
process_journal_term(end(_), _).
process_journal_term(begin(_), _).	% logged transaction (compatibility)
process_journal_term(end, _).
process_journal_term(begin(_,_,_,_), _). % logged transaction (current)
process_journal_term(end(_,_,_), _).


		 /*******************************
		 *	   CREATE JOURNAL	*
		 *******************************/

:- dynamic
	blocked_db/2,			% DB, Reason
	transaction_message/3,		% Nesting, Time, Message
	transaction_db/3.		% Nesting, DB, Id

%%	rdf_persistency(+DB, Bool)
%
%	Specify whether a database is persistent.  Switching to =false=
%       kills the persistent state.  Switching to =true= creates it.

rdf_persistency(DB, Bool) :-
	must_be(atom, DB),
	must_be(boolean, Bool),
	fail.
rdf_persistency(DB, false) :- !,
	(   blocked_db(DB, persistency)
	->  true
	;   assert(blocked_db(DB, persistency)),
	    delete_db(DB)
	).
rdf_persistency(DB, true) :-
	(   retract(blocked_db(DB, persistency))
	->  create_db(DB)
	;   true
	).


%%	start_monitor is det.
%%	stop_monitor is det.
%
%	Start/stop monitoring the RDF database   for  changes and update
%	the journal.

start_monitor :-
	rdf_monitor(monitor,
		    [ -assert(load)
		    ]).
stop_monitor :-
	rdf_monitor(monitor,
		    [ -all
		    ]).

%%	monitor(+Term) is semidet.
%
%	Handle an rdf_monitor/2 callback to  deal with persistency. Note
%	that the monitor calls that come   from rdf_db.pl that deal with
%	database changes are serialized.  They   do  come from different
%	threads though.

monitor(Msg) :-
	debug(monitor, 'Monitor: ~p~n', [Msg]),
	fail.
monitor(assert(S,P,O,DB:Line)) :- !,
	\+ blocked_db(DB, _),
	journal_fd(DB, Fd),
	open_transaction(DB, Fd),
	format(Fd, '~q.~n', [assert(S,P,O,Line)]),
	sync_journal(DB, Fd).
monitor(assert(S,P,O,DB)) :-
	\+ blocked_db(DB, _),
	journal_fd(DB, Fd),
	open_transaction(DB, Fd),
	format(Fd, '~q.~n', [assert(S,P,O)]),
	sync_journal(DB, Fd).
monitor(retract(S,P,O,DB:Line)) :- !,
	\+ blocked_db(DB, _),
	journal_fd(DB, Fd),
	open_transaction(DB, Fd),
	format(Fd, '~q.~n', [retract(S,P,O,Line)]),
	sync_journal(DB, Fd).
monitor(retract(S,P,O,DB)) :-
	\+ blocked_db(DB, _),
	journal_fd(DB, Fd),
	open_transaction(DB, Fd),
	format(Fd, '~q.~n', [retract(S,P,O)]),
	sync_journal(DB, Fd).
monitor(update(S,P,O,DB:Line,Action)) :- !,
	\+ blocked_db(DB, _),
	(   Action = graph(NewDB)
	->  monitor(assert(S,P,O,NewDB)),
	    monitor(retract(S,P,O,DB:Line))
	;   journal_fd(DB, Fd),
	    format(Fd, '~q.~n', [update(S,P,O,Action)]),
	    sync_journal(DB, Fd)
	).
monitor(update(S,P,O,DB,Action)) :-
	\+ blocked_db(DB, _),
	(   Action = graph(NewDB)
	->  monitor(assert(S,P,O,NewDB)),
	    monitor(retract(S,P,O,DB))
	;   journal_fd(DB, Fd),
	    open_transaction(DB, Fd),
	    format(Fd, '~q.~n', [update(S,P,O,Action)]),
	    sync_journal(DB, Fd)
	).
monitor(load(BE, Id)) :-
	(   BE == begin
	->  push_state(Id)
	;   sync_state(Id)
	).
monitor(transaction(BE, Id)) :-
	monitor_transaction(Id, BE).

monitor_transaction(load_journal(DB), begin(_)) :- !,
	assert(blocked_db(DB, journal)).
monitor_transaction(load_journal(DB), end(_)) :- !,
	retractall(blocked_db(DB, journal)).

monitor_transaction(parse(URI), begin(_)) :- !,
	(   blocked_db(URI, persistency)
	->  true
	;   assert(blocked_db(URI, parse))
	).
monitor_transaction(parse(URI), end(_)) :- !,
	(   retract(blocked_db(URI, parse))
	->  create_db(URI)
	;   true
	).
monitor_transaction(unload(DB), begin(_)) :- !,
	(   blocked_db(DB, persistency)
	->  true
	;   assert(blocked_db(DB, unload))
	).
monitor_transaction(unload(DB), end(_)) :- !,
	(   retract(blocked_db(DB, unload))
	->  delete_db(DB)
	;   true
	).
monitor_transaction(log(Msg), begin(N)) :- !,
	check_nested(N),
	get_time(Time),
	asserta(transaction_message(N, Time, Msg)).
monitor_transaction(log(_), end(N)) :-
	check_nested(N),
	retract(transaction_message(N, _, _)), !,
	findall(DB:Id, retract(transaction_db(N, DB, Id)), DBs),
	end_transactions(DBs, N).
monitor_transaction(log(Msg, DB), begin(N)) :- !,
	check_nested(N),
	get_time(Time),
	asserta(transaction_message(N, Time, Msg)),
	journal_fd(DB, Fd),
	open_transaction(DB, Fd).
monitor_transaction(log(Msg, _DB), end(N)) :-
	monitor_transaction(log(Msg), end(N)).
monitor_transaction(reset, begin(L)) :-
	forall(rdf_graph(DB),
	       monitor_transaction(unload(DB), begin(L))).
monitor_transaction(reset, end(L)) :-
	forall(blocked_db(DB, unload),
	       monitor_transaction(unload(DB), end(L))),
	retractall(current_transaction_id(_,_)).


%%	check_nested(+Level) is semidet.
%
%	True if we must log this transaction.   This  is always the case
%	for toplevel transactions. Nested transactions   are only logged
%	if log_nested_transactions(true) is defined.

check_nested(0) :- !.
check_nested(_) :-
	rdf_option(log_nested_transactions(true)).


%%	open_transaction(+DB, +Fd) is det.
%
%	Add a begin(Id, Level, Time,  Message)   term  if  a transaction
%	involves DB. Id is an incremental   integer, where each database
%	has its own counter. Level is the nesting level, Time a floating
%	point timestamp and Message te message   provided as argument to
%	the log message.

open_transaction(DB, Fd) :-
	transaction_message(N, Time, Msg), !,
	(   transaction_db(N, DB, _)
	->  true
	;   next_transaction_id(DB, Id),
	    assert(transaction_db(N, DB, Id)),
	    RoundedTime is round(Time*100)/100,
	    format(Fd, '~q.~n', [begin(Id, N, RoundedTime, Msg)])
	).
open_transaction(_,_).


%%	next_transaction_id(+DB, -Id) is det.
%
%	Id is the number to user for  the next logged transaction on DB.
%	Transactions in each  named  graph   are  numbered  in sequence.
%	Searching the Id of the last transaction is performed by the 2nd
%	clause starting 1Kb from the end   and doubling this offset each
%	failure.

:- dynamic
	current_transaction_id/2.

next_transaction_id(DB, Id) :-
	retract(current_transaction_id(DB, Last)), !,
	Id is Last + 1,
	assert(current_transaction_id(DB, Id)).
next_transaction_id(DB, Id) :-
	db_files(DB, _, Journal),
	exists_file(Journal), !,
	size_file(Journal, Size),
	open_db(Journal, read, In, []),
	call_cleanup(iterative_expand(In, Size, Last), close(In)),
	Id is Last + 1,
	assert(current_transaction_id(DB, Id)).
next_transaction_id(DB, 1) :-
	assert(current_transaction_id(DB, 1)).

iterative_expand(_, 0, 0) :- !.
iterative_expand(In, Size, Last) :-	% Scan growing sections from the end
	Max is floor(log(Size)/log(2)),
	between(10, Max, Step),
	Offset is -(1<<Step),
	seek(In, Offset, eof, _),
	skip(In, 10),			% records are line-based
	read(In, T0),
	last_transaction_id(T0, In, 0, Last),
	Last > 0, !.
iterative_expand(In, _, Last) :-	% Scan the whole file
	seek(In, 0, bof, _),
	read(In, T0),
	last_transaction_id(T0, In, 0, Last).

last_transaction_id(end_of_file, _, Last, Last) :- !.
last_transaction_id(end(Id, _, _), In, _, Last) :-
	read(In, T1),
	last_transaction_id(T1, In, Id, Last).
last_transaction_id(_, In, Id, Last) :-
	read(In, T1),
	last_transaction_id(T1, In, Id, Last).


%%	end_transactions(+DBs:list(atom:id)) is det.
%
%	End a transaction that affected the  given list of databases. We
%	write the list of other affected databases as an argument to the
%	end-term to facilitate fast finding of the related transactions.
%
%	In each database, the transaction is   ended with a term end(Id,
%	Nesting, Others), where  Id  and   Nesting  are  the transaction
%	identifier and nesting (see open_transaction/2)  and Others is a
%	list of DB:Id,  indicating  other   databases  affected  by  the
%	transaction.

end_transactions(DBs, N) :-
	end_transactions(DBs, DBs, N).

end_transactions([], _, _).
end_transactions([DB:Id|T], DBs, N) :-
	journal_fd(DB, Fd),
	once(select(DB:Id, DBs, Others)),
	format(Fd, 'end(~q, ~q, ~q).~n', [Id, N, Others]),
	sync_journal(DB, Fd),
	end_transactions(T, DBs, N).


%	State  handling.  We  use   this    for   trapping   changes  by
%	rdf_load_db/1. In theory, loading such files  can add triples to
%	multiple sources. In practice this rarely   happens. We save the
%	current state and sync all  files   that  have changed. The only
%	drawback of this approach is that loaded files spreading triples
%	over multiple databases cause all these   databases  to be fully
%	synchronised. This shouldn't happen very often.

:- dynamic
	pre_load_state/2.

push_state(Id) :-
	get_state(State),
	asserta(pre_load_state(Id, State)).

get_state(State) :-
	findall(DB-MD5, (rdf_graph(DB), rdf_md5(DB, MD5)), State0),
	keysort(State0, State).

sync_state(Id) :-
	retract(pre_load_state(Id, PreState)),
	get_state(AfterState),
	sync_state(AfterState, PreState).

sync_state([], _).
sync_state([DB-MD5|TA], Pre) :-
	(   memberchk(DB-MD5P, Pre),
	    MD5P == MD5
	->  true
	;   create_db(DB)
	),
	sync_state(TA, Pre).


		 /*******************************
		 *	   JOURNAL FILES	*
		 *******************************/

%%	journal_fd(+DB, -Stream) is det.
%
%	Get an open stream to a journal. If the journal is not open, old
%	journals are closed to satisfy   the =max_open_journals= option.
%	Then the journal is opened in   =append= mode. Journal files are
%	always encoded as UTF-8 for  portability   as  well as to ensure
%	full coverage of Unicode.

journal_fd(DB, Fd) :-
	source_journal_fd(DB, Fd), !.
journal_fd(DB, Fd) :-
	with_mutex(rdf_journal_file,
		   journal_fd_(DB, Out)),
	Fd = Out.

journal_fd_(DB, Fd) :-
	source_journal_fd(DB, Fd), !.
journal_fd_(DB, Fd) :-
	limit_fd_pool,
	db_files(DB, _Snapshot, Journal),
	open_db(Journal, append, Fd,
		[ close_on_abort(false)
		]),
	time_stamp(Now),
	format(Fd, '~q.~n', [start([time(Now)])]),
	assert(source_journal_fd(DB, Fd)).		% new one at the end

%%	limit_fd_pool is det.
%
%	Limit the number of  open   journals  to max_open_journals (10).
%	Note that calls  from  rdf_monitor/2   are  issued  in different
%	threads, but as they are part of write operations they are fully
%	synchronised.

limit_fd_pool :-
	predicate_property(source_journal_fd(_, _), number_of_clauses(N)), !,
	(   rdf_option(max_open_journals(Max))
	->  true
	;   Max = 10
	),
	Close is N - Max,
	forall(between(1, Close, _),
	       close_oldest_journal).
limit_fd_pool.

close_oldest_journal :-
	source_journal_fd(DB, _Fd), !,
	debug(rdf_persistency, 'Closing old journal for ~q', [DB]),
	close_journal(DB).
close_oldest_journal.


%%	sync_journal(+DB, +Fd)
%
%	Sync journal represented by database and   stream.  If the DB is
%	involved in a transaction there is   no point flushing until the
%	end of the transaction.

sync_journal(DB, _) :-
	transaction_db(_, DB, _), !.
sync_journal(_, Fd) :-
	flush_output(Fd).

%%	close_journal(+DB) is det.
%
%	Close the journal associated with DB if it is open.

close_journal(DB) :-
	with_mutex(rdf_journal_file,
		   close_journal_(DB)).

close_journal_(DB) :-
	(   retract(source_journal_fd(DB, Fd))
	->  time_stamp(Now),
	    format(Fd, '~q.~n', [end([time(Now)])]),
	    close(Fd, [force(true)])
	;   true
	).

%	close_journals
%
%	Close all open journals.

close_journals :-
	forall(source_journal_fd(DB, _),
	       catch(close_journal(DB), E,
		     print_message(error, E))).

%%	create_db(+DB)
%
%	Create a saved version of DB in corresponding file, close and
%	delete journals.

create_db(DB) :-
	debug(rdf_persistency, 'Saving DB ~w', [DB]),
	db_abs_files(DB, Snapshot, Journal),
	atom_concat(Snapshot, '.new', NewSnapshot),
	(   catch(( create_directory_levels(Snapshot),
		    rdf_save_db(NewSnapshot, DB)
		  ), Error,
		  ( print_message(warning, Error),
		    fail
		  ))
	->  (   exists_file(Journal)
	    ->  delete_file(Journal)
	    ;   true
	    ),
	    rename_file(NewSnapshot, Snapshot),
	    debug(rdf_persistency, 'Saved DB ~w', [DB])
	;   catch(delete_file(NewSnapshot), _, true)
	).


%%	delete_db(+DB)
%
%	Remove snapshot and journal file for DB.

delete_db(DB) :-
	with_mutex(rdf_journal_file,
		   delete_db_(DB)).

delete_db_(DB) :-
	close_journal_(DB),
	db_abs_files(DB, Snapshot, Journal),
	(   exists_file(Journal)
	->  delete_file(Journal)
	;   true
	),
	(   exists_file(Snapshot)
	->  delete_file(Snapshot)
	;   true
	).


		 /*******************************
		 *	       LOCKING		*
		 *******************************/

%%	lock_db(+Dir)
%
%	Lock the database directory Dir.

lock_db(Dir) :-
	lockfile(Dir, File),
	catch(open(File, update, Out, [lock(write), wait(false)]),
	      error(permission_error(lock, _, _), _),
	      locked_error(Dir)),
	(   current_prolog_flag(pid, PID)
	->  true
	;   PID = 0			% TBD: Fix in Prolog
	),
	time_stamp(Now),
	gethostname(Host),
	format(Out, '/* RDF Database is in use */~n~n', []),
	format(Out, '~q.~n', [ locked([ time(Now),
					pid(PID),
					host(Host)
				      ])
			     ]),
	flush_output(Out),
	set_end_of_stream(Out),
	assert(rdf_lock(Dir, lock(Out, File))),
	at_halt(unlock_db(Out, File)).

locked_error(Dir) :-
	lockfile(Dir, File),
	(   catch(read_file_to_terms(File, Terms, []), _, fail),
	    Terms = [locked(Args)]
	->  Context = rdf_locked(Args)
	;   Context = context(_, 'Database is in use')
	),
	throw(error(permission_error(lock, rdf_db, Dir), Context)).

%%	unlock_db(+Dir) is det.
%%	unlock_db(+Stream, +File) is det.

unlock_db(Dir) :-
	retract(rdf_lock(Dir, lock(Out, File))), !,
	unlock_db(Out, File).

unlock_db(Out, File) :-
	close(Out),
	delete_file(File).

		 /*******************************
		 *	     FILENAMES		*
		 *******************************/

lockfile(Dir, LockFile) :-
	atomic_list_concat([Dir, /, lock], LockFile).

directory_levels(Levels) :-
	rdf_option(directory_levels(Levels)), !.
directory_levels(2).

db_file(Base, File) :-
	rdf_directory(Dir),
	directory_levels(Levels),
	db_file(Dir, Base, Levels, File).

db_file(Dir, Base, Levels, File) :-
	dir_levels(Base, Levels, Segments, [Base]),
	atomic_list_concat([Dir|Segments], /, File).

open_db(Base, Mode, Stream, Options) :-
	db_file(Base, File),
	create_directory_levels(File),
	open(File, Mode, Stream, [encoding(utf8)|Options]).

create_directory_levels(_File) :-
	rdf_option(directory_levels(0)), !.
create_directory_levels(File) :-
	file_directory_name(File, Dir),
	make_directory_path(Dir).

exists_db(Base) :-
	db_file(Base, File),
	exists_file(File).

%%	dir_levels(+File, +Levels, ?Segments, ?Tail) is det.
%
%	Create a list of intermediate directory names for File.  Each
%	directory consists of two hexadecimal digits.

dir_levels(_, 0, Segments, Segments) :- !.
dir_levels(File, Levels, Segments, Tail) :-
	rdf_atom_md5(File, 1, Hash),
	create_dir_levels(Levels, 0, Hash, Segments, Tail).

create_dir_levels(0, _, _, Segments, Segments) :- !.
create_dir_levels(N, S, Hash, [S1|Segments0], Tail) :-
	sub_atom(Hash, S, 2, _, S1),
	S2 is S+2,
	N2 is N-1,
	create_dir_levels(N2, S2, Hash, Segments0, Tail).


%%	db_files(+DB, -Snapshot, -Journal).
%%	db_files(-DB, +Snapshot, -Journal).
%%	db_files(-DB, -Snapshot, +Journal).
%
%	True if named graph DB is represented  by the files Snapshot and
%	Journal. The filenames are local   to the directory representing
%	the store.

db_files(DB, Snapshot, Journal) :-
	nonvar(DB), !,
	rdf_db_to_file(DB, Base),
	atom_concat(Base, '.trp', Snapshot),
	atom_concat(Base, '.jrn', Journal).
db_files(DB, Snapshot, Journal) :-
	nonvar(Snapshot), !,
	atom_concat(Base, '.trp', Snapshot),
	atom_concat(Base, '.jrn', Journal),
	rdf_db_to_file(DB, Base).
db_files(DB, Snapshot, Journal) :-
	nonvar(Journal), !,
	atom_concat(Base, '.jrn', Journal),
	atom_concat(Base, '.trp', Snapshot),
	rdf_db_to_file(DB, Base).

db_abs_files(DB, Snapshot, Journal) :-
	db_files(DB, Snapshot0, Journal0),
	db_file(Snapshot0, Snapshot),
	db_file(Journal0, Journal).


%%	rdf_journal_file(+DB, -File) is semidet.
%%	rdf_journal_file(-DB, -File) is nondet.
%
%	True if File is the absolute  file   name  of  an existing named
%	graph DB.
%
%	@tbd	Avoid using private rdf_db:rdf_graphs_/1.

rdf_journal_file(DB, Journal) :-
	(   var(DB)
	->  rdf_db:rdf_graphs_(All),	% also pick the empty graphs
	    member(DB, All)
	;   true
	),
	db_abs_files(DB, _Snapshot, Journal),
	exists_file(Journal).


%%	rdf_db_to_file(+DB, -File) is det.
%%	rdf_db_to_file(-DB, +File) is det.
%
%	Translate between database encoding (often an   file or URL) and
%	the name we store in the  directory.   We  keep  a cache for two
%	reasons. Speed, but much more important   is that the mapping of
%	raw --> encoded provided by  www_form_encode/2 is not guaranteed
%	to be unique by the W3C standards.

rdf_db_to_file(DB, File) :-
	file_base_db(File, DB), !.
rdf_db_to_file(DB, File) :-
	url_to_filename(DB, File),
	assert(file_base_db(File, DB)).

%%	url_to_filename(+URL, -FileName) is det.
%%	url_to_filename(-URL, +FileName) is det.
%
%	Turn  a  valid  URL  into  a  filename.  Earlier  versions  used
%	www_form_encode/2, but this can produce  characters that are not
%	valid  in  filenames.  We  will  use    the   same  encoding  as
%	www_form_encode/2,  but  using  our  own    rules   for  allowed
%	characters. The only requirement is that   we avoid any filename
%	special character in use.  The   current  encoding  use US-ASCII
%	alnum characters, _ and %

url_to_filename(URL, FileName) :-
	atomic(URL), !,
	atom_codes(URL, Codes),
	phrase(url_encode(EncCodes), Codes),
	atom_codes(FileName, EncCodes).
url_to_filename(URL, FileName) :-
	uri_encoded(path, URL, FileName).

url_encode([0'+|T]) -->
	" ", !,
        url_encode(T).
url_encode([C|T]) -->
	alphanum(C), !,
	url_encode(T).
url_encode([C|T]) -->
	no_enc_extra(C), !,
	url_encode(T).
url_encode(Enc) -->
	(   "\r\n"
	;   "\n"
	), !,
	{ append("%0D%0A", T, Enc)
	},
	url_encode(T).
url_encode([]) -->
	eos, !.
url_encode([0'%,D1,D2|T]) -->
	[C],
	{ Dv1 is (C>>4 /\ 0xf),
	  Dv2 is (C /\ 0xf),
	  code_type(D1, xdigit(Dv1)),
	  code_type(D2, xdigit(Dv2))
	},
	url_encode(T).

eos([], []).

alphanum(C) -->
	[C],
	{ C < 128,			% US-ASCII
	  code_type(C, alnum)
	}.

no_enc_extra(0'_) --> "_".


		 /*******************************
		 *	       REINDEX		*
		 *******************************/

%%	reindex_db(+Dir)
%
%	Reindex the database by creating intermediate directories.

reindex_db(Dir) :-
	directory_levels(Levels),
	reindex_db(Dir, Levels).

reindex_db(Dir, Levels) :-
	directory_files(Dir, Files),
	reindex_files(Files, Dir, '.', 0, Levels),
	remove_empty_directories(Files, Dir).

reindex_files([], _, _, _, _).
reindex_files([Nofollow|Files], Dir, Prefix, CLevel, Levels) :-
	nofollow(Nofollow), !,
	reindex_files(Files, Dir, Prefix, CLevel, Levels).
reindex_files([File|Files], Dir, Prefix, CLevel, Levels) :-
	CLevel \== Levels,
	file_name_extension(_Base, Ext, File),
	db_extension(Ext), !,
	directory_file_path(Prefix, File, DBFile),
	directory_file_path(Dir, DBFile, OldPath),
	db_file(Dir, File, Levels, NewPath),
	debug(rdf_persistency, 'Rename ~q --> ~q', [OldPath, NewPath]),
	file_directory_name(NewPath, NewDir),
	make_directory_path(NewDir),
	rename_file(OldPath, NewPath),
	reindex_files(Files, Dir, Prefix, CLevel, Levels).
reindex_files([D|Files], Dir, Prefix, CLevel, Levels) :-
	directory_file_path(Prefix, D, SubD),
	directory_file_path(Dir, SubD, AbsD),
	exists_directory(AbsD),
	\+ read_link(AbsD, _, _), !,	% Do not follow links
	directory_files(AbsD, SubFiles),
	CLevel2 is CLevel + 1,
	reindex_files(SubFiles, Dir, SubD, CLevel2, Levels),
	reindex_files(Files, Dir, Prefix, CLevel, Levels).
reindex_files([_|Files], Dir, Prefix, CLevel, Levels) :-
	reindex_files(Files, Dir, Prefix, CLevel, Levels).


remove_empty_directories([], _).
remove_empty_directories([File|Files], Dir) :-
	\+ nofollow(File),
	directory_file_path(Dir, File, Path),
	exists_directory(Path),
	\+ read_link(Path, _, _), !,
	directory_files(Path, Content),
	exclude(nofollow, Content, RealContent),
	(   RealContent == []
	->  debug(rdf_persistency, 'Remove empty dir ~q', [Path]),
	    delete_directory(Path)
	;   remove_empty_directories(RealContent, Path)
	),
	remove_empty_directories(Files, Dir).
remove_empty_directories([_|Files], Dir) :-
	remove_empty_directories(Files, Dir).


		 /*******************************
		 *	      PREFIXES		*
		 *******************************/

save_prefixes(Dir) :-
	atomic_list_concat([Dir, /, 'prefixes.db'], PrefixFile),
	setup_call_cleanup(open(PrefixFile, write, Out, [encoding(utf8)]),
			   write_prefixes(Out),
			   close(Out)).

write_prefixes(Out) :-
	format(Out, '% Snapshot of defined RDF prefixes~n~n', []),
	forall(rdf_current_ns(Alias, URI),
	       format(Out, 'prefix(~q, ~q).~n', [Alias, URI])).

%%	load_prefixes(+RDFDBDir) is det.
%
%	If the file RDFDBDir/prefixes.db exists,  load the prefixes. The
%	prefixes are registered using rdf_register_ns/3. Possible errors
%	because the prefix  definitions  have   changed  are  printed as
%	warnings, retaining the  old  definition.   Note  that  changing
%	prefixes generally requires reloading all RDF from the source.

load_prefixes(Dir) :-
	atomic_list_concat([Dir, /, 'prefixes.db'], PrefixFile),
	(   exists_file(PrefixFile)
	->  setup_call_cleanup(open(PrefixFile, read, In, [encoding(utf8)]),
			       read_prefixes(In),
			       close(In))
	;   true
	).

read_prefixes(Stream) :-
	read_term(Stream, T0, []),
	read_prefixes(T0, Stream).

read_prefixes(end_of_file, _) :- !.
read_prefixes(prefix(Alias, URI), Stream) :- !,
	must_be(atom, Alias),
	must_be(atom, URI),
	catch(rdf_register_ns(Alias, URI, []), E,
	      print_message(warning, E)),
	read_term(Stream, T, []),
	read_prefixes(T, Stream).
read_prefixes(Term, _) :-
	domain_error(prefix_term, Term).


		 /*******************************
		 *		UTIL		*
		 *******************************/

%%	mkdir(+Directory)
%
%	Create a directory if it does not already exist.

mkdir(Directory) :-
	exists_directory(Directory), !.
mkdir(Directory) :-
	make_directory(Directory).

%%	time_stamp(-Integer)
%
%	Return time-stamp rounded to integer.

time_stamp(Int) :-
	get_time(Now),
	Int is round(Now).


		 /*******************************
		 *	      MESSAGES		*
		 *******************************/

:- multifile
	prolog:message/3,
	prolog:message_context/3.

prolog:message(rdf(Term)) -->
	message(Term).

message(restore(attached(Graphs, Time/Wall))) -->
	{ catch(Percent is round(100*Time/Wall), _, Percent = 0) },
	[ 'Attached ~D graphs in ~2f seconds (~d% CPU = ~2f sec.)'-
	  [Graphs, Wall, Percent, Time] ].
message(restore(true, Action)) --> !,
	silent_message(Action).
message(restore(brief, Action)) --> !,
	brief_message(Action).
message(restore(_, source(DB, Nth, Total))) -->
	{ file_base_name(DB, Base) },
	[ 'Restoring ~w ... (~D of ~D graphs) '-[Base, Nth, Total], flush ].
message(restore(_, snapshot(_))) -->
	[ at_same_line, '(snapshot) '-[], flush ].
message(restore(_, journal(_))) -->
	[ at_same_line, '(journal) '-[], flush ].
message(restore(_, done(_, Time, Count, _Nth, _Total))) -->
	[ at_same_line, '~D triples in ~2f sec.'-[Count, Time] ].
message(update_failed(S,P,O,Action)) -->
	[ 'Failed to update <~p ~p ~p> with ~p'-[S,P,O,Action] ].
message(reindex(Count, Depth)) -->
	[ 'Restructuring database with ~d levels (~D graphs)'-[Depth, Count] ].
message(reindex(Depth)) -->
	[ 'Fixing database directory structure (~d levels)'-[Depth] ].

silent_message(_Action) --> [].

brief_message(source(DB, Nth, Total)) -->
	{ file_base_name(DB, Base) },
	[ at_same_line,
	  '\r~w~`.t ~D of ~D graphs~72|'-[Base, Nth, Total],
	  flush
	].
brief_message(snapshot(_File)) --> [].
brief_message(journal(_File))  --> [].
brief_message(done(_DB, _Time, _Count, _Nth, _Total)) --> [].

prolog:message_context(rdf_locked(Args)) -->
	{ memberchk(time(Time), Args),
	  memberchk(pid(Pid), Args),
	  format_time(string(S), '%+', Time)
	},
	[ nl,
	  'locked at ~s by process id ~w'-[S,Pid]
	].
