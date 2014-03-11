%Construct query
generateConstructResult(S, P, O, DB) :- 
(
	(var(S); var(P); var(O); var(DB)) -> % Add only valid data to result set.
		true;
		(
			rdfTest(S, P, O, DB) %Do not add data if triple exists
			->  
				(true);
				assert(rdfTest(S, P, O, DB))
		)
).

constructResultIsNotEmpty(DB) :- 
	rdfTest(_S, _P, _O, DB).

%Add data so it is possible to find something.
rdfTest(s1,p1,o1,db1).