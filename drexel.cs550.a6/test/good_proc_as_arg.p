define apply
proc (f, x)
	return f(x)
end;

define addOne
proc (n)
	return n + 1
end;

define addTwo
proc (n)
	return n + 2
end;

a := 1;
aPlusOne := apply(addOne,a);
aPlusTwo := apply(addTwo,a)
