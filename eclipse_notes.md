Clojure/Eclipse Notes
---------------------

Used http://vrapper.sourceforge.net/update-site/stable to provide vi emulation
Help
  Install new software
  
Use the link above.


Edit File in Navigator
----------------------

F3  Opens it in editor. Had to disable Linux's use of it on my machine.

Using clojure to run tests in Eclipse.
--------------------------------------
eclipse:

While in core-test.clj

Eclipse Menus
-------------
   Clojure
	    Load file in Repl (ctrl+alt+S)
   Clojure
	    Switch Repl to file's namespace (ctrl+alt+N)


Then in Repl:

(run-tests)  ;; Runs all tests in current namespace

(my-test)    ;; Run a single test
