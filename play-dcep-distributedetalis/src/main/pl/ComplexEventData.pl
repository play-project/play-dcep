%Construct query
generateConstructResult(S, P, O, DB) :- 
(
	rdfTest(S, P, O, DB) %Do not add data if triple exists
	->  
		(true);
		assert(rdfTest(S, P, O, DB))
).

%Add 
rdfTest(s1,p1,o1,db1).