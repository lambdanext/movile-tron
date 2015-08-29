(defproject movile-tron "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ^:replace [] ; lein 😒
  :java-source-paths ["src-java"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [quil "2.0.0"]
                 [clj-http "2.0.0"]
                 [ring "1.4.0"]])
