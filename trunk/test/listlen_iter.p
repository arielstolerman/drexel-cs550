define listlen
proc(l)
	if intp(l) then
		return -1
	else
		cond := 1;
		n := 0;
		while cond do
			if nullp(l) then
				cond := 0
			else
				n := n + 1;
				l := cdr(l)
			fi
		od;
		return n
end;

x := listlen([1,2,3]);
y := listlen([]);
