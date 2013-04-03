% Time-based tumbling window.
t_tumbling_window(PatternId, WindowTime):- alarm(WindowTime,  resetPattern(PatternId), _ID, []).

resetPattern(PatternId):- (true).