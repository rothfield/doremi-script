$ = jQuery

root = exports ? this

$(document).ready ->
  parser=SargamParser
  renderer=new SargamHtmlRenderer
  debug=false

  strs=[
      """ 
.. ....
SS SSSS SS SS |
        .. ..
      """
      ,
      '''
Rag:Bhairavi
Tal:Tintal
Title:Bansuri
Source:AAK
Mode: phrygian
           . .
[| Srgm PdnS SndP mgrS |]

           I  IV            V   V7 ii    iii7 
               3                  +            2          .
1)|: S S S (Sr | n) S   (gm Pd) | P - P  P   | P - D  <(nDSn)>) |
                 .
            ban-    su-  ri       ba- ja ra-   hi  dhu- na

  0  ~                3               ~       +  .     *  *   
| P  d   P       d    |  (Pm   PmnP) (g m) | (PdnS) -- g  S |
     ma- dhu-ra  kan-     nai-        ya      khe-     la-ta

    2              0     
                   ~
| (d-Pm  g) P  m | r - S :|
   ga-      wa-ta  ho- ri

  '''
  ]
  strs=[strs[1]]
  for str in strs
    try
      $('#test_results').append("<h4>Source:</h4><pre class='show_sargam_source'>#{str}</pre>")
      obj= parser.parse str
      h=renderer.to_html obj
      $('#test_results').append("<h4>Rendered html</h4><div class='rendered_html'>#{h}</div>")
      # TODO: combine with the above line..
      renderer.adjust_slurs_in_dom()
    catch err
      console.log err if console? and console.log?
