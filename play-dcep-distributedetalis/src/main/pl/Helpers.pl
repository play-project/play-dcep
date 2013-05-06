% This file contains methods with are use by other methods.
% Author: Stefan Obermeier

% Transform In to a number representation. In can be a Atom or number. The result is always a number.
transformToNumber(In, Out):- 
	catch(
		atom_number(In, Out),
		_Exception, 
		Out is In
).