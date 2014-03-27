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


