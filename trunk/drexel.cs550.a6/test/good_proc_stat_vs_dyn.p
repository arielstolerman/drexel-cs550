x := 1;

define f
proc (x)
	return g(2)
end;

define g
proc(y)
	return x + y
end;

a := f(5) // static: 3, dynamic: 7
