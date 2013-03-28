% Calulate average (Arithmetic mean) of the values in the last period.
% Author: Stefan Obermeier

%Add new value to list.
addAgregatValue(Id, Element) :- (agregateListExists(Id) -> % Check if valuelist exists.
		aggregatDb(Id,List), putInList(List,[],Element,Lnew), assert(aggregatDb(Id,Lnew)), retractall(aggregatDb(Id,List)); % Add element to existing list.
		assert(aggregatDb(Id,[Element]))). % Put element in new list.

calcAverage(Id, WindowSize, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, (Time-WindowSize), 0, 0, Avg)). %Calc avg recursivly


% Helpers
agregateListExists(Id) :- (catch(aggregatDb(Id,_List), _Exception, false)). % Check if data structure exists.

calcAvgIter([], _WindowEnd, Sum, N, Result):- ((Result is Sum/N)).
calcAvgIter([H|T], WindowEnd, Sum, N, Result) :- (transformToNumber(H, Hn), transformToNumber(WindowEnd, WindowEndN), (Hn >= WindowEndN) ->
			transformToNumber(H, Hn), calcAvgIter(T, WindowEnd, (Sum + Hn), (N + 1), Result);
			transformToNumber(H, Hn), assert(aggregatDb(Id,H)), retractall(aggregatDb(Id,[H|T])), calcAvgIter([], WindowEnd, (Sum + Hn), (N + 1), Result)). % Stop recursion if value is out of window.

% Organize values in increasing order.
putInList([],Left,Element, Result) :- (append(Left, [Element], Result)). % Element is last element in list. 
putInList([H|T],LeftBuffer, Element, Result) :- ((transformToNumber(Element, ElementN), transformToNumber(H, Hn), ElementN>=Hn) -> 
	append(LeftBuffer, ([Element|([H|T])]),Result); %Put in front
	append(LeftBuffer, [H], Left), (putInList(T, Left, Element, Result))). 


%assert(aggregatDb(1,[])).

%Debug versions.
%calcAverage(Id, WindowSize, Avg) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, (Time-WindowSize), 0, 0, Avg), write('Average is: '), write(Avg), nl).

