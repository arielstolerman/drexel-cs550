;; Drexel CS550, Spring 2013
;; Group 1
;; Mini-language Interpreter

;; UTILS

(define (tagged-list? exp tag)
	(if (pair? exp)
		(eq? (car exp) tag)
		false))

(define (true? x)
	(not (eq? x false)))

(define (false? x)
	(eq? x false))

(define (not-zero? x)
	(not (eq? x 0)))

(define (identifier? exp) (symbol? exp))

;; primitive procedures
(define primitive-procs
	(list (list '+ +)
		  (list '- -)
		  (list '* *)
		  ;; more primitives here
		  ))

(define primitive-proc-names
	(map car primitive-procs))

;; list-contains
(define (contains? l exp)
	(if (eq? (memq exp l) #f) false true))

(define (primitive? exp)
	(and (contains? primitive-proc-names (car exp))
		 (eq? (length exp) 3)))

;; retrieves a procedure from a proc-map by the given key
(define (get-proc key proc-map)
	(if (null? proc-map)
		(error "procedure cannot be found in map" key proc-map)
		(if (eq? key (caar proc-map))
			(cadar proc-map)
			(get-proc key (cdr proc-map)))))

;; applies the given primitive-procedure on the given arguments
(define (apply-primitive proc-key arg1 arg2)
	(let ((proc (get-proc proc-key primitive-procs))
		  (args (list arg1 arg2)))
		(apply proc args)))
		

;; main eval method
(define (eval exp env)
	(cond	((prog? exp) (eval-prog exp env))						;; prog
			((stmt-list? exp) (eval-stmt-list exp env))				;; stmt-list
			((stmt-seq? exp) (eval-stmt-seq exp env))				;; stmt-seq
			((stmt? exp) (eval-stmt exp env))						;; stmt
			((assign-stmt? exp) (eval-assign-stmt exp env))			;; assign-stmt
			((if-stmt? exp) (eval-if-stmt exp env))					;; if-stmt
			((while-stmt? exp) (eval-while-stmt exp env))			;; while-stmt
			((expr? exp) (eval-epr exp env))						;; expr
			(else (error "Unknown expression type -- EVAL" exp))))	;; error

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

;; variable add/update
;; returns the updated env
(define (add-update-variable var val env)
	(if (update-variable var val env)
		env								;; found and updated variable
		(cons (cons var val) env)))		;; new binding added to environment

;; looks for the variable and sets its value
;; returns true if found and set, false otherwise
(define (update-variable var val env)
	(cond	((null? env) false)
			((eq? (caar env) var)
			 (begin
				(set-cdr! (car env) val)
				true))
			(else (update-variable var val (cdr env)))))
		

;;; --- mini-language evaluation methods ---

;; prog -> stmt-list

(define (prog? exp)
	(stmt-list? exp))

(define (eval-prog exp env)
	(eval-stmt-list exp env))

;; stmt-list -> (stmt-seq)

(define (stmt-list? exp)
	(stmt-seq? exp))

(define (eval-stmt-list exp env)
	(eval-stmt-seq exp env))


;; stmt-seq -> stmt | stmt stmt-seq

(define (stmt-seq? exp)
	(and (stmt? (car exp))
		 (or (eq? (length exp) 1)
		 	 (stmt-seq? (cdr exp)))))

(define (eval-stmt-seq exp env)
	(eval-stmt-seq-helper exp env))

;; recursively evaluates the next statement with the env
;; output of the previous statement evaluation
(define (eval-stmt-seq-helper stmts env)
	(if (null? stmts)
		env
		(let ((curr-stmt (car stmts))
			  (rest-stmts (cdr stmts)))
			(eval-stmt-seq-helper rest-stmts (eval-stmt curr-stmt env)))))


;; stmt -> assign-stmt | if-stmt | while-stmt

(define (stmt? exp)
	(or	(assign-stmt? exp)
		(if-stmt? exp)
		(while-stmt? exp)))

(define (eval-stmt exp env)
	(cond	((assign-stmt? exp) (eval-assign-stmt exp env))
			((if-stmt? exp) (eval-if-stmt exp env))
			((while-stmt? exp) (eval-while-stmt exp env))
			(else (error "unknown statement" exp))))


;; assign-stmt -> (assign identifier expr)

(define (assign-stmt? exp)
	(and (eq? (length exp) 3)
		 (tagged-list? exp 'assign)
		 (identifier? (cadr exp))
		 (expr? (caddr exp))))

(define (eval-assign-stmt exp env)
	(let ((var (cadr exp))
		  (val (eval-expr (caddr exp) env)))
		(add-update-variable var val env)))


;; if-stmt -> (if expr stmt-list stmt-list)

(define (if-stmt? exp)
	(and (eq? (length exp) 4)
		 (tagged-list? exp 'if)
		 (expr? (cadr exp))
		 (stmt-list? (caddr exp))
		 (stmt-list? (cadddr exp))))

(define (eval-if-stmt exp env)
	(let ((if-cond (cadr exp))
		  (then-clause (caddr exp))
		  (else-clause (cadddr exp)))
		(if (not-zero? (eval-expr if-cond env))
			(eval-stmt-list then-clause env)
			(eval-stmt-list else-clause env))))


;; while-stmt -> (while expr stmt-list)

(define (while-stmt? exp)
	(and (eq? (length exp) 3)
		 (tagged-list? exp 'while)
		 (expr? (cadr exp))
		 (stmt-list? (caddr exp))))

(define (eval-while-stmt exp env)
	(let ((while-cond (cadr exp))
		  (while-body (caddr exp)))
		(eval-while-stmt-helper while-cond while-body env)))

;; recursively evaluates the while body as long as the
;; while condition evaluates to true
(define (eval-while-stmt-helper while-cond while-body env)
	(let ((evald-cond (eval-expr while-cond env)))
		(if (not-zero? evald-cond)
			(let ((updated-env (eval-stmt-list while-body env)))
				;; continue to the next condition check with the updated env
				(eval-while-stmt-helper while-cond while-body updated-env))
			env)))	;; return the env when done


;; expr -> integer | identifier | (+ expr expr) | (- expr expr) | (* expr expr)

(define (expr? exp)
	(cond	((integer? exp) true)
			((identifier? exp) true)
			((primitive? exp) true)
			(else false)))

(define (eval-expr exp env)
	(cond	((integer? exp) exp)										;; integer
			((identifier? exp) (lookup-variable-value exp env))			;; identifier
			((primitive? exp)											;; primitive procedure
				(let ((proc-key (car exp))
					  (arg1 (eval-expr (cadr exp) env))
					  (arg2 (eval-expr (caddr exp) env)))
					(apply-primitive proc-key arg1 arg2)))
			(else (error "unknown expr" exp))))							;; error

;; --- interpreter ---

;; evaluate user input with the empty environment
;; print environment at the end
(define (interpret input)
	(eval-prog input ()))
