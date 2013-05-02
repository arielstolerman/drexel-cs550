x:=0;				//store CONST 0 in pos 1. 	VAR x in pos 4
y:=1;				//store CONST 1 in pos 2	VAR y in pos 5
i:=4;				//store CONST 4 in pos 3	VAR i in pos 6
if x+y then			//Not sure if you can have this in an if, but thought it was worth testing
    while i do		
        x:= x+y;	//add y to x, 3 times.	Store 3 in x position 4
        i:= i-1		//store 0 in i pos 6
    od
else
    x:= i-1
fi