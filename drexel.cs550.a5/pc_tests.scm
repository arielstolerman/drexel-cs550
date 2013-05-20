;; tests for pc.scm
;; - eval methods
;;	* constants
;;	* variables
;;	* and
;;	* or
;;	* not
;;	* implies
;;	* equiv
;;	* mixture
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
















