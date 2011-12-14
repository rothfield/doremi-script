#  $ = jQuery

root = exports ? this

$(document).ready ->
  $('.generated_by_lilypond').hide()
  Logger=_console.constructor
  # _console.level  = Logger.DEBUG
  _console.level  = Logger.WARN
  _.mixin(_console.toObject())
  if Zepto?
    _.debug("***Using zepto.js instead of jQuery***")
  debug=false

  setup_samples_dropdown= () ->
    params=
      type:'GET'
      url:'/list_samples'
      dataType:'json'
      success: (data) ->
        str= ("<option value='/samples/#{item}'>#{item}</option>" for item in data).join('')
        $('#sample_compositions').append(str)
    $.ajax(params)

  setup_samples_dropdown()
  if window.location.host.indexOf('localhost') is -1
    $("#add_to_samples").hide()
  setup_links= (filename) ->
    without_suffix=filename.substr(0, filename.lastIndexOf('.txt')) || filename
    for typ in ["png","pdf","mid","ly","txt","xml"]
      snip = """
      window.open('#{without_suffix}.#{typ}'); return false; 
      """
      $("#download_#{typ}").attr('href',full_path="#{without_suffix}.#{typ}")
      if typ is 'png'
        $('#lilypond_png').attr('src',full_path)
      $("#download_#{typ}").attr('onclick',snip)


  load_filepath= (filepath) ->
    params=
      type:'GET'
      url:filepath
      dataType:'text'
      success: (data) =>
        $('#entry_area').val(data)
        $('#sample_compositions').val("Load sample compositions")
        setup_links(filepath)
        $('.generated_by_lilypond').show()
        $('#run_parser').trigger('click')
    $.ajax(params)

  # Handler for samples dropdown
  sample_compositions_click = ->
    return if this.selectedIndex is 0
    load_filepath(this.value)

  $('#sample_compositions').change(sample_compositions_click)

  handleFileSelect = (evt) =>
    # Handler for file upload button(HTML5)
    file = document.getElementById('file').files[0]
    reader=new FileReader()
    reader.onload =  (evt) ->
      $('#entry_area').val evt.target.result
      $('#lilypond_png').attr('src',"")
    reader.readAsText(file, "")

  document.getElementById('file').addEventListener('change', handleFileSelect, false)

  str="C    C7\nS R- --"
  str='''
  mg
   (m--D) |
  '''
  str='''
     A11 A7    Dm  Bb Dm/A       C      F           F
                 C         Gm           1_____      2______
                   *                    *           *           
   | G - G - | D N S- ND | N- -D P N | (S P m G) :| S - - -     ||
     I said some-thing wrong now I long for  yes-ter-day day 
  '''
  str='''
    Am/D
  | S- - - -   
  '''
  str='<SR>\n|  m'
  str='''
       S
  |(Sr  n)
  '''
  str='mP\n  D-'
  str=' DSnDn\nn---'
  str="""
       GR 
  | (GR S)-
  """
  str=""
  window.timer_is_on=0
  # "/samples/happy_birthday" in URL
  if window.location.pathname.indexOf("/samples/") > -1
    load_filepath("#{window.location.pathname}.txt")
  if window.location.pathname.indexOf("/compositions/") > -1
    load_filepath("#{window.location.pathname}.txt")

  $('#entry_area').val(str)
  window.last_val=str
  window.timed_count = () =>
    cur_val= $('#entry_area').val()
    if window.last_val != cur_val
      $('#run_parser').trigger('click')
      window.last_val= cur_val
    t=setTimeout("timed_count()",1000)

  window.do_timer  =  () =>
    if !window.timer_is_on
      window.timer_is_on=1
      window.timed_count()
  parser=DoremiScriptParser
  parser.is_parsing=false
  window.parse_errors=""
  $('#show_parse_tree').click ->
      $('#parse_tree').toggle()

  $('#generate_staff_notation').click =>
    $('#lilypond_png').attr('src',"")
    $('.generated_by_lilypond').hide()
    my_data =
      as_html:true
      fname:window.the_composition.filename
      data: window.the_composition.lilypond
      doremi_script_source: $('#entry_area').val()
      musicxml:window.the_composition.musicxml
      save_to_samples: $('#save_to_samples').val() is "on"
    obj=
      type:'POST'
      url:'/lilypond.txt'
      data: my_data
      error: (some_data) ->
        alert "Generating staff notation failed"
        $('#lilypond_png').attr('src','none.jpg')
      success: (some_data,text_status) ->
        setup_links(some_data.fname)
        #window.location = String(window.location).replace(/\#.*$/, "") + "#staff_notation"
        $('.generated_by_lilypond').show()
        $('#lilypond_output').html(some_data.lilypond_output)
        if some_data.error
          $('#lilypond_output').toggle()
      dataType: "json"
    $.ajax(obj)

  get_dom_fixer = () ->
    params=
      type:'GET'
      url:'/js/dom_fixer.js'
      dataType:'text'
      success: (data) ->
        $('#dom_fixer_for_html_doc').html(data)
        window.generate_html_doc_ctr--
        generate_html_page_aux()
    $.ajax(params)
  
  get_zepto = () ->
    params=
      type:'GET'
      url:'/js/third_party/zepto.unminified.js'
      dataType:'text'
      success: (data) ->
        $('#zepto_for_html_doc').html(data)
        window.generate_html_doc_ctr--
        generate_html_page_aux()
    $.ajax(params)
 
  get_css = () ->
    params=
      type:'GET'
      url:'/css/application.css'
      dataType:'text'
      success: (data) ->
        $('#css_for_html_doc').html(data)
        window.generate_html_doc_ctr--
        generate_html_page_aux()
    $.ajax(params)
 
 
  generate_html_page_aux = () ->
    return if window.generate_html_doc_ctr > 0
    css=$('#css_for_html_doc').html()
    js=$('#zepto_for_html_doc').html()
    js2=$('#dom_fixer_for_html_doc').html()
    composition=window.the_composition
    full_url="http://ragapedia.com"
    html_str=to_html_doc(composition,full_url,css,js+js2)
    my_data =
      timestamp: new Date().getTime()
      filename: composition.filename
      html_to_use:html_str
    obj=
      type:'POST'
      url:"/generate_html_page"
      data: my_data
      error: (some_data) ->
        alert "Create html page failed, some_data is #{some_data}"
      success: (some_data,text_status) ->
        window.open("compositions/#{some_data}")
      dataType: "text"
    $.ajax(obj)

  $('#generate_html_page').click =>
    # Generate standalone html page
    # load these from the server if they weren't already loaded
    if ((css=$('#css_for_html_doc').html()).length < 100)
      return if window.generate_html_doc_ctr? and  (window.generate_html_doc_ctr > 0)
      # Load the things we need from the server asynchronously
      # from the server. Use a simple counter mechanism to synchronize 
      # things. I'm using a very lightweight library (zepto) that doesn't
      # support 'promises'
      window.generate_html_doc_ctr=3
      get_css()
      get_zepto()
      get_dom_fixer()
      return
    generate_html_page_aux()

  $('#show_lilypond_output').click ->
    $('#lilypond_output').toggle()
  $('#show_musicxml_source').click ->
    $('#musicxml_source').toggle()

  $('#show_lilypond_source').click ->
    $('#lilypond_source').toggle()


  run_parser = () ->
    return if parser.is_parsing
    window.parse_errors=""
    $('#parse_tree').text('parsing...')
    try
      parser.is_parsing=true
      $('#warnings_div').hide()
      $('#warnings_div').html("")
      src= $('#entry_area').val()
      composition_data= parser.parse(src)
      composition_data.source=src
      # TODO: not really necessary on every keystroke!!
      # TODO: think when to run these.
      composition_data.lilypond=to_lilypond(composition_data)
      composition_data.musicxml=to_musicxml(composition_data)
      window.the_composition=composition_data
      $('#parse_tree').text("Parsing completed with no errors \n"+JSON.stringify(composition_data,null,"  "))
      if composition_data.warnings.length > 0
        $('#warnings_div').html "The following warnings were reported:<br/>"+composition_data.warnings.join('<br/>')
        $('#warnings_div').show()
      $('#parse_tree').hide()
      $('#rendered_doremi_script').html(to_html(composition_data))
      $('#lilypond_source').text(composition_data.lilypond)
      $('#musicxml_source').text(composition_data.musicxml)
      # TODO: combine with the above line..
      dom_fixes()
      canvas = $("#rendered_in_staff_notation")[0]
    catch err
      #console.log "err parsing, err is",err
      window.parse_errors= window.parse_errors + "\n"+ err
      $('#parse_tree').text(window.parse_errors)
      $('#parse_tree').show()
    finally
      window.last_val=$('#entry_area').val()
      parser.is_parsing=false

  $('#run_parser').click ->
      run_parser()

  get_current_line_of_text_area = (obj) ->
    get_current_line_of(obj.value,obj.selectionStart,obj.selectionEnd)

  get_current_line_of = (val,sel_start,sel_end) ->
    # doesn't use dom
    # extract current line from some text given start and
    # end positions representing current section
    to_left_of_cursor=val.slice(0,sel_start)
    to_right_of_cursor=val.slice(sel_end)
    pos_of_start_of_line=to_left_of_cursor.lastIndexOf('\n')
    if pos_of_start_of_line is -1
      start_of_line_to_end=val
    else
      start_of_line_to_end=val.slice(pos_of_start_of_line+1)
    index_of_end_of_line=start_of_line_to_end.indexOf('\n')
    if index_of_end_of_line is -1
      line=start_of_line_to_end
    else
      line=start_of_line_to_end.slice(0,index_of_end_of_line)
    line

  handle_key_stroke = (event) ->
    # The purpose of this code is to filter the characters as the
    # user types to make it easier to enter notes. For example, if the
    # user is entering the main line of sargam, then it is nice to automatically convert a "s" or "p" that the user types into uppercase "S" or "P".
    # For now use a primitive test to see if the user is "in" a sargam line.
    # In the future, can add feature to constrain to notes in mode or a "NotesUsed" attribute
    el=this
    val=el.value
    sel_start=el.selectionStart
    sel_end=el.selectionEnd
    to_left_of_cursor=val.slice(0,sel_start)
    to_right_of_cursor=val.slice(sel_end)
    pos_of_start_of_line=to_left_of_cursor.lastIndexOf('\n')
    if pos_of_start_of_line is -1
      start_of_line_to_end=val
    else
      start_of_line_to_end=val.slice(pos_of_start_of_line+1)
    index_of_end_of_line=start_of_line_to_end.indexOf('\n')
    if index_of_end_of_line is -1
      line=start_of_line_to_end
    else
      line=start_of_line_to_end.slice(0,index_of_end_of_line)
    line=get_current_line_of_text_area(el)
    if event.which in [115,112]
       if (line.indexOf('|') > -1)
         hash=
           112:"P"
           115:"S"
         char=hash[event.which]
         event.preventDefault()
         el.value="#{to_left_of_cursor}#{char}#{to_right_of_cursor}"
         #window.last_val=el.value
         el.setSelectionRange(sel_start+1,sel_start+1)
         el.focus()
         #$('#run_parser').click


  $('#entry_area').keypress(handle_key_stroke)

  $('#parse_tree').hide()
  $('#lilypond_output').hide()
  $('#lilypond_source').hide()
  $('#musicxml_source').hide()
  window.do_timer()




