% Calulate average (Arithmetic mean) of the values in the last period.
% Author: Stefan Obermeier

%Add new value to list.
addAgregatValue(Id, Element) :- (agregateListExists(Id) -> % Check if valuelist exists.
		aggregatDb(Id,List), putInList(List,[],Element,Lnew), retractall(aggregatDb(Id,List)), assert(aggregatDb(Id,Lnew)); % Add element to existing list.
		assert(aggregatDb(Id,[Element]))). % Put element in new list.

calcAverage(Id, WindowSize, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, Id, (Time-WindowSize), 0, 0, Avg), retractall(aggregatDb(Id, _Dc))). %Calc avg recursivly

% Safe value if it is the smallest value ever given.
% Distinction between different sorages is done bay Id.
storeMin(Id, Value) :- (catch(minValue(Id, V_old), E, assertMinVal(E, Id, Value)) -> % If value exists check if it is smaller than the saved value.
			(minValue(Id, V_old), ((V_old >= Value) -> (retract(minValue(Id, _V)), assert(minValue(Id, Value))); true));
			true). 
assertMinVal(_E,Id, Value) :- (assert(minValue(Id, Value)),false).

% Safe value if it is the bigest value ever given.
% Distinction between different sorages is done bay Id.
storeMax(Id, Value) :- (catch(maxValue(Id, V_old), E, assertMaxVal(E, Id, Value)) -> % If value exists check if it is bigger than the saved value.
			(maxValue(Id, V_old), ((V_old =< Value) -> (retract(maxValue(Id, _V)), assert(maxValue(Id, Value))); true));
			true). 
assertMaxVal(_E,Id, Value) :- (assert(maxValue(Id, Value)),false).

% Helpers
agregateListExists(Id) :- (catch(aggregatDb(Id,_List), _Exception, false)). % Check if data structure exists.

calcAvgIter([], _Id, _WindowEnd, Sum, N, Result):- ((Result is (Sum/N))).
calcAvgIter([H|T], Id, WindowEnd, Sum, N, Result) :- (transformToNumber(H, Hn), transformToNumber(WindowEnd, WindowEndN), (Hn >= WindowEndN) ->
			transformToNumber(H, Hn), calcAvgIter(T, Id, WindowEnd, (Sum + Hn), (N + 1), Result);
			transformToNumber(H, Hn), calcAvgIter([], Id, WindowEnd, (Sum + Hn), (N + 1), Result)). % Stop recursion if value is out of window.
%assert(aggregatDb(Id,H)), retractall(aggregatDb(Id,[H|T])),
% Organize values in increasing order.
putInList([],Left,Element, Result) :- (append(Left, [Element], Result)). % Element is last element in list. 
putInList([H|T],LeftBuffer, Element, Result) :- ((transformToNumber(Element, ElementN), transformToNumber(H, Hn), ElementN>=Hn) -> 
	append(LeftBuffer, ([Element|([H|T])]),Result); %Put in front
	append(LeftBuffer, [H], Left), (putInList(T, Left, Element, Result))).



%Debug versions.
%calcAverage(Id, WindowSize, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, (Time-WindowSize), 0, 0, Avg), write('Average is: '), write(Avg), nl).