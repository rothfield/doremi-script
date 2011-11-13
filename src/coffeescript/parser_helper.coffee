debug=false

root = exports ? this


root.ParserHelper=
  # THESE METHODS WILL GET MIXED IN THE PEG PARSER (for now)
  # look at top of peg parser, need to manually add new methods for now


  sa_helper: (source,normalized) ->
    # save a little typing
    obj=
      source:source
      normalized_pitch:normalized
    obj

  parse_sargam_pitch: (slur,musical_char,end_slur) ->
    # Note that we need to track pitch_source separately from
    # source. We need source to track columns, but pitch_source is
    # used to render the pitch. This comes up in the case of slurs.
    # (SR). Note that the grammar doesn't define a slurred_phrase.
    source=''
    attributes=[]
    if (slur !='')
      attributes.push(slur)
      source=source+slur.source
    source=source+musical_char.source
    if (end_slur !='')
      attributes.push(end_slur)
      source=source+end_slur.source
    {
       my_type: "pitch"
       normalized_pitch: musical_char.normalized_pitch
       attributes:attributes
       pitch_source:musical_char.source
       source: source
       octave: 0
    }

  parse_beat_delimited: (begin_symbol,beat_items,end_symbol) ->
    beat_items.unshift(begin_symbol)
    beat_items.push(end_symbol)
    my_beat =
      my_type:"beat"
      source: this.get_source_for_items(beat_items)
      items: beat_items
    my_beat.subdivisions=this.count_beat_subdivisions(my_beat)
    @log("count_beat_subdivisions returned",my_beat.subdivisions,"my beat was",my_beat)
    @measure_note_durations(my_beat)
    my_beat
  
  parse_beat_undelimited: (beat_items) ->
    beat_items=_.flatten beat_items
    my_beat =
      my_type:"beat"
      source: this.get_source_for_items(beat_items)
      items: beat_items
    my_beat.subdivisions=this.count_beat_subdivisions(my_beat)
    @measure_note_durations(my_beat)
    my_beat

  parse_sargam_line: (line_number,items,kind) ->
    if (line_number !='')
      items.unshift(line_number)
    source = this.get_source_for_items(items)
    my_items =  _.flatten(items)
    my_line =
      line_number:line_number
      my_type:"sargam_line"
      source: source
      items: my_items
      kind:kind
    if (this.parens_unbalanced(my_line))
      @log("unbalanced parens") 
    this.measure_dashes_at_beginning_of_beats(my_line)
    this.measure_pitch_durations(my_line)
    my_line

  parse_measure: (start_obs,items,end_obs) ->
    if start_obs != ""
      items.unshift(start_obs)
    this.log("end.length is"+end_obs.length)
    if (end_obs != "")
      items.push(end_obs)
    source = this.get_source_for_items(items)
    obj=
      my_type: "measure"
      source:source
      items:items

  get_attribute: (key) ->
    return null if !@composition_data.attributes
    att=_.detect(@composition_data.attributes.items, (item) ->
      item.key is key
      )
    return null if !att
    att.value

  extract_lyrics: () ->
    ary=[]
    for logical_line in @composition_data.logical_lines
      for item in this.all_items(logical_line.sargam_line,[])
        @log "extract_lyrics-item is",item
        ary.push item.syllable if item.syllable
    ary

  mark_partial_measures: ()->
    for logical_line in @composition_data.logical_lines
      @log "processing #{logical_line.sargam_line.source}"
      measures=  (item for item in logical_line.sargam_line.items when item.my_type is "measure")
      @log 'mark_partial_measures: measures', measures
      for measure in measures
        beats=  (item for item in measure.items when item.my_type is "beat")
        @log 'mark_partial_measures: beats is', beats
        @log 'mark_partial_measures: beats.length ', beats.length
        # HACK TODO
        measure.beat_count=beats.length
        if measure.beat_count < 4  #hack
          @log("setting is_partial true")
          @log "inside if"
          measure.is_partial=true 


  measure_pitch_durations: (line) ->
    @log("measure_pitch_durations line is",line)
    last_pitch=null
    _.each(all_items(line),
           (item) =>
             console.log("***measure_pitch_durations:item.my_type is",item.my_type)
             if item.my_type is "measure"
               console.log("***going into measure")
             if item.my_type is "pitch"
               item.fraction_array=[] if !item.fraction_array?
               frac=new Fraction(item.numerator,item.denominator)
               item.fraction_array.push(frac)
               last_pitch=item
               @my_inspect item

             if item.my_type is "dash" and item.dash_to_tie
               frac=new Fraction(item.numerator,item.denominator)
               last_pitch.fraction_array.push(frac)
               my_funct= (memo,frac) ->
                 if !memo?  then frac else frac.add memo
               last_pitch.fraction_total=_.reduce(last_pitch.fraction_array,my_funct,null)
          )

  measure_dashes_at_beginning_of_beats: (line) ->
    @log("measure_dashes line is",line)
    measures=  (item for item in line.items when item.my_type=="measure")
    @log("measure_dashes measures is",measures)
    beats=[]
    _.each measures,(measure) =>
       m_beats=(item for item in measure.items when item.my_type=='beat')
       beats=beats.concat m_beats
    @log "measure_dashes - beasts is",beats

    beats_with_dashes_at_start_of_beat = _.select beats, (beat) =>
       pitch_dashes=(item for item in beat.items when item.my_type=="dash" or item.my_type=="pitch")
       @log "pitch_dashes",pitch_dashes
       return false if pitch_dashes.length is 0
       return true if pitch_dashes[0].my_type=="dash"
    @log("measure_dashes ;beats_with_dashes_at_start_of_beat =", beats_with_dashes_at_start_of_beat)

    _.each beats_with_dashes_at_start_of_beat,(my_beat) => 
      denominator=my_beat.subdivisions
      done=false
      ctr=0
      first_dash=null
      _.each my_beat.items,  (item) => 
        return if done
        done=item.my_type=="pitch"
        if item.my_type=="dash"
           ctr++
           first_dash=item if ctr is 1
      first_dash.numerator=ctr
      first_dash.denominator=denominator
      first_dash.dash_to_tie=true
    #Now, loop through the line. Keep track of the last pitch and
    # when you come to a tied dash, set the dashes tied_pitch to the 
    # last pitch
    @log "looping through items"
    last_pitch=null
    all=[]
    for item in this.all_items(line,all)
      @log "in loop,item is", item 
      last_pitch = item if item.my_type is "pitch"
      if item.dash_to_tie and last_pitch?
        last_pitch.tied=true
        item.pitch_to_use_for_tie=last_pitch
        last_pitch=item
      if item.dash_to_tie and !last_pitch?
        item.rest=true
        item.dash_to_tie=false
    return

    
  measure_note_durations: (beat) ->
    # @log("entering measure_note_durations for beat:"+beat.source)
    denominator=beat.subdivisions
    microbeats=0
    len=beat.items.length
    ctr=0
    for ctr in [0...len]
      do (ctr) =>
        item=beat.items[ctr]
        @log("in do loop, item is "+item.source)
        @log("in do loop, ctr is "+ctr)
        if item.my_type != "pitch"
          return
        @log("setting denominator for #{item.source}")
        numerator=1
        done=false
        if  ctr <len
          for x in [(ctr+1)..(len-1)]
            do (x) =>
              if (x < len)
                @log 'x is'+x
                if !done
                  my_item=beat.items[x]
                  if (my_item.my_type=="dash")
                    numerator++
                  if (my_item.my_type=="pitch")
                    done=true
                  @log("in inner loop, my_item is"+my_item.source)
                  @log("in inner loop, x is"+x)
        @log("setting numerator,denominator for #{item.source}")
        item.numerator=numerator 
        item.denominator=denominator

  count_beat_subdivisions: (beat) ->
    # use all_items to in case pitches or dashes are not at top
    # level of tree
    @log("all_items",all_items(beat))
    (_.select(all_items(beat), (item) ->
      (item.my_type=="pitch" || item.my_type=="dash"))).length

  item_has_attribute: (item,attr_name) ->
      return false if  !item.attributes?
      _.detect item.attributes,  (attr) ->
         return false if !attr.my_type?
         attr.my_type is "begin_slur"

  parens_unbalanced: (line) ->
    @log("entering parens_unbalanced")
    ary=this.collect_nodes(line,[])
    @log("ary is")
    this.my_inspect(ary)
    x= _.select ary,  (item) ->
      @item_has_attribute item,'begin_slur'
    y= _.select ary,  (item) ->
      item_has_attribute item,'end_slur'
    if x.length isnt y.length
      @warnings.push "Error on line ? unbalanced parens, line was #{line.source} Note that parens are used for slurs and should bracket pitches as so (S--- R)--- and NOT  (S--) "
      return true
    false

  get_source_for_items: (items) ->
    str=''
    _.each items, (item) =>
      if !item.source?
        return 
      str = str + item.source 
    return str

  measure_columns: (items,pos) ->
    _.each items, (item) =>
      item.column=pos
      # HACK
      item.column=item.column + 1 if (item.my_type=="pitch") and (item.source[0]=="(")
      item.column=item.column - 1 if (item.my_type=="pitch") and (item.source[item.source.length]==")")

      if item.items?
        this.measure_columns(item.items,pos)
      pos=pos + item.source.length


  check_semantics: (sargam,sarg_obj,attribute) ->
    # return false if the attribute is not to be added to sarg_obj
    # TODO: take more object oriented approach
    # eventually move this to a separate function
    return false if attribute.my_type is "whitespace"
    # if node.my_type is "kommal_indicator"
    # TODO: should only apply to devanagri!!
    # TODO: sometimes the attribute can be attached to a sargam object
    # NOT directly below it. For example, endings
    if (!sarg_obj)
      @warnings.push "Attribute #{attribute.my_type} above/below nothing, column is #{attribute.column}"
      return false
    if attribute.my_type is "kommal_indicator"
      # TODO: review
      srgmpdn_in_devanagri="\u0938\u0930\u095A\u092E\u092a\u0927"
      if srgmpdn_in_devanagri.indexOf(sarg_obj.source) > -1
        sarg_obj.normalized_pitch=sarg_obj.normalized_pitch+"b"
        return true
      @warnings.push "Error on line ?, column "+sarg_obj.column + "kommal indicator below non-devanagri pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:"+sargam.source
      return false 
      #if sarg_obj.source 
    if attribute.octave?
      if (sarg_obj.my_type isnt 'pitch')
        @warnings.push "Error on line ?, column "+sarg_obj.column + "lower octave indicator below non-pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:"+sargam.source
        return false
      sarg_obj.octave=attribute.octave
      return false # as we consumed the attribute
    if attribute.syllable?
      if (sarg_obj.my_type isnt 'pitch')
        @warnings.push "Error on line ?, column "+sarg_obj.column + "syllable #{attribute.syllable} below non-pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:"+sargam.source
        return false
      sarg_obj.syllable=attribute.syllable
      return false # as we consumed the attribute
    true

  assign_attributes: (sargam,attribute_lines) ->
    # IN PROGRESS
    # blindly assign attributes from the list of attribute_lines to
    # sargam.attributes_new
    @log("entering assign_attributes=sargam,attribute_lines",sargam,attribute_lines) 
    # gets leaf nodes-
    sargam_nodes= this.map_nodes(sargam)
    for attribute_line in attribute_lines
      @log "processing",attribute_line
      attribute_map={}
      attribute_nodes=this.map_nodes(attribute_line)
      for column, attribute of attribute_nodes 
        @log "processing column,attribute",column,attribute
        sarg_obj=sargam_nodes[column]
        # TODO: eventually move this to an semantic analyzer phase
        if this.check_semantics(sargam,sarg_obj,attribute) is not false
          sarg_obj.attributes=[] if !sarg_obj.attributes?
          sarg_obj.attributes.push attribute

  collect_nodes: (obj,ary) ->
    ary.push obj if  obj.my_type? and  !obj.items?
    if obj.items?
       _.each obj.items, (my_obj) =>
         ary.push my_obj if my_obj.my_type?
         if my_obj.items?
           this.collect_nodes(my_obj,ary)
    this.my_inspect("leaving collect_nodes, ary is ")
    this.my_inspect(ary)
    ary

  map_nodes: (obj,map={}) ->
    # creates a javascript object functioning as a hash
    # where the column is the key and the value is the object
    # recurses to handle cases like a measure or beat that contain
    # other objects
    this.my_inspect("Entering map_nodes, map is ")
    this.my_inspect(map)
    @log("obj.column is ")
    this.my_inspect(obj.column)
    if obj.column?
      map[obj.column]=obj
    if obj.items?
       _.each obj.items, (my_obj) =>
         @log("my_obj.column is ")
         if my_obj.column?
           map[my_obj.column]=my_obj
         if my_obj.items?
           this.map_nodes(my_obj,map)
    map

  log: (x) ->
    # TODO: figure out how to forward args to console.log
    # using call or apply
    #console.log x if console
    return if !@debug?
    return if !@debug
    return if !console?
    return if !console.log?
    for arg in arguments
      console.log arg if console

  running_under_node: ()->
    false #todo

  my_inspect: (obj) ->
    return if !debug?
    return if !debug
    return if !console?
    return if !console.log?
    if sys?
      console.log(sys.inspect(obj,false,null)) 
      return
    console.log obj

