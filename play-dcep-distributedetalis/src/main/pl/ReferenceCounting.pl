% Increment counter for given event id.
incrementReferenceCounter(ID):- (referenceCounter(ID, ID2, X), X2 is X + 1,retractall(referenceCounter(ID, ID2, X)), assert(referenceCounter(ID, ID2, X2))).

% Decrement counter for given event id.
decrementReferenceCounter(ID):- (referenceCounter(ID, ID2, X), X2 is X - 1,retractall(referenceCounter(ID, ID2, X)), assert(referenceCounter(ID, ID2, X2)), collectGarbage(ID)).

% Set id of last Event. New id must be > than the old id.
setLastInsertedEvent(Id):- (retractall(lastInsertedEvent(_)), assert(lastInsertedEvent(Id))).

% Remove elements if counter == 0
collectGarbage(ID) :- (referenceCounter(ID,_ID2 ,X), X==0, rdf_retractall(_S,_P,_O,ID), retractall(referenceCounter(ID, _ID2, X)); true).

%Delte unused triples.
%Related event was fired and counter == 0.
collectGarbage :- (forall(referenceCounter(_ID, ID2, _X), (lastInsertedEvent(Id3), (ID2=<Id3), referenceCounter(_ID, ID2, X), (X == 0)) -> retract(referenceCounter(_D1, ID2, _I2)); (true))).

transformToNumber(A, B):- catch(atom_number(A, B), _Exception, B is A).

%Save variable values.
variabeValuesAdd(PatternId, VariableName, Value):- (assert(variableValues(PatternId, VariableName,Value)); true).

%Remove Variable value
variabeValuesDel(PatternId, VariableName, Value):- retract(variableValues(PatternId, VariableName,Value)).

