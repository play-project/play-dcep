/*  $Id$

    Part of SWI-Prolog

    Author:        Jan Wielemaker
    E-mail:        wielemak@science.uva.nl
    WWW:           http://www.swi-prolog.org
    Copyright (C): 1985-2007, University of Amsterdam

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

:- module(test_rdf_db,
	  [ test_rdf_db/0
	  ]).

:- asserta(user:file_search_path(foreign, '../sgml')).
:- asserta(user:file_search_path(library, '../sgml')).
:- asserta(user:file_search_path(library, '../plunit')).
:- asserta(user:file_search_path(foreign, '../clib')).
:- asserta(user:file_search_path(library, '../clib')).
:- asserta(user:file_search_path(library, '../RDF')).
:- asserta(user:file_search_path(foreign, '.')).
:- use_module(rdf_db).
:- use_module(rdfs).
:- use_module(library(xsdp_types)).
:- use_module(library(lists)).
:- use_module(library(plunit)).

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
RDF-DB test file.  A test is a clause of the form:

	<TestSet>(<Name>-<Number>) :- Body.

If the body fails, an appropriate  error   message  is  printed. So, all
goals are supposed to  succeed.  The   predicate  testset/1  defines the
available test sets. The public goals are:

	?- runtest(+TestSet).
	?- test.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

test_rdf_db :-
	test,
	run_tests([ lang_matches,
		    lit_ranges
		  ]).


		 /*******************************
		 *	     TEST DATA		*
		 *******************************/

data(string, '').
data(string, 'This is a nice string').
data(string, '\u0411\u0435\u0441\u043f\u043b\u0430\u0442\u043d\u0430\u044f').

data(int, 0).
data(int, -67).
data(int, 327848).

data(float, 0.0).
data(float, 48.25).

data(term, [let, us, test, a, list]).
data(term, [let, us, test, another, list]).


		 /*******************************
		 *	      LOAD/SAVE		*
		 *******************************/

save_reload_db :-
	tmp_file(rdf, File),
	rdf_save_db(File),
	rdf_reset_db,
	rdf_load_db(File),
	delete_file(File).


save_reload :-
	tmp_file(rdf, File),
	rdf_save(File),
	rdf_reset_db,
	rdf_load(File,
		 [ base_uri([]),	% do not qualify
		   convert_typed_literal(convert_typed),
		   format(xml)
		 ]),
	delete_file(File).

save_reload(Encoding) :-
	tmp_file(rdf, File),
	rdf_save(File, [encoding(Encoding)]),
	rdf_reset_db,
	rdf_load(File,
		 [ base_uri([]),	% do not qualify
		   convert_typed_literal(convert_typed),
		   format(xml)
		 ]),
	delete_file(File).

%	convert_typed(+Type, +Content, -Object)
%
%	Convert to type(Type, PrologValue), providing the inverse of
%	the default RDF as produced by rdf_db.pl

convert_typed(Type, Content, type(Type, Value)) :-
	xsdp_convert(Type, Content, Value).


		 /*******************************
		 *	      RESOURCE		*
		 *******************************/

resource(1) :-
	rdf_assert(x, a, aap),
	rdf_assert(x, a, noot),
	findall(X, rdf(x, a, X), L),
	L == [aap, noot].


		 /*******************************
		 *	    SIMPLE LITERAL	*
		 *******************************/

literal(1) :-
	findall(V, data(_, V), Vs),
	forall(member(Value, Vs),
	       rdf_assert(x, a, literal(Value))),
	findall(V, (rdf(x, a, X), X = literal(V)), V2),
	V2 == Vs.


		 /*******************************
		 *	   UNIFYING ARGS	*
		 *******************************/

same(1) :-
	rdf_assert(a,b,c),
	rdf_assert(x,x,x),
	rdf(X,X,X),
	X == x.

		 /*******************************
		 *	   TYPED LITERALS	*
		 *******************************/

