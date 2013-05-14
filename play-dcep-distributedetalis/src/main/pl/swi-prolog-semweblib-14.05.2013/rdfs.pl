/*  $Id$

    Part of SWI-Prolog

    Author:        Jan Wielemaker
    E-mail:        wielemak@science.uva.nl
    WWW:           http://www.swi-prolog.org
    Copyright (C): 1985-2005, University of Amsterdam

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

:- module(rdfs,
	  [ rdfs_subproperty_of/2,	% ?SubProperties, ?Property
	    rdfs_subclass_of/2,		% ?SubClass, ?Class
	    rdfs_class_property/2,	% +Class, ?Property
	    rdfs_individual_of/2,	% ?Resource, ?Class

	    rdfs_label/2,		% ?Resource, ?Label
	    rdfs_label/3,		% ?Resource, ?Language, ?Label
	    rdfs_ns_label/2,		% +Resource, -Label
	    rdfs_ns_label/3,		% +Resource, ?Label, -Label

	    rdfs_member/2,		% ?Object, +Set
	    rdfs_list_to_prolog_list/2,	% +Set, -List
	    rdfs_assert_list/3,		% +List, -Resource, +DB
	    rdfs_assert_list/2,		% +List, -Resource

	    rdfs_find/5			% +String, +Dom, +Props, +Method, -Subj
	  ]).
:- use_module(library(debug)).
:- use_module(library(rdf)).
:- use_module(library(lists)).
:- use_module(rdf_db).


/** <module> RDFS handling

This module provides various primitives for  more high-level handling of
RDF models from an RDFS viewpoint. Note  that there exist two approaches
for languages on top of RDF:

	* Provide new predicates according to the concept of the high
	  level language (used in this module)

	* Extend rdf/3 relation with triples _implied_ by the high-level
	  semantics.  This approach is taken by the SeRQL system.
*/

		 /*******************************
		 *	    EXPANSION		*
		 *******************************/

:- rdf_meta
	rdfs_subproperty_of(r,r),
	rdfs_subclass_of(r,r),
	rdfs_class_property(r,r),
	rdfs_individual_of(r,r),
	rdfs_label(r,-).


		 /*******************************
		 *	PROPERTY HIERARCHY	*
		 *******************************/

%%	rdfs_subproperty_of(+SubProperty, ?Property) is nondet.
%%	rdfs_subproperty_of(?SubProperty, +Property) is nondet.
%
%	Query the property hierarchy.

rdfs_subproperty_of(SubProperty, Property) :-
	rdf_reachable(SubProperty, rdfs:subPropertyOf, Property).


		 /*******************************
		 *	  CLASS HIERARCHY	*
		 *******************************/

