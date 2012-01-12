# Uses module pattern, exports to_html 
# Usage:
# to_html(composition)

root = exports ? this

id_ctr=new Date().getTime() - 1326011100000

last_slur_id=-1
lookup_simple = (str) ->
  # for displaying characters where there is not font support 
  # in the browser
  # The user enters the source using ascii characters. Map
  # these to html entities
  LOOKUP=
    "b"    : "b" # flat  sign
    "#"    : "#" # sharp sign
    "."    : "&bull;"
    "*"    : "&bull;"
    "|:"  : "|:"
    "~"   : "~" #mordent
    ":|"  : ":|"
    "|"  : "|"
    "||"  : "||" #double barline- TODO: use css to bring them closer
    "%"  : "%"
    "|]"  : "|" # final barline
    "[|"  : "|" # reverse final barline
  LOOKUP[str]


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

draw_upper_octave_symbol = (item) ->
  return "" if !item.octave?
  return "" if item.octave is 0
  bull=lookup_html_entity(".")
  return "" if  item.octave < 1
  return "" if  item.octave > 2
  upper_sym=bull if  item.octave == 1
  upper_sym=":" if  item.octave == 2
  """
     <span class="#{class_for_octave(item.octave)} upper_octave_indicator">#{upper_sym}</span>
  """

draw_syllable = (item) ->
  return '' if !item.syllable?
  return '' if item.syllable is ''
  """
     <span class="syllable">#{item.syllable}</span>
  """

draw_lower_octave_symbol = (item) ->
  return "" if !item.octave?
  return "" if item.octave is 0
  return "" if  item.octave > -1
  return "" if  item.octave < -2
  bull=lookup_html_entity(".")
  lower_sym=bull if  item.octave == -1
  lower_sym=":" if  item.octave == -2
  """
  <span class="#{class_for_octave(item.octave)}">#{lower_sym}</span>
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
  id_ctr++
  """
  <span id="#{id_ctr}" class="upper_attribute ornament placement_#{ornament.placement}">#{x}</span>
  """

draw_pitch_sign = (my_source) ->
  snip=""
  snip= "" if my_source.length is 1
  if (my_source[1] is "#")
    simple=lookup_simple("#")
    my_source=my_source[0]
    snip="<span data-fallback-if-no-utf8-chars='#{simple}' class='pitch_sign sharp'>#{lookup_html_entity('#')}</span>"
  if (my_source[1] is "b")
    my_source=my_source[0]
    simple=lookup_simple("b")
    snip= "<span data-fallback-if-no-utf8-chars='#{simple}' class='pitch_sign flat'>#{lookup_html_entity('b')}</span>"
  [snip,my_source]

draw_pitch= (pitch) ->
  my_source=pitch.source
  my_source=pitch.pitch_source if pitch.pitch_source?
  title=""
  title="#{pitch.numerator}/#{pitch.denominator} of a beat" if pitch.numerator?
  [pitch_sign, my_source] =draw_pitch_sign(my_source)
  has_pitch_sign = (if pitch_sign isnt '' then  "has_pitch_sign" else "")
  upper_octave_symbol_html= draw_upper_octave_symbol(pitch)
  lower_octave_symbol_html=draw_lower_octave_symbol(pitch)
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
        data=""
        simple=lookup_simple(my_item.source)
        if my_source2?
          data="data-fallback-if-no-utf8-chars='#{simple}'"
        my_source2=my_item.source if !my_source2
        """
        <span #{data} class="upper_attribute #{my_item.my_type}">#{my_source2}</span>
        """).join('')
  """
  <span title="#{title}" class="note_wrapper" #{data1}>#{upper_attributes_html}#{upper_octave_symbol_html}#{lower_octave_symbol_html}#{syl_html}#{pitch_sign}<span class="note #{has_pitch_sign} #{pitch.my_type}">#{my_source}</span></span>
  """


draw_item= (item) ->
  return draw_pitch(item) if item.my_type is "pitch"
  return "" if item.my_type is "begin_beat"
  return "" if item.my_type is "end_beat"
  return draw_beat(item) if item.my_type is "beat"
  return draw_measure(item) if item.my_type is "measure"
  my_source=""
  source2 = lookup_html_entity(item.source)
  fallback=""
  if source2?
    simple=lookup_simple(item.source)
    fallback="data-fallback-if-no-utf8-chars='#{simple}'"
  my_source=if source2? then source2 else item.source
  my_source=(Array(item.source.length+1).join "&nbsp;") if item.my_type=="whitespace"
  my_source="" if !my_source?
  title=""
  upper_attributes_html=""
  data1=""
  # TODO: refactor
  if item.attributes
    upper_attributes_html=(for attribute in item.attributes
      do (attribute) =>
        return "" if attribute.my_type=="upper_octave_indicator"
        if (attribute.my_type=="begin_slur")
          console.log "begin slur"
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
        data=""
        simple=lookup_simple(my_item.source)
        my_source2 = lookup_html_entity(my_item.source) # TODO:
        if simple?
          data="data-fallback-if-no-utf8-chars='#{simple}'"
        my_source2=my_item.source if !my_source2
        """
        <span #{data} class="upper_attribute #{my_item.my_type}">#{my_source2}</span>
        """).join('')
  """
  <span title="#{title}" class="note_wrapper" #{data1}>#{upper_attributes_html}<span #{fallback} class="note #{item.my_type}" >#{my_source}</span></span>
  """

draw_beat= (beat) ->
  looped_class= if beat.subdivisions > 1 then "looped" else ""
  x=(draw_item(item) for item in beat.items).join('')
  extra=""
  extra= "data-subdivisions=#{beat.subdivisions} " if beat.subdivisions > 1
  """
  <span #{extra}class='beat #{looped_class}'>#{x}</span>
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
  <span id="testing_utf_support" style="display:none" class="note left_repeat">&#x1d106;</span>
  <script type="text/javascript">
  #{js}
  $(document).ready(function() {
      return dom_fixes()
  })
  </script>
<script id="source" type="text/html">
#{composition.source}
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


line_to_html= (line) ->
  # returns the text of an html div rendering the line
  draw_line(line)
  #"<div class='composition'>#{line}</div>"

to_html= (composition) ->
  # returns the text of an html div rendering the composition.
  attrs=draw_attributes(composition.attributes)
  lines=(draw_line(item) for item in composition.lines).join('\n')
  "<div class='composition'>#{attrs}#{lines}</div>"

root.to_html=to_html
root.line_to_html=line_to_html
root.to_html_doc=to_html_doc
