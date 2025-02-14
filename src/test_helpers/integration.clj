(ns test-helpers.integration
  (:require [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]))

(defmacro with-app-initialized
  [& body]
  `(if (app-initialized)
     (do ~@body)
     (do
       (rf/dispatch [:app-started])
       (rf-test/wait-for
         [:profile/get-profiles-overview-success]
         ~@body))))

(defmacro with-account
  [& body]
  `(if (messenger-started)
     (do ~@body)
     (do
       (create-multiaccount!)
       (rf-test/wait-for
         [:status-im.transport.core/messenger-started]
         (assert-messenger-started)
         ~@body))))
