# Uses module pattern, exports to_lilypond
# Usage:
# to_lilypond(composition_json_data)


  # TODO: dashes at beginning of measure need to be rendered as 
  # rests in lilypond!!
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
debug=true

root = exports ? this
if require?
  shared=require('./shared.js')
  root._ = require("underscore")._
  root._.extend(root,shared)


extract_lyrics= (composition_data) ->
  ary=[]
  for sargam_line in composition_data.lines
    for item in root.all_items(sargam_line,[])
      @log "extract_lyrics-item is",item
      ary.push item.syllable if item.syllable
  ary

get_attribute= (composition_data,key) ->
  return null if !composition_data.attributes
  att=root._.detect(composition_data.attributes.items, (item) ->
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
    console.log(util.inspect(obj,false,null)) 
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

get_ornament = (pitch) ->
  return false if !pitch.attributes?
  root._.detect(pitch.attributes, (attribute) -> attribute.my_type is "ornament")
  
has_mordent = (pitch) ->
  return false if !pitch.attributes?
  root._.detect(pitch.attributes, (attribute) -> attribute.my_type is "mordent")

lookup_lilypond_pitch= (pitch) ->
  lilypond_pitch_map[pitch.normalized_pitch]

lilypond_grace_note_pitch = (pitch) ->
  # generate a single pitch for use as a grace note
  duration="32"
  lilypond_pitch=lookup_lilypond_pitch(pitch)
  lilypond_octave=lilypond_octave_map["#{pitch.octave}"]
  return "???#{pitch.octave}" if !lilypond_octave?
  "#{lilypond_pitch}#{lilypond_octave}#{duration}"

lilypond_grace_notes = (ornament) ->
  # generate a series of grace notes for an ornament
  #  c1 \afterGrace d1( { c16[ d]) } c1
  #  In the above line, generate what is between {}
  ary=(lilypond_grace_note_pitch(pitch) for pitch in ornament.ornament_items)
  
  needs_beam = (ary.length > 1)
  begin_beam=end_beam=""
  begin_slur="("
  begin_slur=""
  end_slur=")"
  if needs_beam
    begin_beam="["
    end_beam="]"
  ary[0]= "#{ary[0]}#{begin_slur}#{begin_beam}" 
  length=ary.length
  ary[length-1]="#{ary[length-1]}#{end_beam}" 
  # TODO: end slur??????????
  ary.join ' '

get_chord= (item) ->
  if e =root._.detect(item.attributes, (x) -> x.my_type is "chord_symbol")
    return """
    ^"#{e.source}"
    """
  ""

get_ending= (item) ->
  if e =root._.detect(item.attributes, (x) -> x.my_type is "ending")
    return """
    ^"#{e.source}"
    """
  ""

normalized_pitch_to_lilypond= (pitch) ->
  # Render a pitch/dash as lilypond
  # needs work
  chord=get_chord(pitch)
  ending=get_ending(pitch)
  if pitch.fraction_array?
    first_fraction=pitch.fraction_array[0]
  else
    first_fraction=new Fraction(pitch.numerator,pitch.denominator)
  duration=calculate_lilypond_duration first_fraction.numerator.toString(),first_fraction.denominator.toString()
  @log("normalized_pitch_to_lilypond, pitch is",pitch)
  if pitch.my_type is "dash"
    # unfortunately this is resulting in tied 1/4s.
    if pitch.dash_to_tie is true
      pitch.normalized_pitch=pitch.pitch_to_use_for_tie.normalized_pitch
      pitch.octave=pitch.pitch_to_use_for_tie.octave
    else
      return "r#{duration}#{chord}#{ending}"
  lilypond_pitch=lilypond_pitch_map[pitch.normalized_pitch]
  return "???#{pitch.source}" if  !lilypond_pitch?
  lilypond_octave=lilypond_octave_map["#{pitch.octave}"]
  return "???#{pitch.octave}" if !lilypond_octave?
  # Lower markings would be added as follows:
  # "-\"#{pp}\""
  mordent = if has_mordent(pitch) then "\\mordent" else ""
  begin_slur = if item_has_attribute(pitch,"begin_slur") then "("  else ""
  end_slur  =  if item_has_attribute(pitch,"end_slur") then ")" else ""
  lilypond_symbol_for_tie=  if pitch.tied? then '~' else ''
  #If you want to end a note with a grace, 
  # use the \afterGrace command. It takes two 
  # arguments: the main note, and the 
  # grace notes following the main note.
  #
  #  c1 \afterGrace d1( { c32[ d]) } c1
  #
  #  Use
  #  \acciaccatura { e16 d16 } c4
  #  for ornaments with ornament.placement is "before"


  # The afterGrace in lilypond require parens to get lilypond
  # to render a slur.
  # The acciatura in lilypond don't require parens to get lilypond
  # to render a slur.
  ornament=get_ornament(pitch)
  grace1=grace2=grace_notes=""
  if ornament?.placement is "after"
    grace1 = "\\afterGrace "
    grace2="( { #{lilypond_grace_notes(ornament)}) }"
  if ornament?.placement is "before"
  #  \acciaccatura { e16 d16 } c4
    grace1= "\\acciaccatura {#{lilypond_grace_notes(ornament)}}"
  "#{grace1}#{lilypond_pitch}#{lilypond_octave}#{duration}#{lilypond_symbol_for_tie}#{mordent}#{begin_slur}#{end_slur}#{ending}#{chord}#{grace2}"


lookup_lilypond_barline= (barline_type) ->
  # maps my_type field for barlines
  map=
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
  map[barline_type] or map["single_barline"]

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
  "Fb":"ff"
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
  root._.detect(composition_data.lines, (line) -> is_sargam_line(line))

beat_is_all_dashes= (beat) ->
  fun = (item) ->
    return true if !item.my_type?
    return true if item.my_type is "dash"
    return false if item.my_type is "pitch"
    return true
  root.all_items(beat).every(fun)
 
lilypond_transpose=(composition) ->
  return "" if composition_data.key is "C"
  fixed=composition_data.key[0].toLowerCase()
  return "\\transpose c' #{lilypond_pitch_map[composition.key]}'"

to_lilypond= (composition_data,options={}) ->
  ary=[]
  in_times=false #hack
  at_beginning_of_first_measure_of_line=false
  dashes_at_beginning_of_line_array=[]
  tied_array=[]

  for line in composition_data.lines
    at_beginning_of_first_measure_of_line=false
    in_times=false #hack
    @log "processing #{line.source}"
    all=[]
    x=root.all_items(line,all)
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
        if dashes_at_beginning_of_line_array.length > 0
          for dash in dashes_at_beginning_of_line_array
            ary.push normalized_pitch_to_lilypond(dash)
          dashes_at_beginning_of_line_array=[]
        ary.push normalized_pitch_to_lilypond(item) 
      if item.is_barline
        ary.push(lookup_lilypond_barline(item.my_type))
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
        if item.dash_to_tie
          #TODO:review

          ary.push normalized_pitch_to_lilypond(item)
          item=null
      if item? and item.my_type is "measure"
         measure=item
         if measure.is_partial
            ary.push "\\partial 4*#{measure.beat_count} "
      if item? and item.dash_to_tie
        tied_array.push item if item?
    if in_times
      ary.push "}"
      in_times=false
    emit_tied_array(last_pitch,tied_array,ary) if tied_array.length >0 
    ary.push "\\break\n"
  mode = get_mode(composition_data,'Mode')
  mode or= "major"
  mode=mode.toLowerCase()
  composer = get_attribute(composition_data,"Author")
  composer_snippet=""
  if composer
    composer_snippet= """
      composer = "#{composer}"
     """

  title = get_attribute(composition_data,"Title")
  time = get_attribute(composition_data,"TimeSignature")
  transpose_snip=lilypond_transpose(composition_data)
  # Don't transpose non-sargam notation TODO:review
  if ! notation_is_in_sargam(composition_data)
    transpose_snip=""
  time="4/4" if !time
  key_snippet= """
  \\key c \\#{mode}
  """
  if ! notation_is_in_sargam(composition_data)
    key_snippet= """
    \\key #{lilypond_pitch_map[composition_data.key]} \\#{mode}
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

  if options.omit_header
    title_snippet=composer_snippet=""

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


root.to_lilypond=to_lilypond
