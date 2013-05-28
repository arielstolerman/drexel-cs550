app([],Y,Y).
app([U|V],Y,[U|Z]) :- app(V,Y,Z).

rev([],[]).
rev([U|V],Y) :- rev(V,R), app(R,[U],Y).

%% tests:
%% rev([1,2,3],[3,2,1]).	-- yes
%% rev([1,2,3],[3,1,2]).	-- no
%% rev([1,2,3],U).			-- U = [3,2,1]
%%							   yes
%% rev(U,[1,2,3]).			-- U = [3,2,1] ? <enter>
%%							   yes
