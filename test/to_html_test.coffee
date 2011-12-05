root = exports ? this

debug=false
global._console ||= require('./underscore.logger.js') if global?
Logger=global._console.constructor

_ = require("underscore")._ if require?
require './doremi_script_parser.js'
sys = require('sys')
utils=require './tree_iterators.js'
_console.level  = Logger.ERROR
_.mixin(_console.toObject())

`_.mixin({
  each_slice: function(obj, slice_size, iterator, context) {
    var collection = obj.map(function(item) { return item; });
    
    if (typeof collection.slice !== 'undefined') {
      for (var i = 0, s = Math.ceil(collection.length/slice_size); i < s; i++) {
        iterator.call(context, _(collection).slice(i*slice_size, (i*slice_size)+slice_size), obj);
      }
    }
    return; 
  }
});`

to_html=require('./html_renderer.js').to_html

parser=DoremiScriptParser

test_to_html = (str,test,msg="") ->
  composition=parser.parse(str)
  _.debug "test_to_html:composition is #{composition}"
  composition.source=str
  _.debug("test_to_html, str is \n#{str}\n")
  html=to_html(composition)
  _.debug("test_to_html returned \n#{html}\n")
  html

long_sample = """
Rag:Bhairavi
Tal:Tintal
Title:Bansuri
Author:Traditional
Source:AAK
Mode: phrygian
Filename: bansuri.sargam
Time: 4/4
Key: d


          
                              i            IV         . 
         3                    +             2        DSnDn
1)|: (Sr | n) S   (gm <Pd)> | P - P  P   | P - D    n     |
           .
      ban-    su-  ri         ba- ja ra-   hi  dhu- na

  0  ~                 3           mgm        +  .     *  *   
| P  d   P    d    |  (Pm   PmnP)    (g m) | (PdnS) -- g  S |
  ma-dhu-ra   kan-     nai-           ya      khe-     la-ta

   2               0     
                   ~
| (d-Pm  g) P  m | r - S :|
   ga-      wa-ta  ho- ri

"""


exports.test_long_sample = (test) ->
  html=test_to_html(long_sample,test)
  test.ok(html.indexOf("note_wrapper") > -1,"failure")
  test.done()

test_data = [
  "P#" ,
  '''
<div class='composition'><div class='stave sargam_line'><span class='beat'><span title="1/1 of a beat" class="note_wrapper" ><span data-fallback-if-no-utf8-chars='#' class='pitch_sign sharp'>&#9839;</span><span class="note pitch">P</span></span></span></div></div>
  '''
  "not expected"
  ]

exports.test_all = (test) ->
  console.log "test_all"
  fun = (args) ->
    [str,expected,msg]= args
    val=test_to_html(str,test)
    _.warn("✔ Testing #{str} -> #{expected}")
    test.ok(val.indexOf(expected) > -1,"FAILED*** #{msg}. Expected output of #{str} to include #{expected}. Output was \n\n#{val}\n\n")
  _.each_slice(test_data,3,fun)
  test.done()

