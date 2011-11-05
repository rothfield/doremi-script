$ = jQuery

root = exports ? this

$(document).ready ->
  window.debug=false
  renderer=new SargamHtmlRenderer
  renderer.adjust_slurs_in_dom()


