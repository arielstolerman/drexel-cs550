define genAdder
proc (toAdd)
	define p
	proc (n)
		return n + toAdd
	end;
	return p
end;

a := 1;
toAdd := 3; // this way it works in both static and dynamic mode
addThree := genAdder(toAdd);
aPlusThree := addThree(a)
