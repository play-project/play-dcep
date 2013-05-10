% Aggregate functions
%
% This file provide functions to aggregate data.
% E.g. aggregate function like SUM, AVG, MIN, MAX...
%
% @author Stefan Obermeier



%% storeMaxT(+ID, +Value:int)
%
% Store value if it is the biggest value ever given.
%
% @param Id  Id to identify storage destination.
% @param Value Value to store.
storeMaxT(Id, Value):- 
(
	% If value is the first value for the given Id, assert it as the biggest value.
	catch(
		maxValue(Id, V_old),
		E,
		assertMaxVal(E, Id, Value)
	)
% If existing value is smaller than the new one replace it.
-> 
	(
		maxValue(Id, V_old),
		(
			(V_old =< Value)
		-> 
		(
			retract(maxValue(Id, _V)),
			assert(maxValue(Id, Value))
		)
		;
			true
		)
	)
;
	true
). 

%% assertMaxVal( -_E, +Id, +Value:int)
%
% Assert Id and Value pair. Used by MaxAggregate functions.
%
% @param _E  Exception.
% @param Id  Id to retrieve value.
% @param Value Value to store.
assertMaxVal(_E,Id, Value) :- 
(
	assert(maxValue(Id, Value))
,
	false
).



%% addAgregatValue (+Id, +Element)
%
% Data structure to store (time, value) tuple.
% All tuples are ordered by date, descending, so the newest value is always on top.
% Local system time is used. 
% Data structure: aggregatDb(Id,[[Time,Element]]).
%
% @param Id Id to identify datastructure.
% @param Element element to store.
addAgregatValue(Id, Element) :- 
(
	get_time(Time),
	addAgregatValue(Id, Time, Element)
).

%% addAgregatValue (+Id, +Time, +Element)
%
% Data structure to store (time, value) tuple.
% All tuples are ordered by date, descending, so the newest value is always on top.
% Data structure: aggregatDb(Id,[[Time,Element]]).
%
% @param Id Id to identify data structure.
% @param Element element to store.
addAgregatValue(Id, Time, Element) :- 
(
	agregateListExists(Id)
	 -> % Check if value list exists.
		aggregatDb(Id,List), 
		putInList(List,[],[Time,Element],Lnew), 
		retractall(aggregatDb(Id,List)), 
		assert(aggregatDb(Id,Lnew)) % Add element to existing list.
	;
		assert(aggregatDb(Id,[[Time,Element]])) % Put element in new list.
).
 

% Calulate average (Arithmetic mean) of the values in the last period.
calcAverage(Id, WindowSize, Avg) :- 
(
	aggregatDb(Id,List),
	get_time(Time),
	calcAvgIter(List, Id, (Time-WindowSize), 0, 0, Avg),
	retractall(aggregatDb(Id, _Dc))  %Calc avg recursivly
).




% Delete all values. 
resetMaxT(Id):- 
	retractall(maxValue(Id, _)).




% TODO use this.
% calcAverage(Id, WindowSize, Exp, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, Id, (Time-WindowSize), 0, 0, Avg), retractall(aggregatDb(Id, _Dc))). %Calc avg recursivly

% Add value to sum with given Id.
sumAdd(Id, Value):- (catch(sum(Id, V_old), E, assertSum(E, Id, Value)) -> % If value exists add new value. If not set value only.
			(sum(Id, V_old), retract(sum(Id, V_old)), assert(sum(Id, (V_old + Value)))); true).
assertSum(_E,Id, Value) :- (assert(sum(Id, Value)),false).


% Safe value if it is the smallest value ever given.
% Distinction between different sorages is done bay Id.
storeMin(Id, Value) :- (catch(minValue(Id, V_old), E, assertMinVal(E, Id, Value)) -> % If value exists check if it is smaller than the saved value.
			(minValue(Id, V_old), ((V_old >= Value) -> (retract(minValue(Id, _V)), assert(minValue(Id, Value))); true));
			true). 
assertMinVal(_E,Id, Value) :- (assert(minValue(Id, Value)),false).




% Helpers
agregateListExists(Id) :- 
(
	catch(
		aggregatDb(Id,_List), _Exception, false % Check if data structure exists.
	)
). 

% Return time and corresponding value.
% E.g. getFirstAndSecondElement(Input,Time,Value).
getTimeAndValue([H|T], Time, Value) :- 
(
	Time is H, 
	nextV(T,1,Value)
).
nextV([H|T], 1, Value) :- 
	Value = H.

% Organize values in increasing order.
% putInList(+OldList, [], +Element, -NewList). 
%
% Element is last element in list. 
putInList([],Left,Element, Result) :- 
(
	append(Left, [Element], Result)
). 

% Search place for Element.
putInList([H|T],LeftBuffer, Element, Result) :- 
(
	getTimeAndValue(H,TimeExistingElement, _),
	getTimeAndValue(Element,TimeNewElement, _),
	(TimeNewElement >= TimeExistingElement)
) 
-> 
	append(LeftBuffer, ([Element|([H|T])]),Result) % Put new value on top.
;
	append(LeftBuffer, [H], Left), (putInList(T, Left, Element, Result) % Check next element.
).
	
	
calcAvgIter([], _Id, _WindowEnd, Sum, N, Result):- 
(
	(Result is (Sum/N))
).

calcAvgIter([H|T], Id, WindowEnd, Sum, N, Result) :- 
(
	getTimeAndValue(H,Time, Value),
	(Time >= WindowEnd)
->
	transformToNumber(Value, Hn), 
	calcAvgIter(T, Id, WindowEnd, (Sum + Hn), 
	(N + 1), Result)
;
	transformToNumber(Value, Hn),
	calcAvgIter([], Id, WindowEnd,
	(Sum + Hn), (N + 1), Result)  % Stop recursion if value is out of window.
). 



% The idea behind this methods is a little bit like the GoV strategy pattern.
% The user knows the interface executeFunction(+Id, +In, -Result). Depending on the given Id
% different code is executed.

% Add new function (strategy). It can be executed with executeFunction(+Id, +ValueList, -Result).
% In the function the result has to be stored in variable named Result.
% Values stored in variable In can be used in the function.
assertFunction(Id, Exp) :- 
	assert(function(Id, In, Result) :- (Exp)).

% Execute function with id given in variable Id. 
% Parameters in variable In will be used. 
%The result of the calculation is stored in variable Result.
executeFunction(Id, In, Result):- 
(
	function(Id, In, Result)
).


%Debug versions.
%calcAverage(Id, WindowSize, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, (Time-WindowSize), 0, 0, Avg), write('Average is: '), write(Avg), nl).