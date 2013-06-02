%% solutionExists(+Class, +EventId)
%
% Check if it is possible to find a solution for the given Class.
solutionExists(Class, EventId) :- 
(
	\+forall(
		rdf(Subject, Class, Object, EventId), % DB query. This query depends on the BDPL query.
		false
	 )
).
