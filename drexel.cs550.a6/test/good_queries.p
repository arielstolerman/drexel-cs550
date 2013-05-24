x := 1;
y := [];
z := [1,2,3];
w := [z,4];
define foo
proc (n)
	return n + 1
end;

intpx := intp(x);
listpx := listp(x);
procpx := procp(x);
intpy := intp(y);
listpy := listp(y);
nlpy := nullp(y);
nlpz := nullp(z);
procpz := procp(z);
nlpaw := nullp(car(w));
intpfoo := intp(foo);
procpfoo := procp(foo)
