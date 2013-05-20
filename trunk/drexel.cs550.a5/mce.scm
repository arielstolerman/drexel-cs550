;;;;METACIRCULAR EVALUATOR FROM CHAPTER 4 (SECTIONS 4.1.1-4.1.4) of
;;;; STRUCTURE AND INTERPRETATION OF COMPUTER PROGRAMS

;;;;Matches code in ch4.scm

;;;;This file can be loaded into Scheme as a whole.
;;;;Then you can initialize and start the evaluator by evaluating
;;;; the two commented-out lines at the end of the file (setting up the
;;;; global environment and starting the driver loop).

;;;;**WARNING: Don't load this file twice (or you'll lose the primitives
;;;;  interface, due to renamings of apply).

;;;from section 4.1.4 -- must precede def of metacircular apply
(define apply-in-underlying-scheme apply)

;;;SECTION 4.1.1

(define (eval exp env)
  (cond ((self-evaluating? exp) exp)
        ((variable? exp) (lookup-variable-value exp env))
        ((quoted? exp) (text-of-quotation exp))
        ((assignment? exp) (eval-assignment exp env))
        ((definition? exp) (eval-definition exp env))
        ((if? exp) (eval-if exp env))
        ((lambda? exp)
         (make-procedure (lambda-parameters exp)
                         (lambda-body exp)
                         env))
        ((begin? exp) 
         (eval-sequence (begin-actions exp) env))
        ((cond? exp) (eval (cond->if exp) env))
		;; proposition calculus
		((and? exp) (eval-and exp env))
		((or? exp) (eval-or exp env))
		((not? exp) (eval-not exp env))
		((implies? exp) (eval-implies exp env))
		((equiv? exp) (eval-equiv exp env))
		;; ---
        ((application? exp)
         (apply (eval (operator exp) env)
                (list-of-values (operands exp) env)))
        (else
         (error "Unknown expression type -- EVAL" exp))))

(define (apply procedure arguments)
  (cond ((primitive-procedure? procedure)
         (apply-primitive-procedure procedure arguments))
        ((compound-procedure? procedure)
         (eval-sequence
           (procedure-body procedure)
           (extend-environment
             (procedure-parameters procedure)
             arguments
             (procedure-environment procedure))))
        (else
         (error
          "Unknown procedure type -- APPLY" procedure))))


(define (list-of-values exps env)
  (if (no-operands? exps)
      '()
      (cons (eval (first-operand exps) env)
            (list-of-values (rest-operands exps) env))))

(define (eval-if exp env)
  (if (true? (eval (if-predicate exp) env))
      (eval (if-consequent exp) env)
      (eval (if-alternative exp) env)))

(define (eval-sequence exps env)
  (cond ((last-exp? exps) (eval (first-exp exps) env))
        (else (eval (first-exp exps) env)
              (eval-sequence (rest-exps exps) env))))

