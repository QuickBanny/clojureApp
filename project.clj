(defproject myfirstproject "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [clj-postgresql "0.7.0"]
                 [com.layerware/hugsql "0.4.8"]
                 [clj-time "0.15.2"]]
  :repl-options {:init-ns rest.core}
  :main rest.core)
