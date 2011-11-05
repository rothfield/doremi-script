root = exports ? this

class Composition
  first_sargam_line: () ->
    @composition_data.logical_lines[0].sargam_line
  first_logical_line: () ->
    @composition_data.logical_lines[0]

  constructor: () ->
    @composition_data={}
    @warnings=[]

  set_composition_data: (some_composition_data) ->
    @composition_data=some_composition_data

  extract_lyrics: () ->
    ary=[]
    for logical_line in @composition_data.logical_lines
      for item in this.all_items_in_line(logical_line.sargam_line,[])
        @log "extract_lyrics-item is",item
        ary.push item.syllable if item.syllable
    ary

  tie_notes: ->

  get_attribute: (key) ->
    return null if !@composition_data.attributes
    att=_.detect(@composition_data.attributes.items, (item) ->
      item.key is key
      )
    return null if !att
    att.value

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


  all_items_in_line: (line_or_item,items) ->
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
    for item in this.all_items_in_line(line,all)
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
    @log("entering measure_note_durations for beat:"+beat.source)
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
          #beat.items.length
    (_.select(beat.items, (item) ->
      (item.my_type=="pitch" || item.my_type=="dash"))).length

  parens_unbalanced: (line) ->
    @log("entering parens_unbalanced")
    #this.my_inspect(items)
    #nodes=this.map_nodes(items,{})
    #@log('nodes=')
    #this.my_inspect(this.collect_nodes(items))
    ary=[]
    ary=this.collect_nodes(line,ary)
    @log("ary is")
    this.my_inspect(ary)
    x= _.select(ary,  (item) =>
      item.begin_slur? && item.begin_slur is true)
    y= _.select(ary,  (item) =>
      item.end_slur? && item.end_slur is true)
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

  assign_from_upper: (obj,upper) ->
    @log("entering assign_from_upper obj=") 
    this.my_inspect obj
    ok=true
    sargam=obj.sargam_line
    @log "sargam is"
    this.my_inspect sargam
    sargam_map= this.map_nodes(sargam)
    @log(sargam_map)
    upper_map=this.map_nodes(upper)
    this.my_inspect upper_map
    _.each upper_map, (node,column) =>
      @log("node.my_type =",node.my_type)
            #@log("node,column =",node,column)
      return if node.my_type=="whitespace"
      sarg_obj=sargam_map[column]
      if !sarg_obj?
        @warnings.push "Error on line ?, column "+column + " "+node.my_type         +  node.source + " above nothing. sargam line was:"+sargam.source
        ok=false
        return false #returns from inner function
      sarg_obj.attributes=[] if !sarg_obj.attributes?
      sarg_obj.attributes.push node
      if node.octave?
        sarg_obj.octave=node.octave
    @log "map_nodes returned"
    this.my_inspect sargam_map
    return ok

  assign_from_lyrics: (obj) ->
    ok=true
    if !obj.lyrics?
      return ok
    lyrics=obj.lyrics
    sargam=obj.sargam_line
    sargam_map= {}
    lyrics_map={}
    sargam_nodes= this.map_nodes(sargam,sargam_map)
    lyric_nodes=this.map_nodes(lyrics,lyrics_map)
    _.each lyrics_map, (node,column) =>
      @log("node,column =",node,column)
      sarg_obj=sargam_map[column]
      @log "sarg_obj is"
      this.my_inspect sarg_obj
      this.my_inspect node
      if node.syllable?
        if (!sarg_obj?) or (sarg_obj.my_type isnt "pitch")
          ok=false
          @warnings.push "Error on line ?, column "+column + "syllable #{node.syllable} below non-pitch. sargam line was:"+sargam.source
          return ok
        sarg_obj.syllable=node.syllable
    @log "map_nodes returned"
    this.my_inspect sargam_nodes
    return ok

  assign_from_lower: (obj) ->
    ok=true
    if !obj.lower?
      return ok
    lower=obj.lower
    sargam=obj.sargam_line
    sargam_map= {}
    lower_map={}
    sargam_nodes= this.map_nodes(sargam,sargam_map)
    lower_nodes=this.map_nodes(lower,lower_map)
    _.each lower_map, (node,column) =>
      @log("node,column =",node,column)
      sarg_obj=sargam_map[column]
      @log "sarg_obj is"
      this.my_inspect sarg_obj
      if node.octave?
        if (!sarg_obj?)
          ok=false
          @warnings.push "Error on line ?, column "+column + "lower octave indicator below non-pitch(no object at column #{column}). sargam line was:"+sargam.source
          return false
        if (sarg_obj.my_type isnt 'pitch')
          @warnings.push "Error on line ?, column "+column + "lower octave indicator below non-pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:"+sargam.source
          return false
        sarg_obj.octave=node.octave
    this.my_inspect sargam_nodes
    return true

  collect_nodes: (obj,ary) ->
          #this.my_inspect("Entering collect_nodes, ary is ")
          #this.my_inspect(ary)
          #this.my_inspect("Entering collect_nodes, obj is ")
          #this.my_inspect(obj)
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
    # return if ! root.debug
    return if !console?
    return if  !console.log?
    console.log x if console

  running_under_node: ->
    module? && module.exports

  my_inspect: (obj) ->
    return if !console?
    return if !console.log?
    return if window? and window.debug==false
    return if !root.debug?
    if this.running_under_node()
      console.log(sys.inspect(obj,false,null)) 
      return
    console.log obj

root.Composition=Composition
