% Data will be handled by garbage collector if last access is older than gcDelay/1.
:- dynamic gcDelay/1.
:- assert(gcDelay(1)).

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
	get_time(Time), assert(referenceCounter(ID, Time, X2))
	% write('increment GC counter: '), write(X2), write(' ID: '), write(ID), nl
).

% Decrement counter for given event id.
decrementReferenceCounter(ID):- 
(
	referenceCounter(ID, _Time, X), 
	X2 is X - 1,
	%write('decrement GC counter: '), write(X2), write(' ID: '), write(ID), nl,
	retractall(referenceCounter(ID, _Time, _X)), 
	get_time(Time), assert(referenceCounter(ID, Time, X2)),
	collectGarbage
).

% Remove elements if counter == 0
collectGarbage(ID) :- 
(
	 referenceCounter(ID, _Time ,X),
	 X = 0, 
	 rdf_retractall(_S,_P,_O, ID),
	 % write(' GC delete ID:'), write(ID), 
	 retractall(referenceCounter(ID, _Time, X))
	 ; 
	 true
).

%Delte unused triples.
%Delete all events older than value of $Delay. If they are not in use.
collectGarbage :- 
(
	forall(
		referenceCounter(_ID, Time, _X), 
		(
			get_time(TimeNow),
			gcDelay(Delay),
			(Time < (TimeNow - Delay)), % Event has to be $Delay seconds old. 
			referenceCounter(IDr, Time, X),
			(X = 0) % RecerecneCounter is 0
		)
		-> 
			collectGarbage(IDr)
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
	assert(variableValues(PatternId, VariableName, Value))
	% , nl, write('variabeValuesAdd: PatternId, VariableName, Value'), nl, write(PatternId), nl, write(VariableName),nl, write('value')
;
	true
).

%Remove Variable value
variabeValuesDel(Id):- 
	retractall(variableValues(Id, _, _)).

