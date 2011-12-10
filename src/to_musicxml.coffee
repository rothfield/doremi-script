# Uses module pattern, exports to_lilypond
# Usage:
# to_musicxml(composition_json_data)

fs= require 'fs' if require?
templates={}
_.templateSettings = {
          interpolate : /\{\{(.+?)\}\}/g
}
if require?
  templates.composition = _.template(fs.readFileSync(__dirname + '/composition.mustache', 'UTF-8'))

debug=true

root = exports ? this

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
    console.log(util.inspect(obj,false,null)) 
    return
  console.log obj



fraction_to_musicxml_type_and_dots = 
  "2/1":"<type>half</type>"
  "3/1":"<type>half</type><dot/>"
  "4/1":"<type>whole</type>"
  "5/1":"<type>whole</type><dot/><dot/>"
  "1/1":"<type>quarter</type>"
  "1/2":"<type>eighth</type>"
  "1/3": "<type>eighth</type>"  # 1/3 1/5 1/7 all 8th notes so one beat will beam together
  "1/4": "<type>16th</type>"
  "1/8": "<type>32nd</type>"
  "1/9":"<type>eighth</type>"
  "1/11":"<type>eighth</type>"
  "1/13":"<type>eighth</type>"
  "1/5": "<type>16th</type>"
  "2/5":"<type>eighth</type>"
  "3/5":"<type>eighth</type><dot/>" #TODO should be tied
  "4/5":"<type>quarter</type>" #TODO should be tied
  "5/5":"<type>quarter</type>"
  "6/6":"<type>quarter</type>"
  "7/7":"<type>quarter</type>"
  "8/8":"<type>quarter</type>"
  "9/9":"<type>quarter</type>"
  "10/10":"<type>quarter</type>"
  "11/11":"<type>quarter</type>"
  "12/12":"<type>quarter</type>"
  "13/13":"<type>quarter</type>"
  "1/7": "<type>thirtysecond</type>" # ??? correct???hhhhhhhhhh
  "2/7": "<type>16th</type>" # ??? correct???hhhhhhhhhh
  "3/7": "<type>16th</type><dot/>" # ??? correct???hhhhhhhhhh
  "4/7": "<type>eighth</type>" # ??? correct???hhhhhhhhhh
  "5/7": "<type>eighth</type><dot/><dot/>" # ??? correct???hhhhhhhhhh
  "2/8": "<type>16th</type>"
  "3/8": "<type>16th</type><dot/>"  # 1/4 + 1/8
  "5/8": "<type>eighth</type>"   # TODO: WRONG
  "4/8": "<type>eighth</type>"
  "7/8": "<type>eighth</type><dot/><dot/>" # 1/2 + 1/4 + 1/8
  "1/6": "<type>16th</type>"
  "2/6": "<type>eighth</type>"
  "3/6": "<type>quarter</type>" # not sure??
  "4/6":"<type>quarter</type>" # NOT SURE ????
  "5/6":"<type>eighth</type><dot/><dot/>" #  WRONGnot sure TODO??
  "2/2":"<type>quarter</type>"
  "3/3":"<type>quarter</type>"
  "4/4":"<type>quarter</type>"
  "8/8":"<type>quarter</type>"
  "1/4":"<type>16th</type>"
  "2/4":"<type>eighth</type>"
  "3/4":"<type>eighth</type><dot/>"
  "3/8":"<type>16th</type><dot/>"

get_ornament = (pitch) ->
  return false if !pitch.attributes?
  _.detect(pitch.attributes, (attribute) -> attribute.my_type is "ornament")
  
has_mordent = (pitch) ->
  return false if !pitch.attributes?
  _.detect(pitch.attributes, (attribute) -> attribute.my_type is "mordent")

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
  if e =_.detect(item.attributes, (x) -> x.my_type is "chord_symbol")
    return """
    ^"#{e.source}"
    """
  ""

get_ending= (item) ->
  if e =_.detect(item.attributes, (x) -> x.my_type is "ending")
    return """
    ^"#{e.source}"
    """
  ""

normalized_pitch_to_musicxml_step = (normalized_pitch) ->

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
  """
  """



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
  _.detect(composition_data.lines, (line) -> is_sargam_line(line))

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


beat_is_all_dashes= (beat) ->
  fun = (item) ->
    return true if !item.my_type?
    return true if item.my_type is "dash"
    return false if item.my_type is "pitch"
    return true
  all_items_in_line(beat).every(fun)
  
