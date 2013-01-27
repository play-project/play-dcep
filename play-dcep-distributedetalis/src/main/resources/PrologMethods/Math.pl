%TODO multiple results.
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