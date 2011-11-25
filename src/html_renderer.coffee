# Uses module pattern, exports to_html 
# Usage:
# to_html(composition)

root = exports ? this

#"Global" to this module
@id_ctr=new Date().getTime()

last_slur_id=-1


lookup_html_entity = (str) ->
  # The user enters the source using ascii characters. Map
  # these to html entities
  LOOKUP=
    "b"    : "&#9837;" # flat  sign
    "#"    : "&#9839;" # sharp sign
    "."    : "&bull;"
    "*"    : "&bull;"
    "|:"  : "&#x1d106"
  # U+1D19D for the half Pralltriller, and adding U+1D1A0
    "~"   : "&#x1D19D&#x1D19D" #mordent
    ":|"  : "&#x1d107"
    "|"  : "&#x1d100"
    "||"  : "&#x1d101" #double barline
    "%"  : "&#x1d10E"
    "|]"  : "&#x1d102" # final barline
    "[|"  : "&#x1d103" # reverse final barline
  LOOKUP[str]

draw_line= (line) ->
  x=(draw_item(item) for item in line.items).join('')
  "<div class='stave sargam_line'>#{x}</div>"

draw_measure= (measure) ->
  (draw_item(item) for item in measure.items).join('')

draw_upper_sym = (item) ->
  return "" if !item.octave?
  bull=lookup_html_entity(".")
  return "" if  item.octave < 1
  return "" if  item.octave > 2
  upper_sym=bull if  item.octave == 1
  upper_sym=":" if  item.octave == 2
  """
     <span class="upper_octave1 upper_octave_indicator">#{upper_sym}</span>
  """

draw_syllable = (item) ->
  return '' if !item.syllable?
  return '' if item.syllable is ''
  """
     <span class="syllable1">#{item.syllable}</span>
  """

draw_lower_sym = (item) ->
  return "" if !item.octave?
  return "" if  item.octave > -1
  return "" if  item.octave < -2
  bull=lookup_html_entity(".")
  lower_sym=bull if  item.octave == -1
  lower_sym=":" if  item.octave == -2
  """
  <span class="lower_octave1">#{lower_sym}</span>
  """


class_for_octave = (octave_num) ->
  return "octave0" if !octave_num?
  if octave_num < 0
     return "lower_octave_#{octave_num*-1}"
  if octave_num > 0
     return "upper_octave_#{octave_num}"
  "octave0"

draw_ornament_item= (item) ->
  """
  <span class="ornament_item #{class_for_octave(item.octave)}">#{item.source}</span>
  """


draw_ornament= (ornament) ->
  x=(draw_ornament_item(orn_item) for orn_item in ornament.ornament_items).join('')
  @id_ctr++
  """
  <span id="#{@id_ctr}" class="upper_attribute ornament placement_#{ornament.placement}">#{x}</span>
  """

draw_pitch= (pitch) ->
  source2 = lookup_html_entity(pitch.source)
  my_source=if source2? then source2 else pitch.source
  my_source=(Array(pitch.source.length+1).join "&nbsp;") if pitch.my_type=="whitespace"
  pitch_sign="" # flat,sharp,etc
  title=""
  # refactor to draw_pitch?
  title="#{pitch.numerator}/#{pitch.denominator} of a beat"
  my_source=pitch.pitch_source
  # TODO: make less hackish
  if (my_source[1] is "#")
    my_source=my_source[0]
    pitch_sign="<span class='pitch_sign sharp'>#{lookup_html_entity('#')}</span>"
  if (my_source[1] is "b")
    my_source=my_source[0]
    pitch_sign="<span class='pitch_sign flat'>#{lookup_html_entity('b')}</span>"
  upper_sym_html= draw_upper_sym(pitch)
  lower_sym_html=draw_lower_sym(pitch)
  syl_html=draw_syllable(pitch)
  upper_attributes_html=""
  data1=""
  # TODO: refactor
  if pitch.attributes
    upper_attributes_html=(for attribute in pitch.attributes
      do (attribute) =>
        return "" if attribute.my_type=="upper_octave_indicator"
        if (attribute.my_type=="begin_slur")
          id_ctr++
          last_slur_id=id_ctr
          return """
          <span id="#{id_ctr}" class="slur">&nbsp;&nbsp;</span>
          """
        if (attribute.my_type is "end_slur")
          data1="data-begin-slur-id='#{last_slur_id}'"
          return ""
        if (attribute.my_type is "ornament")
          return draw_ornament(attribute)
        my_item=attribute
        my_source2 = lookup_html_entity(my_item.source) # TODO:
        my_source2=my_item.source if !my_source2
        """
        <span class="upper_attribute #{my_item.my_type}">#{my_source2}</span>
        """).join('')
  """
  <span title="#{title}" class="note_wrapper" #{data1}>#{upper_attributes_html}#{upper_sym_html}#{lower_sym_html}#{syl_html}<span class="note #{pitch.my_type}" tabindex="0">#{my_source}</span>#{pitch_sign}</span>
  """