%%	rdfs_subclass_of(+Class, ?Super) is nondet.
%%	rdfs_subclass_of(?Class, +Super) is nondet.
%
%	Generate  sub/super  classes.  rdf_reachable/3    considers  the
%	rdfs:subPropertyOf relation as well  as   cycles.  Note  that by
%	definition all classes are  subclass   of  rdfs:Resource, a case
%	which is dealt with by the 1st and 3th clauses :-(
%
%	According to production 2.4 "rdfs:Datatype", Each instance of
%	rdfs:Datatype is a subclass of rdfs:Literal.

rdfs_subclass_of(Class, Super) :-
	rdf_equal(rdfs:'Resource', Resource),
	Super == Resource, !,
	(   nonvar(Class)
	->  true			% must check for being a class?
	;   rdfs_individual_of(Class, rdfs:'Class')
	).
rdfs_subclass_of(Class, Super) :-
	rdf_reachable(Class, rdfs:subClassOf, Super).
rdfs_subclass_of(Class, Super) :-
	nonvar(Class),
	var(Super),
	\+ rdf_reachable(Class, rdfs:subClassOf, rdfs:'Resource'),
	rdfs_individual_of(Class, rdfs:'Class'),
	rdf_equal(Super, rdfs:'Resource').
rdfs_subclass_of(Class, Super) :-	% production 2.4
	(   nonvar(Class)
	->  rdf_has(Class, rdf:type, CType),
	    rdf_reachable(CType, rdfs:subClassOf, rdfs:'Datatype'),
	    \+ rdf_reachable(Class, rdfs:subClassOf, rdfs:'Literal'),
	    rdf_equal(Super, rdfs:'Literal')
	;   nonvar(Super)
	->  rdf_reachable(Super, rdfs:subClassOf, rdfs:'Literal'),
	    rdfs_individual_of(Class, rdfs:'Datatype')
	).


		 /*******************************
		 *	    INDIVIDUALS		*
		 *******************************/

%%	rdfs_individual_of(+Resource, +Class) is semidet.
%%	rdfs_individual_of(+Resource, -Class) is nondet.
%%	rdfs_individual_of(-Resource, +Class) is nondet.
%
%	Generate resources belonging to a class   or  classes a resource
%	belongs to. We assume everything at the `object' end of a triple
%	is a class. A validator should confirm this property.
%
%	rdfs_individual_of(+, -) does  not  exploit   domain  and  range
%	properties, deriving that if rdf(R,  P,   _)  is  present R must
%	satisfy the domain of P (and similar for range).
%
%	There are a few hacks:
%
%		* Any resource is an individual of rdfs:Resource
%		* literal(_) is an individual of rdfs:Literal

rdfs_individual_of(Resource, Class) :-
	nonvar(Resource), !,
	(   nonvar(Class)
	->  (   rdfs_individual_of_r_c(Resource, Class)
	    ->	true
	    )
	;   rdfs_individual_of_r_c(Resource, Class)
	).
rdfs_individual_of(Resource, Class) :-
	nonvar(Class), !,
	(   rdf_equal(Class, rdfs:'Resource')
	->  rdf_subject(Resource)
	;   rdfs_subclass_of(SubClass, Class),
	    rdf_has(Resource, rdf:type, SubClass)
	).
rdfs_individual_of(_Resource, _Class) :-
	throw(error(instantiation_error, _)).

rdfs_individual_of_r_c(literal(_), Class) :- !,
	rdfs_subclass_of(Class, rdfs:'Literal').
rdfs_individual_of_r_c(Resource, Class) :-
	rdf_has(Resource, rdf:type, MyClass),
	rdfs_subclass_of(MyClass, Class).
rdfs_individual_of_r_c(_, Class) :-
	rdf_equal(Class, rdfs:'Resource').


%%	rdfs_label(+Resource, -Label).
%%	rdfs_label(-Resource, +Label).
%
%	Convert between class and label.  If the label is generated from
%	the resource the it uses both rdf:label and its sub-properties,
%	but labels registered with rdf:label are returned first.

rdfs_label(Resource, Label) :-
	rdfs_label(Resource, _, Label).

%%	rdfs_label(+Resource, ?Lang, -Label) is multi.
%%	rdfs_label(+Resource, ?Lang, +Label) is semidet.
%%	rdfs_label(-Resource, ?Lang, ?Label) is nondet.
%
%	Resource  has  Label  in  Lang.  If  Resource  is  nonvar  calls
%	take_label/3 which is guaranteed to succeed label.

rdfs_label(Resource, Lang, Label) :-
	nonvar(Resource), !,
	take_label(Resource, Lang, Label).
rdfs_label(Resource, Lang, Label) :-
	rdf_has(Resource, rdfs:label, literal(lang(Lang, Label))).

%%	rdfs_ns_label(+Resource, -Label) is multi.
%%	rdfs_ns_label(+Resource, ?Lang, -Label) is multi.
%
%	Present label with  namespace  indication.   This  predicate  is
%	indented  to  provide  meaningful  short   names  applicable  to
%	ontology maintainers.  Note that this predicate is non-deterministic
%	if the resource has multiple rdfs:label properties

rdfs_ns_label(Resource, Label) :-
	rdfs_ns_label(Resource, _, Label).

rdfs_ns_label(Resource, Lang, Label) :-
	rdfs_label(Resource, Lang, Label0),
	(   rdf_global_id(NS:_, Resource),
	    Label0 \== ''
	->  atomic_list_concat([NS, Label0], :, Label)
	;   \+ rdf_has(Resource, rdfs:label, _)
	->  Label = Resource
	;   member(Sep, [#,/]),
	    sub_atom(Resource, B, L, A, Sep),
	    sub_atom(Resource, _, A, 0, Frag),
	    \+ sub_atom(Frag, _, _, _, Sep)
	->  Len is B+L,
	    sub_atom(Resource, 0, Len, _, NS),
	    atomic_list_concat([NS, Label0], :, Label)
	;   Label = Label0
	).


%%	take_label(+Resource, ?Lang, -Label) is multi.
%
%	Get the label to use for a  resource in the give Language. First
%	tries label_of/3.  If this fails, break the Resource over # or /
%	and if all fails, unify Label with Resource.

take_label(Resource, Lang, Label) :-
	(   label_of(Resource, Lang, Label)
	*-> true
	;   after_char(Resource, '#', Local)
	->  Label = Local
	;   after_char(Resource, '/', Local)
	->  Label = Local
	;   Label = Resource
	).

after_char(Atom, Char, Rest) :-
	State = last(-),
	(   sub_atom(Atom, _, _, L, Char),
	    nb_setarg(1, State, L),
	    fail
	;   arg(1, State, L),
	    L \== (-)
	),
	sub_atom(Atom, _, L, 0, Rest).


%%	label_of(+Resource, ?Lang, ?Label) is nondet.
%
%	True if rdf_has(Resource, rdfs:label,   literal(Lang, Label)) is
%	true,  but  guaranteed  to  generate    rdfs:label   before  any
%	subproperty thereof.

label_of(Resource, Lang, Label) :-
	rdf(Resource, rdfs:label, literal(lang(Lang, Label))).
label_of(Resource, Lang, Label) :-
	rdf_equal(rdfs:label, LabelP),
	rdf_has(Resource, LabelP, literal(lang(Lang, Label)), P),
	P \== LabelP.
label_of(Resource, Lang, Label) :-
	var(Lang),
	rdf_has(Resource, rdfs:label, literal(type(xsd:string, Label))).

%%	rdfs_class_property(+Class, ?Property)
%
%	Enumerate the properties in the domain of Class.

rdfs_class_property(Class, Property) :-
	rdfs_individual_of(Property, rdf:'Property'),
	rdf_has(Property, rdfs:domain, Domain),
	rdfs_subclass_of(Class, Domain).


		 /*******************************
		 *	     COLLECTIONS	*
		 *******************************/

%%	rdfs_member(?Element, +Set)
%
%	As Prolog member on sets.  Operates both on attributes parsed as
%	parseType="Collection" as well as on Bag, Set and Alt.

rdfs_member(Element, Set) :-
	rdf_has(Set, rdf:first, _), !,
	rdfs_collection_member(Element, Set).
rdfs_member(Element, Set) :-
	container_class(Class),
	rdfs_individual_of(Set, Class), !,
	(   nonvar(Element)
	->  rdf(Set, Predicate, Element),
	    rdf_member_property(Predicate, _N)
	;   findall(N-V, rdf_nth(Set, N, V), Pairs),
	    keysort(Pairs, Sorted),
	    member(_-Element, Sorted)
	).

rdf_nth(Set, N, V) :-
	rdf(Set, P, V),
	rdf_member_property(P, N).

:- rdf_meta container_class(r).

container_class(rdf:'Bag').
container_class(rdf:'Seq').
container_class(rdf:'Alt').


rdfs_collection_member(Element, Set) :-
	rdf_has(Set, rdf:first, Element).
rdfs_collection_member(Element, Set) :-
	rdf_has(Set, rdf:rest, Tail), !,
	rdfs_collection_member(Element, Tail).


%%	rdfs_list_to_prolog_list(+RDFSList, -PrologList)
%
%	Convert ann RDFS list (result from parseType=Collection) into a
%	Prolog list of elements.

rdfs_list_to_prolog_list(Set, []) :-
	rdf_equal(Set, rdf:nil), !.
rdfs_list_to_prolog_list(Set, [H|T]) :-
	rdf_has(Set, rdf:first, H),
	rdf_has(Set, rdf:rest, Tail), !,
	rdfs_list_to_prolog_list(Tail, T).


%%	rdfs_assert_list(+Resources, -List) is det.
%%	rdfs_assert_list(+Resources, -List, +DB) is det.
%
%	Create an RDF list from the given Resources.

rdfs_assert_list(Resources, List) :-
	rdfs_assert_list(Resources, List, user).

rdfs_assert_list([], Nil, _) :-
	rdf_equal(rdf:nil, Nil).
rdfs_assert_list([H|T], List, DB) :-
	rdfs_assert_list(T, Tail, DB),
	rdf_bnode(List),
	rdf_assert(List, rdf:rest, Tail, DB),
	rdf_assert(List, rdf:first, H, DB),
	rdf_assert(List, rdf:type, rdf:'List', DB).


		 /*******************************
		 *     SEARCH IN HIERARCHY	*
		 *******************************/

%%	rdfs_find(+String, +Domain, ?Properties, +Method, -Subject)
%
%	Search all classes below Domain for a literal property with
%	that matches String.  Method is one of
%
%		* substring
%		* word
%		* prefix
%		* exact
%
%	domain is defined by owl_satisfy from owl.pl
%
%	Note that the rdfs:label field is handled by rdfs_label/2,
%	making the URI-ref fragment name the last resort to determine
%	the label.

rdfs_find(String, Domain, Fields, Method, Subject) :-
	var(Fields), !,
	For =.. [Method,String],
	rdf_has(Subject, Field, literal(For, _)),
	owl_satisfies(Domain, Subject),
	Fields = [Field].		% report where we found it.
rdfs_find(String, Domain, Fields, Method, Subject) :-
	globalise_list(Fields, GlobalFields),
	For =.. [Method,String],
	member(Field, GlobalFields),
	(   Field == resource
	->  rdf_subject(Subject),
	    rdf_match_label(Method, String, Subject)
	;   rdf_has(Subject, Field, literal(For, _))
	),
	owl_satisfies(Domain, Subject).

owl_satisfies(Domain, _) :-
	rdf_equal(rdfs:'Resource', Domain), !.
					% Descriptions
owl_satisfies(class(Domain), Resource) :- !,
	(   rdf_equal(Domain, rdfs:'Resource')
	->  true
	;   rdfs_subclass_of(Resource, Domain)
	).
owl_satisfies(union_of(Domains), Resource) :- !,
	member(Domain, Domains),
	owl_satisfies(Domain, Resource), !.
owl_satisfies(intersection_of(Domains), Resource) :- !,
	in_all_domains(Domains, Resource).
owl_satisfies(complement_of(Domain), Resource) :- !,
	\+ owl_satisfies(Domain, Resource).
owl_satisfies(one_of(List), Resource) :- !,
	memberchk(Resource, List).
					% Restrictions
owl_satisfies(all_values_from(Domain), Resource) :-
	(   rdf_equal(Domain, rdfs:'Resource')
	->  true
	;   rdfs_individual_of(Resource, Domain)
	), !.
owl_satisfies(some_values_from(_Domain), _Resource) :- !.
owl_satisfies(has_value(Value), Resource) :-
	rdf_equal(Value, Resource).


in_all_domains([], _).
in_all_domains([H|T], Resource) :-
	owl_satisfies(H, Resource),
	in_all_domains(T, Resource).

globalise_list([], []) :- !.
globalise_list([H0|T0], [H|T]) :- !,
	globalise_list(H0, H),
	globalise_list(T0, T).
globalise_list(X, G) :-
	rdf_global_id(X, G).

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
TOP-DOWN


rdfs_find(String, Domain, Fields, Method, Subject) :-
	globalise_list(Fields, GlobalFields),
	generate_domain(Domain, Subject),
	member(Field, GlobalFields),
	(   rdf_equal(Field, rdfs:label)
	->  rdfs_label(Subject, Arg)
	;   rdf_has(Subject, Field, literal(Arg))
	),
	rdf_match_label(Method, String, Arg).

%%	generate_domain(+Domain, -Resource)
%
%	Generate all resources that satisfy some a domain specification.

generate_domain(All, Subject) :-
	rdf_equal(All, rdfs:'Resource'), !,
	rdf_subject(Subject).
generate_domain(class(Class), Subject) :- !,
	rdfs_subclass_of(Subject, Class).
generate_domain(all_values_from(Class), Individual) :-
	(   rdf_equal(Class, rdfs:'Resource')
	->  rdf_subject(Individual)			% this is OWL-full
	;   rdfs_individual_of(Individual, Class)
	).
generate_domain(some_values_from(Class), Individual) :- % Actually this is
	rdfs_individual_of(Individual, Class).		% anything
generate_domain(union_of(Domains), Individual) :-
	member(Domain, Domains),
	generate_domain(Domain, Individual).
generate_domain(intersection_of(Domains), Individual) :-
	in_all_domains(Domains, Individual).
generate_domain(one_of(Individuals), Individual) :-
	member(Individual, Individuals).

in_all_domains([], _).
in_all_domains([H|T], Resource) :-
	generate_domain(H, Resource),
	in_all_domains(T, Resource).
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

