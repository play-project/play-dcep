/*  $Id$

    Part of SWI-Prolog

    Author:        Jan Wielemaker
    E-mail:        wielemak@science.uva.nl
    WWW:           http://www.swi-prolog.org
    Copyright (C): 1985-2006, University of Amsterdam

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

    As a special exception, if you link this library with other files,
    compiled with a Free Software compiler, to produce an executable, this
    library does not by itself cause the resulting executable to be covered
    by the GNU General Public License. This exception does not however
    invalidate any other reasons why the executable file might be covered by
    the GNU General Public License.
*/

:- module(rdf_random_test,
	  [ concur/2,			% +Threads, +Actions
	    go/0,
	    go/1,			% +Actions
	    record/1,			% +Actions
	    replay/1			% +Actions
	  ]).
:- asserta(user:file_search_path(foreign, '.')).
:- use_module(rdf_db).
:- use_module(library(thread)).
:- use_module(library(debug)).

replay_file('rnd.reply').

%%	concur(+Threads:int, +Actions:int) is det.
%
%	Create _N_ Threads, each performing Actions using go/1.

concur(1, Actions) :- !,
	go(Actions).
concur(Threads, Actions) :-
	create_threads(Threads, go(Actions), Ids),
	wait(Ids).

create_threads(0, _, []) :- !.
create_threads(N, G, [Id|T]) :-
	thread_create(G, Id, []),
	N2 is N - 1,
	create_threads(N2, G, T).

wait([]).
wait([H|T]) :-
	thread_join(H, Result),
	(   Result == true
	->  true
	;   format('ERROR from ~w: ~w~n', [H, Result])
	),
	wait(T).

%%	go is det.
%%	go(+N) is det.
%
%	Perform N random operations on the database.

go :-
	go(20000).
go(N) :-
	nb_setval(rnd_file, none),
	do_random(N),
	rdf_statistics(triples(T)),
	rdf_predicate_property(rdfs:subPropertyOf, triples(SP)),
	format('~D triples; property hierarchy complexity: ~D~n', [T, SP]).

%%	record(+N)
%
%	As go/1, but  record  generated  random   numbers  in  the  file
%	specified with replay_file/1.

record(N) :-
	replay_file(File),
	open(File, write, Out),
	nb_setval(rnd_file, out(Out)),
	do_random(N).

%%	replay(+N)
%
%	Replay first N actions recorded using   record/1.  N is normally
%	the same as used for record/1.

replay(N) :-
	replay_file(File),
	open(File, read, In),
	nb_setval(rnd_file, in(In)),
	do_random(N).

%%	next(-N, +Max)
%
%	Produce a random number 1 =< N <= Max. During record/1, write
%	to file. Using replay/1, read from file.

next(N, Max) :-
	nb_getval(rnd_file, X),
	(   X == none
	->  N is random(Max)+1
	;   X = in(Fd)
	->  read(Fd, N)
	;   X = out(Fd),
	    N is random(Max)+1,
	    format(Fd, '~q.~n', [N]),
	    flush_output(Fd)
	).


%%	do_random(N) is det.
%
%	Take a random action on the database.

do_random(N) :-
	nb_setval(line, 1),
	random_actions(N).

random_actions(N) :-
	MM is N mod 100,
	(   MM = 0
	->  rdf_statistics(triples(Triples)),
	    debug(count, 'Count ~w, Triples ~w', [N, Triples])
	;   true
	),
	next(Op, 10),
	rans(Subject),
	ranp(Predicate),
	rano(Object),
	rang(Graph),
	do(Op, Subject, Predicate, Object, Graph),
	N1 is N - 1,
	(   N > 1
	->  random_actions(N1)
	;   true
	).

%%	do(+Operation, +Subject, +Predicate, +Object, +Graph) is det.
%
%	Execute an operation on Graph.
%
%	@tbd	Test update

