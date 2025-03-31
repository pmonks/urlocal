;
; Copyright © 2023 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns urlocal.api-test
  (:require [urlocal.impl.cache :as uic]
            [clojure.test       :refer [deftest testing is]]
            [urlocal.api        :refer [set-cache-name! reset-cache! cache-check-interval-secs set-cache-check-interval-secs! input-stream]]))

; Make sure we reset (delete) the cache before we run the tests
(set-cache-name! "urlocal-tests")
(reset-cache!)

(defn valid-cached-response?
  [url input-strm]
  (let [not-nil?              (not (nil? input-strm))
        correct-type?         (instance? java.io.InputStream input-strm)
        content-file          (uic/url->content-file url)
        content-file-exists?  (.exists content-file)
        metadata-file         (uic/url->metadata-file url)
        metadata-file-exists? (.exists metadata-file)
        result                (and not-nil? correct-type? content-file-exists? metadata-file-exists?)]
    (when-not result                (println "⚠️ Incorrect response for" url))
    (when-not not-nil?              (println "  * input stream is nil"))
    (when-not correct-type?         (println "  * input stream is wrong data type:" (class input-strm)))
    (when-not content-file-exists?  (println "  * content file does not exist:" (str content-file)))
    (when-not metadata-file-exists? (println "  * metadata file does not exist:" (str metadata-file)))
    result))

(defn test-is
  ([url]     (test-is url nil))
  ([url opts]
    (input-stream url (merge {:connect-timeout 5000 :read-timeout 5000} opts))))  ; Increase the default timeouts

(deftest input-stream-tests
  (testing "nil, blank, etc."
    (is (nil?                                   (test-is nil)))
    (is (thrown? java.net.MalformedURLException (test-is ""))))
  (testing "Invalid URLs"
    (is (thrown? java.io.IOException            (test-is "http://INVALID_HOST_THAT_DOES_NOT_EXIST.local/"))))
  (testing "Cache miss"
    (is (valid-cached-response? "https://spdx.org/licenses/licenses.json" (test-is "https://spdx.org/licenses/licenses.json"))))
  (testing "Cache hit & content fidelity"
    (is (= (slurp "https://spdx.org/licenses/licenses.json") (slurp (test-is "https://spdx.org/licenses/licenses.json")))))  ; This URL must have previously been cached
  (testing "Cache miss, Within cache interval period"
    (is (valid-cached-response? "https://spdx.org/licenses/exceptions.json" (test-is "https://spdx.org/licenses/exceptions.json"))))
  (testing "Cache hit, outside cache interval period"
    (let [cache-check-interval (cache-check-interval-secs)]
      (set-cache-check-interval-secs! 0)
      (Thread/sleep 1000)  ; Make sure we have at least a once second gap between cache checks
      (is (valid-cached-response? "https://spdx.org/licenses/equivalentwords.txt" (test-is "https://spdx.org/licenses/equivalentwords.txt")))
      (set-cache-check-interval-secs! cache-check-interval)))
  (testing "Binary MIME types"
    (is (valid-cached-response? "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png" (test-is "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png" {:request-headers {"Accept" "image/png"}})))
    (is (valid-cached-response? "https://getsamplefiles.com/download/pdf/sample-3.pdf"                                     (test-is "https://getsamplefiles.com/download/pdf/sample-3.pdf" {:request-headers {"Accept" "application/pdf"}}))))
  (testing "Request volume"
    (is (valid-cached-response? "https://www.apache.org/licenses/LICENSE-1.0.txt"                 (test-is "https://www.apache.org/licenses/LICENSE-1.0.txt")))
    (is (valid-cached-response? "https://www.apache.org/licenses/LICENSE-1.1.txt"                 (test-is "https://www.apache.org/licenses/LICENSE-1.1.txt")))
    (is (valid-cached-response? "https://www.apache.org/licenses/LICENSE-2.0.txt"                 (test-is "https://www.apache.org/licenses/LICENSE-2.0.txt")))
    (is (valid-cached-response? "https://www.eclipse.org/org/documents/epl-1.0/EPL-1.0.txt"       (test-is "https://www.eclipse.org/org/documents/epl-1.0/EPL-1.0.txt")))
    (is (valid-cached-response? "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt"       (test-is "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt")))
    (is (valid-cached-response? "https://spdx.org/licenses/CDDL-1.0.txt"                          (test-is "https://spdx.org/licenses/CDDL-1.0.txt")))
    (is (valid-cached-response? "https://spdx.org/licenses/CDDL-1.1.txt"                          (test-is "https://spdx.org/licenses/CDDL-1.1.txt")))
    (is (valid-cached-response? "https://creativecommons.org/publicdomain/zero/1.0/legalcode.txt" (test-is "https://creativecommons.org/publicdomain/zero/1.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by/3.0/legalcode.txt"       (test-is "https://creativecommons.org/licenses/by/3.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by/4.0/legalcode.txt"       (test-is "https://creativecommons.org/licenses/by/4.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by-sa/4.0/legalcode.txt"    (test-is "https://creativecommons.org/licenses/by-sa/4.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by-nc/4.0/legalcode.txt"    (test-is "https://creativecommons.org/licenses/by-nc/4.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt" (test-is "https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by-nd/4.0/legalcode.txt"    (test-is "https://creativecommons.org/licenses/by-nd/4.0/legalcode.txt")))
    (is (valid-cached-response? "https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.txt" (test-is "https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.txt")))
    ; Note: URLs don't match because this is a redirect 
    (is (valid-cached-response? "https://www.wtfpl.net/txt/copying/"                              (test-is "http://www.wtfpl.net/txt/copying/")))
    (is (valid-cached-response? "https://www.mozilla.org/media/MPL/2.0/index.txt"                 (test-is "https://www.mozilla.org/media/MPL/2.0/index.txt")))
    (is (valid-cached-response? "https://mit-license.org/license.txt"                             (test-is "https://mit-license.org/license.txt"))))
  (testing "Redirected requests"
    ; Note difference between URLs
    (is (valid-cached-response? "https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt" (test-is "https://www.gnu.org/licenses/gpl-2.0.txt" {:follow-redirects? true}))))
  (testing "Throttled requests"
    ; At times, gnu.org has throttled requests for license texts. Sadly this behaviour seems to change randomly, so this unit test is not guaranteed to actually test throttling behaviour at all times.
    (is (valid-cached-response? "https://www.gnu.org/licenses/gpl-3.0.txt" (test-is "https://www.gnu.org/licenses/gpl-3.0.txt" {:retry-when-throttled? true})))))