to_musicxml= (composition_data) ->
  context=
    in_slur:false
    slur_number:0
    measure_number:2
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
    x=all_items_in_line(line,all)
    @log("in to_lilypond, all_items_in_line x=",x)
    last_pitch=null
    for item in all
      if item.my_type is "measure"
        ary.push draw_measure(item,context)
        context.measure_number++
  mode = get_attribute(composition_data,'Mode')
  mode or= "major"
  composer = get_attribute(composition_data,"Author")
  composer_snippet=""
  if composer
    composer_snippet= """
      composer = "#{composer}"
     """

  title = get_attribute(composition_data,"Title")
  time = get_attribute(composition_data,"TimeSignature")
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
  params=
    body:ary.join(" ")
    movement_title:title
    title:title
    composer:""
    poet:""
    encoding_date:""
    mode:mode
  templates.composition(params)
  return templates.composition(params)

"""
            <note default-y="-30.00" default-x="108.08">
                <pitch>
                    <step>G</step>
                    <octave>4</octave>
                </pitch>
                <duration>2</duration>
                <voice>1</voice>
                <type>eighth</type>
                <stem>up</stem>
                <beam number="1">begin</beam>
                <lyric number="1">
                    <syllabic>begin</syllabic>
                    <text>Yes</text>
                </lyric>
                <lyric number="2">
                    <syllabic>begin</syllabic>
                    <text>Sud</text>
                </lyric>
            </note>
"""
note_template_str='''
            {{before_ornaments}}
            <note>
              <pitch>
                <step>{{step}}</step>
                {{alter}}
                <octave>{{octave}}</octave>
              </pitch>
              <duration>{{duration}}</duration>
              {{tie}}
              <voice>1</voice>
              {{type_and_dots}}
              {{lyric}}
              <notations>{{tied}}{{ornament_before_slur_end}}
              {{begin_slur}}{{end_slur}}{{ornament_after_slur_begin}}</notations>
             </note>
             {{after_ornaments}}
'''
templates.note = _.template(note_template_str)


x="""

divisions set to 96 per note

if our note is S-R-

then sa has fraction 2/4

2/4 * 1/4 = 2/16th, an eighth note

but divisions is 96 so multiply by

2/16 = x/96

x= 2/16 *96


1/2 of a  1/4 is 1/8   1/8=x/24 =3

example- 2/4

2/4 * 1/4 *24 = 2/4 * 6 = 3
                <lyric number="1">
                    <syllabic>begin</syllabic>
                    <text>Yes</text>
                </lyric>

"""

draw_note = (pitch,context) ->
  console.log "Entering draw_note, pitch is #{pitch}" if !running_under_node()
  if pitch.my_type is "dash"
    return "" if !pitch.pitch_to_use_for_tie?
  [before_ornaments,after_ornaments]=draw_ornaments(pitch,context)
  ornament_before_slur_end=""
  if (orn = get_ornament(pitch)) and orn.placement is "before"
    ornament_before_slur_end="""
          <slur number="#{context.slur_number}" type="stop"/>
    """
  if (orn = get_ornament(pitch)) and orn.placement is "after"
    ornament_after_slur_begin="""
          <slur number="#{context.slur_number}" type="start"/>
    """
  if pitch.dash_to_tie? and pitch.dash_to_tie is true
    pitch.normalized_pitch=pitch.pitch_to_use_for_tie.normalized_pitch
    pitch.octave=pitch.pitch_to_use_for_tie.octave
  if pitch.my_type is "dash"
    return if pitch.dash_to_tie? and pitch.dash_to_tie is false
  console.log "" if !running_under_node()
  #if pitch.fraction_total?
  #  fraction=new Fraction(pitch.fraction_total.numerator,pitch.fraction_total.denominator)
  #else
  fraction=new Fraction(pitch.numerator,pitch.denominator)

  divisions_per_quarter=24
  frac2=fraction.multiply(divisions_per_quarter)
  duration=frac2.numerator
  console.log "frac2 is",frac2 if !running_under_node()
  if pitch.denominator not in [0,1,2,4,8,16,32,64,128] 
     x=2
     if pitch.denominator is 6
       x=4
     if  pitch.denominator is 5
       x=4
       #ary.push "\\times #{x}/#{beat.subdivisions} { "
       #in_times=true #hack
     duration=divisions_per_quarter/x
  if pitch.fraction_array?
    f=pitch.fraction_array[0]
  else
    f=pitch
  console.log "numerator,denominator",f.numerator,f.denominator if !running_under_node()

  type_and_dots= musicxml_type_and_dots(f.numerator,f.denominator)
  tie=""
  tied=""
  if pitch.tied?
    tie="""
    <tie type="start"/>
    """
    tied="""
    <tied type="start"/>
    """
  if pitch.my_type is "dash" and pitch.dash_to_tie is true
    tied2="""
    <tied type="end"/>
    """
    tied=tied+tied2
  lyric=""
  if pitch.syllable?
    lyric="""
     <lyric number="1">
       <text>#{pitch.syllable}</text>
     </lyric>
    """
  begin_slur = end_slur =""
  if item_has_attribute(pitch,"end_slur")
    end_slur="""
<slur number="#{context.slur_number-1}" type="stop"/>
    """
    context.in_slur=false

  if item_has_attribute(pitch,"begin_slur") 
    begin_slur="""
<slur number="#{++context.slur_number}" type="start"/>
    """
    context.in_slur=true
  params=
    step: musicxml_step(pitch)
    octave:musicxml_octave(pitch)
    duration:duration
    alter:musicxml_alter(pitch)
    type_and_dots:type_and_dots
    tied:tied
    tie:tie
    lyric:lyric
    begin_slur:begin_slur
    end_slur:end_slur
    before_ornaments:before_ornaments
    after_ornaments:after_ornaments
    ornament_before_slur_end: ornament_before_slur_end
    ornament_after_slur_begin:ornament_after_slur_begin
  templates.note(params)