typed(1) :-
	findall(type(T,V), data(T, V), TVs),
	forall(member(Value, TVs),
	       rdf_assert(x, a, literal(Value))),
	findall(V, (rdf(x, a, X), X = literal(V)), V2),
	V2 == TVs.
typed(2) :-
	findall(type(T,V), data(T, V), TVs),
	forall(member(Value, TVs),
	       rdf_assert(x, a, literal(Value))),
	findall(V, rdf(x, a, literal(V)), V2),
	V2 == TVs.
typed(3) :-
	findall(type(T,V), data(T, V), TVs),
	forall(member(Value, TVs),
	       rdf_assert(x, a, literal(Value))),
	X = type(T,V),
	findall(X, rdf(x, a, literal(X)), TV2),
	TV2 == TVs.
typed(save_db) :-
	findall(type(T,V), data(T, V), TVs),
	forall(member(Value, TVs),
	       rdf_assert(x, a, literal(Value))),
	save_reload_db,
	X = type(T,V),
	findall(X, rdf(x, a, literal(X)), TV2),
	TV2 == TVs.
typed(save) :-
	findall(type(T,V),
		( data(T, V),
		  T \== term,
		  V \== ''
		), TVs),
	forall(member(Value, TVs),
	       rdf_assert(x, a, literal(Value))),
	save_reload,
	findall(X, rdf(x, a, literal(X)), TV2),
	(   same_set(TV2, TVs)
	->  true
	;   format('TV2 = ~q~n', [TV2]),
	    fail
	).
typed(match) :-
	rdf_assert(x, a, literal(c)),
	\+ rdf(x, a, literal(type(t, c))),
	\+ rdf(x, a, literal(type(t, _))).


		 /*******************************
		 *	 XML:LANG HANDLING	*
		 *******************************/

lang_data :-
	lang_data(x, a).

lang_data(S, A) :-
	rdf_assert(S, A, literal(lang(nl, 'Jan'))),
	rdf_assert(S, A, literal(lang(en, 'John'))),
	rdf_assert(S, A, literal(lang(en, ''))),
	rdf_assert(S, A, literal('Johannes')).

same_set(S1, S2) :-
	sort(S1, Sorted1),
	sort(S2, Sorted2),
	Sorted1 =@= Sorted2.

lang(1) :-
	lang_data,
	findall(X, rdf(x, a, literal(X)), Xs),
	Xs == [ lang(nl, 'Jan'),
		lang(en, 'John'),
		lang(en, ''),
		'Johannes'
	      ].
lang(2) :-
	lang_data,
	findall(X, rdf(x, a, literal(lang(nl, X))), Xs),
	Xs == [ 'Jan' ].
lang(3) :-
	lang_data,
	X = lang(_,_),
	findall(X, rdf(x, a, literal(X)), Xs),
	Xs =@= [ lang(nl, 'Jan'),
		 lang(en, 'John'),
		 lang(en, ''),
		 lang(_,  'Johannes')
	       ].
lang(4) :-
	lang_data,
	rdf(S, P, literal('Jan')), S == x, P == a,
	rdf(S, P, literal('Johannes')), S == x, P == a.
lang(save_db) :-
	lang_data,
	save_reload_db,
	X = lang(_,_),
	findall(X, rdf(x, a, literal(X)), Xs),
	(   Xs =@= [ lang(nl, 'Jan'),
		     lang(en, 'John'),
		     lang(en, ''),
		     lang(_, 'Johannes')
		   ]
	->  true
	;   format(user_error, 'Xs = ~w~n', [Xs]),
	    fail
	).
lang(save) :-
	lang_data,
	save_reload,
	findall(X, rdf(x, a, literal(X)), Xs),
	(   same_set(Xs,
		     [ lang(nl, 'Jan'),
		       lang(en, 'John'),
		       lang(en, ''),
		       'Johannes'
		     ])
	->  true
	;   format(user_error, 'Xs = ~q~n', [Xs]),
	    fail
	).


		 /*******************************
		 *	    NAMESPACES		*
		 *******************************/

