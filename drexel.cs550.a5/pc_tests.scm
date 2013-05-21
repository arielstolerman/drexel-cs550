;; tests for pc.scm
;; - eval methods
;;	* constants
;;	* variables
;;	* and
;;	* or
;;	* not
;;	* implies
;;	* equiv
;;	* mixture - tested using interpreter
;; - interpreter
;; - prover

;; --- eval methods ---

;; boolean constants -- good

(eval '#t ())
;Value: #t

(eval '#f ())
;Value: #f

;; constants -- bad (should error)

; number constant
(eval '1 ())

; list constant
(eval '(list 1 2 3) ())

;; variables -- good

(eval 'a (list (cons 'a #t)))
;Value: #t

(eval 'a (list (cons 'a #f)))
;Value: #f

;; variables -- bad (should error)

; unbound variable
(eval 'a ())

;; and -- good

(eval '(and #t #t) ())
;Value: #t

(eval '(and #t (and #t #t #t) #t) ())
;Value: #t

(eval '(and #t (and #t #f #t) #t) ())
;Value: #f

eval '(and #t #f #t #t #t #t #t) ())
;Value: #f

;; and -- bad (should error)

; incorrect number of variables
(eval '(and #t) ())

(eval '(and) ())

; non-boolean constant
(eval '(and #t #t 1) ())

;; or -- good

(eval '(or #f #f #t) ())
;Value: #t

(eval '(or #f (or #f #f #t) #f) ())
;Value: #t

(eval '(or #f #f #f) ())
;Value: #f

;; or -- bad (should error)

; incorrect number of variables
(eval '(or #t) ())

(eval '(or) ())

; non-boolean constant
(eval '(or #t #t 1) ())

;; not -- good

(eval '(not #t) ())
;Value: #f

(eval '(not #f) ())
;Value: #t

;; not -- bad (should error)

; incorrect number of variables
(eval '(not #t #f) ())

(eval '(not) ())

; non-boolean constant
(eval '(not 1) ())

;; implies -- good

(eval '(implies #f #f) ())
;Value: #t

(eval '(implies #f #t) ())
;Value: #t

(eval '(implies #t #f) ())
;Value: #f

(eval '(implies #t #t) ())
;Value: #t

;; implies -- bad (should error)

; incorrect number of variables
(eval '(implies #t) ())

(eval '(implies #t #f #t) ())

; non-boolean constant
(eval '(implies #t 1) ())

;; equiv -- good

(eval '(equiv #f #f) ())
;Value: #t

(eval '(equiv #f #t) ())
;Value: #f

(eval '(equiv #t #f) ())
;Value: #f

(eval '(equiv #t #t) ())
;Value: #t

;; equiv -- bad (should error)

; incorrect number of variables
(eval '(equiv #t) ())

(eval '(equiv #t #f #t) ())

; non-boolean constant
(eval '(equiv #t 1) ())


;; --- interpreter ---

;; good

; a => b  <=>  !a v b
(interpret '(equiv (implies a b) (or (not a) b)) (list (cons 'a #t) (cons 'b #f)))
;Value: #t
(interpret '(equiv (implies a b) (or (not a) b)) (list (cons 'a #f) (cons 'b #t)))
;Value: #t

; a <=> b  <=>  (a => b) ^ (b => a)
(interpret '(equiv (equiv a b) (and (implies a b) (implies b a))) (list (cons 'a #t) (cons 'b #f)))
;Value: #t
(interpret '(equiv (equiv a b) (and (implies a b) (implies b a))) (list (cons 'a #f) (cons 'b #t)))
;Value: #t

; long and complex boolean expression
(interpret '(equiv (or a (and b (not a))) (implies (and c (implies d a) (not e)) (equiv (and a e) c)))
	(list (cons 'a #t) (cons 'b #f) (cons 'c #t) (cons 'd #t) (cons 'e #f)))
;Value: #f


;; --- prover ---

;; tautologies

(prove '(or P (not P)))
;Value: #t

(prove '(equiv (or P Q) (or Q P)))
;Value: #t

(prove '(equiv (or P Q) (or P (and (not P) Q))))
;Value: #t

;; not tautologies

(prove '(or P Q))
;Value: #f

(prove '(and P (not P)))
;Value: #f

(prove '(equiv (or a (and b (not a))) (implies (and c (implies d a) (not e)) (equiv (and a e) c))))
;Value: #f
