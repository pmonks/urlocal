;
; Copyright © 2023 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns urlocal.xdg-impl-test
  (:require [clojure.string   :as s]
            [clojure.test     :refer [deftest testing is]]
            [urlocal.impl.xdg :refer [cache-home]]))

(deftest cache-home-tests
  (testing "cache-home is set"
    (is (not (s/blank? cache-home)))))
