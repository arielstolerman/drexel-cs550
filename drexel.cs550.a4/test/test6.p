x := 5;					// x should end up being 5

define fact
  proc(n)
    i := n;
    f := 1; 
       while i do 
           f := f * i; 
           i := i - 1 
       od;
       return f
end;

y := fact(x)			// y should end up being 120
