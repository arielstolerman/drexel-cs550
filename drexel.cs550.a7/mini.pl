% Part II of assignment 4
% reduction rules for arithmetic expressions.
% Author: Jeremy Johnson
% --------
% Group 1 extension

% test cases.
%
% reduce_all(config(times(plus(x,3),minus(5,y)),[value(x,2),value(y,1)]),V).
%    V = config(20,[value(x,2),value(y,1)]) ? 


% Env lookup

lookup([value(I,V)|_],I,V).
lookup([_|Es],I,V) :- lookup(Es,I,V), !.

% (1) '0' => 0,..., '9' => 9
% (2) V'0' => 10*V,...,V'9' => 10*V+9

reduce_all(config(V,Env),config(V,Env)) :- integer(V), !.

% (3) V1 '+' V2 => V1 + V2

reduce(config(plus(V1,V2),Env),config(R,Env)) :- integer(V1), integer(V2), !, R is V1+V2.

% (4) V1 '+' V2 => V1 + V2

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



% (17)		   <E | Env> => <E1 | Env> 
% 		-------------------------------------
% 		<I ':=' E | Env> => <I ':=' E1 | Env> 


 
% (18)		  <S | Env> => Env1
% 		-----------------------------
% 		<S ';' L | Env> => <L | Env1> 


 
% (19)	L => <L | Env0>


 
% (20)								<E | Env> => <E1| Env>
% 		-----------------------------------------------------------------------------------
% 		<'if' E 'then' L1 'else' L2 'fi' | Env> => <'if' E1 'then' L1 'else' L2 'fi' | Env> 


 
% (21)						  V > 0
% 		-----------------------------------------------------
% 		<'if' V 'then' L1 'else' L2 'fi' | Env> => <L1 | Env>


 
% (22)						  V => 0
% 		-----------------------------------------------------
% 		<'if' V 'then' L1 'else' L2 'fi' | Env> => <L2 | Env>


 
% (23)		<E | Env> => <V | Env>, V => 0
% 		------------------------------------
% 		<'while' E 'do' L 'od' | Env> => Env


 
% (24)						<E | Env> ? <V| Env>, V > 0
% 		-----------------------------------------------------------------
% 		<'while' E 'do' L 'od' | Env> => <L; 'while' E 'do' L 'od' | Env>


% =============================================================================

reduce_value(config(E,Env),V) :- reduce_all(config(E,Env),config(V,Env)).
