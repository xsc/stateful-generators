nil (do (set! *warn-on-reflection* nil) (set! *warn-on-reflection* true) (clojure.core/require (quote codox.main)) (codox.main/generate-docs (quote {:description "Stateful Generators for Clojure's test.check", :package xsc/stateful, :source-uri "https://github.com/xsc/stateful/blob/master/{filepath}#L{line}", :namespaces [stateful.core], :output-path "/git/github/stateful-generators/target/doc", :license {:name "MIT License", :url "https://opensource.org/licenses/MIT", :year 2016, :key "mit"}, :name "stateful", :source-paths ("/git/github/stateful-generators/src"), :themes [:rdash], :project {:name "stateful"}, :root-path "/git/github/stateful-generators", :version "0.1.0-SNAPSHOT", :metadata {:doc/format :markdown}})))