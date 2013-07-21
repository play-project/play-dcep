%Increment a counter for the given pattern id. 
%If a counter does not exist it will be created.
%If no counter for this pattern does not exist it will be added.
%To get the value use: eventCounter(PatternID,X).
incrementEventCounter(PatternID):- write('Increment counter'), nl,
	catch(eventCounter(PatternID, X1), E, assertCounter(E, PatternID))
	 -> (eventCounter(PatternID, X1), 
		X2 is X1 + 1, 
		retract(eventCounter(PatternID, X1)), 
		assert(eventCounter(PatternID, X2)))
	; assert(eventCounter(PatternID, 1)).
assertCounter(_Existence_error, PatternID):- assert(eventCounter(PatternID, 0)).

% In mode "on" the events of a given pattern ID will be counted.
setMeasurementMode(Mode):-(retractall(measurementMode(_X)); true), assert(measurementMode(Mode)).

measure(PatternID) :- (measurementMode(on), incrementEventCounter(PatternID));true.

deleteMeasuredData:- retractall(eventCounter(_PatternID,_X)).