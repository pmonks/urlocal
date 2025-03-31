;
; Copyright © 2023 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns urlocal.impl.xdg
  "Vars related to the XDG Base Directory Specification: https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
  This namespace is not part of the public API of urlocal and may change without
  notice."
  (:require [clojure.string :as s]))

(def cache-home
  "The base location of the XDG 'cache home' directory, as per the specification.
  Does not include include any application-specific sub-directories, and always
  ends with a path separator character."
  (let [xdg-cache-home (System/getenv "XDG_CACHE_HOME")]
    (if (s/blank? xdg-cache-home)
      (str (System/getProperty "user.home") java.io.File/separator ".cache" java.io.File/separator)
      (if (s/ends-with? xdg-cache-home java.io.File/separator)
        xdg-cache-home
        (str xdg-cache-home java.io.File/separator)))))