term_expansion(ns_data(S0,P0,O0),
	       ns_data(S,P,O)) :-
	rdf_global_id(S0, S),
	rdf_global_id(P0, P),
	rdf_global_id(O0, O).

:- rdf_register_ns(dynamic, 'http://www.dynamic.org/').

ns_data(x, rdf:type, rdf:is).
ns_data(y, rdf:type, rdf:(dynamic)).
ns_data(z, rdf:type, (dynamic):rdf).
ns_data(z, (dynamic):attr1, literal(dynamic)).
ns_data(z, (dynamic):attr2, (dynamic):rdf).

namespace(save) :-
	findall(rdf(S,P,O), ns_data(S,P,O), Triples),
	forall(member(rdf(S,P,O), Triples), rdf_assert(S,P,O)),
	save_reload,
	findall(rdf(S,P,O), rdf(S,P,O), NewTriples),
	(   same_set(Triples, NewTriples)
	->  true
	;   format(user_error, 'NewTriples = ~q~n', [NewTriples]),
	    fail
	).




		 /*******************************
		 *	 LITERAL SHARING	*
		 *******************************/

lshare(1) :-
	rdf_assert(a,b,literal(aap)),
	rdf_statistics(literals(1)).
lshare(2) :-
	rdf_assert(a,b,literal(aap)),
	rdf_retractall(a,b,literal(aap)),
	rdf_statistics(literals(X)),
	X == 0.
lshare(3) :-
	rdf_assert(a,b,literal(aap)),
	rdf_assert(a,c,literal(aap)),	% shared
	rdf_statistics(literals(1)).
lshare(4) :-
	rdf_assert(a,b,literal(aap)),
	rdf_assert(a,c,literal(aap)),
	rdf_retractall(a,b,literal(aap)),
	rdf_retractall(a,c,literal(aap)),
	rdf_statistics(literals(X)),
	X == 0.
lshare(5) :-
	rdf_assert(a,b,literal(aap)),
	rdf_assert(a,b,literal(aap)),
	rdf_retractall(a,b,literal(aap)),
	rdf_statistics(literals(X)),
	X == 0.


		 /*******************************
		 *	  WIDE CHARACTERS	*
		 *******************************/

wide_atom(A) :-
	atom_codes(A, [97, 1080, 1081]).

wide(iso-object-resource) :-
	wide_atom(A),
	rdf_assert(aap, noot, A),
	save_reload(iso_latin_1).
wide(utf8-object-resource) :-
	wide_atom(A),
	rdf_assert(aap, noot, A),
	save_reload(utf8).
wide(iso-object-literal) :-
	wide_atom(A),
	rdf_assert(aap, noot, literal(A)),
	save_reload(iso_latin_1).
wide(utf8-object-literal) :-
	wide_atom(A),
	rdf_assert(aap, noot, literal(A)),
	save_reload(utf8).
%wide(iso-predicate) :-			Requires XML UTF-8 names.
%	wide_atom(A),
%	rdf_assert(aap, A, noot),
%	save_reload(iso_latin_1).
%wide(utf8-predicate) :-
%	wide_atom(A),
%	rdf_assert(aap, A, noot),
%	save_reload(utf8).
wide(iso-subject) :-
	wide_atom(A),
	rdf_assert(A, aap, noot),
	save_reload(iso_latin_1).
wide(utf8-subject) :-
	wide_atom(A),
	rdf_assert(A, aap, noot),
	save_reload(utf8).
wide(db-object-literal) :-
	wide_atom(A),
	rdf_assert(aap, noot, literal(A)),
	save_reload_db.




		 /*******************************
		 *	       UPDATE		*
		 *******************************/

update(subject) :-
	rdf_assert(x, a, v),
	rdf_update(x, a, v, subject(y)),
	rdf(y, a, v).
update(predicate) :-
	rdf_assert(x, a, v),
	rdf_update(x, a, v, predicate(b)),
	rdf(x, b, v).
