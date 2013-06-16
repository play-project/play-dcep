%Construct query
generateConstructResult(S, P, O, DB) :- 
(
	assert(rdfTest(S, P, O, DB))
).