do(1, S, P, O, G) :-
	debug(bug(S,P,O), 'ASSERT(~q,~q,~q,~q)', [S,P,O,G]),
	rdf_assert(S,P,O,G).
do(2, S, P, O, G) :-
	debug(bug(S,P,O), 'RETRACTALL(~q,~q,~q,~q)', [S,P,O,G]),
	rdf_retractall(S,P,O,G).
do(3, S, _P, _O, _G) :- rdf_s(S).	% allow profiling
do(4, S, P, _O, _G)  :- rdf_sp(S, P).
do(5, S, _P, _O, _G) :- has_s(S).
do(6, S, P, _O, _G)  :- has_sp(S, P).
do(7, S, P, _O, _G)  :- reach_sp(S, P).
do(8, _S, P, O, _G)  :- reach_po(P, O).
do(9, _, P, _, G) :-			% add a random subproperty below me
	repeat,
	    ranp(P2),
	P2 \== P, !,
	rdf_assert(P2, rdfs:subPropertyOf, P, G),
	debug(subPropertyOf, 'Added ~p rdfs:subPropertyOf ~p~n', [P2, P]).
do(10, _, P, _, G) :-			% randomly delete a subproperty
	(   rdf(_, rdfs:subPropertyOf, P)
	->  repeat,
	       ranp(P2),
	    P2 \== P,
	    rdf(P2, rdfs:subPropertyOf, P), !,
	    debug(subPropertyOf, 'Delete ~p rdfs:subPropertyOf ~p~n', [P2, P]),
	    rdf_retractall(P2, rdfs:subPropertyOf, P, G)
	;   true
	).

rdf_s(S) :-
	forall(rdf(S, _, _), true).
rdf_sp(S, P) :-
	forall(rdf(S, P, _), true).
has_s(S) :-
	forall(rdf_has(S, _, _), true).
has_sp(S, P) :-
	forall(rdf_has(S, P, _), true).
reach_sp(S, P) :-
	forall(rdf_reachable(S, P, _), true).
reach_po(P, O) :-
	(   atom(O)
	->  forall(rdf_reachable(_, P, O), true)
	;   true
	).


%%	rans(-Subject) is det.
%
%	Generate a random subject.

rans(X) :-
	next(I, 4),
	rs(I, X).

rs(1, a).
rs(2, b).
rs(3, c).
rs(4, d).

%%	ranp(-Predicate) is det.
%
%	Generate a random predicate.

ranp(X) :-
	next(I, 4),
	rp(I, X).
rp(1, a).
rp(2, p1).
rp(3, p2).
rp(4, p3).

%%	rano(-Object) is det.
%
%	Generate a random object.

rano(X) :-
	next(I, 13),
	ro(I, X).
ro(1, a).
ro(2, b).
ro(3, c).
ro(4, p1).
ro(5, literal(1)).
ro(6, literal(hello_world)).
ro(7, literal(bye)).
ro(8, literal(lang(en, bye))).
ro(9, literal(lang(nl, bye))).
ro(10, d).
ro(11, R) :-
	next(I, 1000),
	atom_concat(r, I, R).
ro(12, literal(L)) :-
	next(I, 1000),
	atom_concat(l, I, L).
ro(13, literal(lang(Lang, L))) :-
	next(I, 1000),
	atom_concat(l, I, L),
	ranl(Lang).

ranl(Lang) :-
	next(I, 2),
	rl(I, Lang).

rl(1, en).
rl(2, nl).


%%	rang(-Graph) is det.
%
%	Generate a random graph.

graph_count(200).

rang(X:Line) :-
	graph_count(Count),
	next(I, Count),
	rg(I, X),
	Line = 1.
%	line(Line).

term_expansion(rg(x,x), Clauses) :-
	graph_count(Count),
	findall(rg(I,N), (between(1, Count, I), atom_concat(g,I,N)), Clauses).

rg(x,x).

line(Line) :-
	nb_getval(line, Line),
	NL is Line+1,
	nb_setval(line, NL).

