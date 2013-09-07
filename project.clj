(defproject thesisplot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [incanter "1.5.4"]
                 [clojure-csv/clojure-csv "2.0.0-alpha1"]
                 [com.lowagie/itext "1.3.1"] 
                 [org.clojars.weiqiu/debug-repl "0.4.0-SNAPSHOT"]
                 [swank-clojure "1.4.2"]
                 [batik/batik-dom "1.6-1"]
                 [batik/batik-svggen "1.6-1"]
                 [batik/batik-awt-util "1.6-1"]
                 [batik/batik-util "1.6-1"]
                 [batik/batik-xml "1.6-1"]
                 ]
  :plugins [[lein-swank "1.4.5"] [lein-kibit "0.0.8"]]
  :java-source-paths ["src"]
  :main thesisplot.core)
