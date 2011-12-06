  $('#run_parser').click ->
    return if parser.is_parsing

    window.parse_errors=""
    $('#parse_tree').text('parsing...')
    try
      $('#warnings_div').hide()
      $('#warnings_div').html("")
      parser.is_parsing=true
      src= $('#entry_area').val()
      #src2=src.replace(/(\n|\r\f)[\t ]+(\n|\r\f)/g, "\n\n")
      #src2=src2.replace(/(\n|\r\f)[\t ]+$/g, "\n")
      #console.log(src2)
      #composition_data= parser.parse (src=$('#entry_area').val())
      composition_data= parser.parse(src)
      composition_data.source=src
      composition_data.lilypond=to_lilypond(composition_data)
      window.the_composition=composition_data
      $('#parse_tree').text("Parsing completed with no errors \n"+JSON.stringify(composition_data,null,"  "))
      if composition_data.warnings.length > 0
        $('#warnings_div').html "The following warnings were reported:<br/>"+composition_data.warnings.join('<br/>')
        $('#warnings_div').show()
      $('#parse_tree').hide()
      $('#rendered_doremi_script').html(to_html(composition_data))
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
