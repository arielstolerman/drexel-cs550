% Part II of assignment 4
% reduction rules for arithmetic expressions.
% Author: Jeremy Johnson
% --------
% Group 1 extension

% Test cases:
%
%	reduce_all(times(plus(2,3),minus(5,1)),V).
%	V = 20 ?
%
%	reduce_all(config(times(plus(x,3),minus(5,y)),[value(x,2),value(y,1)]),V).
%	V = config(20,[value(x,2),value(y,1)]) ? 
%
%	reduce_exp_all(config(plus(times(2,5),minus(2,5)),[]),V).
%	V = config(7,[])
%
%	reduce_exp_all(config(plus(times(x,5),minus(2,y)),[value(x,2),value(y,5)]),V).
%	V = config(7,[value(x,2),value(y,5)])
%
%	reduce_all(config(seq(assign(x,3),assign(y,4)),[]),Env).
%	Env = [value(x,3),value(y,4)]
%
%	reduce(config(if(3,assign(x,3),assign(x,4)),[]),Env).
%	Env = [value(x,3)]
%
%	reduce(config(if(0,assign(x,3),assign(x,4)),[]),Env).
%	Env = [value(x,4)]
%
%	reduce_all(config(if(n,assign(i,0),assign(i,1)),[value(n,3)]),Env).
%	Env = [value(n,3),value(i,0)]
%
%	reduce_all(config(while(x,assign(x,minus(x,1))),[value(x,3)]),Env).
%	Env = [value(x,0)]
%
%	reduce_all(config(
%		seq(assign(n,minus(0,3)),
%		seq(if(n,assign(i,n),assign(i,minus(0,n))),
%		seq(assign(fact,1),
%			while(i,seq(assign(fact,times(fact,i)),assign(i,minus(i,1)))))))
%		,[]),Env).
%	Env = [value(n,-3),value(i,0),value(fact,6)]
%

% Env lookup

lookup([value(I,V)|_],I,V).
lookup([_|Es],I,V) :- lookup(Es,I,V), !.

% (1) '0' => 0,..., '9' => 9
% (2) V'0' => 10*V,...,V'9' => 10*V+9

reduce_all(config(V,Env),config(V,Env)) :- integer(V), !.

% (3) V1 '+' V2 => V1 + V2

reduce(config(plus(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1+V2.

% (4) V1 '-' V2 => V1 - V2

reduce(config(minus(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1-V2.

% (5) V1 '*' V2 => V1 * V2

reduce(config(times(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1*V2.

% rule (6) is missing

% (7)					<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<E | Env> '+' <E2 | Env> => <E1 | Env> '+' <E2 | Env>

reduce(config(plus(E,E2),Env),config(plus(E1,E2),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% (8)					<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<E | Env> '-' <E2 | Env> => <E1 | Env> '-' <E2 | Env>

reduce(config(minus(E,E2),Env),config(minus(E1,E2),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% (9)					<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<E | Env> '*' <E2 | Env> => <E1 | Env> '*' <E2 | Env>

reduce(config(times(E,E2),Env),config(times(E1,E2),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% (10)				<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<V | Env> '+' <E | Env> => <V | Env> '+' <E1 | Env>

reduce(config(plus(V,E),Env),config(plus(V,E1),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% (11)				<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<V | Env> '-' <E | Env> => <V | Env> '-' <E1 | Env>

reduce(config(minus(V,E),Env),config(minus(V,E1),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% (12)				<E | Env> => <E1 | Env>
% 		-----------------------------------------------------
% 		<V | Env> '*' <E | Env> => <V | Env> '*' <E1 | Env>

reduce(config(times(V,E),Env),config(times(V,E1),Env)) :- 
     reduce(config(E,Env),config(E1,Env)).

% rule (13) is missing

% (14)     <E | Env> => <E1 | Env>, <E1 | Env> => <E2 | Env>	[transitive closure]
% 		-----------------------------------------------------
% 					<E | Env> => <E2 | Env>

reduce_all(config(E,Env),config(E2,Env)) :- 
	reduce(config(E,Env),config(E1,Env)), reduce_all(config(E1,Env),config(E2,Env)).

% (15)		  Env(I) = V
% 		----------------------
% 		<I | Env> => <V | Env>

reduce(config(I,Env),config(V,Env)) :- atom(I), lookup(Env,I,V).


% =============================================================================
% Added code
% =============================================================================

% (16)	<I ':=' V | Env> => Env & {I = V}

lookup(Env,I,V) :- atom(I), integer(V), config(assign(I,V),Env).								% ?????

% (17)		   <E | Env> => <E1 | Env> 
% 		-------------------------------------
% 		<I ':=' E | Env> => <I ':=' E1 | Env> 

reduce(config(assign(I,E),Env),config(assign(I,E1),Env)) :-
	reduce(config(E,Env),config(E1,Env)).
 
% (18)		  <S | Env> => Env1
% 		-----------------------------
% 		<S ';' L | Env> => <L | Env1> 

reduce(config(seq(S,L),Env),config(L,Env1)) :-
	reduce(config(S,Env),Env1).
 
% (19)	L => <L | Env0>

reduce(L,config(L,Env0)) :- seq(L).																% ?????
 
% (20)								<E | Env> => <E1| Env>
% 		-----------------------------------------------------------------------------------
% 		<'if' E 'then' L1 'else' L2 'fi' | Env> => <'if' E1 'then' L1 'else' L2 'fi' | Env> 

reduce(config(if(E,L1,L2),Env),config(if(E1,L1,L2),Env)) :-
	reduce(config(E,Env),config(E1,Env)).
 
% (21)						  V > 0
% 		-----------------------------------------------------
% 		<'if' V 'then' L1 'else' L2 'fi' | Env> => <L1 | Env>

reduce(config(gtzero(V),Env),config(R,Env)) :- integer(V), !, R is (V > 0).						% ?????
reduce(config(if(V,L1,L2),Env),config(L1,Env)) :- integer(V), gtzero(V).
 
% (22)						  V => 0
% 		-----------------------------------------------------
% 		<'if' V 'then' L1 'else' L2 'fi' | Env> => <L2 | Env>

reduce(config(iszero(V),Env),config(R,Env)) :- integer(V), !, R is (V == 0).					% ?????
reduce(config(if(V,L1,L2),Env),config(L2,Env)) :- integer(V), iszero(V).
 
% (23)		<E | Env> => <V | Env>, V => 0
% 		------------------------------------
% 		<'while' E 'do' L 'od' | Env> => Env

reduce(config(while(E,L),Env),Env) :-															% ?????
	reduce(config(E,Env),config(V,Env)), iszero(V).
 
% (24)						<E | Env> => <V| Env>, V > 0
% 		-----------------------------------------------------------------
% 		<'while' E 'do' L 'od' | Env> => <L; 'while' E 'do' L 'od' | Env>

reduce(config(while(E,L),Env),config(seq(L,while(E,L)),Env)) :-									% ?????
	reduce(config(E,Env),config(V,Env)), gtzero(V).

% =============================================================================

reduce_value(config(E,Env),V) :- reduce_all(config(E,Env),config(V,Env)).
