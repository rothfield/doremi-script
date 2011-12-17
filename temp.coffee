setup_samples_dropdown= () ->
  params=
    type:'GET'
    url:'/list_samples'
    dataType:'json'
    success: (data) ->
      str= "<option value='#{item}'>#{item}</option>" 
      str=(for item in data
        short=item.slice(item.lastIndexOf('/')+1)
        "<option value='#{item}'>#{short}</option>").join('')
      $('#sample_compositions').append(str)
  $.ajax(params)


setup_links= (filename) ->
  console.log "setup_links"
  without_suffix=filename.substr(0, filename.lastIndexOf('.txt')) || filename
  for typ in ["png","pdf","mid","ly","txt","xml","html"]
    snip = """
    window.open('#{without_suffix}.#{typ}'); return false; 
    """
    $("#download_#{typ}").attr('href',full_path="#{without_suffix}.#{typ}")
    if typ is 'png'
      $('#lilypond_png').attr('src',full_path)
    $("#download_#{typ}").attr('onclick',snip)

setup_links_after_save= (filename) ->
  without_suffix=filename.substr(0, filename.lastIndexOf('.txt')) || filename
  $('#url_to_reopen').val(x=window.location.href)
  $('#reopen_link').text(x)
  $('#reopen_link').attr('href',x)
  for typ in ["ly","txt","xml"]
    snip = """
    window.open('#{without_suffix}.#{typ}'); return false; 
    """
    $("#download_#{typ}").attr('href',full_path="#{without_suffix}.#{typ}")
    $("#download_#{typ}").attr('onclick',snip)

load_filepath= (filepath) ->
  # Gets called if url is like http://ragapedia.com/compositions/yesterday
  params=
    type:'GET'
    url:"#{filepath}.txt"
    dataType:'text'
    success: (data) =>
      console.log "load_filepath"
      $('#entry_area').val(data)
      $('#sample_compositions').val("Load sample compositions")
      setup_links_after_save(filepath)
      setup_links(filepath)
      $('.generated_by_lilypond').show()
      $('#run_parser').trigger('click')
  $.ajax(params)

# Handler for samples dropdown
sample_compositions_click = ->
  return if this.selectedIndex is 0
  redirect_helper(this.value)

handleFileSelect = (evt) =>
  # Handler for file upload button(HTML5)
  file = document.getElementById('file').files[0]
  reader=new FileReader()
  reader.onload =  (evt) ->
    $('#entry_area').val evt.target.result
    $('#lilypond_png').attr('src',"")
  reader.readAsText(file, "")

