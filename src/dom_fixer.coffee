# Uses module pattern, exports adjust_slurs_in_dom
# Usage:
# dom_fixes()
root = exports ? this


expand_note_widths_to_accomodate_syllables= () ->
  ###
       Example:
   RS S
   yes-ter-day

   The html renderer blindly lays out the syllables underneath
   the corresponding notes without regard to the syllables colliding
   with each other. Examine each syllable, and if the next syllable
   collides, then adjust the width of the NOTE accordingly     
  ###
  syllables=$('span.syllable').get()
  len=syllables.length
  for syllable,index in syllables
    continue if index is (len-1) 
    $syllable=$(syllable)
    # $syllable.val() seems not to work in zepto
    syl_str=syllable.textContent || syllable.innerText # zepto fix
    is_word_end=syl_str[syl_str.length-1] isnt "-"
    extra2= if is_word_end then 5 else 0
    $next=$(syllables[index+1])
    width=$syllable.width()
    left=$next.offset().left
    # on different line case
    continue if $next.offset().top  !=  $syllable.offset().top
    next_left=$next.offset().left
    syl_right=$syllable.offset().left + width
    if (syl_right + extra2) > next_left
      $par=$syllable.parent()
      $note=$('span.note',$par)
      margin_right=$note.css("margin-right")
      existing_margin_right=0
      extra=5
      $note.css("margin-right","#{ existing_margin_right + syl_right - next_left + extra + extra2}px")


fallback_if_utf8_characters_not_supported= () ->
  if !window.left_repeat_width?
    x=$('#testing_utf_support')
    x.show()
    window.left_repeat_width=$(x).width()
    if !window.left_repeat_width?
      window.left_repeat_width=0
    x.hide()
  #  $('body').append("left_repeat_width is #{window.left_repeat_width}")
  if (window.left_repeat_width is 0) or (window.left_repeat_width > 10) 
    tag="data-fallback-if-no-utf8-chars"
    $("span[#{tag}]").each  (index) ->
      obj=$(this)
      attr=obj.attr(tag)
      obj.html(attr)

adjust_slurs_in_dom= () ->
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

fix_before_ornaments= () ->
  # For the case of the ornament that is placed before the pitch,
  # use css to set margin-left to the negative of the width!!
  #
  # Source looks like:
  #
  # Pmg
  #    R
  $('span.ornament.placement_before').each (index) ->
    el=$(this)
    el.css('margin-left',"-#{el.width()}px")


dom_fixes = () ->
  adjust_slurs_in_dom()
  fallback_if_utf8_characters_not_supported()
  fix_before_ornaments()
  expand_note_widths_to_accomodate_syllables()

root.dom_fixes=dom_fixes

