define add 
proc(n)
  i := n;
  s := 0;
  while i do
  	s := s + i;
  	i := i-1
  od;
  return s
end;
n := 5;			// n should end up being 5
s := add(n)		// s should end up being 15
