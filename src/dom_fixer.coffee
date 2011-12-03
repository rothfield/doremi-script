# Uses module pattern, exports adjust_slurs_in_dom
# Usage:
# adjust_slurs_in_dom()
root = exports ? this


adjust_slurs_in_dom= () ->
  console.log "adjust_slurs_in_dom"
  if !window.left_repeat_width?
    x=$('#testing_utf_support')
    x.show()
    window.left_repeat_width=$(x).width()
    x.hide()
  console.log "checking left_repeat_width"
  if window.left_repeat_width is 0 or window.left_repeat_width > 8 
    tag="data-fallback-if-no-utf8-chars"
    $("span[#{tag}]").each  (index) ->
      console.log("utf8 fix loop, this is", this)
      obj=$(this)
      console.log('dom-fixer',this)
      attr=obj.attr(tag)
      obj.html(attr)

  $('span[data-begin-slur-id]').each  (index) ->
    pos2=$(this).offset()
    attr=$(this).attr("data-begin-slur-id")
    slur=$("##{attr}")
    return if slur.length==0
    pos1=$(slur).offset()
    val=pos2.left-pos1.left
    if val <0
      _.error "adjust_slurs_in_dom, negative width"
      return
    $(slur).css {width: pos2.left- pos1.left + $(this).width()}
  $('span.ornament.placement_before').each (index) ->
    # For the case of the ornament that is placed before the pitch,
    # use css to set margin-left to the negative of the width!!
    #
    # Source looks like:
    #
    # Pmg
    #    R
    el=$(this)
    el.css('margin-left',"-#{el.offset().width}px")
root.adjust_slurs_in_dom=adjust_slurs_in_dom
