# movile-tron

A Tron game waiting for your bots

## Usage

```clojure
(use '[movile-tron.core :only [battle]])

(battle {:buzz (fn [{[x y] :pos} _]
                 {:pos [(inc x) y]})
         :stairway (fn [{[x y] :pos flag :flag} _]
                     {:pos (if flag
                             [(inc x) y]
                             [x (inc y)])
                      :flag (not flag)})})
```

## License

Copyright © 2015 λ→

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
