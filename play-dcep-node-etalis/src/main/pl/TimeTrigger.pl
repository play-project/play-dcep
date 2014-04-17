% Time based trigger with prolog.
%
% @author Stefan Obermeier

% Fire event after a delay.
%
% Depends on etalis.
% @param Event name of event which will be produced.
% @param Delay in seconds.
triggerEventWithDelay(Event, Delay):- 
alarm(
		Delay,
		event(Event),
		_ID,
		[]
	).