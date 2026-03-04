;
; Copyright © 2026 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns urlocal.impl.locking)

(def ^java.util.concurrent.ConcurrentMap locks (java.util.concurrent.ConcurrentHashMap.))

; We do this here instead of inline because doing it inline makes eastwood shit the bed
(def ^java.util.function.Function new-lock-fn (reify java.util.function.Function
                                                (apply [_ _]
                                                  (java.util.concurrent.locks.ReentrantLock.))))

(defmacro locking-by-value
  "Similar to [clojure.core/locking](https://clojuredocs.org/clojure.core/locking),
  but locks per `val` rather than per object.  This means that values that are
  `clojure.core/=` will share the same lock, even if they're not
  `clojure.core/identical?`."
  [val & body]
  `(let [val# ~val]
     (.computeIfAbsent locks val# new-lock-fn)
     (let [^java.util.concurrent.locks.ReentrantLock lock# (.get locks val#)]
       (.lock lock#)
       (try
         ~@body
         (finally
           (.unlock lock#))))))
