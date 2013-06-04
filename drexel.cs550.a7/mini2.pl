% Part II of assignment 4
% reduction rules for arithmetic expressions.
% Author: Jeremy Johnson
% --------------------------------------------
% Group 1 Extension
%

lookup([value(I,V)|_],I,V).
lookup([_|Es],I,V) :- lookup(Es,I,V), !.

reduce_exp(config(plus(E,E2),Env),config(plus(E1,E2),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).
reduce_exp(config(minus(E,E2),Env),config(minus(E1,E2),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).
reduce_exp(config(times(E,E2),Env),config(times(E1,E2),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).

reduce_exp(config(plus(V,E),Env),config(plus(V,E1),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).
reduce_exp(config(minus(V,E),Env),config(minus(V,E1),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).
reduce_exp(config(times(V,E),Env),config(times(V,E1),Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)).

reduce_exp(config(plus(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1+V2.
reduce_exp(config(minus(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1-V2.
reduce_exp(config(times(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1*V2.

reduce_exp(config(I,Env),config(V,Env)) :- atom(I), lookup(Env,I,V).

reduce_exp_all(config(V,Env),config(V,Env)) :- integer(V), !.
reduce_exp_all(config(E,Env),config(E2,Env)) :- 
     reduce_exp(config(E,Env),config(E1,Env)), reduce_exp_all(config(E1,Env),config(E2,Env)).

reduce_value(config(E,Env),V) :- reduce_exp_all(config(E,Env),config(V,Env)).

% ==============================================================================
% Added code
% ==============================================================================

% Env update
update([],value(I,V),[value(I,V)]).
update([value(I,_)|Env], value(I,V), [value(I,V)|Env]).
update([value(J,V1)|Env], value(I,V2), [value(J,V1)|Env1]) :-
	update(Env, value(I,V2), Env1).

% assign
reduce(config(assign(I,E),Env),Env1) :-
	atom(I), reduce_value(config(E,Env),V), update(Env, value(I,V), Env1).

reduce(config(assign(I,E),Env),config(assign(I,E1),Env)) :-
	reduce_exp(config(E,Env),config(E1,Env)).

% if
reduce(config(if(E,L1,_),Env),config(L1,Env)) :-
	reduce_value(config(E,Env),V), V>0, reduce_all(config(L1,Env),Env).

reduce(config(if(E,_,L2),Env),config(L2,Env)) :-
	reduce_value(config(E,Env),V), V=:=0, reduce_all(config(L2,Env),Env).

% while
reduce(config(while(E,L),Env),config(seq(L,while(E,L)),Env)) :-
	reduce_value(config(E,Env),V), V>0.

reduce(config(while(E,_),Env),Env) :-
	reduce_value(config(E,Env),config(V,Env)), V=:=0.

% seq
%reduce_all(config(S,Env),Env1) :-
%	reduce(config(if(E,L1,L2),Env),config(S,Env)), reduce_all(config(if(E,L1,L2),Env),Env1), !.
%reduce_all(config(S,Env),Env1) :-
%	reduce(config(while(E,L),Env),config(S,Env)), reduce_all(config(while(E,L),Env),Env1), !.
%reduce_all(config(S,Env),Env1) :-
%	reduce(config(assign(I,E),Env),config(S,Env)), reduce_all(config(assign(I,E),Env),Env1), !.

reduce_all(config(S,Env),Env1) :- reduce(config(S,Env),Env1), !.

reduce_all(config(seq(S,L),Env),Env2) :-
	reduce(config(S,Env),Env1), reduce_all(config(L,Env1),Env2).

% initial env
reduce_all(config(L,[]),Env) :- reduce_all(L,Env).

% transitive closure for statements
reduce_all(config(S,Env),config(S2,Env)) :- 
	reduce(config(S,Env),config(S1,Env)), reduce_all(config(S1,Env),config(S2,Env)).
