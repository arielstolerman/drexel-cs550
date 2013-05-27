define foo
proc (n)
	x := 3;
	
	define bar
	proc (m)
		y := 4;
		
		define moo
		proc (l)
			z := 5;
			return l + z
		end;
		
		return m + moo(y)
	end;
	
	return n + bar(x)
end;

a := foo(2)
