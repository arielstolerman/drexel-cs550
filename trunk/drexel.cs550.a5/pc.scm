;; Drexel CS550, Spring 2013
;; Group 1
;; Proposition Calculus Interpreter

;; UTILS

(define (tagged-list? exp tag)
	(if (pair? exp)
		(eq? (car exp) tag)
		false))

(define (true? x)
	(not (eq? x false)))

(define (false? x)
	(eq? x false))
  
(define (variable? exp) (symbol? exp))

;; main eval method
(define (eval exp env)
  (cond ((boolean? exp) exp)
        ((variable? exp) (lookup-variable-value exp env))
		((and? exp) (eval-and exp env))
		((or? exp) (eval-or exp env))
		((not? exp) (eval-not exp env))
		((implies? exp) (eval-implies exp env))
		((equiv? exp) (eval-equiv exp env))
        (else
         (error "Unknown expression type -- EVAL" exp))))

;; variable lookup
;; environment is a single symbol table in the form of an association list
;; i.e. ((var_1 val_1) ... (var_n val_n))
(define (lookup-variable-value var env)
	(if (null? env)
		;; variable not found
		(error "unbound variable" var)
		(if (eq? (caar env) var)
			(cdar env)
			(lookup-variable-value var (cdr env)))))

;;; --- proposition calculus grammar ---

;; < boolexp > ? #t | #f [boolean constants]
;; self-evaluated in the main eval method

;; < boolexp > ? variable [boolean variables]
;; handled by lookup-variable-value

;; < boolexp > ? (and boolexp ... boolexp)

(define (and? exp) (tagged-list? exp 'and))

;; 'and' predicate
(define (eval-and exp env)
	;; enforce at least 2 arguments for AND
	(if (< (length exp) 3)
		(error "AND must have at least 2 arguments" exp)
		(eval-and-helper (cdr exp) env)))

;; 'and' predicate recursive calculator
(define (eval-and-helper exp env)
	(if (null? exp)
		;; base case - end of expression list, all evaluated to true
		true
		;; recursive case - evaluate current arg; if true, continue
		;; otherwise return false immediately
		(let ((first-eval (eval (car exp) env))
			  (rest (cdr exp)))
			(if (boolean? first-eval)
				(if (true? first-eval)
					(eval-and-helper rest env)
					false)
				;; enforce boolean arguments
				(error "AND arguments must all be boolean expressions" exp)))))

;; < boolexp > ? (and boolexp ... boolexp)

(define (or? exp) (tagged-list? exp 'or))

;; 'or' predicate
(define (eval-or exp env)
	;; enforce at least 2 arguments for OR
	(if (< (length exp) 3)
		(error "OR must have at least 2 arguments" exp)
		(eval-or-helper (cdr exp) env)))

;; 'or' predicate recursive calculator
(define (eval-or-helper exp env)
	(if (null? exp)
		;; base case - end of expression list, no 'true' is found
		false
		;; recursive case - evaluate current arg; if true, return immediately
		;; otherwise continue evaluating
		(let ((first-eval (eval (car exp) env))
			  (rest (cdr exp)))
			(if (boolean? first-eval)
				(if (true? first-eval)
					true
					(eval-or-helper rest env))
				;; enforce boolean arguments
				(error "OR arguments must all be boolean expressions" exp)))))


;; < boolexp > ? (not boolexp)

(define (not? exp) (tagged-list? exp 'not))

;; 'not' predicate
(define (eval-not exp env)
	;; enforce exactly 1 argument for NOT
	(if (not (= (length exp) 2))
		(error "NOT must have exactly 1 argument" exp)
		(let ((evaluated (eval (cadr exp) env)))
			(if (boolean? evaluated)
				(not evaluated)
				;; enforce a boolean argument
				(error "NOT argument must be a boolean expression" exp)))))

;; < boolexp > ? (implies boolexp boolexp)

(define (implies? exp) (tagged-list? exp 'implies))

;; 'implies' predicate
(define (eval-implies exp env)
	;; enforce exactly 2 arguments for IMPLIES
	(if (not (= (length exp) 3))
		(error "IMPLIES must have exactly 2 arguments" exp)
		(let ((p (eval (cadr exp) env))
			 (q (eval (caddr exp) env)))
			 (if (and (boolean? p) (boolean? q))
				;; p => q is equivalent to ^p v q
				(or (not p) q)
				;; enforce boolean arguments
				(error "IMPLIES arguments must be boolean expressions" exp)))))

;; < boolexp > ? (equiv boolexp boolexp)

(define (equiv? exp) (tagged-list? exp 'equiv))

;; 'equiv' predicate
(define (eval-equiv exp env)
	;; enforce exactly 2 arguments for EQUIV
	(if (not (= (length exp) 3))
		(error "EQUIV must have exactly 2 arguments" exp)
		(let ((p (eval (cadr exp) env))
			 (q (eval (caddr exp) env)))
			 (if (and (boolean? p) (boolean? q))
				;; p <=> q is equivalent to (p ^ q) or (!p ^ !q)
				(or (and p q) (and (not p) (not q)))
				;; enforce boolean arguments
				(error "EQUIV arguments must be boolean expressions" exp)))))


;; --- proposition calculus interpreter ---
(define (interpret input env)
	(eval input env))

;; --- tautology prover ---
(define (prove input)
	(prove-rec input (extract-vars input) ()))
		
;; recursive prover - assigns the next variable to both possible values and
;; calls AND on both recursive proofs (we assume efficient AND applied by scheme)
(define (prove-rec input vars env)
	(if (null? vars)
		;; all variables are assigned, evaluate for current environment
		(interpret input env)
		;; recursively try both assignments of the next var in vars
		(let ((tenv (cons (cons (car vars) #t) env))
			  (fenv (cons (cons (car vars) #f) env))
			  (vars-rest (cdr vars)))
			(and (prove-rec input vars-rest tenv)
				 (prove-rec input vars-rest fenv)))))

;; extracting variables to bind from the input
(define (extract-vars input)
	(if (not (list? input))
		(if (symbol? input)
			(list input)
			())
		(parse-list input)))

;; for parsing a list expression with an operator as the CAR
(define (parse-list l)
	(parse-list-elems (cdr l)))

;; for parsing a list expression with only arguments
(define (parse-list-elems elems)
	(if (null? elems)
		()
		(let ((first (car elems))
			  (rest-parsed (parse-list-elems (cdr elems))))
			(if (list? first)
				(append (parse-list first) rest-parsed)
				(if (symbol? first)
					(cons first rest-parsed)
					rest-parsed)))))

'PROPOSITION-CALCULUS-LOADED