update(object-1) :-
	rdf_assert(x, a, v),
	rdf_update(x, a, v, object(w)),
	rdf(x, a, w).
update(object-2) :-
	rdf_assert(x, a, v),
	rdf_update(x, a, v, object(literal(hello))),
	rdf(x, a, literal(hello)).
update(object-3) :-
	rdf_assert(x, a, v),
	rdf_update(x, a, v, object(literal(lang(nl, hallo)))),
	rdf(x, a, literal(lang(nl, hallo))).
update(object-4) :-			% add lang
	rdf_assert(x, a, literal(hallo)),
	rdf_update(x, a, literal(hallo),
		   object(literal(lang(nl, hallo)))),
	rdf(x, a, literal(lang(nl, hallo))).
update(object-5) :-			% only change lang
	rdf_assert(x, a, literal(lang(en, hallo))),
	rdf_update(x, a, literal(lang(en, hallo)),
		   object(literal(lang(nl, hallo)))),
	rdf(x, a, literal(lang(nl, hallo))).
update(object-6) :-			% drop lang
	rdf_assert(x, a, literal(lang(en, hallo))),
	rdf_update(x, a, literal(lang(en, hallo)),
		   object(literal(hallo))),
	rdf(x, a, literal(hallo)).
update(object-7) :-			% transaction update
	rdf_assert(x, a, literal(lang(en, hallo))),
	rdf_transaction(rdf_update(x, a, literal(lang(en, hallo)),
				   object(literal(hallo)))),
	rdf(x, a, literal(hallo)).


		 /*******************************
		 *	    TRANSACTIONS	*
		 *******************************/

transaction(empty-1) :-
	rdf_transaction(true),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [].
transaction(assert-1) :-
	rdf_transaction(rdf_assert(x, a, v)),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [ rdf(x, a, v)
	     ].
transaction(assert-2) :-
	\+ rdf_transaction((rdf_assert(x, a, v), fail)),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [].
transaction(nest-1) :-
	rdf_transaction( ( rdf_assert(x, a, v),
			   rdf_transaction(rdf_assert(x, a, v2)))),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [ rdf(x, a, v),
	       rdf(x, a, v2)
	     ].
transaction(nest-2) :-
	rdf_transaction( ( rdf_assert(x, a, v),
			   \+ rdf_transaction((rdf_assert(x, a, v2),fail)))),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [ rdf(x, a, v)
	     ].
transaction(deadlock-1) :-
	rdf_assert(x,y,z,g),
	rdf_assert(x,y,z,g),
	rdf_transaction(rdf(_S, _P, _O, _G)).
transaction(deadlock-2) :-
	tmp_file(rdf, F1),
	tmp_file(rdf, F2),
	rdf_assert(a, b, c, f1),
	rdf_assert(x, y, z, f2),
	rdf_save_db(F1, f1),
	rdf_save_db(F2, f2),
	rdf_reset_db,

	rdf_assert(l, f, F1),
	rdf_assert(l, f, F2),
	rdf_transaction(forall(rdf(l, f, F),
			       rdf_load_db(F))),
	findall(rdf(S,P,O), rdf(S,P,O), L),
	L == [ rdf(l,f,F1),
	       rdf(l,f,F2),
	       rdf(a,b,c),
	       rdf(x,y,z)
	     ],
	delete_file(F1),
	delete_file(F2).
transaction(active-1) :-
	\+ rdf_active_transaction(_).
transaction(active-2) :-
	rdf_transaction(rdf_active_transaction(x), x).
transaction(active-3) :-
	rdf_transaction(findall(X, rdf_active_transaction(X), Xs), x),
	Xs == [x].
transaction(active-4) :-
	rdf_transaction(rdf_active_transaction(Y), X),
	X == Y.
transaction(active-5) :-
	rdf_transaction(rdf_active_transaction(x), X),
	X == x.


		 /*******************************
		 *	       LABELS		*
		 *******************************/

