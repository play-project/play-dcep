% Time-based tumbling window. Reset pattern when widow time is up and restart timer.
tumbling_window(PatternId, WindowTime):- alarm(WindowTime,  (resetPattern(PatternId), tumbling_window(PatternId, WindowTime)), _ID, []).

% Delete all all consumed events for given pattern.
resetPattern(PatternId) :- findall(etr_db(T2,T1,B,A,C,PatternId),(etr_db(T2,T1,B,A,C,PatternId),retract(etr_db(T2,T1,B,A,C,PatternId))),_L).
