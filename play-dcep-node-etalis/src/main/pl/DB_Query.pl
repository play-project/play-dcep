%% findSubclasses(+Parent, -SubClasses)
% 
% Find sub classes of the class given in 'Parent'. 
% The result will be a list with all subclasses.
% 
findSubclasses(Parent, SubClasses) :- 
(
	findall(Subclass, rdf(Subclass, 'http://www.w3.org/2000/01/rdf-schema#subClassOf', Parent), SubClasses)
).


%% solutionExists(+Class, +EventId)
%
% Check if it is possible to find a solution for the given Class.
solutionExists(Class, EventId) :- 
(
	\+forall(
		rdf(_Subject, Class, _Object, EventId), % DB query. This query depends on the BDPL query.
		false
	 )
).


% Einfach geschaltelte Schleife.

%rdf_assert(a1, b1, c1, e1).
%rdf_assert(a2, b1, c1, e1).
%rdf_assert(a3, b2, c1, e1).
%rdf_assert(a4, b3, c1, e1).

f6:-(\+forall(rdf(A, B, c1, e1), (\+((rdf(A, B, c1, e1), rdf(A,  b1, c1, e1)))))). % Result exits
f7:-(\+forall(rdf(A, B, c1, e1), (\+((rdf(A, B, c1, e1), rdf(a5, b1, c1, e1)))))). % No positive result.

