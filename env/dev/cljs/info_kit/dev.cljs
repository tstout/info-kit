(ns ^:figwheel-no-load info-kit.dev
  (:require
    [info-kit.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
