=============================================================================
Test cases:
=============================================================================

reduce_all(config(times(plus(x,3),minus(5,y)),[value(x,2),value(y,1)]),V).
% V = config(20,[value(x,2),value(y,1)]) ? 

reduce_all(config(plus(times(2,5),minus(2,5)),[]),V).
% V = config(7,[])

reduce_all(config(plus(times(x,5),minus(2,y)),[value(x,2),value(y,5)]),V).
% V = config(7,[value(x,2),value(y,5)])

reduce_all(config(seq(assign(x,3),assign(y,4)),[]),Env).
% Env = [value(x,3),value(y,4)]

reduce(config(if(3,assign(x,3),assign(x,4)),[]),Env).
% Env = [value(x,3)]

reduce(config(if(0,assign(x,3),assign(x,4)),[]),Env).
% Env = [value(x,4)]

reduce_all(config(if(n,assign(i,0),assign(i,1)),[value(n,3)]),Env).
% Env = [value(n,3),value(i,0)]

reduce_all(config(while(x,assign(x,minus(x,1))),[value(x,3)]),Env).
% Env = [value(x,0)]

reduce_all(config(
seq(assign(n,minus(0,3)),
seq(if(n,assign(i,n),assign(i,minus(0,n))),
seq(assign(fact,1),
while(i,seq(assign(fact,times(fact,i)),assign(i,minus(i,1)))))))
,[]),Env).
% Env = [value(n,-3),value(i,0),value(fact,6)]
