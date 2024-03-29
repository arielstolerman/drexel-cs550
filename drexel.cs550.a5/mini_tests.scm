;; tests for mini.scm
;; - expr
;;	* integer
;;	* identifier
;;	* primitive procedures
;; - assign-stmt
;; - if-stmt
;; - while-stmt
;; - interpreter: checks altogether (including prog, stmt-list, stmt-seq, stmt)
;; - error checks

;; --- expr ---

;; integer

(eval-expr '1 ())
;Value: 1

(eval-expr '-5 ())
;Value: -5 -- negatives are accepted since using scheme's integer? predicate

;; identifier

(eval-expr 'a (list (cons 'a 4)))
;Value: 4

;; primitive procedures

(eval-expr '(+ 8 4) ())
;Value: 12

(eval-expr '(- 8 4) ())
;Value: 4

(eval-expr '(* 8 4) ())
;Value: 32

(eval-expr '(+ 2 (* 7 (- a 6))) (list (cons 'a 9)))
;Value: 23 -- ok since grammar does not define order of operations (i.e. * not necessarily precedes + or -)


;; --- assign-stmt ---

(eval-assign-stmt '(assign a 4) ())
;env on return: ((a . 4))

(eval-assign-stmt '(assign b (+ 9 8)) ())
;env on return: ((b . 17))

(eval-assign-stmt '(assign a (+ b 1)) (eval-assign-stmt '(assign b 2) ()))
;env on return: ((a . 3) (b . 2))


;; --- if-stmt ---

(eval-if-stmt '(if a ((assign b 1)) ((assign b 2))) (list (cons 'a 1)))
;env on return: ((b . 1) (a . 1))

(eval-if-stmt '(if a ((assign b 1)) ((assign b 2))) (list (cons 'a 0)))
;env on return: ((b . 2) (a . 0))


;; --- while-stmt ---

(eval-while-stmt '(while a ((assign b (+ a 1)) (assign a (- a 1)))) (list (cons 'a 6)))
;env on return: ((b . 2) (a . 0))

(eval-while-stmt '(while a ((assign b 6))) (list (cons 'a 0)))
;env on return: ((a . 0))


;; --- interpreter ---

; calculating the factorial of 5
; result in m
(interpret '((assign n 5) (assign m 1) (while n ((assign m (* m n)) (assign n (- n 1))))))
;env on return: ((m . 120) (n . 0))

; calculating the 9th fibonacci number, which is 21
; base cases: 1st fib = 0, 2nd fib = 1
; result in b
(interpret '((assign n (- 9 2)) (assign a 0) (assign b 1) (while n ((assign tmp b) (assign b (+ a b)) (assign a tmp) (assign n (- n 1))))))
;env on return: ((tmp . 13) (b . 21) (a . 13) (n . 0))


;; --- error checks ---
;; the following should error
;; note that specific eval methods are assumed to be called only after check of
;; the corresponding predicate, so correctness of input types is assumed

; undefined variable
(eval-expr 'a ())

; undefined primitive procedure
(eval-expr '(/ 3 4) ())

; illegal assignment
(interpret '((assign 2 4)))

; illegal if (clauses must be stmt-lists)
(interpret '((if 1 (assign a 2) (assign a 3))))

; illegal if (condition must be an expr)
(interpret '((if (assign a 2) ((assign b 3)) ((assign b 4)))))

; illegal while (body must be a stmt-list)
(interpret '((assign a 3) (while a (assign a (- a 1)))))

; illegal prog/stmt-list/stmt-seq
(interpret '(assign a 2))
(interpret '((- 3 1) (+ 4 2)))
(interpret '(() (assign a 2)))
