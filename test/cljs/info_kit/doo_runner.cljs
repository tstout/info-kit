(ns info-kit.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [info-kit.core-test]))

(doo-tests 'info-kit.core-test)
