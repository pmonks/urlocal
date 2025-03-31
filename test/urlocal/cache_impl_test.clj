;
; Copyright © 2023 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns urlocal.cache-impl-test
  (:require [clojure.java.io    :as io]
            [clojure.test       :refer [deftest testing is]]
            [urlocal.impl.cache :as ulic :refer [base64-encode http-get prep-cache!]]))

(deftest base64-encode-tests
  (testing "nil, blank, etc."
    (is (nil?     (base64-encode nil)))
    (is (= ""     (base64-encode "")))
    (is (= "ICA=" (base64-encode "  ")))
    (is (= "DQoJ" (base64-encode "\r\n\t"))))
  (testing "Some urls"
    (is (= "aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS8=" (base64-encode "https://www.google.com/")))))

(deftest http-get-tests
  (testing "nil"
    (is (nil? (http-get nil)))
    (is (nil? (http-get nil nil))))
  (testing "Invalid URLs"
    (is (thrown? java.io.IOException (http-get (io/as-url "http://INVALID_HOST_THAT_DOES_NOT_EXIST.local/")))))
  (testing "Valid URLs"
    (is (not (nil? (http-get (io/as-url "https://www.google.com/")))))))

(defn prep-and-check-url-was-cached?
  "Preps the cache for url, then returns true if all of the cache files for that
  URL exist."
  [url]
  (let [url-to-test-b64      (ulic/base64-encode url)
        cached-content-file  (io/file (str @ulic/cache-dir-a "/" url-to-test-b64 ".content"))
        cached-metadata-file (io/file (str @ulic/cache-dir-a "/" url-to-test-b64 ".metadata.edn"))
        _                    (prep-cache! url nil)]
    (and (.exists cached-content-file) (.exists cached-metadata-file))))

(deftest prep-cache!-tests
  (testing "nil"
    (is (nil? (prep-cache! nil nil))))
  (testing "Valid URLs"
    (is (true? (prep-and-check-url-was-cached? (io/as-url "https://raw.githubusercontent.com/spdx/license-list-data/main/template/Apache-2.0.template.txt"))))))  ; Note: need to make sure this URL hasn't been used in a previous test