label(1) :-
	rdf_global_id(rdfs:label, Label),
	lang_data(x, Label),
	findall(L, rdfs_label(x, L), Ls), Ls = ['Jan', 'John', '', 'Johannes'].
label(2) :-
	rdf_global_id(rdfs:label, Label),
	lang_data(x, Label),
	findall(L, rdfs_label(x, en, L), Ls), Ls = ['John', ''].


		 /*******************************
		 *	       MATCH		*
		 *******************************/

match(1) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(substring('llo'), _)).
match(2) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(word('there'), _)).
match(3) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(word('hello'), _)).
match(4) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(word('world'), _)).
match(5) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(like('*there*'), _)).
match(6) :-
	rdf_assert(a,b,literal('hello there world!')),
	rdf(a,b,literal(like('*world!*'), _)).
match(7) :-				% test backtracking
	rdf_assert(a,b,literal('hello there world there universe!')),
	rdf(a,b,literal(like('*th*uni*'), _)).


		 /*******************************
		 *	       PREFIX		*
		 *******************************/

prefix_data(s, p1, aaaaa).
prefix_data(s, p1, aaaab).
prefix_data(s, p1, aaabb).
prefix_data(s, p1, aaacc).
prefix_data(s, p1, aaccc).
prefix_data(s, p1, adddd).

prefix_data(s, p2, 'BBBBB').
prefix_data(s, p2, 'bbbbb').
prefix_data(s, p2, 'bbbcc').
prefix_data(s, p2, 'BBBcc').

mkprefix_db(P) :-
	forall(prefix_data(S,P,O),
	       rdf_assert(S, P, literal(O))).

tprefix(P, Prefix) :-
	mkprefix_db(P),
	findall(rdf(A,P,L), rdf(A,P,literal(prefix(Prefix), L)), List),
	findall(rdf(A,P,L),
		(   prefix_data(A,P,L),
		    case_prefix(Prefix, L)
		), L2),
%	writeln(List),
	L2 == List.

case_prefix(Prefix, Atom) :-
	atom_codes(Prefix, PC),
	atom_codes(Atom, AC),
	prefix_codes(PC, AC).

prefix_codes([], _).
prefix_codes([H0|T0], [H|T]) :-
	code_type(L, to_lower(H0)),
	code_type(L, to_lower(H)),
	prefix_codes(T0, T).

prefix(1) :- tprefix(p1, '').
prefix(2) :- tprefix(p1, a).
prefix(3) :- tprefix(p1, aa).
prefix(4) :- tprefix(p1, aaa).
prefix(5) :- tprefix(p1, aaaa).
prefix(6) :- tprefix(p1, aaaaa).
prefix(7) :- tprefix(p2, bbbb).
prefix(8) :- tprefix(p2, bbbbb).
prefix(9) :- tprefix(p2, 'Bbbbb').
prefix(10) :- tprefix(p2, 'BBBBB').

prefix(like-1) :-
	mkprefix_db(_),
	findall(L, rdf(_,_,literal(like('a*b'), L)), Ls),
	Ls = [aaaab, aaabb].


		 /*******************************
		 *	     RETRACTALL		*
		 *******************************/

rdf_retractall(nopred-1) :-
	rdf_retractall(aap, noot, mies).
rdf_retractall(term) :-
	rdf_assert(a, b, literal(x)),
	rdf_assert(a, b, literal(x(1))),
	rdf_retractall(a, b, literal(x(_))),
	findall(V, rdf(a,b,V), [literal(x)]).


		 /*******************************
		 *	       MONITOR		*
		 *******************************/

do_monitor(assert(S, P, O, DB)) :-
	atom(O),
	ip(P, IP),
	rdf_transaction(rdf_assert(O, IP, S, DB)).
do_monitor(retract(S, P, O, DB)) :-
	atom(O),
	ip(P, IP),
	rdf_transaction(rdf_retractall(O, IP, S, DB)).

ip(a, ia).
ip(b, ib).