(define (eval-assignment exp env)
  (set-variable-value! (assignment-variable exp)
                       (eval (assignment-value exp) env)
                       env)
  'ok)

(define (eval-definition exp env)
  (define-variable! (definition-variable exp)
                    (eval (definition-value exp) env)
                    env)
  'ok)

;;;SECTION 4.1.2

(define (self-evaluating? exp)
  (cond ((number? exp) true)
        ((string? exp) true)
		((boolean? exp) true)
		((null? exp) true)
        (else false)))

(define (quoted? exp)
  (tagged-list? exp 'quote))

(define (text-of-quotation exp) (cadr exp))

(define (tagged-list? exp tag)
  (if (pair? exp)
      (eq? (car exp) tag)
      false))

(define (variable? exp) (symbol? exp))

(define (assignment? exp)
  (tagged-list? exp 'set!))

(define (assignment-variable exp) (cadr exp))

(define (assignment-value exp) (caddr exp))


(define (definition? exp)
  (tagged-list? exp 'define))

(define (definition-variable exp)
  (if (symbol? (cadr exp))
      (cadr exp)
      (caadr exp)))

(define (definition-value exp)
  (if (symbol? (cadr exp))
      (caddr exp)
      (make-lambda (cdadr exp)
                   (cddr exp))))

(define (lambda? exp) (tagged-list? exp 'lambda))

(define (lambda-parameters exp) (cadr exp))
(define (lambda-body exp) (cddr exp))

(define (make-lambda parameters body)
  (cons 'lambda (cons parameters body)))


(define (if? exp) (tagged-list? exp 'if))

(define (if-predicate exp) (cadr exp))

(define (if-consequent exp) (caddr exp))

(define (if-alternative exp)
  (if (not (null? (cdddr exp)))
      (cadddr exp)
      'false))

(define (make-if predicate consequent alternative)
  (list 'if predicate consequent alternative))


(define (begin? exp) (tagged-list? exp 'begin))

(define (begin-actions exp) (cdr exp))

(define (last-exp? seq) (null? (cdr seq)))
(define (first-exp seq) (car seq))
(define (rest-exps seq) (cdr seq))

(define (sequence->exp seq)
  (cond ((null? seq) seq)
        ((last-exp? seq) (first-exp seq))
        (else (make-begin seq))))

(define (make-begin seq) (cons 'begin seq))


(define (application? exp) (pair? exp))
(define (operator exp) (car exp))
(define (operands exp) (cdr exp))

(define (no-operands? ops) (null? ops))
(define (first-operand ops) (car ops))
(define (rest-operands ops) (cdr ops))


(define (cond? exp) (tagged-list? exp 'cond))

(define (cond-clauses exp) (cdr exp))

(define (cond-else-clause? clause)
  (eq? (cond-predicate clause) 'else))

(define (cond-predicate clause) (car clause))

(define (cond-actions clause) (cdr clause))

(define (cond->if exp)
  (expand-clauses (cond-clauses exp)))

(define (expand-clauses clauses)
  (if (null? clauses)
      'false                          ; no else clause
      (let ((first (car clauses))
            (rest (cdr clauses)))
        (if (cond-else-clause? first)
            (if (null? rest)
                (sequence->exp (cond-actions first))
                (error "ELSE clause isn't last -- COND->IF"
                       clauses))
            (make-if (cond-predicate first)
                     (sequence->exp (cond-actions first))
                     (expand-clauses rest))))))

;;;SECTION 4.1.3

(define (true? x)
  (not (eq? x false)))

(define (false? x)
  (eq? x false))


(define (make-procedure parameters body env)
  (list 'procedure parameters body env))

(define (compound-procedure? p)
  (tagged-list? p 'procedure))


(define (procedure-parameters p) (cadr p))
(define (procedure-body p) (caddr p))
(define (procedure-environment p) (cadddr p))


(define (enclosing-environment env) (cdr env))

(define (first-frame env) (car env))

(define the-empty-environment '())

(define (make-frame variables values)
  (cons variables values))

(define (frame-variables frame) (car frame))
(define (frame-values frame) (cdr frame))

(define (add-binding-to-frame! var val frame)
  (set-car! frame (cons var (car frame)))
  (set-cdr! frame (cons val (cdr frame))))

(define (extend-environment vars vals base-env)
  (if (= (length vars) (length vals))
      (cons (make-frame vars vals) base-env)
      (if (< (length vars) (length vals))
          (error "Too many arguments supplied" vars vals)
          (error "Too few arguments supplied" vars vals))))

(define (lookup-variable-value var env)
  (define (env-loop env)
    (define (scan vars vals)
      (cond ((null? vars)
             (env-loop (enclosing-environment env)))
            ((eq? var (car vars))
             (car vals))
            (else (scan (cdr vars) (cdr vals)))))
    (if (eq? env the-empty-environment)
        (error "Unbound variable" var)
        (let ((frame (first-frame env)))
          (scan (frame-variables frame)
                (frame-values frame)))))
  (env-loop env))

(define (set-variable-value! var val env)
  (define (env-loop env)
    (define (scan vars vals)
      (cond ((null? vars)
             (env-loop (enclosing-environment env)))
            ((eq? var (car vars))
             (set-car! vals val))
            (else (scan (cdr vars) (cdr vals)))))
    (if (eq? env the-empty-environment)
        (error "Unbound variable -- SET!" var)
        (let ((frame (first-frame env)))
          (scan (frame-variables frame)
                (frame-values frame)))))
  (env-loop env))

(define (define-variable! var val env)
  (let ((frame (first-frame env)))
    (define (scan vars vals)
      (cond ((null? vars)
             (add-binding-to-frame! var val frame))
            ((eq? var (car vars))
             (set-car! vals val))
            (else (scan (cdr vars) (cdr vals)))))
    (scan (frame-variables frame)
          (frame-values frame))))

;;;SECTION 4.1.4

(define (setup-environment)
  (let ((initial-env
         (extend-environment (primitive-procedure-names)
                             (primitive-procedure-objects)
                             the-empty-environment)))
    (define-variable! 'true true initial-env)
    (define-variable! 'false false initial-env)
    initial-env))

;[do later] (define the-global-environment (setup-environment))

(define (primitive-procedure? proc)
  (tagged-list? proc 'primitive))

(define (primitive-implementation proc) (cadr proc))

(define primitive-procedures
  (list (list 'car car)
        (list 'cdr cdr)
        (list 'cons cons)
        (list 'null? null?)
;;      more primitives
        (list '+ +)
        (list '- -)
        (list '* *)
        (list '/ /)
        (list '= =)
        ))

(define (primitive-procedure-names)
  (map car
       primitive-procedures))

(define (primitive-procedure-objects)
  (map (lambda (proc) (list 'primitive (cadr proc)))
       primitive-procedures))

;[moved to start of file] (define apply-in-underlying-scheme apply)

(define (apply-primitive-procedure proc args)
  (apply-in-underlying-scheme
   (primitive-implementation proc) args))



(define input-prompt ";;; M-Eval input:")
(define output-prompt ";;; M-Eval value:")

(define (driver-loop)
  (prompt-for-input input-prompt)
  (let ((input (read)))
    (let ((output (eval input the-global-environment)))
      (announce-output output-prompt)
      (user-print output)))
  (driver-loop))

(define (prompt-for-input string)
  (newline) (newline) (display string) (newline))

(define (announce-output string)
  (newline) (display string) (newline))

(define (user-print object)
  (if (compound-procedure? object)
      (display (list 'compound-procedure
                     (procedure-parameters object)
                     (procedure-body object)
                     '<procedure-env>))
      (display object)))

;;;Following are commented out so as not to be evaluated when
;;; the file is loaded.
;;(define the-global-environment (setup-environment))
;;(driver-loop)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;								ADDED CODE
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; shortcuts

(define (dl) (driver-loop))

;;; primitive procedures added above

;;; --- proposition calculus ---

;; < boolexp > ? #t | #f [boolean constants]
;; added to list of self-evaluating expressions

;; < boolexp > ? variable [boolean variables]
;; already in list of self-evaluating expressions

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

				
;; interpreter
;(define pc-input-prompt ";;; Proposition Calculator input:")
;(define pc-output-prompt ";;; Proposition Calculator value:")

;(define (pc-interpret)
  ;(prompt-for-input pc-input-prompt)
  ;(let ((input (read)))
    ;(let ((output (eval input the-global-environment)))
      ;(announce-output output-prompt)
      ;(user-print output)))
  ;(driver-loop))

'METACIRCULAR-EVALUATOR-LOADED


; (define the-global-environment (setup-environment))
