###
  sargam_json_to_lilypond.to_lilypond 
  -- Take in json structure representing a composition and 
          return lilypond 
###
debug=false

root = exports ? this

root.SargamJsonToLilypond= (class SargamJsonToLilypond

  constructor: (@composition_data) ->

  extract_lyrics: () ->
    ary=[]
    for logical_line in @composition_data.logical_lines
      for item in this.all_items_in_line(logical_line.sargam_line,[])
        @log "extract_lyrics-item is",item
        ary.push item.syllable if item.syllable
    ary

  get_attribute: (key) ->
    return null if !@composition_data.attributes
    att=_.detect(@composition_data.attributes.items, (item) ->
      item.key is key
      )
    return null if !att
    att.value

  log: (x) ->
    return if ! @debug?
    return if !@debug
    console.log x if console

  running_under_node: ->
    module? && module.exports

  my_inspect: (obj) ->
    return if ! debug?
    return if !debug
    if this.running_under_node()
      console.log(sys.inspect(obj,false,null)) 
      return
    console.log obj

  fraction_to_lilypond:
     # Todo: use fractions.js
     # TODO: have to tie notes for things like 5/8
     # which would be an 1/8th and a 32nd
     # To do it right should perhaps use fractional math as follows:
     # 5/8 = 1/2 + 1/8 => 1/8 + 1/32
    "1/1":"4"
    "1/2":"8"
    "1/3": "8"  # 1/3 1/5 1/7 all 8th notes so one beat will beam together
    "1/9":"8"
    "1/11":"8"
    "1/13":"8"
    "1/5":"16"
    "1/7": "32" # ??? correct???hhhhhhhhhh
    "2/7": "16" # ??? correct???hhhhhhhhhh
    "3/7": "16." # ??? correct???hhhhhhhhhh
    "4/7": "8" # ??? correct???hhhhhhhhhh
    "5/7": "8.." # ??? correct???hhhhhhhhhh
    "6/7": "8.." # ??? correct???hhhhhhhhhh
    "6/8": "8." 
    "2/3": "4"
    "2/8": "16"
    "3/8": "16."  # 1/4 + 1/8
    "5/8": "8"   # TODO: WRONG
    "4/8": "8"
    "7/8": "8.." # 1/2 + 1/4 + 1/8
    "1/6": "16"
    "2/6": "8"
    "3/6": "4"
    "4/6":"4" # NOT SURE ????
    "5/6":"8.." #  WRONGnot sure TODO??
    "2/2":"4"
    "3/3":"4"
    "4/4":"4"
    "8/8":"4"
    "1/4":"16"
    "2/4":"8"
    "3/4":"8."
    "3/8":"16."
    "4/16":"16"
    "3/16":""
    "1/8":"32"
    "3/8":"16."
    "6/16":"16"

  pitch_to_lilypond: (pitch) ->

    @log("pitch_to_lilypond, pitch is",pitch)
    p=this.lilypond_pitch_map[pitch.pitch_source]
    return "???#{pitch.source}" if  !p?
    o=this.lilypond_octave_map["#{pitch.octave}"]
    return "???#{pitch.octave}" if !o?
    begin_slur=""
    # Lower markings would be added as follows:
    # "-\"#{pp}\""
    mordent=""
    if pitch.attributes
      if _.detect(pitch.attributes, (x) -> x.my_type is "mordent")
        mordent="\\mordent"
    end_slur=""
    begin_slur="(" if pitch.begin_slur
    end_slur=")" if pitch.end_slur
    t=""
    t='~' if pitch.tied?
    frac="#{pitch.numerator}/#{pitch.denominator}"
    r=this.fraction_to_lilypond[frac]
    return "???#{frac}" if !r?
    "#{p}#{o}#{r}#{t}#{mordent}#{begin_slur}#{end_slur}"

  lilypond_octave_map:
    "-2":","
    "-1":""
    "0":"'"
    "1":"'"+"'"
    "2":"'"+"'"+"'"

  lilypond_pitch_map:
    "S":"c"
    "r":"df"
    "R":"d"
    "g":"ef"
    "G":"e"
    "m":"f"
    "M":"fs"
    "P":"g"
    "d":"af"
    "D":"a"
    "n":"bf"
    "N":"b"

  to_lilypond: () ->
    ary=[]
    in_times=false #hack
    for logical_line in @composition_data.logical_lines
      in_times=false #hack
      @log "processing #{logical_line.sargam_line.source}"
      all=[]
      x=this.all_items_in_line(logical_line.sargam_line,all)
      @log("in to_lilypond, x=",x)
      for item in all
        if in_times 
          if item.my_type is "beat" or item.my_type is "barline"
            ary.push "}" 
            in_times=false
        @log "processing #{item.source}, my_type is #{item.my_type}"
        ary.push this.pitch_to_lilypond(item) if item.my_type=="pitch"
        bar='''
        \\bar "|" 
        '''
        bar='|' # lilypond bar check # hack
        if item.is_barline
          ary.push bar 
        if item.my_type is "beat"
           beat=item
           if beat.subdivisions not in [0,1,2,4,8,16,32,64,128] 
               @log "odd beat.subdivisions=",beat.subdivisions
               x=2
               if beat.subdivisions is 6
                 x=4
               if  beat.subdivisions is 5
                 x=4
               ary.push "\\times #{x}/#{beat.subdivisions} { "
               in_times=true #hack
        if item.my_type is "measure"
           measure=item
           if measure.is_partial
              ary.push "\\partial 4*#{measure.beat_count} "
        if item.dash_to_tie
          @log "item.dash_to_tie case, item is",item
          orig=item.pitch_to_use_for_tie
          while orig.pitch_to_use_for_tie
             orig=orig.pitch_to_use_for_tie
          #orig = orig.pitch_to_use_for_tie if orig.pitch_to_use_for_tie
          @log("dash_to_tie, orig is",orig)
          obj={
             source:orig.source
             pitch_source:orig.pitch_source
             octave:orig.octave
             numerator:item.numerator
             denominator:item.denominator
             tied:item.tied
          }
          @log("dash_to_tie case")
          ary.push this.pitch_to_lilypond(obj)
      if in_times
        ary.push "}"
        in_times=false
      ary.push "\\break\n"
    part2= '''
    
    
    '''


    mode="major"
    my_mode=@get_attribute('Mode')
    mode = my_mode if my_mode
    # TODO: dry
    composer = @get_attribute("Author")
    composer_snippet=""
    if composer
      composer_snippet= """
        composer = "#{composer}"
       """

    title = @get_attribute("Title")
    time = @get_attribute("Time Signature")
    transpose = @get_attribute("Key")
    @log('transpose',transpose)
    transpose_snip=""
    valid_keys=[
      "c"
      "d"
      "e"
      "f"
      "g"
      "a"
      "b"
      "cs"
      "df"
      "ds"
      "ef"
      "fs"
      "gb"
      "gs"
      "ab"
      "as"
      "bf"
    ] 
    if transpose
      if _.detect(valid_keys, (x) -> x is transpose)
        transpose_snip="\\transpose c' #{transpose}'"
      else
        @log("#{transpose} not supported as key for lilypond")
    time="4/4" if !time 
    title_snippet="" 
    if title
      title_snippet= """
        title = "#{title}"
       """
    notes = ary.join " "
    #    
    # #{@extract_lyrics(@composition_data).join(' ') if false}
    #
    #
    latest= """
\\include "english.ly"
    \\header{ #{title_snippet} #{composer_snippet} }
    \\include "english.ly"

melody = {
  \\clef treble
  \\key c \\#{mode}
  \\time #{time}
  
  #{notes}
}

text = \\lyricmode {
    #{@extract_lyrics(@composition_data).join(' ')}
}

\\score{
  #{transpose_snip}
  <<
    \\new Voice = "one" {
      \\melody
    }
    \\new Lyrics \\lyricsto "one" \\text
  >>
  \\layout { }
  \\midi { }
}
    """
    latest

  all_items_in_line: (line_or_item,items) ->
    # TODO: dry this up
    # return (recursively) items in the line_or_item, delves into the hierarchy
    # looks for an items property and if so, recurses to it.
    # line 
    #   measure
    #     beat
    #       item
    if  (!line_or_item.items)
       return [line_or_item]
    for an_item in line_or_item.items
      do (an_item) =>
        items.push an_item #if !an_item.items?
        items.concat this.all_items_in_line(an_item,items)
    @log 'all_items_in_line returns', items
    return [line_or_item].concat(items)

)
