x := 5;					// x should end up being 5

define recfact
  proc(n)
    if n then
    	return n * recfact(n - 1)
    else
    	return 1
    fi
end;

y := recfact(x)			// y should end up being 120