monitor(transaction-1) :-
	rdf_reset_db,
	rdf_monitor(do_monitor, []),
	rdf_transaction(rdf_assert(x, a, y, db)),
	rdf_monitor(do_monitor, [-all]),
	findall(rdf(S,P,O), rdf(S,P,O), DB),
	DB == [ rdf(x, a, y),
		rdf(y, ia, x)
	      ].


		 /*******************************
		 *	   SUB-PROPERTY		*
		 *******************************/


subproperty(1) :-
	rdf_assert(a, p, b),
	\+ rdf_has(_, p2, b, _).


		 /*******************************
		 *      PROPERTY HIERACHY	*
		 *******************************/

%%	ptree/1
%
%	Property hierarchy handling for rdf_has/3. The routines maintain
%	clouds of connected properties and for   each  cloud a bitmatrix
%	filled with the closure of the rdfs:subPropertyOf relation.

ptree(1) :-
	rdf_assert(a, rdfs:subPropertyOf, b),
	rdf_assert(x, a, y),
	rdf_has(x, b, y).
ptree(2) :-				% simple cycle
	rdf_assert(a, rdfs:subPropertyOf, b),
	rdf_assert(b, rdfs:subPropertyOf, a),
	rdf_assert(x, a, y),
	rdf_has(x, b, y).
ptree(3) :-				% self-cycle
	rdf_assert(a, rdfs:subPropertyOf, a),
	rdf_assert(x, a, y),
	rdf_has(x, a, y).
ptree(4) :-				% two roots
	rdf_assert(c, rdfs:subPropertyOf, b),
	rdf_assert(c, rdfs:subPropertyOf, d),
	rdf_assert(x, c, y),
	rdf_has(x, b, y),
	rdf_has(x, d, y).
ptree(5) :-				% two roots, 2nd leg
	rdf_assert(c, rdfs:subPropertyOf, b),
	rdf_assert(c, rdfs:subPropertyOf, d),
	rdf_assert(a, rdfs:subPropertyOf, b),
	rdf_assert(x, c, y),
	rdf_assert(x, a, z),
	rdf_has(x, b, y),
	rdf_has(x, d, y),
	rdf_has(x, b, z),
	\+ rdf_has(x, d, z).
ptree(6) :-				% two root cycles
	rdf_assert(c,  rdfs:subPropertyOf, b),
	rdf_assert(c,  rdfs:subPropertyOf, d),
	rdf_assert(b,  rdfs:subPropertyOf, bc),
	rdf_assert(bc, rdfs:subPropertyOf, b),
	rdf_assert(d,  rdfs:subPropertyOf, dc),
	rdf_assert(dc, rdfs:subPropertyOf, d),
	rdf_assert(x, c, y),
	rdf_has(x, b, y),
	rdf_has(x, d, y),
	rdf_has(x, dc, y),
	rdf_has(x, bc, y).
ptree(7) :-				% create and break the cycles
	rdf_assert(x, a, y),
	rdf_assert(a, rdfs:subPropertyOf, b),
	rdf_retractall(a, rdfs:subPropertyOf, b),
	\+ rdf_has(x, b, y).



		 /*******************************
		 *	    REACHABLE		*
		 *******************************/

graph(a, p, b).
graph(b, p, c).
graph(c, p, d).
graph(b, p, d).
graph(e, p, d).

graph(Symmetric) :-
	rdf_set_predicate(p, symmetric(Symmetric)),
	forall(graph(S,P,O),
	       rdf_assert(S,P,O)).

reachable(1) :-
	rdf_reachable(a, x, a).
reachable(2) :-
	graph(false),
	rdf_reachable(a, p, d).
reachable(3) :-
	graph(false),
	rdf_reachable(a, p, X),
	X == c, !.
reachable(4) :-
	graph(false),
	findall(O, rdf_reachable(a, p, O), Os),
	Os = [a,b,c,d].
reachable(5) :-
	graph(false),
	\+ rdf_reachable(d, p, a).
reachable(6) :-
	graph(true),
	rdf_reachable(d, p, a).
