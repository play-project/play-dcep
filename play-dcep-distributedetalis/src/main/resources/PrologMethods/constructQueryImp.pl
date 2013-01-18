%Construct query
generateConstructResult(S,P,O,DB) :- assert(currentDB(DB)), calcSxPxO(S,P,O).

% Calculates the cartesian product. SxPxO
calcSxPxO(S,P,O) :- visitSubjects(S,P,O).
visitSubjects(S,P,O) :- getNextS(S, P, O).
getNextS([],_P,_O):- retract(currentDB(_DB)).
getNextS([S|L],P,O) :-   visitPredicates(S,P,O), getNextS(L,P,O).

visitPredicates(S,P,O) :- getNextP(S,P,O).
getNextP(_S,[],_O).
getNextP(S,[P|L],O) :- visitObjects(S,P,O), getNextP(S,L,O).

visitObjects(S,P,O) :- getNextO(S,P,O).
getNextO(_S,_P,[]).
% getNextO(S,P,[C|L]) :-  write(S), write('\t'), write(P), write('\t'), write(C), nl,  getNextO(S,P,L).
getNextO(S,P,[C|L]) :-  writeInDB(S,P,C), getNextO(S,P,L).

% Write parameter S,P,O in SWI-Prolog "semweb/rdf_db" db.
%writeInDB(S,P,O):- write('write Result'), currentDB(DB), rdf_assert(S,P,O,DB).

% writeInDB(S,P,O):- write('write Result'),currentDB(DB), assert(rdfTest(S,P,O,DB)). % for testing

 writeInDB(S,P,O):- currentDB(DB), assert(rdfTest(S,P,O,DB)).
% With debugging output.
% writeInDB(S,P,O):- currentDB(DB), assert(rdfTest(S,P,O,DB)), write('Used stack: '), statistics(globalused, STACK),  write(STACK), write(' Used heap: '), statistics(heapused, HEAP) , write(HEAP), write('GC collected heap: '),statistics(garbage_collection, SIZE), write(SIZE), write('\n'), write(' Number of triples: '), rdf_statistics(triples(Count)), write(Count), write('\n\n'), garbage_collect_atoms, garbage_collect .
 


