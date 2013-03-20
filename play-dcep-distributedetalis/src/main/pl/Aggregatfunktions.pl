% Calulate average (Arithmetic mean) of the values in the last period.
% Author: Stefan Obermeier

%Add new value to list.
addAgregatValue(Id, Element) :- (agregateListExists(Id) -> % Check if valuelist exists.
		aggregatDb(Id,List), putInList(List,[],Element,Lnew), assert(aggregatDb(Id,Lnew)), retractall(aggregatDb(Id,List)); % Add element to existing list.
		assert(aggregatDb(Id,[Element]))). % Put element in new list.

calcAverage(Id, WindowSize) :- (aggregatDb(Id,List),get_time(Time), calcAvgIter(List, (Time-WindowSize), 0, 0, Avg), write(Avg)). %Calc avg recursivly


% Helpers

agregateListExists(Id) :- (catch(aggregatDb(Id,_List), _Exception, false)). % Check if datastructure exists.

calcAvgIter([], _WindowEnd, Sum, N, Result):- (Result is Sum/N).
calcAvgIter([H|T], WindowEnd, Sum, N, Result) :- ((H >= WindowEnd) ->
			calcAvgIter(T, WindowEnd, (Sum + H), (N + 1), Result);
			calcAvgIter([], WindowEnd, Sum, N, Result)). % Stop recursion if value is out of window.

% Organize values in increasing order.
putInList([],Left,Element, Result) :- append(Left, [Element], Result). % Element is last element in list. 
putInList([H|T],LeftBuffer, Element, Result) :- ((Element>=H) -> 
	append(LeftBuffer, ([Element|([H|T])]),Result); 
	append(LeftBuffer, [H], Left), (putInList(T, Left, Element, Result))). 


addCurrentTime(Id) :- (get_time(T), save(Id, T)).
%assert(aggregatDb(1,[])).

