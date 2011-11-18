# Uses module pattern, exports to_lilypond
# Usage:
# to_lilypond(composition_json_data)
debug=true

root = exports ? this

is_valid_key= (str) ->
  ary= [
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
  _.indexOf(ary,str) > -1

extract_lyrics= (composition_data) ->
  ary=[]
  for sargam_line in composition_data.lines
    for item in all_items_in_line(sargam_line,[])
      @log "extract_lyrics-item is",item
      ary.push item.syllable if item.syllable
  ary

get_attribute= (composition_data,key) ->
  return null if !composition_data.attributes
  att=_.detect(composition_data.attributes.items, (item) ->
    item.key is key
    )
  return null if !att
  att.value

log= (x) ->
  return if !@debug?
  return if !@debug
  console.log arguments... if console

running_under_node= ->
  module? && module.exports

my_inspect= (obj) ->
  return if ! debug?
  return if !debug
  return if !console?
  if running_under_node()
    console.log(sys.inspect(obj,false,null)) 
    return
  console.log obj


fraction_to_lilypond=
   # Todo: use fractions.js
   # TODO: have to tie notes for things like 5/8
   # which would be an 1/8th and a 32nd
   # To do it right should perhaps use fractional math as follows:
   # 5/8 = 1/2 + 1/8 => 1/8 + 1/32
  "2/1":"2"
  "3/1":"2."
  "4/1":"1"
  "5/1":"1.."
  "1/1":"4"
  "1/1":"4"
  "1/1":"4"
  "1/1":"4"
  "1/2":"8"
  "1/3": "8"  # 1/3 1/5 1/7 all 8th notes so one beat will beam together
  "1/9":"8"
  "1/11":"8"
  "1/13":"8"
  "1/5":"16"
  "2/5":"8"
  "3/5":"8." #TODO should be tied
  "4/5":"4" #TODO should be tied
  "5/5":4
  "6/6":4
  "7/7":4
  "8/8":4
  "9/9":4
  "10/10":4
  "11/11":4
  "12/12":4
  "13/13":4
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

calculate_lilypond_duration= (numerator,denominator) ->
  if numerator is denominator
    return "4"
  frac="#{numerator}/#{denominator}"
  looked_up_duration=fraction_to_lilypond[frac]
  if !looked_up_duration?
    alternate="16"
    return alternate # return something
  looked_up_duration

normalized_pitch_to_lilypond= (pitch) ->
  # Render a pitch/dash as lilypond
  # needs work
  ending=""
  if pitch.attributes
    if e =_.detect(pitch.attributes, (x) -> x.my_type is "ending")
      ending="""
      ^"#{e.source}"
      """
  #duration=calculate_lilypond_duration pitch.numerator,pitch.denominator
  # TODO: clean this up!!!
  if pitch.fraction_array?
    first_fraction=pitch.fraction_array[0]
  else
    first_fraction=new Fraction(pitch.numerator,pitch.denominator)
  duration=calculate_lilypond_duration first_fraction.numerator.toString(),first_fraction.denominator.toString()
  @log("normalized_pitch_to_lilypond, pitch is",pitch)
  if pitch.my_type is "dash"
     return "r#{duration}#{ending}"
  p=lilypond_pitch_map[pitch.normalized_pitch]
  return "???#{pitch.source}" if  !p?
  o=lilypond_octave_map["#{pitch.octave}"]
  return "???#{pitch.octave}" if !o?
  begin_slur=""
  # Lower markings would be added as follows:
  # "-\"#{pp}\""
  mordent=""
  if pitch.attributes
    if _.detect(pitch.attributes, (x) -> x.my_type is "mordent")
      mordent="\\mordent"
  end_slur=""
  begin_slur="(" if item_has_attribute(pitch,"begin_slur")
  end_slur=")" if item_has_attribute(pitch,"end_slur")
  t=""
  t='~' if pitch.tied?
  "#{p}#{o}#{duration}#{t}#{mordent}#{begin_slur}#{end_slur}#{ending}"

lilypond_barline_map=
  # maps my_type field for barlines
  "reverse_final_barline":'''
    \\bar "|."
  '''
  "final_barline":'''
    \\bar "||"
  '''
  "double_barline":'''
    \\bar "||" 
  '''
  "single_barline":'''
    \\bar "|" 
  '''
  "left_repeat":'''
    \\bar "|:" 
  '''
  "right_repeat":'''
    \\bar ":|" 
  '''

lilypond_octave_map=
  "-2":","
  "-1":""
  "0":"'"
  "1":"'"+"'"
  "2":"'"+"'"+"'"

lilypond_pitch_map=
  "-":"r"
  "C":"c"
  "C#":"cs"
  "Cb":"cf"
  "Db":"df"
  "D":"d"
  "D#":"ds"
  "Eb":"ef"
  "E":"e"
  "E#":"es"
  "F":"f"
  "F#":"fs"
  "Gb":"gf"
  "G":"g"
  "G#":"gs"
  "Ab":"af"
  "A":"a"
  "A#":"as"
  "Bb":"bf"
  "B":"b"
  "B#":"bs"


emit_tied_array=(last_pitch,tied_array,ary) ->

  return if !last_pitch?
  return if tied_array.length is 0

  my_funct= (memo,my_item) ->
    frac=new Fraction(my_item.numerator,my_item.denominator)
    if !memo?  then frac else frac.add memo
    
  fraction_total=_.reduce(tied_array,my_funct,null)
  
  obj={}
  for key of last_pitch
    obj[key]=last_pitch[key]
  # hack the obj attributes to remove mordents
 
  filter = (attr) ->
    attr.my_type? and attr.my_type is not "mordent"
  obj.attributes= _.select(last_pitch.attributes,filter)
  obj.numerator=fraction_total.numerator
  obj.denominator=fraction_total.denominator
  obj.fraction_array=null
  #TODO: make more general
  my_fun = (attr) ->
    attr.my_type is not "mordent"
  obj.attrs2= _.select(obj.attributes, my_fun)
  @log "emit_tied_array-last is", last
  last=tied_array[tied_array.length-1]
  obj.tied= last.tied
  @log "leaving emit_tied_array"
  tied_array.length=0 # clear it
  ary.push normalized_pitch_to_lilypond(obj)
 
is_sargam_line= (line) ->
  return false if !line.kind?
  line.kind.indexOf('sargam') > -1

notation_is_in_sargam= (composition_data) ->
  @log "in notation_is_in_sargam"
  _.detect(composition_data.lines, (line) -> is_sargam_line(line))

beat_is_all_dashes= (beat) ->
  x=all_items_in_line(beat)
  fun = (item) ->
    return true if !item.my_type?
    return false if item.my_type is "pitch"
  _.all(beat,fun)
  
to_lilypond= (composition_data) ->
  # TODO: dashes at beginning of measure need to be rendered as 
  # rests in lilypond!!
  ary=[]
  in_times=false #hack
  at_beginning_of_first_measure_of_line=false
  # Note that the parser produces something like this for
  # -- --S- 
  #
  # composition
  #   line
  #     measure
  #       beat
  #         dash 
  #           numerator:2
  #           denominator:2
  #           rest: true #### NOTICE ###
  #           source: "-"
  #         dash
  #           source: "-"
  #       whitespace
  #       beat
  #         dash
  #           numerator:2
  #           denominator:4
  #           source: "-"
  #         dash
  #           source: "-"
  #         pitch:
  #           source: "S"
  #           numerator:2
  #           denominator:2
  #         dash:
  #           source: "-"
  #
  #
  #   So that the parser has marked off 1 1/2 beats as rests
  #   Note that Sargam only has rests at the beginning of a line by
  #   my interpretation!!
  dashes_at_beginning_of_line_array=[]
  tied_array=[]

  for line in composition_data.lines
    at_beginning_of_first_measure_of_line=false
    in_times=false #hack
    @log "processing #{line.source}"
    all=[]
    x=all_items_in_line(line,all)
    @log("in to_lilypond, all_items_in_line x=",x)
    last_pitch=null
    for item in all
      if item.my_type in ["pitch","barline","measure"] or item.is_barline
        emit_tied_array(last_pitch,tied_array,ary) if tied_array.length >0 

      # TODO refactor
      # TODO: barlines should get attributes like endings too and talas too!
      if in_times
        if item.my_type is "beat" or item.my_type is "barline"
          ary.push "}"
          in_times=false
      @log "processing #{item.source}, my_type is #{item.my_type}"
      if item.my_type=="pitch"
        last_pitch=item  #use this to help render ties better(hopefully)
        # TODO: before emitting pitch, try using up tied array!!
        # process dashes_at_beginning_of_line_array
        if dashes_at_beginning_of_line_array.length > 0
          for dash in dashes_at_beginning_of_line_array
            # TODO: combine 1/4 rests???
            ary.push normalized_pitch_to_lilypond(dash)
          dashes_at_beginning_of_line_array=[]
        ary.push normalized_pitch_to_lilypond(item) if item.my_type=="pitch"
      bar='''
      \\bar "|" 
      '''
      if item.is_barline
        # TODO:extract method
        x= lilypond_barline_map[item.my_type] 
        x=bar if !x?
        x= ary.push  x
      if item.my_type is "beat"
         beat=item
         if beat.subdivisions not in [0,1,2,4,8,16,32,64,128] and !beat_is_all_dashes(beat) 
             @log "odd beat.subdivisions=",beat.subdivisions
             x=2
             if beat.subdivisions is 6
               x=4
             if  beat.subdivisions is 5
               x=4
             ary.push "\\times #{x}/#{beat.subdivisions} { "
             in_times=true #hack
      if item.my_type is "dash"
        if !item.dash_to_tie and item.numerator? #THEN its at beginning of line!
          @log "pushing item onto dashes_at_beginning_of_line_array"
          dashes_at_beginning_of_line_array.push item
      if item.my_type is "measure"
         measure=item
         if measure.is_partial
            ary.push "\\partial 4*#{measure.beat_count} "
      if item.dash_to_tie
        tied_array.push item
        if false
          @log "item.dash_to_tie case, item is",item
          orig=item.pitch_to_use_for_tie
          while orig.pitch_to_use_for_tie
             orig=orig.pitch_to_use_for_tie
          #orig = orig.pitch_to_use_for_tie if orig.pitch_to_use_for_tie
          @log("dash_to_tie, orig is",orig)
          obj={
             source:orig.source
             normalized_pitch:orig.normalized_pitch
             octave:orig.octave
             numerator:item.numerator
             denominator:item.denominator
             tied:item.tied
          }
  
          @log("dash_to_tie case")
          ary.push normalized_pitch_to_lilypond(obj)
    if in_times
      ary.push "}"
      in_times=false
    emit_tied_array(last_pitch,tied_array,ary) if tied_array.length >0 
    ary.push "\\break\n"
  mode="major"
  my_mode=get_attribute(composition_data,'Mode')
  mode = my_mode if my_mode
  # TODO: dry
  composer = get_attribute(composition_data,"Author")
  composer_snippet=""
  if composer
    composer_snippet= """
      composer = "#{composer}"
     """

  title = get_attribute(composition_data,"Title")
  time = get_attribute(composition_data,"Time Signature")
  if (key_is_valid=is_valid_key(composition_data.key))
    transpose_snip="\\transpose c' #{composition_data.key}'" 
  else
    transpose_snip=""
    if composition_data.key?
      @log("#{composition_data.key} is invalid")
      composition_data.warnings.push "Invalid key. Valid keys are cdefgab etc. Use a Mode: directive to set the mode(major,minor,aeolian, etc). See the lilypond documentation for more info"
  # Don't transpose non-sargam notation TODO:review
  if ! notation_is_in_sargam(composition_data)
    transpose_snip=""
  time="4/4" if !time
  key_snippet= """
  \\key c \\#{mode}
  """
  if ! notation_is_in_sargam(composition_data) and key_is_valid
    key_snippet= """
    \\key #{composition_data.key} \\#{mode}
    """
  
  title_snippet=""
  if title
    title_snippet= """
      title = "#{title}"
     """
  notes = ary.join " "
  # Anything that is enclosed in %{ and %} is ignored  by lilypond
  composition_data.source="" if !composition_data.source?
  src1= composition_data.source.replace /%\{/gi, "% {"
  src= src1.replace /\{%/gi, "% }"
  lilypond_template= """
  #(ly:set-option 'midi-extension "mid")
  \\version "2.12.3"
  \\include "english.ly"
  \\header{ #{title_snippet} #{composer_snippet} }
  \\include "english.ly"
%{
#{src}  
%}
melody = {
\\clef treble
#{key_snippet}
\\time #{time}
\\autoBeamOn  
#{notes}
}

text = \\lyricmode {
  #{extract_lyrics(composition_data).join(' ')}
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
  lilypond_template

all_items_in_line= (line_or_item,items=[]) ->
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
      items.concat all_items_in_line(an_item,items)
  @log 'all_items_in_line returns', items
  return [line_or_item].concat(items)

root.to_lilypond=to_lilypond
