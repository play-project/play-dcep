% Increment counter for given ID.
incrementReferenceCounter(ID):- (id(ID,X), X2 is X + 1,retractall(id(ID,X)), assert(id(ID,X2))).

% Decrement counter for given ID.
decrementReferenceCounter(ID):- (id(ID,X), X2 is X - 1,retractall(id(ID,X)), assert(id(ID,X2))).

% Remove elements if counter == 0
collectGarbage(ID) :- (id(ID,X), X<2, rdf_retractall(_S,_P,_O,ID), retractall(id(ID,X)); true).

%Relational operator
equal(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1=B1).
notEqual(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1=\=B1).
greater(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1>B1).
greaterOrEqual(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1>=B1).
less(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1<B1).
lessOrEqual(A,B) :- (transformToNumber(A,A1), transformToNumber(B,B1), A1=<B1).

minus(A,B,Result) :- (transformToNumber(A,A1), transformToNumber(B,B1), (Result is A1-B1)).
plus(A,B,Result) :- (transformToNumber(A,A1), transformToNumber(B,B1), (Result is A1+B1)).
multiply(A,B,Result) :- (transformToNumber(A,A1), transformToNumber(B,B1), (Result is A1*B1)).


transformToNumber(A, B):- catch(atom_number(A, B), _Exception, B is A).

%Get abs value
abs(InputValue,AbsValue):- (InputValue>=0, AbsValue is InputValue).
abs(InputValue,AbsValue):- (InputValue<0, AbsValue is (InputValue*(-1))). 

%Save variable values.
variabeValuesAdd(PatternId, VariableName, Value):- (assert(variableValues(PatternId, VariableName,Value)); true).

%Remove Variable value
variabeValuesDel(PatternId, VariableName, Value):- retract(variableValues(PatternId, VariableName,Value)).