%Construct query
generateConstructResult(S, P, O, DB) :- 
(
	rdfTest(S, P, O, DB) %Do not add data if triple exists
	->  
		(true);
		assert(rdfTest(S, P, O, DB))
).

constructResultIsNotEmpty(DB) :- 
	rdfTest(S, P, O, DB).

%Add data so it is possible to find something.
rdfTest(s1,p1,o1,db1).