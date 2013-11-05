% Increment counter for given event id.
% X == -1 has a special meaning. If X== -1, ID was never used before.
% X == 0: ID is no longer referenced.
% X >  0: ID is X times referenced.
incrementReferenceCounter(ID):- 
(
	(
		referenceCounter(ID, _Time, X), (X = -1)
		 
	-> 
		(X2 is X + 2)
	;
		(
			referenceCounter(ID, _Time, X), 
			X2 is X + 1
		)
	), 
	retractall(referenceCounter(ID, _Time, _X)),
	get_time(Time), assert(referenceCounter(ID, Time, X2)),
	write('increment GC counter: '), write(X2), write(' ID: '), write(ID), nl
).

% Decrement counter for given event id.
decrementReferenceCounter(ID):- 
(
	referenceCounter(ID, _Time, X), 
	X2 is X - 1,
	write('decrement GC counter: '), write(X2), write(' ID: '), write(ID), nl,
	retractall(referenceCounter(ID, _Time, X)), 
	get_time(Time), assert(referenceCounter(ID, Time, X2)),
	collectGarbage
).

% Remove elements if counter == 0
collectGarbage(ID) :- 
(
	 referenceCounter(ID,_Time ,X),
	 X = 0, 
	 rdf_retractall(_S,_P,_O,ID),
	 write('GC delete ID:'), write() 
	 retractall(referenceCounter(ID, _Time, X))
	 ; 
	 true
).

%Delte unused triples.
%Delete all events older than 5s. If they are not in use.
collectGarbage :- 
(
	forall(
		referenceCounter(_ID, Time, _X), 
		(
			get_time(TimeNow),
			(Time < (TimeNow - 5)), % Event has to be 10s old. 
			referenceCounter(_ID, Time, X),
			 (X = 0) % RecerecneCounter is 0
		)
		-> 
			retract(
				referenceCounter(_D1, Time, X)
			)
		;
			true
	)
).



transformToNumber(A, B):- 
catch(
	atom_number(A, B),
	_Exception, 
	B is A
).

%Save variable values.
variabeValuesAdd(PatternId, VariableName, Value):- 
(
	assert(variableValues(PatternId, VariableName,Value))
;
	true
).

%Remove Variable value
variabeValuesDel(PatternId, VariableName, Value):- 
	retract(variableValues(PatternId, VariableName, Value)).

