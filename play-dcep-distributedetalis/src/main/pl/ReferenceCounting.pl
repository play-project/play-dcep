% Increment counter for given event id.
incrementReferenceCounter(ID):- (id(ID, ID2, X), X2 is X + 1,retractall(id(ID, ID2, X)), assert(id(ID, ID2, X2))).

% Decrement counter for given event id.
decrementReferenceCounter(ID):- (id(ID, ID2, X), X2 is X - 1,retractall(id(ID, ID2, X)), assert(id(ID, ID2, X2))).

% Represents the id of the last inserted event.
%assert(lastsentEvent(0)).

% Remove elements if counter == 0
collectGarbage(ID) :- (id(ID,_ID2 ,X), X==0, rdf_retractall(_S,_P,_O,ID), retractall(id(ID, _ID2, X)); true).

%Delte unused triples.
collectGarbage :- (forall(id(_ID, ID2, _X), ((lastsentEvent(C), (ID2<C)) -> (write('K ID2  '), write(ID2) , write('\n')); (write('G ID2  '), write(ID2), write('\n')) ))).

transformToNumber(A, B):- catch(atom_number(A, B), _Exception, B is A).

%Save variable values.
variabeValuesAdd(PatternId, VariableName, Value):- (assert(variableValues(PatternId, VariableName,Value)); true).

%Remove Variable value
variabeValuesDel(PatternId, VariableName, Value):- retract(variableValues(PatternId, VariableName,Value)).