reachable(6) :-
	graph(true),
	rdf_reachable(e, p, a).


		 /*******************************
		 *	    DUPLICATES		*
		 *******************************/


duplicates(1) :-
	rdf_assert(a, b, literal(lang(en, l))),
	rdf_assert(a, b, literal(l)),
	rdf_retractall(a, b, literal(lang(en, l))).


		 /*******************************
		 *	      SOURCE		*
		 *******************************/

source(1) :-
	rdf_assert(a,b,c,test),
	get_time(Now),
	rdf_db:rdf_set_graph_source(test, 'test.rdf', Now),
	rdf_source(test, X),
	X == 'test.rdf'.

		 /*******************************
		 *	        UNLOAD		*
		 *******************************/

unload(1) :-
	rdf_load(dc),
	rdf_statistics(triples(T0)),
	rdf_unload(dc),
	rdf_statistics(triples(T1)),
	rdf_load(dc),
	rdf_statistics(triples(T2)),
	T0 == T2,
	T1 == 0.

		 /*******************************
		 *	      SCRIPTS		*
		 *******************************/

:- dynamic
	script_dir/1.

set_script_dir :-
	script_dir(_), !.
set_script_dir :-
	find_script_dir(Dir),
	assert(script_dir(Dir)).

find_script_dir(Dir) :-
	prolog_load_context(file, File),
	follow_links(File, RealFile),
	file_directory_name(RealFile, Dir).

follow_links(File, RealFile) :-
	read_link(File, _, RealFile), !.
follow_links(File, File).


:- set_script_dir.

run_test_script(Script) :-
	file_base_name(Script, Base),
	file_name_extension(Pred, _, Base),
	load_files(Script, [silent(true)]),
	Pred.

run_test_scripts(Directory) :-
	(   script_dir(ScriptDir),
	    atomic_list_concat([ScriptDir, /, Directory], Dir),
	    exists_directory(Dir)
	->  true
	;   Dir = Directory
	),
	atom_concat(Dir, '/*.pl', Pattern),
	expand_file_name(Pattern, Files),
	file_base_name(Dir, BaseDir),
	format('Running scripts from ~w ', [BaseDir]), flush,
	run_scripts(Files),
	format(' done~n').

run_scripts([]).
run_scripts([H|T]) :-
	(   catch(run_test_script(H), Except, true)
	->  (   var(Except)
	    ->  put(.), flush
	    ;   Except = blocked(Reason)
	    ->  assert(blocked(H, Reason)),
		put(!), flush
	    ;   script_failed(H, Except)
	    )
	;   script_failed(H, fail)
	),
	run_scripts(T).

script_failed(File, fail) :-
	format('~NScript ~w failed~n', [File]),
	assert(failed(script(File))).
script_failed(File, Except) :-
	message_to_string(Except, Error),
	format('~NScript ~w failed: ~w~n', [File, Error]),
	assert(failed(script(File))).


		 /*******************************
		 *        TEST MAIN-LOOP	*
		 *******************************/

testset(resource).
testset(literal).
testset(lshare).
testset(same).
testset(typed).
testset(lang).
testset(wide).
testset(namespace).
testset(update).
testset(transaction).
testset(label).
testset(match).
testset(prefix).
testset(rdf_retractall).
testset(monitor).
testset(subproperty).
testset(ptree).
testset(reachable).
testset(duplicates).
testset(source).
testset(unload).

%	testdir(Dir)
%
%	Enumerate directories holding tests.

testdir('Tests').

:- dynamic
	failed/1,
	blocked/2.

watch(_).

test :-
	retractall(failed(_)),
	retractall(blocked(_,_)),
	rdf_monitor(watch, []),	% check consistency
	forall(testset(Set), runtest(Set)),
	scripts,
%	statistics,
	report_blocked,
	report_failed.

scripts :-
	forall(testdir(Dir), run_test_scripts(Dir)).


