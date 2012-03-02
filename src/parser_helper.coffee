debug=false

root = exports ? this
if exports?
  Hypher=require('./third_party/hypher/hypher.js')
  english = require('./third_party/hyphenation_patterns/en-us.js')
else
  english=window.english
  Hypher=window.Hypher
#hypher = new Hypher(english,{ leftmin:2, rightmin:2, minLength:1})
# Had to add leftmin and rightmin to en-us. TODO: review
hypher = new Hypher(english,{ minLength:1})
#console.log "english.leftmin,rightmin,",english.leftmin,english.rightmin


root.ParserHelper=
  # THESE METHODS WILL GET MIXED IN THE PEG PARSER (for now)
  # look at top of peg parser, need to manually add new methods for now

  hypher: hypher

  sa_helper: (source,normalized) ->
    # save a little typing
    obj=
      source:source
      normalized_pitch:normalized
    obj



  assign_lyrics: (sargam,lyrics) ->
    return if !lyrics?
    return if lyrics is ""
    slurring_state=false
    syls=  (item.syllable for item in lyrics.items when item.my_type is "syllable")

    for item in this.all_items(sargam)
      do (item) =>
        return if item.my_type != "pitch"
        return if syls.length is 0
        if !slurring_state 
          item.syllable = syls.shift()
        slurring_state=true if item_has_attribute item,'begin_slur'
        slurring_state=false if item_has_attribute item,'end_slur'
      
  parse_lyrics_section: (lyrics_lines) ->
    #console.log "parse_lyrics_section"
    if lyrics_lines is ""
      source=""
    else
      source= (line.source for line in lyrics_lines).join("\n")

    all_words=(for line in lyrics_lines
      # should it be item.word? TODO:review
      item.syllable for item in line.items when item.my_type is "syllable")
    all_words= _.flatten all_words
    hy_ary=[]
    hyphenated_words=(
      for word in all_words
        result=hypher.hyphenate(word.toLowerCase()))
    hyphenated_words= _.flatten hyphenated_words
    hyphenated_words= hypher.hyphenateText(all_words.join(' '))
    soft_hyphen="\u00AD"
    hyphenated_words_str= hyphenated_words.split(soft_hyphen).join('-')
    regex = new RegExp /([^ -]+)/
    regex=/([^- ]+[- ]?)/g
    hyphenated_words=hyphenated_words_str.match regex
    #console.log hyphenated_words
    {
       my_type: "lyrics_section"
       source: source
       lyrics_lines: lyrics_lines
       line_warnings: [] # HACK. Add these attributes so it looks like line
       items:[]
       all_words:all_words
       hyphenated_words: hyphenated_words # hyphenated_words or hyphenated_syllables
    }

  parse_line: (uppers,sargam,lowers,lyrics) ->
    @line_warnings=[] # reset global
    lyrics = '' if lyrics.length is 0
    lowers= '' if  lowers.length is 0
    uppers= '' if  uppers.length is 0
    # doesn't include lyrics
    my_items = _.flatten(_.compact([uppers,sargam,lowers]))
    my_items2 = _.flatten(_.compact([uppers,sargam,lowers,lyrics]))
    sargam.source=(x=(item.source for item in my_items2)).join("\n")
    #add a group_line_no to each source line
    ctr=0
    for upper in uppers
      upper.group_line_no=ctr
      ctr= ctr + 1
      sargam.group_line_no=ctr
      ctr = ctr + 1
    for lower in lowers
      lower.group_line_no=ctr
      ctr= ctr + 1
    for lyric in lyrics
      lyric.group_line_no=ctr
    for my_line in my_items
      this.measure_columns(my_line.items,0)
    my_uppers=_.flatten(_.compact([uppers]))
    my_lowers=_.flatten(_.compact([lowers]))
    attribute_lines=_.flatten(_.compact([uppers,lowers,lyrics]))
    this.assign_attributes(sargam,attribute_lines)
    # TODO: Have this done using lyrics section! at a top level
    this.assign_lyrics(sargam,lyrics)
    sargam.line_warnings=@line_warnings
    sargam

  assign_lyrics2: (sargam,syls) ->
    #console.log("entering assign_lyrics2-syls is",syls)
    #console.log("entering assign_lyrics2-sargam is",sargam)
    return if !syls
    return if syls is ""
    slurring_state=false

    for item in this.all_items(sargam)
      do (item) =>
        return if item.my_type != "pitch"
        return if syls.length is 0
        if !slurring_state
          item.syllable = syls.shift()
        slurring_state=true if item_has_attribute item,'begin_slur'
        slurring_state=false if item_has_attribute item,'end_slur'

  assign_syllables_from_lyrics_sections :(composition) ->
    #console.log "entering assign_syllables_from_lyrics_sections"
    syls=[]
    for line in composition.lines
      #console.log "in for loop, line is", line.my_type
      if line.my_type is "lyrics_section"
        syls= syls.concat line.hyphenated_words
      if line.my_type is "sargam_line"
        this.assign_lyrics2 line,syls

  parse_composition: (attributes,lines) ->
    #lines= (x for x in my_lines when x.my_type isnt 'lyrics_section')
    #lyrics_sections= (x for x in my_lines when x.my_type is 'lyrics_section')
    #console.log "lyrics_sections",lyrics_sections
    ctr=0
    line.index=ctr++ for line in lines
    attributes=null if (attributes=="")
    @log("in composition, attributes is")
    @my_inspect(attributes)
    to_string= () ->
      zz=this.to_string
      delete this.to_string
      str= JSON.stringify(this,null," ")
      this.to_string=zz
      "\n#{str}"
    @composition_data =
      my_type:"composition"
      apply_hyphenated_lyrics:false
      title:""
      filename: ""
      attributes: attributes
      lines: _.flatten(lines)
      warnings:@warnings
      source:"" # can't get input source here, put it in later
      toString:to_string
      id:null
    # Certain attributes get set on the data object
    x=get_composition_attribute(@composition_data,"NotesUsed")
    valid=true
    if x? and (/^[sSrRgGmMpPdDnN]*$/.test(x) is false)
       this.warnings.push("ForceSargamChars should be all sargam characters, for example 'SrGmMdN'")
       valid=false
    @composition_data.notes_used = x  || ""
    hash={}
    if x and valid
      split_chars=@composition_data.force_sargam_chars.split('')
      for char in split_chars
        lower=char.toLowerCase(char)
        if char in ['S','R','G','M','P','D','N'] 
          if (lower not in split_chars)
            hash[char.toLowerCase(char)]=char
    @composition_data.force_sargam_chars_hash=hash
    x=get_composition_attribute(@composition_data,"TimeSignature")
    @composition_data.time_signature = x or "4/4"
    x=get_composition_attribute(@composition_data,"id")
    #console.log "x is #{x}"
    if x?
      @composition_data.id=parseInt(x)
    else
      @composition_data.id=new Date().getTime()
    x=get_composition_attribute(@composition_data,"Mode")
    x= x.toLowerCase() if x?
    @composition_data.mode = x or "major"
    x=get_composition_attribute(@composition_data,"Key")
    if  x? and ! valid_abc_pitch(x)
       this.warnings.push("Invalid key #{x}. Valid keys are C,D,Eb,F# etc")
       x="C"
    # TODO: put key validations here?
    @composition_data.key = x or "C"
    x=get_composition_attribute(@composition_data,"Filename")
    if x? and x isnt ""
      if (/^[a-zA-Z0-9_]+$/.test(x) is false)
        this.warnings.push("Filename must consist of alphanumeric characters plus underscores only")
        x="untitled"
    # TODO: dry or simply put all attributes the same way!
    @composition_data.filename = x or "untitled"
    x=get_composition_attribute(@composition_data,"Title")
    @composition_data.title= x or "" # "Untitled"
    x=get_composition_attribute(@composition_data,"Source")
    @composition_data.source= x or ""
    x=get_composition_attribute(@composition_data,"Author")
    @composition_data.author= x or ""
    x=get_composition_attribute(@composition_data,"Raga")
    @composition_data.raga= x if x?
    x=get_composition_attribute(@composition_data,"StaffNotationURL")
    @composition_data.staff_notation_url= x if x?
    x=get_composition_attribute(@composition_data,"ApplyHyphenatedLyrics")
    if x is "true"
      x=true
    else
      x=false
    @composition_data.apply_hyphenated_lyrics =  x #(x or false)
    @mark_partial_measures()
    assign_syllables_from_lyrics_sections(@composition_data) if  @composition_data.apply_hyphenated_lyrics
    @composition_data
  
  parse_sargam_pitch: (begin_slur,musical_char,end_slur) ->
    # AKA parse_note, parse_pitch
    #
    # Note that we need to track pitch_source separately from
    # source. We need source to track columns, but pitch_source is
    # used to render the pitch. This comes up in the case of slurs.
    # (SR). Note that the grammar doesn't define a slurred_phrase.
    source=''
    attributes=[]
    column_offset=0
    if (begin_slur !='')
      column_offset=1
      attributes.push(begin_slur)
      # if the source looks like (S - we want the column 
      # to be set 1 greater so that attributes will get assigned
      # properly
      source=source+begin_slur.source
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
       column_offset:column_offset
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
  
  parse_ornament: (left_delim="",items,right_delim="") ->
    if left_delim.length >0
      column_offset=1
    source="#{left_delim}#{z=get_source_for_items(items)}#{right_delim}"
    usable_source= z
    ornament =
      my_type:"ornament"
      id: ++@id_ctr
      column_offset:column_offset
      source: source
      usable_source: usable_source
      ornament_items: items
    ornament


  parse_sargam_line: (line_number,items,kind) ->
    if (line_number !='')
      items.unshift(line_number)
    source = this.get_source_for_items(items)
    my_items =  _.flatten(items)
    my_line =
      line_number:line_number
      my_type:"sargam_line"
      id: ++@id_ctr
      source: source
      items: my_items
      kind:kind
    if (this.parens_unbalanced(my_line))
      _.debug("unbalanced parens")
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
      id: ++@id_ctr
      source:source
      items:items

  extract_lyrics: () ->
    ary=[]
    for sargam_line in @composition_data.lines
      for item in this.all_items(sargam_line,[])
        @log "extract_lyrics-item is",item
        ary.push item.syllable if item.syllable
    ary

  mark_partial_measures: ()->
    for sargam_line in @composition_data.lines
      continue if sargam_line.my_type is 'lyrics_section'
      @log "processing #{sargam_line.source}"
      measures=  (item for item in sargam_line.items when item.my_type is "measure")
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
    for item in all_items(line)
      @log("***measure_pitch_durations:item.my_type is",item.my_type)
      if item.my_type is "measure"
        @log("***going into measure")
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
        last_pitch.fraction_total=_.reduce(last_pitch.fraction_array,
                                              my_funct,null)
          

  measure_dashes_at_beginning_of_beats: (line) ->
    @log("measure_dashes line is",line)
    measures=  (item for item in line.items when item.my_type=="measure")
    @log("measure_dashes measures is",measures)
    beats=[]
    for measure in measures
       m_beats=(item for item in measure.items when item.my_type=='beat')
       beats=beats.concat m_beats
    @log "measure_dashes - beasts is",beats

    beats_with_dashes_at_start_of_beat = _.select beats, (beat) =>
       pitch_dashes=(item for item in beat.items when item.my_type=="dash" or item.my_type=="pitch")
       @log "pitch_dashes",pitch_dashes
       return false if pitch_dashes.length is 0
       return true if pitch_dashes[0].my_type=="dash"
    @log("measure_dashes ;beats_with_dashes_at_start_of_beat =", beats_with_dashes_at_start_of_beat)
    for my_beat in beats_with_dashes_at_start_of_beat
      denominator=my_beat.subdivisions
      done=false
      ctr=0
      first_dash=null
      for item in my_beat.items
        do (item) =>
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


  parens_unbalanced: (line) ->
    #console.log "parens_unbalanced"
    @log("entering parens_unbalanced")
    ary=this.collect_nodes(line,[])
    @log("ary is")
    this.my_inspect(ary)
    x= _.select ary,  (item) =>
      item_has_attribute item,'begin_slur'
    y= _.select ary,  (item) =>
      item_has_attribute item,'end_slur'
    if x.length isnt y.length
      this.push_warning "Error on line ? unbalanced parens, line was #{line.source} Note that parens are used for slurs and should bracket pitches as so (S--- R)--- and NOT  (S--) "
      return true
    false

  get_source_for_items: (items) ->
    str=''
    for item in items
      str = str + item.source if item.source?
    return str

  measure_columns: (items,pos) ->
    for item in items
      item.column=pos
      if (item.my_type is "ornament") and (item.source[0] is "<")
        #console.log("measure_columns,buggy case",item) if !running_under_node()
        item.column=item.column + 1
      if (item.my_type is "pitch") and (item.source[0] is "(")
        #console.log("measure_columns,buggy case",item) if !running_under_node()
        item.column=item.column + 1
      if item.items?
        pos=this.measure_columns(item.items,pos)
      if !item.items?
        pos=pos + item.source.length
    pos

  handle_ornament: (sargam,sarg_obj,ornament,sargam_nodes) ->
    #console.log("handle_ornament, ornament is", ornament)
    # Assign the ornament to a pitch if possible
    # 2 kinds so far:
    #  mDP        mD
    # P      and    P
    #
    # also
    # <mD>
    #    P
    # if ornament is BEFORE sargam..
    _.debug "handle_ornament"
    target_column=ornament.column+ornament.ornament_items.length
    #console.log "target_column",target_column
    #console.log "handle_ornament, sargam_nodes",sargam_nodes
    s=sargam_nodes[target_column]
    if s? and (s.my_type is "pitch")
      _.debug "handle_ornament, before case, s is #{s}"
      ornament.placement="before"
      s.attributes = [] if !s.attributes?
      s.attributes.push(ornament)
      return
    s=sargam_nodes[ornament.column-1]
    if s? and (s.my_type is "pitch")
      ornament.placement="after"
      s.attributes = [] if !s.attributes?
      s.attributes.push(ornament)
      return
    this.push_warning "#{ornament.my_type} (#{ornament.source}) not to right "+
            "or left of pitch , column is #{ornament.column}"


  push_warning: (str) ->
    #console.log "in push_warning, str=",str
    @warnings.push str
    @line_warnings.push str

  check_semantics: (sargam,sarg_obj,attribute,sargam_nodes) ->
    # return false if the attribute is not to be added to sarg_obj
    # TODO: take more object oriented approach
    # eventually move this to a separate function
    return false if attribute.my_type is "whitespace"
    # if node.my_type is "kommal_indicator"
    # TODO: should only apply to devanagri!!
    # TODO: sometimes the attribute can be attached to a sargam object
    # NOT directly below it. For example, endings
    if (attribute.my_type is"ornament")
      handle_ornament(sargam,sarg_obj,attribute,sargam_nodes)
      return false
    if (!sarg_obj)
      this.push_warning "Attribute #{attribute.my_type} (#{attribute.source}) above/below nothing, column is #{attribute.column}"
      return false
    if attribute.my_type is "kommal_indicator"
      # TODO: review
      srgmpdn_in_devanagri="\u0938\u0930\u095A\u092E\u092a\u0927"
      if srgmpdn_in_devanagri.indexOf(sarg_obj.source) > -1
        sarg_obj.normalized_pitch=sarg_obj.normalized_pitch+"b"
        return true
      this.push_warning "Error on line ?, column "+sarg_obj.column + "kommal indicator below non-devanagri pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:\n"+sargam.source
      return false
      #if sarg_obj.source 
    if attribute.octave?
      if (sarg_obj.my_type isnt 'pitch')
        this.push_warning "Error on line ?, column "+sarg_obj.column + " #{attribute.my_type} below non-pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:\n"+sargam.source
        return false
      sarg_obj.octave=attribute.octave
      return false # as we consumed the attribute
    if attribute.syllable?
      if (sarg_obj.my_type isnt 'pitch')
        this.push_warning "Error on line ?, column "+sarg_obj.column + " syllable #{attribute.syllable} below non-pitch. Type of obj was #{sarg_obj.my_type}. sargam line was:\n"+sargam.source
        return false
      sarg_obj.syllable=attribute.syllable
      return false # as we consumed the attribute
    true

  find_ornaments: (attribute_lines) ->
    orns=[]
    for line in attribute_lines
      for item in line.items
        if item.my_type is "ornament"
          item.group_line_no=line.group_line_no
          for orn_item in item.ornament_items
            orn_item.group_line_no=line.group_line_no
          orns.push item
    orns

  map_ornaments: (ornaments) ->
    # returns a map, column_number --> ornament_item
    map={}
    ##map[orn.column]= orn for orn in ornaments
    for  orn in ornaments
      _.debug orn
      column=orn.column
      for ornament_item in orn.ornament_items
        map[column]=ornament_item
        column= column + 1
    map
   
  assign_attributes: (sargam,attribute_lines) ->
    # blindly assign attributes from the list of attribute_lines to
    @log("entering assign_attributes=sargam,attribute_lines",sargam,attribute_lines)
    # gets leaf nodes-
    sargam_nodes= this.map_nodes(sargam)
    ornaments=this.find_ornaments(attribute_lines)
    _.debug  "assign_attributes:ornaments are: #{ornaments}"
    ornament_nodes=this.map_ornaments(ornaments)
    _.debug "in assign_attributes ornament nodes: #{ornament_nodes}"
    for attribute_line in attribute_lines
      @log "processing",attribute_line
      attribute_map={}
      attribute_nodes=this.map_nodes(attribute_line)
      for column, attribute of attribute_nodes
        do (column,attribute) =>
          @log "processing column,attribute",column,attribute
          sarg_obj=sargam_nodes[column]
          orn_obj=ornament_nodes[column]
          # TODO: eventually move this to an semantic analyzer phase
          # handle case of an octave indicator for an ornament
          if orn_obj?
            if attribute.my_type is "upper_octave_indicator"
              orn_obj.octave=1
              _.debug "assign_attributes:upper_octave_indicator case",attribute
              if orn_obj.group_line_no < attribute_line.group_line_no
                # note that for ornaments, the dots will 
                # always be upper_octave_indicator
                # since ornaments are above the main line
                # That is why multiply octave by -1
                attribute.my_type = "lower_octave_indicator" 
                orn_obj.octave= (orn_obj.octave * -1)
              orn_obj.attributes=[] if !orn_obj.attributes?
              orn_obj.attributes.push attribute
              return  
          if this.check_semantics(sargam,sarg_obj,attribute,sargam_nodes) is not false
            sarg_obj.attributes=[] if !sarg_obj.attributes?
            sarg_obj.attributes.push attribute

  collect_nodes: (obj,ary) ->
    ary.push obj if  obj.my_type? and  !obj.items?
    if obj.items?
       for my_obj in obj.items
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
       for my_obj in obj.items
         @log("my_obj.column is ")
         if my_obj.column?
           map[my_obj.column]=my_obj
         if my_obj.items?
           this.map_nodes(my_obj,map)
    map

  log: (x) ->
    # TODO: figure out how to forward args to console.log
    # using call or apply
    return if !@debug?
    return if !@debug
    return if !console?
    return if !console.log?
    for arg in arguments
      console.log arg if console

  running_under_node: ()->
    module? && module.exports

  my_inspect: (obj) ->
    return if !debug?
    return if !debug
    return if !console?
    return if !console.log?
    if util?
      console.log(util.inspect(obj,false,null))
      return
    console.log obj

