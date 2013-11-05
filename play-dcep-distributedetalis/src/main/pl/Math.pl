% Math functions.
%
% This file contains common math operators.
% This functions are able to deal with integer and atom representation of numbers.
%
% @author Stefan Obermeier

%Relational operator
equal(A, B) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	A1 = B1
).

notEqual(A, B) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	A1 =\= B1
).

greater(A, B) :- 
(
	transformToNumber(A, A1), 
	transformToNumber(B, B1), 
	A1 > B1
).

greaterOrEqual(A, B) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	A1 >= B1
).

less(A, B) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	A1 < B1
).

lessOrEqual(A, B) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	A1 =< B1
).

minus(A, B, Result) :- 
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	Result is (A1 - B1)
).

plus(A, B, Result) :-
(
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	Result is (A1+B1)
).

multiply(A, B, Result) :- (
	transformToNumber(A, A1),
	transformToNumber(B, B1),
	Result is (A1 * B1)
).

%Get abs value
abs(InputValue, AbsValue):- 
(
	transformToNumber(InputValue, InputValue1),
	InputValue1 >= 0,
	AbsValue is InputValue1
).

abs(InputValue1, AbsValue):- 
(
	transformToNumber(InputValue, InputValue1),
	InputValue < 0,
	AbsValue is (InputValue1*(-1))
).