report_blocked :-
	findall(Head-Reason, blocked(Head, Reason), L),
	(   L \== []
        ->  format('~nThe following tests are blocked:~n', []),
	    (	member(Head-Reason, L),
		format('    ~p~t~40|~w~n', [Head, Reason]),
		fail
	    ;	true
	    )
        ;   true
	).
report_failed :-
	findall(X, failed(X), L),
	length(L, Len),
	(   Len > 0
        ->  format('~n*** ~w tests failed ***~n', [Len]),
	    fail
        ;   format('~nAll tests passed~n', [])
	).

runtest(Name) :-
	format('Running test set "~w" ', [Name]),
	flush,
	functor(Head, Name, 1),
	nth_clause(Head, _N, R),
	clause(Head, _, R),
	rdf_reset_db,			% reset before each script
	(   catch(Head, Except, true)
	->  (   var(Except)
	    ->  put(.), flush
	    ;   Except = blocked(Reason)
	    ->  assert(blocked(Head, Reason)),
		put(!), flush
	    ;   test_failed(R, Except)
	    )
	;   test_failed(R, fail)
	),
	fail.
runtest(_) :-
	format(' done.~n').

test_failed(R, Except) :-
	clause(Head, _, R),
	functor(Head, Name, 1),
	arg(1, Head, TestName),
	clause_property(R, line_count(Line)),
	clause_property(R, file(File)),
	(   Except == fail
	->  format('~N~w:~d: Test ~w(~w) failed~n',
		   [File, Line, Name, TestName])
	;   message_to_string(Except, Error),
	    format('~N~w:~d: Test ~w(~w):~n~t~8|ERROR: ~w~n',
		   [File, Line, Name, TestName, Error])
	),
	assert(failed(Head)).

blocked(Reason) :-
	throw(blocked(Reason)).


		 /*******************************
		 *	      UNIT TESTS	*
		 *******************************/

:- begin_tests(lang_matches).

test(lang_matches, true) :-
	lang_matches('EN', en).
test(lang_matches, true) :-
	lang_matches(en, 'EN').
test(lang_matches, fail) :-
	lang_matches(nl, 'EN').
test(lang_matches, true) :-
	lang_matches('en-GB', en).
test(lang_matches, fail) :-
	lang_matches('en-GB', 'en-*-x').
test(lang_matches, true) :-
	lang_matches('en-GB-x', 'en-*-x').
test(lang_matches, true) :-
	lang_matches('en-GB-x-y', 'en-*-x-*').
test(lang_matches, true) :-
	lang_matches('en-GB-x-y', 'en-*-y').

:- end_tests(lang_matches).

:- begin_tests(lit_ranges, [cleanup(rdf_reset_db)]).

letters :-
	rdf_reset_db,
	forall(between(0'a, 0'z, X),
	       (   char_code(C, X),
		   rdf_assert(a,b,literal(C))
	       )).

integers :-
	rdf_reset_db,
	forall(between(0, 9, X),
	       rdf_assert(a,b,literal(X))).

ge(S, X) :-
	rdf(_,b,literal(ge(S),X)).
le(S, X) :-
	rdf(_,b,literal(le(S),X)).
bt(L,H,X) :-
	rdf(_,b,literal(between(L,H),X)).

test(ge, [setup(letters), cleanup(rdf_reset_db), all(X==[x,y,z])]) :-
	ge(x, X).
test(le, [setup(letters), cleanup(rdf_reset_db), all(X==[a,b,c,d,e])]) :-
	le(e, X).
test(bt, [setup(letters), cleanup(rdf_reset_db), all(X==[m,n,o,p])]) :-
	bt(m, p, X).
test(ge, [setup(integers), cleanup(rdf_reset_db), all(X==[4,5,6,7,8,9])]) :-
	ge(4, X).
test(le, [setup(integers), cleanup(rdf_reset_db), all(X==[0,1,2,3])]) :-
	le(3, X).
test(bt, [setup(integers), cleanup(rdf_reset_db), all(X==[6,7,8])]) :-
	bt(6,8, X).

:- end_tests(lit_ranges).
