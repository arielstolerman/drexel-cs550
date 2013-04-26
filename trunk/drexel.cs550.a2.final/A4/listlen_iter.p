define listlen
proc(l)
	if intp(l) then
		return 0
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
		od
	fi;
	return n
end;

l := listlen([1,2,3]);
m := listlen([1,[2,3,4],5]);
n := listlen([]);
o := listlen(666)