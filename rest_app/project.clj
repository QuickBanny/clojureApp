(defproject rest-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [clj-postgresql "0.7.0"]
                 [com.layerware/hugsql "0.4.8"]
                 [clj-time "0.15.2"]
                 [metosin/ring-http-response "0.9.1"]
                 [ring/ring-mock "0.4.0"]
                 [cheshire "5.10.0"]
                 [ring/ring-json "0.5.0"]
                 [ring-json-response "0.2.0"]
                 [cljs-ajax "0.7.5"]
                 [selmer "1.12.27"]
                 [reagent "1.0.0-alpha2"]
                 [org.clojars.frozenlock/reagent-modals "0.2.8"]
                 [reagent-forms "0.5.44"]
                 [ring/ring-anti-forgery "1.3.0"]
                 [formative "0.8.9"]
                 [honeysql "1.0.444"]
                 [re-frame "0.9.3"]
                 [day8.re-frame/http-fx "v0.2.0"]]
  :repl-options {:init-ns rest.core}
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-ancient "0.6.15"]]
  :main rest.core
  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src-cljs"]
     :compiler
     {
      :output-to "resources/public/js/main.js"
      :optimizations :whitespace
      :pretty-print  true
      }}]})


