# Uses module pattern, exports  dom_fixes()
# Usage:
# dom_fixes()
root = exports ? this

add_right_margin_to_notes_with_pitch_signs= (context=null) ->
  # TODO: DRY with others
  $('span.note_wrapper *.pitch_sign',context).each (index) ->
    parent=$(this).parent()
    current_margin_right=parseInt($(parent).css('margin-right').replace('px', ''))
    $(parent).css('margin-right',current_margin_right + $(this).width())

add_left_margin_to_notes_with_left_superscripts= (context=null) ->
  $('span.note_wrapper *.ornament.placement_before',context).each (index) ->
    parent=$(this).parent()
    current_margin_left=parseInt($(parent).css('margin-left').replace('px', ''))
    $(parent).css('margin-left',current_margin_left + $(this).width())

add_right_margin_to_notes_with_right_superscripts= (context=null) ->
  $('span.note_wrapper *.ornament.placement_after',context).each (index) ->
    parent=$(this).parent()
    current_margin_right=parseInt($(parent).css('margin-right').replace('px', ''))
    $(parent).css('margin-right',current_margin_right + $(this).width())
expand_note_widths_to_accomodate_syllables= (context=null) ->
  ###
       Example:
   RS S
   yes-ter-day

   The html renderer blindly lays out the syllables underneath
   the corresponding notes without regard to the syllables colliding
   with each other. Examine each syllable, and if the next syllable
   collides, then adjust the width of the NOTE accordingly     
  ###
  syllables=$('span.syllable',context).get()
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


fallback_if_utf8_characters_not_supported= (context=null) ->
  # If the browser supports utf8 music characters
  # then the width of single barline and left repeat will
  # NOT be the same.
  if !window.ok_to_use_utf8_music_characters?
    #console.log("Testing utf8 support") 
    width1=$('#utf_left_repeat').show().width()
    width2=$('#utf_single_barline').show().width()
    $('#utf_left_repeat').hide()
    $('#utf_single_barline').hide()
    #console.log("width of left_repeat is",width1) 
    #console.log("width of single_barline is",width2) 
    window.ok_to_use_utf8_music_characters= (width1 isnt width2)
  if ! window.ok_to_use_utf8_music_characters
    #console.log("Falling back to ascii characters")
    tag="data-fallback-if-no-utf8-chars"
    $("span[#{tag}]",context).addClass('dont_use_utf8_chars')
    $("span[#{tag}]",context).each  (index) ->
      obj=$(this)
      attr=obj.attr(tag)
      obj.html(attr)

adjust_slurs_in_dom= (context=null) ->
  $('span[data-begin-slur-id]',context).each  (index) ->
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

fix_before_ornaments= (context=null) ->
  # For the case of the ornament that is placed before the pitch,
  # use css to set margin-left to the negative of the width!!
  #
  # Source looks like:
  #
  # Pmg
  #    R
  $('span.ornament.placement_before',context).each (index) ->
    el=$(this)
    el.css('margin-left',"-#{el.width()}px")


dom_fixes = () ->
  # apply fixes divs that have NOT been fixed
  # div.stave is the output of the html renderer
  context=$("div.stave:not([data-dom-fixed='true'])")
  console.log "entering dom_fixes, context is",context
  console.log "entering dom_fixes, context.size() is",context.size()
  # Order matters!
  fallback_if_utf8_characters_not_supported(context)
  fix_before_ornaments(context)
  add_left_margin_to_notes_with_left_superscripts(context)
  add_right_margin_to_notes_with_right_superscripts(context)
  add_right_margin_to_notes_with_pitch_signs(context)
  expand_note_widths_to_accomodate_syllables(context)
  adjust_slurs_in_dom(context)
  # Mark divs as fixed
  context.attr('data-dom-fixed','true')
root.dom_fixes=dom_fixes