draw_item= (item) ->
  return draw_pitch(item) if item.my_type is "pitch"
  return "" if item.my_type is "begin_beat"
  return "" if item.my_type is "end_beat"
  return draw_beat(item) if item.my_type=="beat"
  return draw_measure(item) if item.my_type=="measure"
  # TODO: clumsy and hard to understand. source, pitch_source
  source2 = lookup_html_entity(item.source)
  my_source=if source2? then source2 else item.source
  my_source=(Array(item.source.length+1).join "&nbsp;") if item.my_type=="whitespace"
  pitch_sign="" # flat,sharp,etc
  title=""
  upper_sym_html= draw_upper_sym(item)
  lower_sym_html=draw_lower_sym(item)
  syl_html=draw_syllable(item)
  upper_attributes_html=""
  data1=""
  # TODO: refactor
  if item.attributes
    upper_attributes_html=(for attribute in item.attributes
      do (attribute) =>
        return "" if attribute.my_type=="upper_octave_indicator"
        if (attribute.my_type=="begin_slur")
          id_ctr++
          last_slur_id=id_ctr
          return """
          <span id="#{id_ctr}" class="slur">&nbsp;&nbsp;</span>
          """
        if (attribute.my_type is "end_slur")
          data1="data-begin-slur-id='#{last_slur_id}'"
          return ""
        if (attribute.my_type is "ornament")
          return draw_ornament(attribute)
        my_item=attribute
        my_source2 = lookup_html_entity(my_item.source) # TODO:
        my_source2=my_item.source if !my_source2
        """
        <span class="upper_attribute #{my_item.my_type}">#{my_source2}</span>
        """).join('')
  # TODO: make more elegant by not including empty tags
  # hacked here
  """
  <span title="#{title}" class="note_wrapper" #{data1}>#{upper_attributes_html}#{upper_sym_html}#{lower_sym_html}#{syl_html}<span class="note #{item.my_type}" tabindex="0">#{my_source}</span>#{pitch_sign}</span>
  """

draw_beat= (beat) ->
  x=(draw_item(item) for item in beat.items).join('')
  extra=""
  extra= "data-subdivisions=#{beat.subdivisions}" if beat.subdivisions > 1
  """
  <span #{extra} class='beat'>#{x}</span>
  """


to_html_doc= (composition,full_url="http://ragapedia.com",css="",js="") ->
  # create standalone html document with all the css and javascript inline.
  # The created document doesn't need to go to the server
  # css - contents of application.js
  # js - contents of all javascript needed
  #
  rendered_composition=to_html(composition)
  """
  <html>
    <head>
    <style type="text/css">
      #{css}
    </style>
      <title>#{composition.title}</title>
      <!--
      <link media="all" type="text/css" href="#{full_url}/css/application.css" rel="stylesheet">
       -->
      <meta content="text/html;charset=utf-8" http-equiv="Content-Type">
    </head>
  <body>
    <div id="rendered_sargam">
      #{rendered_composition}
    </div>
  <script type="text/javascript">
  #{js}
  $(document).ready(function() {
      return adjust_slurs_in_dom()
  })
  </script>
  </body>
  </html>
  """


draw_attributes = (attributes)->
  return "" if !attributes?
  attrs=(for attribute in attributes.items
      """
      <div class="attribute"><span class="attribute_key">#{attribute.key}
      </span>:<span class="attribute_value">#{attribute.value}
      </span></div>
      """
      ).join('\n')
  "<div class='attribute_section'>#{attrs}</div>"

to_html= (composition) ->
  # returns the text of an html div rendering the composition.
  attrs=draw_attributes(composition.attributes)
  lines=(draw_line(item) for item in composition.lines).join('\n')
  "<div class='composition'>#{attrs}#{lines}</div>"

root.to_html=to_html
root.to_html_doc=to_html_doc
