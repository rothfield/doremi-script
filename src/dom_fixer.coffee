# Uses module pattern, exports adjust_slurs_in_dom
# Usage:
# adjust_slurs_in_dom()
root = exports ? this


adjust_slurs_in_dom= () ->

  syllables=$('span.syllable').get()
  len=syllables.length
  for syllable,index in syllables
    continue if index is (len-1) 
    $syllable=$(syllable)
    $next=$(syllables[index+1])
    width=$syllable.offset().width
    left=$next.offset().left
    # on different line case
    continue if $next.offset().top  !=  $syllable.offset().top
    next_left=$next.offset().left
    syl_right=$syllable.offset().left + width
    if syl_right > next_left 
      console.log "correcting syllable #{$syllable.html()}"
      #  $syllable.css('background-color','red')
      $par=$syllable.parent()
      $note=$('span.note',$par)
      margin_right=$note.css("margin-right")
      existing_margin_right=0
      extra=5
      $note.css("margin-right","#{ existing_margin_right + (syl_right - next_left)+ extra}px")
  console.log "adjust_slurs_in_dom"

  if !window.left_repeat_width?
    x=$('#testing_utf_support')
    x.show()
    window.left_repeat_width=$(x).width()
    if !window.left_repeat_width?
      window.left_repeat_width=0
    x.hide()
    $('body').append("left_repeat_width is #{window.left_repeat_width}")
  if (window.left_repeat_width is 0) or (window.left_repeat_width > 10) 
    tag="data-fallback-if-no-utf8-chars"
    $("span[#{tag}]").each  (index) ->
      obj=$(this)
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
