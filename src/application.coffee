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
  str="| S--R --G- | -m-- P"
  str3='''
       . .. .
        NRSNS      NRSNS
               .  .    
 |     S          S

  m
 | g


  '''
  str3='''
   . .. .
    NRSNS      NRSNS
               .  .    
 | S          S

    m
|    g
  '''
  str3='''
  PmPm       
 |    P   
 '''
  $('#entry_area').val(str3)
  parser=SargamParser
  window.parse_errors=""
  $('#load_long_composition').click ->
    $('#entry_area').val($('#long_sample').html())
  $('#load_sample_composition').click ->
    $('#entry_area').val(str)
  $('#show_parse_tree').click ->
      $('#parse_tree').toggle()
  my_url="lilypond.txt"

  $('#generate_staff_notation').click =>
    $('#lilypond_png').attr('src',"")
    $('.generated_by_lilypond').hide()
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
        for typ in ["png","pdf","mid","ly","txt"]
          snip = """
          window.open('compositions/#{some_data.fname}.#{typ}'); return false; 
          """
          $("#download_#{typ}").attr('href',x="compositions/#{some_data.fname}.#{typ}")
          if typ is 'png'
            $('#lilypond_png').attr('src',x)
          $("#download_#{typ}").attr('onclick',snip)

        window.location = String(window.location).replace(/\#.*$/, "") + "#staff_notation"
        $('.generated_by_lilypond').show()
        $('#lilypond_output').html(some_data.lilypond_output)
        if some_data.error
          $('#lilypond_output').toggle()

      dataType: "json"
    $.ajax(obj)

  get_dom_fixer = () ->
    params=
      type:'GET'
      url:'js/dom_fixer.js'
      dataType:'text'
      success: (data) ->
        $('#dom_fixer_for_html_doc').html(data)
        generate_html_page_aux()
    $.ajax(params)
  
  get_zepto = () ->
    params=
      type:'GET'
      url:'js/third_party/zepto.unminified.js'
      dataType:'text'
      success: (data) ->
        $('#zepto_for_html_doc').html(data)
        get_dom_fixer()
    $.ajax(params)
 
  get_css = () ->
    params=
      type:'GET'
      url:'css/application.css'
      dataType:'text'
      success: (data) ->
        $('#css_for_html_doc').html(data)
        get_zepto()
    $.ajax(params)
 
 
  generate_html_page_aux = () ->
    css=$('#css_for_html_doc').html()
    js=$('#zepto_for_html_doc').html()
    js2=$('#dom_fixer_for_html_doc').html()
    my_url="generate_html_page"
    composition=window.the_composition
    full_url="http://ragapedia.com"
    html_str=to_html_doc(composition,full_url,css,js+js2)
    my_data =
      timestamp: new Date().getTime()
      filename: composition.filename
      html_to_use:html_str
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

  $('#generate_html_page').click =>
    # load these from the server if they weren't already loaded
    if ((css=$('#css_for_html_doc').html()).length < 100)
      get_css()
      return 
    generate_html_page_aux()

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
      $('#rendered_sargam').html(to_html(composition_data))
      $('#lilypond_source').html(composition_data.lilypond)
      # TODO: combine with the above line..
      adjust_slurs_in_dom()
      canvas = $("#rendered_in_staff_notation")[0]
    catch err
      window.parse_errors= window.parse_errors + "\n"+ err
      $('#parse_tree').text(window.parse_errors)
      $('#parse_tree').show()
      throw err
    finally
      window.last_val=$('#entry_area').val()
      parser.is_parsing=false

  $('#run_parser').trigger('click')
  $('#parse_tree').hide()
  $('#lilypond_output').hide()
  $('#lilypond_source').hide()




  window.do_timer()


