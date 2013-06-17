(defproject thesisplot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [incanter "1.4.1"]
                 [clojure-csv/clojure-csv "2.0.0-alpha1"]
                 [com.lowagie/itext "1.3.1"] 
                 [org.clojars.weiqiu/debug-repl "0.4.0-SNAPSHOT"]
                 [swank-clojure "1.4.2"]
                 ]
  :plugins [[lein-swank "1.4.5"] [lein-kibit "0.0.8"]]
  :java-source-paths ["src"]
  :main thesisplot.core)
