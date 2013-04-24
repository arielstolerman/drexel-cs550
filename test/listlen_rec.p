define listlen
proc(l)
	if intp(l) then
		return -1
	else
		if nullp(l) then
			return 0
		else
			return 1 + listlen(cdr(l))
		fi
	fi
end;

x := listlen([1,2,3]);
y := listlen([]);
