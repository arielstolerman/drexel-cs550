define foo
proc (n)
	if n then
		return bar(n - 1)
	else
		return 1
	fi
end;

define bar
proc (n)
	if n then
		return foo(n - 1)
	else
		return 0
	fi
end;

define even
proc (n)
	return foo(n)
end;

a := 2;
isAEven := even(a);
b := 5;
isBEven := even(b)

