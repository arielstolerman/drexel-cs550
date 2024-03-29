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
% ----------
% base case
update([],value(I,V),[value(I,V)]).
% override binding
update([value(I,_)|Env], value(I,V), [value(I,V)|Env]).
% continue search
update([value(J,V1)|Env], value(I,V2), [value(J,V1)|Env1]) :-
	update(Env, value(I,V2), Env1).

% assign
% ------
reduce(config(assign(I,E),Env),Env1) :-
	atom(I), reduce_value(config(E,Env),V), update(Env, value(I,V), Env1).

% if
% --
% then clause
reduce(config(if(E,L1,_),Env),Env1) :-
	reduce_value(config(E,Env),V), V>0, reduce_all(config(L1,Env),Env1).
% else clause
reduce(config(if(E,_,L2),Env),Env1) :-
	reduce_value(config(E,Env),V), V=<0, reduce_all(config(L2,Env),Env1).

% while
% -----
% continue to another iteration
reduce(config(while(E,L),Env),Env2) :-
	reduce_value(config(E,Env),V), V>0, reduce_all(config(L,Env),Env1), reduce(config(while(E,L),Env1),Env2).
% stop while
reduce(config(while(E,_),Env),Env) :-
	reduce_value(config(E,Env),V), V=<0.

% seq
% base case
reduce_all(config(S,Env),Env1) :- reduce(config(S,Env),Env1), !.
% recursive case
reduce_all(config(seq(S,L),Env),Env2) :-
	reduce(config(S,Env),Env1), reduce_all(config(L,Env1),Env2).

% initial env
reduce_all(config(L,[]),Env) :- reduce_all(L,Env).

% transitive closure for statements
reduce_all(config(S,Env),config(S2,Env)) :- 
	reduce(config(S,Env),config(S1,Env)), reduce_all(config(S1,Env),config(S2,Env)).