musicxml_type_and_dots= (numerator,denominator) ->
  console.log "musicxml_type_and_dots(#{numerator},#{denominator}" if !running_under_node()
  if numerator is denominator
    return "<type>quarter</type>"
  frac="#{numerator}/#{denominator}"
  looked_up_duration=fraction_to_musicxml_type_and_dots[frac]
  if !looked_up_duration?
    alternate= "<type>16th</type>"
    return alternate # return something
  looked_up_duration
   
# for grace notes before the main note
grace_note_template_str =  """
      <note>
        <grace {{steal_time}} />
        <pitch>
          <step>{{step}}</step>
          <octave>{{octave}}</octave>
        </pitch>
        <voice>1</voice>
        <type>{{type}}</type>
        {{beaming_begin}}
        {{beaming_end}}
        <notations>{{slur_start}}{{slur_end}}</notations>
      </note>
  """

templates.grace_note = _.template(grace_note_template_str)

grace_note_after_template_str =  """
        <grace steal-time-previous="{{steal_time_percentage}}"/>
        <pitch>
          <step>{{step}}</step>
          <octave>{{octave}}</octave>
        </pitch>
        <voice>1</voice>
        <type>{{type}}</type>
      </note>
"""

templates.grace_note_after = _.template(grace_note_after_template_str)

draw_grace_note = (ornament_item,which,len,steal_time="",placement,context) ->
  beaming_begin=beaming_end=""
  if which is 0
    beaming_begin="""
        <beam number="1">begin</beam>
        <beam number="2">begin</beam>
    """
  if which is 0 and placement is "before" and context.in_slur is false
    slur_start="""
    <slur number="#{++context.slur_number}" type="start"/>
    """
  if which is (len-1) and placement is "after" and context.in_slur is false
    slur_end="""
    <slur number="#{context.slur_number}" type="stop"/>
    """
  if which is (len-1) 
    beaming_end="""
        <beam number="1">end</beam>
        <beam number="2">end</beam>
    """
  params=
    step:musicxml_step(ornament_item)
    octave:musicxml_octave(ornament_item)
    type:"<span>32nd</span>"
    beaming_begin:beaming_begin
    beaming_end:beaming_end
    slur_start:slur_start
    slur_end:slur_end
    steal_time:steal_time
  templates.grace_note(params)

draw_ornaments = (pitch,context) ->
  console.log "Entering draw_ornaments",pitch if !running_under_node()
  before_ary=[]
  ornament=get_ornament(pitch)
  return ['',''] if !ornament
  if ornament.placement is "before"
    len=ornament.ornament_items.length
    steal_time=""
    before_ary=(draw_grace_note(x,ctr,len,steal_time,ornament.placement,context) for x,ctr in ornament.ornament_items)
    return [before_ary.join("/n"),""]
  if ornament.placement is "after"
    len=ornament.ornament_items.length
    num=50/len
    steal_time="""
      steal-time-previous="#{num}"
    """
    after_ary=(draw_grace_note(x,ctr,len,steal_time,ornament.placement,context) for x,ctr in ornament.ornament_items)
    return ["",after_ary.join('')]
  ["",""]

musicxml_step = (pitch) ->
  pitch.normalized_pitch[0]

musicxml_alter = (pitch) ->
  alt="" 
  if pitch.normalized_pitch.indexOf('#') > -1
    alt="1"
  else if pitch.normalized_pitch.indexOf('b') > -1
    alt="-1"
  else
    return ""
  "<alter>#{alt}</alter>"

    

musicxml_octave = (pitch) ->
  pitch.octave + 4
musicxml_duration = (pitch) ->
  1 # TODO  


draw_measure= (measure,context) ->
  ary=[]
  for item in all_items(measure)
    #console.log "item"
    ary.push(draw_note(item,context)) if item.my_type is "pitch"
    ary.push(draw_note(item,context)) if item.my_type is "dash"
  #console.log "ary is",ary
  # 1st measure gets combined with clef
  measure="""
  <measure number="#{context.measure_number}">
  """
  measure="" if context.measure_number is 2
  """
    #{measure}
    #{ary.join(' ')}
</measure>
  """

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

to_musicxml.templates=templates

root.to_musicxml=to_musicxml
