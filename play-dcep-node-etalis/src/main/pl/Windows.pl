% Windows in CEP.
%
% This file contains functions to deal with different kind of windows.
% Possible window types are: tumbling window, sliding window, count-based window.
%
% @author Stefan Obermeier


%% tumbling_window(+PatternId:atom, +WindowSize:int)
%
% Start tumbling window timer. Reset pattern if time is up and restart timer.
%
% @param PatternId Id of the pattern the window is dedicated to.
% @param WindowSize Time in seconds. After this time the pattern will be reseted  and the timer will restart.

tumbling_window(PatternId, WindowSize):- 
alarm(
	WindowSize,
	(
		resetPattern(PatternId),
		tumbling_window(PatternId, WindowSize)
	),
	 _ID,
	[]
).

print :- alarm(1 , (write('Hello World!')), _ID, []).

%% resetPattern(+PatternId:atom)
%
% Delete all consumed events for given pattern.
% 
% @param PatternId Id of the pattern which should be reseted.
resetPattern(PatternId) :- 
findall(
	etr_db(T2,T1,B,A,C,PatternId),
	(
		etr_db(T2,T1,B,A,C,PatternId),
		retract(etr_db(T2,T1,B,A,C,PatternId))
	),
	_L
).
