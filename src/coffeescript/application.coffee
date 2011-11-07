$ = jQuery

root = exports ? this

$(document).ready ->
  # This is for the online version
  debug=false

  handleFileSelect = (evt) =>
    file = document.getElementById('file').files[0]
    reader=new FileReader()
    reader.onload =  (evt) ->
      $('#entry_area').val evt.target.result
      $('#lilypond_png').attr('src',"")

    reader.readAsText(file, "")


  document.getElementById('file').addEventListener('change', handleFileSelect, false)


  root.debug=true
  # TODO: don't use window. Use an application object
  window.timer_is_on=0
  window.last_val=str
  
  window.timed_count = () =>
    cur_val= $('#entry_area').val()
    if window.last_val != cur_val
      $('#run_parser').trigger('click')
    t=setTimeout("timed_count()",1000)

  window.do_timer  =  () =>
    if !window.timer_is_on
      window.timer_is_on=1
      window.timed_count()
  long_composition = '''

Rag:Bhairavi
Tal:Tintal
Title:Bansuri
Author:Traditional
Source:AAK
Mode: phrygian
Filename: bansuri.sargam
Time: 4/4
Key: d


          
                            i            IV
         3                  +            2          .
1)|: (Sr | n) S   (gm Pd) | P - P  P   | P - D  <(nDSn)>) |
           .
      ban-    su-  ri       ba- ja ra-   hi  dhu- na

  0  ~                3            ~       +  .     *  *   
| P  d   P    d    |  (Pm   PmnP) (g m) | (PdnS) -- g  S |
  ma-dhu-ra   kan-     nai-        ya      khe-     la-ta

   2               0     
                   ~
| (d-Pm  g) P  m | r - S :|
   ga-      wa-ta  ho- ri
  '''
  zlong_composition = '''
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
  str="S"
  str1 = '''
                            I   IV       V   V7 ii  iii7 
         3                  +            2          .
  |: (Sr | n) S   (gm Pd) | P - P  P   | P - D  (<nDSn>) |
           .
      ban-    su-  ri       ba- ja ra-   hi  dhu- na
  
    0  ~                3           ~       +  .     *  .
  | P  d   P   d    |  (Pm   PmnP) (g m) | (PdnS) -- g  S |
    ma-dhu-ra  kan-     nai-        ya      khe-     la-ta
  '''
  str= '''
      I                       IV             V
                   .  .   .    . ..
  |: (SNRSNS) N    S--S --S- | SNRS N D P || mm GG RR S-SS :|  
      .   .   .
      he-     llo
  '''
  str = '''
  Rag:Bhairavi
  Tal:Tintal
  Title:Bansuri
  Source:AAK

            3             ~    +            2         .
  1) |: (Sr | n) S   (gm Pd)|| P - P  P   | P - D  (<nDSn>) |
              .
         ban-    su-  ri       ba- ja ra-   hi  dhu- na

  0                 3                    +     .    *  .
  | P  d   P   d    | <(Pm>   PmnP) (g m)|| PdnS -- g  S |
    ma-dhu-ra  kan-     nai-         ya     khe-    la-ta

  2              0     ~
  |  d-Pm g P  m | r - S :|
     ga-    wa-ta  ho- ri

        I                     IV
                . .                   . .
  2)  [| Srgm PdnS SndP mgrS | Srgm PdnS SndP mgrS | % | % |]
  '''
  str_simple = '''
     + 
     .                        .
  |<(S--  r)>  (r---  g-m) | (Sn-d    Pmg rS) - - |
     test-ing   looped-       melisma-
  '''
  str=str1
  str="S--R --G- | -m-- P"
  $('#entry_area').val(str_simple)
  # window.last_val=$('#entry_area').val()
  parser=SargamParser
  # uses coffeescripts classes
  renderer=new SargamHtmlRenderer
  #staff_renderer=new VexflowRenderer
  window.parse_errors=""

  params_for_download_lilypond =
  	filename: () ->
      "#{window.the_composition.filename}.ly"
    data: () ->
      window.the_composition.lilypond
    swf: 'js/third_party/downloadify/media/downloadify.swf'
    downloadImage: 'images/download.png'
    height:21
    width:76
    transparent:false
    append:false
    onComplete: () ->
      alert("Your file was saved")
  params_for_download_sargam= _.clone(params_for_download_lilypond)
  params_for_download_sargam.data = () ->
    $('#entry_area').val()
  params_for_download_sargam.filename = () ->
    "#{window.the_composition.filename}_sargam.txt"

  $("#download_lilypond_source").downloadify(params_for_download_lilypond)
  $("#download_sargam_source").downloadify(params_for_download_sargam)
  $('#load_long_composition').click ->
    $('#entry_area').val(long_composition)
  $('#load_sample_composition').click ->
    $('#entry_area').val(str)
  $('#show_parse_tree').click ->
      $('#parse_tree').toggle()
  my_url="lilypond.txt"
 # url="hlilypond"

  $('#generate_staff_notation').click =>
    $('#lilypond_png').attr('src',"")
    my_data =
      fname:window.the_composition.filename
      data: window.the_composition.lilypond
      sargam_source: $('#entry_area').val()
    obj=
      type:'POST'
      url:my_url
      data: my_data
      error: (some_data) ->
        alert "Generating staff notation failed"
        $('#lilypond_png').attr('src','none.jpg')
      success: (some_data,text_status) ->
        $('#play_midi').attr('src',some_data.midi)
        window.the_composition.midi=some_data.midi
        for typ in ["png","pdf","midi","ly","txt"]
          snip = """
          window.open('compositions/#{some_data.fname}.#{typ}'); return false; 
          """
          $("#download_#{typ}").attr('href',"compositions/#{some_data.fname}.#{typ}")
          $("#download_#{typ}").attr('onclick',snip)

        $('#lilypond_png').attr('src',some_data.png)
        if true
          fooOffset = $('#lilypond_png').offset()
          destination = fooOffset.top
          $(document).scrollTop(destination)
          window.location = String(window.location).replace(/\#.*$/, "") + "#staff_notation"
                #.effect("highlight", {}, 3000)
        $('#lilypond_output').html(some_data.lilypond_output)
        if some_data.error
          $('#lilypond_output').toggle()

      dataType: "json"
    $.ajax(obj)

  $('#generate_html_page').click =>
    my_url="generate_html_page"
    composition=window.the_composition
    rendered_composition=renderer.to_html(composition)
    full_url="http://ragapedia.com"
    tmpl= """
<html>
  <head>
    <title>#{composition.title}</title>
    <link media="all" type="text/css" href="#{full_url}/css/application.css" rel="stylesheet">
    <meta content="text/html;charset=utf-8" http-equiv="Content-Type">
  </head>
<body>
  <div id="rendered_sargam">
    #{rendered_composition}
  </div>
    <!-- all this is needed for adjust_parens method call -->
    <script type="text/javascript" src="#{full_url}/js/third_party/jquery.js"></script>
    <script type="text/javascript" src="#{full_url}/js/third_party/underscore.js"></script>
    <script type="text/javascript" src="#{full_url}/js/sargam_html_renderer.js"></script>
    <script type="text/javascript" src="#{full_url}/js/standalone_html_page_application.js"></script>
</body>
</html>
    """
    my_data =
      timestamp: new Date().getTime()
      filename: composition.filename
      html_to_use:tmpl
    obj=
      type:'POST'
      url:my_url
      data: my_data
      error: (some_data) ->
        alert "Create html page failed, some_data is #{some_data}"
      success: (some_data,text_status) ->
        window.open("compositions/#{some_data}")
      dataType: "text"
    $.ajax(obj)

  $('#show_lilypond_output').click ->
    $('#lilypond_output').toggle()

  $('#show_lilypond_source').click ->
    $('#lilypond_source').toggle()
  $('#lilypond_output').click ->
          # $('#lilypond_output').hide()

  $('#lilypond_source').click ->
          # $('#lilypond_source').hide()

    return if parser.is_parsing
  $('#run_parser').click ->
    return if parser.is_parsing

    window.parse_errors=""
    $('#parse_tree').text('parsing...')
    try
      $('#warnings_div').hide()
      $('#warnings_div').html("")
      parser.is_parsing=true
      composition_data= parser.parse (src=$('#entry_area').val())
      composition_data.source=src
      composition_data.lilypond=to_lilypond(composition_data)
      window.the_composition=composition_data
      $('#parse_tree').text("Parsing completed with no errors \n"+JSON.stringify(composition_data,null,"  "))
      if composition_data.warnings.length > 0
        $('#warnings_div').html "The following warnings were reported:<br/>"+composition_data.warnings.join('<br/>')
        $('#warnings_div').show()
      $('#parse_tree').hide()
      $('#rendered_sargam').html(renderer.to_html(composition_data))
      $('#lilypond_source').html(composition_data.lilypond)
      # TODO: combine with the above line..
      renderer.adjust_slurs_in_dom()
      if false
        $('span[data-begin-slur-id]').each  (index) ->
          pos2=$(this).offset()
          attr=$(this).attr("data-begin-slur-id")
          slur=$("##{attr}")
          pos1=$(slur).offset()
          $(slur).css({width: pos2.left- pos1.left + $(this).width()})
      canvas = $("#rendered_in_staff_notation")[0]
    catch err
      window.parse_errors= window.parse_errors + "\n"+ err
      $('#parse_tree').text(window.parse_errors)
      $('#parse_tree').show()
    finally
      window.last_val=$('#entry_area').val()
      parser.is_parsing=false

  # $('#load_sample_composition').trigger('click')
  $('#run_parser').trigger('click')
  $('#parse_tree').hide()
  $('#lilypond_output').hide()
  $('#lilypond_source').hide()




  window.do_timer()


