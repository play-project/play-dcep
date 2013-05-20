% Print statistical data.

% Print number of reference counters.
printRefCountN :- 
(
	retractall(statClounter(_)),
	assert(statClounter(0)),
	forall(
		referenceCounter(DC, _, _),
		increment(DC)
	), 
	write('Number of reference counters: '), 
	statClounter(N), 
	write(N), 
	nl
).

% Print number of events available in this instance.
printNumberOfEvents :- 
(	
	rdf_statistics(graphs(Count)),
	write('Number of events: '),
	write(Count),
	nl
).

% SEM-Web Lib statistics.
printRdfStat :- 
	write('Number of triples: '),
	rdf_statistics(triples(Count)),
	write(Count), 
	nl
.

% Print all reference counters.
printReferenceCounters :-
(
	forall(
	  referenceCounter(ID1, ID2, V),
	  (
	    write('referenceCounter('),
	    write(ID1),
	    write(', '),
	    write(ID2),
	    write(', '),
	    write(V),
	    write(')'),
	    nl
	  )	
	)
).

% Helpers
%---------

% Increment value in predicate statClounter\1.
increment(_V) :- 
	statClounter(N),
	(Np1 is (N + 1)),
	retractall(statClounter(_)),
	assert(statClounter(Np1))
.