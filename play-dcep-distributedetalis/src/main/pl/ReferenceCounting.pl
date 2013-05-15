% Increment counter for given event id.
% X == -1 has a special meaning. If X== -1, ID was never used before.
% X == 0: ID is no longer referenced.
% X >  0: ID is X times referenced.
incrementReferenceCounter(ID):- 
(
	(
		referenceCounter(ID, ID2, X), (X == -1) 
	-> 
		(X2 is X + 2)
	;
		(
			referenceCounter(ID, ID2, X), 
			X2 is X + 1)
		), 
	retractall(referenceCounter(ID, ID2, X)),
	assert(referenceCounter(ID, ID2, X2))
).

% Decrement counter for given event id.
decrementReferenceCounter(ID):- 
(
	referenceCounter(ID, ID2, X), 
	X2 is X - 1,
	retractall(referenceCounter(ID, ID2, X)), 
	assert(referenceCounter(ID, ID2, X2)),
	collectGarbage(ID)
).

% Set id of last Event. New id must be > than the old id.
setLastInsertedEvent(Id):- 
(
	retractall(lastInsertedEvent(_)),
	assert(lastInsertedEvent(Id))
).

% Remove elements if counter == 0
collectGarbage(ID) :- 
(
	referenceCounter(ID,_ID2 ,X),
	 X == 1, 
	 rdf_retractall(_S,_P,_O,ID), 
	 retractall(referenceCounter(ID, _ID2, X))
	 ; 
	 true
).

%Delte unused triples.
%Delete all events older than the last fired event and if they are not in use.
collectGarbage :- 
(
	forall(
		referenceCounter(_ID, ID2, _X), 
		(
			lastInsertedEvent(Id3), 
			(ID2=<Id3), 
			referenceCounter(_ID, ID2, X),
			 (X < 1)
		)
		-> 
			retract(
				referenceCounter(_D1, ID2, _I2)
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

