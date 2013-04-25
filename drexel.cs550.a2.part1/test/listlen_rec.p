define listlen
proc(l)
	if intp(l) then
		return 0
	else
		if nullp(l) then
			return 0
		else
			return 1 + listlen(cdr(l))
		fi
	fi
end;

l := listlen([1,2,3]);
m := listlen([1,[2,3,4],5]);
n := listlen([]);
o := listlen(666)