x:=0;
y:=1;
i:=4;
if x+y then
    while i do		
        x:= x+y;	// x should end up being 4
        i:= i-1		// i should end up being 0
    od
else
    x:= i-1
fi
