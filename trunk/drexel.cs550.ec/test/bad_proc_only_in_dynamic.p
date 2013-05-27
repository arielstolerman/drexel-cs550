// this code should fail only for dynamic scoping

define foo
proc (n)
	define bar
	proc (m)
		return m + n
	end;
	return bar
end;

f := foo(5);
a := f(3)
