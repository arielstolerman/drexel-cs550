i:=5;
j:=3;
x:=0;
while i do
    
    if j then
        x:= x+2;	
        j:=j-1			// j should end up being 0
    else
        x:= x+1			// x should end up being 8
    fi;
    i:=i-1				// i should end up being 0
od
