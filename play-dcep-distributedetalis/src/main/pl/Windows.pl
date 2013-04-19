% Time-based tumbling window.
tumbling_window(PatternId, WindowTime):- alarm(WindowTime,  resetPattern(PatternId), _ID, []).

resetPattern(PatternId) :- findall(etr_db(T2,T1,B,A,C,PatternId),(etr_db(T2,T1,B,A,C,PatternId),retract(etr_db(T2,T1,B,A,C,PatternId))),_L).
