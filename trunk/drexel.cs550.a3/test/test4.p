i:=5;		//store CONST 5 in pos 1.	store VAR i in pos 4	
j:=3;		//store CONST 3 in pos 2.	store VAR j in pos 5
x:=0;		//store CONST 0 in pos 3.	store VAR x in pos 6
while i do
    
    if j then
        x:= x+2;	
        j:=j-1			//store final value of j, 0, in pos 5
    else
        x:= x+1			//store final value of x, 8, in pos 6
    fi;
    i:=i-1				//store final value of i, 0, in pos 4
od