root = exports ? this

debug=true
global._console ||= require('./underscore.logger.js') if global?
Logger=global._console.constructor
#_console.level  = Logger.WARN
_console.level  = Logger.DEBUG
_ = require("underscore")._ if require?
require './sargam_parser.js'
sys = require('sys')
utils=require './tree_iterators.js'
_.mixin(_console.toObject())



my_inspect = (x) ->
  return if !debug?
  console.log "debug is #{debug}"
  return if !debug
  return if !JSON?
  console.log(JSON.stringify(arg,null," ")) for arg in arguments
  #  if arg?
  #    JSON.stringify arg,null," "

_.mixin(my_inspect: my_inspect)
parser=SargamParser

aux1 = (str,result) ->
  return if !sys
  _.debug("Result of parsing <<#{str}>> is")
  _.debug(sys.inspect(result,true,null))
  
should_not_parse = (str,test,msg) ->
  _.debug(str)
  _.debug("Testing that <<#{str}>> does NOT parse")
  test.throws (-> parser.parse(str)),"<<\n#{str}\n>> should not parse!. #{msg}"

parse_without_reporting_error = (str) ->
  _.debug("Entering parse_without_reporting_error")
  log "Parsing <<\n#{str}>>"
  try
    parser.parse(str)
  catch error
    _.debug("Didn't parse")

first_sargam_line =  (composition_data) ->
    composition_data.lines[0]

first_line = (composition_data) ->
    composition_data.lines[0]

test_parses = (str,test,msg="") ->
  _.debug("Entering test_parses, str is #{str}")
  composition=parser.parse(str)
  _.debug("in test_parses,composition is #{composition}")
  composition
  ###
  test.doesNotThrow(-> result=parser.parse(str))
  test.ok(result?,"didn't parse")
  _.debug(sys.inspect(result,false,null))
  ###

exports.test_bad_input = (test) ->
  str = ':'
  should_not_parse(str,test)
  test.done()

exports.test_does_not_accept_single_barline = (test) ->
  str = '|'
  should_not_parse(str,test)
  test.done()

exports.test_accepts_various_eols = (test) ->

  for str in ["SS\nRR","SS\rSS","SS\r\nRR","SS\n\rSS"]
    test_parses(str,test)
  test.done()

exports.test_does_not_accepts_single_left_repeat = (test) ->
  str = '|:'
  should_not_parse(str,test)
  test.done()

exports.test_accepts_five_octaves_of_chromatic_notes = (test) ->
  str= '''                                                      .  .... .... .... : 
1) SrRg GmMP dDnN S | SrRg GmMP dDnN S | SrRg GmMP dDnN S| SrRg GmMP dDnN S |
   :::: :::: :::: .   .... .... ....

                     .  .... .... .... :   :::: :::: ::::
2)  | SrRg GmMP dDnN S| SrRg GmMP dDnN S | SrRg GmMP dDnN ---- |
  '''
  test_parses(str,test)
  test.done()

exports.test_accepts_right_repeat = (test) ->
  str = '|: :|'
  test_parses(str,test)
  test.done()

exports.test_accepts_chords = (test) ->
  str = '''
   V  i IVm
  |SR

  '''
  test_parses(str,test)
  test.done()


exports.test_eof_ends_beat = (test) ->
  str = '''
  |SR
  '''
  composition=test_parses(str,test)
  log composition
  line=first_sargam_line(composition)
  measure=line.items[0]
  second_item= measure.items[1]
  _.debug(second_item)
  test.equal(second_item.my_type,"beat","second item of #{str} should be a beat containing SR")
  test.equal(second_item.subdivisions,2)
  test.done()
  
exports.test_barline_ends_beat = (test) ->
  str = '''
  |SR|S
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  measure=line.items[0]
  second_item= measure.items[1]
  _.debug(second_item)
  test.equal(second_item.my_type,"beat","second item of #{str} should be a beat containing SR")
  test.equal(second_item.subdivisions,2)
  test.done()

exports.test_dashes_inside_beat = (test) ->
  str = '''
  |S--R|
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  measure=line.items[0]
  second_item= measure.items[1]
  _.debug(second_item)
  test.equal(second_item.my_type,"beat","second item of #{str} should be a beat containing S--R")
  test.equal(second_item.subdivisions,x=4,"subdivisions of beat #{str} should be #{x}")
  test.done()
  
exports.test_lines = (test) ->
  str = '''
  | S- |


  '''
  composition=test_parses(str,test)
  _.debug(composition)
  test.done()
  return
  second_item= composition.lines[0].items[1]
  _.debug(second_item)
  test.equal(second_item.my_type,"beat","second item of #{str} should be a beat")
  test.equal(second_item.items.length,2,"the beat's length should be 2")
  test.equal(second_item.items[0].my_type,"dash","dash should be first item")
  third_item= composition.lines[0].items[2]
  _.debug(third_item)
  test.equal(third_item.my_type,"barline","third item of #{str} should be a barline")
  test.done()

exports.test_dash_starts_beat = (test) ->
  str = '''
  |-R|
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  measure=line.items[0]
  second_item=measure.items[1]
  _.debug(second_item)
  test.equal(second_item.my_type,"beat","second item of #{str} should be a beat")
  test.equal(second_item.items.length,2,"the beat's length should be 2")
  test.equal(second_item.items[0].my_type,"dash","dash should be first item")
  test.done()
  
  
exports.test_recognizes_upper_octave_line = (test) ->
  str = '''
  .:~*
  SRGR
  '''
  composition=test_parses(str,test)
  test.done()
exports.test_recognizes_lower_octave_line = (test) ->
  str = '''
  SRG  R
  .:   *
  '''
  composition=test_parses(str,test)
  test.done()

exports.test_recognizes_slurs = (test) ->
  str = '''
  (S  R)
   .

  '''
  composition=test_parses(str,test)
  test.done()

exports.test_accepts_delimited_beat = (test) ->
  str = '''
  | <SR>

  '''
  composition=test_parses(str,test)
  _.debug(composition)
  test.done()

exports.test_accepts_spaces_in_delimited_beat = (test) ->
  str = '''
  | <S R>

  '''
  composition=test_parses(str,test)
  _.debug(composition)
  test.done()

exports.test_accepts_delimited_beat = (test) ->
  str = '''
  | <SR>

  '''
  composition=test_parses(str,test)
  _.debug(composition)
  test.done()

exports.test_recognizes_ornament_symbol = (test) ->
  str = '''
  ~
  S  R
  '''
  composition=test_parses(str,test)
  test.done()

exports.test_attributes = (test) ->
  str = '''
  Rag:Bhairavi

  SRG
  '''
  composition=test_parses(str,test)
  test.done()


exports.test_line_can_come_right_after_header_line = (test) ->
  str = '''
  Rag:Kafi
  S
  '''
  composition=test_parses(str,test)
  test.done()

exports.test_leading_spaces_in_upper_octave_line = (test) ->
  str = '''
  Rag:Kafi

            .
     SRGmPDNS
  '''
  composition=test_parses(str,test)
  test.done()

exports.test_leading_spaces_in_sargam_line = (test) ->
  str = '''
  Rag:Kafi

     Sr
  '''
  composition=test_parses(str,test)
  test.done()

exports.test_gives_warning_if_misplaced_upper_octave_indicator= (test) ->
  str = '''
  Rag:Kafi
    .
     r
  '''
  composition=test_parses(str,test)
  test.ok(composition.warnings.length > 0,"expected warnings")
  test.ok(composition.warnings[0].indexOf(z="upper_octave_indicator")> -1,"Expected warning to include #{z}. Warning was #{composition.warnings[0]}")
  test.done()

exports.test_gives_warning_if_unmatched_parens_for_slurs = (test) ->
  str = '''
  (Pm
  '''
  composition=test_parses(str,test)
  test.ok(composition.warnings.length > 0,"expected warnings")
  test.ok(composition.warnings[0].indexOf(z="unbalanced parens")> -1,"Expected warning to include #{z}. Warning was #{composition.warnings[0]}")
  test.done()

exports.test_gives_warning_if_syllable_under_right_paren = (test) ->
  str = '''
  | <(Pm)>
        nai- 
  '''
  composition=test_parses(str,test)
  test.ok(composition.warnings.length > 0,"expected warnings")
  test.ok(composition.warnings[0].indexOf(z="Attribute syllable above/below nothing" > -1),"Expected warning to include #{z}, warning was #{warnings[0]}")
  test.done()

exports.test_gives_warning_if_syllable_under_left_paren = (test) ->
  str = '''
  | <(Pm)>
     nai- 
  '''
  composition=test_parses(str,test)
  test.ok(composition.warnings.length > 0,"expected warnings")
  test.ok(composition.warnings[0].indexOf(z="Attribute syllable above/below nothing" > -1),"Expected warning to include #{z}, warning was #{warnings[0]}")
  test.done()
exports.test_gives_warning_if_syllable_not_under_note = (test) ->
  str = '''
  |S
    hi 

  '''
  composition=test_parses(str,test)
  test.ok(composition.warnings.length > 0,"No warnings")
  test.ok(composition.warnings[0].indexOf(z="Attribute syllable above/below nothing" > -1),"Expected warning to include #{z}, warning was #{warnings[0]}")
  test.done()

exports.test_syllable_assigned_to_note_above_it = (test) ->
  str = '''
  S
  hi 

  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  measure=line.items[0]
  beat=measure.items[0]
  test.equal("beat",beat.my_type)
  pitch=beat.items[0]
  test.equal("pitch",pitch.my_type)
  test.equal("hi",pitch.syllable)
  test.done()

exports.test_upper_octave_assigned_to_note_below_it = (test) ->
  str = '''
  .*::
  Srgm
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  log line
  measure=line.items[0]
  log "measure is",measure
  beat=measure.items[0]
  log "beat is",beat
  test.equal("beat",beat.my_type)
  pitch=beat.items[0]
  log "pitch is",pitch
  test.equal("pitch",pitch.my_type)
  test.equal(beat.items[0].octave,1,"#{str} should have octave 1 for S")
  test.equal(beat.items[1].octave,1,"#{str} should have octave 1 for r")
  test.equal(beat.items[2].octave,2,"#{str} should have octave 2 for g")
  test.equal(beat.items[3].octave,2,"#{str} should have octave 2 for m")
  test.done()

exports.test_lower_octave_assigned_to_note_above_it = (test) ->
  str = '''
  Srgm
  .*::
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  log "line is #{line}"
  measure=line.items[0]
  test.equal("measure",measure.my_type)
  beat=measure.items[0]
  test.equal("beat",beat.my_type)
  pitch=beat.items[0]
  test.equal("pitch",pitch.my_type)
  log line
  test.equal(beat.items[0].octave,-1,"#{str} should have octave -1 for S")
  test.equal(beat.items[1].octave,-1,"#{str} should have octave -1 for r")
  test.equal(beat.items[2].octave,-2,"#{str} should have octave -2 for g")
  test.equal(beat.items[3].octave,-2,"#{str} should have octave -2 for m")
  test.done()

exports.test_all = (test) ->
  x=  '''
dog
'''
  log  "x=#{x}" 
  str = '''
Rag:Bhairavi
Tal:Tintal
Title:Bansuri
Source:AAK

          3                   +            2         .
1) |: (Sr | n) S   (gm Pd) || P - P  P   | P - D  <(nDSn)> |
            .
       ban-    su-  ri        ba- ja ra-   hi  dhu- na

0                 3                     +     .    *  .
| P  d   P   d    | <(Pm>   PmnP) (g m) || PdnS -- g  S |
  ma-dhu-ra  kan-     nai-         ya      khe-    la-ta

2              0     ~
|  d-Pm g P  m | r - S :| %
   ga-    wa-ta  ho- ri

     +                     2    0     3
2)  [| Srgm PdnS SndP mgrS | %    | %   | S--S --S- ---- R-G-     |]

'''

  # 2)  | Srgm PdnS SndP mgrS |     |    |     |
  composition=test_parses(str,test)
  first_sargam_source=str.split('\n')[6]
  line=first_sargam_line(composition)
  test.equal(line.source,first_sargam_source,"sanity check, expected first line's source to be #{first_sargam_source}")
  test.done()



exports.test_parses_measure_at_beginning_of_line = (test) ->
  str = '''
  Sr
  ban-
  '''
  composition=test_parses(str,test)
  line=first_sargam_line(composition)
  measure=line.items[0]
  test.equal(measure.my_type,"measure","<<#{str}>> should be parsed as a measure with beat #{str}")
  test.equal(measure.items[0].my_type,"beat","<<#{str}>> should be parsed as a measure with beat #{str}")
  test.done()



exports.test_parses_lyrics_line_without_leading_and_trailing_spaces = (test) ->
  # TODO-rewrite
  str = '''
  Srgm|  S
  he-llo john
  '''
  composition=test_parses(str,test)
  test.done()
  ###
  test.equal(lyrics.items[0].source,"he-","he- source should be he-")
  test.equal(lyrics.items[0].syllable,"he-","he- should be parsed as a syllable")
  test.equal(lyrics.items[1].syllable,"llo","llo should be parsed as a syllable")
  
  test.equal(lyrics.items[1].source,"llo","source for syllable should NOT include trailing white space!")
  test.equal(lyrics.items[3].source,"john","source for john should john")
  test.equal(lyrics.items[3].syllable,"john","john should be parsed as a syllable")
  aux1(str,composition)
  test.done()
  ###

exports.test_collects_sargam_line_source = (test) ->
  str = '''
  |Sr  g    m    |
  '''
  composition=test_parses(str,test)
  expect="|Sr  g    m    |"
  test.equal(first_sargam_line(composition).source,"|Sr  g    m    |","should collect source, expected #{expect} . Note that eol is not included")
  test.done()


exports.test_parses_lyrics_line_with_leading_and_trailing_spaces = (test) ->
  str = '''
  |Sr  g    m    |
   he- llo  john   
  '''
  ###
  Note that whitespace is included.
  lyric items are parsed something like:
     items: 
             [ { my_type: 'whitespace', source: ' ' },
               { my_type: 'syllable', syllable: 'he-', source: 'he-' },
               { my_type: 'whitespace', source: ' ' },
               { my_type: 'syllable', syllable: 'llo', source: 'llo' },
               { my_type: 'whitespace', source: '  ' },
               { my_type: 'syllable',
                 syllable: 'john',
                 source: 'john' },
               { my_type: 'whitespace', source: '   ' } ] 
   ### 
  composition=test_parses(str,test)
  test.ok(composition.lines?,"parsed composition should have a lines attribute")
  test.equal(composition.lines.length,1,"<<\n#{str}\n>> should have 1 line")
  first= first_line(composition)
  # TODO:
  test.done()

exports.test_position_counting = (test) ->
  str = 'Sr'
  composition=test_parses(str,test)
  aux1(str,composition)
  test.done()


exports.test_parses_lines = (test) ->
  str = '''
  S

  R
  '''
  composition=test_parses(str,test)
  _.log("test_parses_lines, after test_parses")
  _.debug(composition.toString())
  _.log "z"
  test.ok(composition.lines?,"parsed composition should have a lines attribute")
  test.equal(composition.lines.length,2,"Should have 2 lines")
  aux1(str,composition)
  test.done()

exports.test_position_counting = (test) ->
  str = 'Sr'
  composition=test_parses(str,test)
  aux1(str,composition)
  test.done()
###
[ { my_type: 'sargam_line',
    items: 
         [ { my_type: 'pitch', source: 'S', octave: 0 },
                { my_type: 'pitch', source: 'r', octave: 0 } ],
                    aux1: 'hi' } ]
###

exports.test_accepts_attributes = (test) ->
  str = "hi:john\n"
  test_parses(str,test)
  test.done()
exports.test_accepts_attributes2 = (test) ->
  str = "hi:john\nhi:jane\n"
  test_parses(str,test)
  test.done()
exports.test_accepts_attributes3 = (test) ->
  str = "hi:john\nhi:jane\n\n\n\n   \n"
  test_parses(str,test)
  test.done()
  
  
exports.test_accepts_double_barline = (test) ->
  # Actually only || shouldn't parse, as a bar should have some content
  # The following should parse as a single measure
  str = '|| S'
  test_parses str,test,"should parse as a single measure with a single S"
  test.done()

exports.test_accepts_mordent = (test) ->
  str = '''
        ~
        S
  '''
  test_parses(str,test)
  test.done()

exports.test_sargam_characters_only_should_parse_as_sargam_line = (test) -> 
  str = '''
        SrRgGmMPdDnN
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  test.equal(line.my_type,"sargam_line","#{str} only should parse as a  sargam_line and NOT a lyrics_line")
  test.done()
 

exports.test_parses_one_as_tala = (test) ->
  str = '''
        1 
        S
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('tala') > -1,"#{x} -1st line should have tala object")
  test.done()

exports.test_ending_one_dot = (test) ->
  str = '''
        1.
        S
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('ending') > -1,"#{x} -line should have ending object")
  test.done()

exports.test_chord_iv = (test) ->
  str = '''
        iv
        S
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('chord_symbol') > -1,"#{x} -line should have chord object")
  test.done()

exports.test_ending_one_dot_underscores = (test) ->
  str = '''
        1.__
        S
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('ending') > -1,"#{x} -line should have ending object")
  test.done()

exports.test_ending_two_underscores = (test) ->
  str = '''
        2______
        S
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('ending') > -1,"#{x} -line should have ending object")
  test.done()

exports.test_tivra_ma_devanagri = (test) ->
  str = '''
    म'
    tivrama
  '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf("source: 'म\\'")  > -1,"#{x} -line should have tivra ma")
  test.done()

exports.test_devanagri_and_latin_sargam_together_should_fail = (test) ->
  #  \u0938\u0930\u095A\u092E\u092a\u0927\u0929\u0938
  str = '''
           .
    सरग़मपधऩस SrRgGmMPdDnN
    SRGmPDNS
  '''
  composition = should_not_parse(str,test)
  test.done()
exports.test_devanagri = (test) ->
  #  \u0938\u0930\u095A\u092E\u092a\u0927\u0929\u0938
  str = '''
           .
    सरग़मपधऩस
    SRGmPDNS
  '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('स') > -1,"#{x} -line should have स")
  test.ok(x.indexOf('denominator: 8') > -1,"#{x} -line should have 8 pitches")
  test.equal(line.kind,z="devanagri","line.kind should be #{z}")
  test.done()

exports.test_kommal_indicator = (test) ->
  str = '''
        र
        _
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  test.ok(x.indexOf('kommal_indicator') > -1,"#{x} -line should have kommal indicator")
  test.done()

exports.test_abc = (test) ->
  str = '''
        C#D#F#G#A#B#DbEbGbAbBbCC#DbDD#EbEFF#GbGAbAA#BbBB# 
        '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  x=sys.inspect(line,true,null)
  #test.ok(x.indexOf('kommal_indicator') > -1,"#{x} -line should have kommal indicator")
  test.done()

exports.test_title = (test) ->
  str = '''
  Title: The entertainer 

  S
  '''
  composition = test_parses(str,test)
  test.equal(composition.title, "The entertainer" )
  test.done()

exports.test_filename = (test) ->
  str = '''
  Filename: the_entertainer 

  S
  '''
  composition = test_parses(str,test)
  test.equal(composition.filename, "the_entertainer" )
  test.done()

exports.test_empty_lines_with_blanks = (test) ->
  str = '''
        --S- ---- --r-
     
    
                S
  '''
  composition = test_parses(str,test)
  test.done()

exports.test_recognizes_number_notation= (test) ->
  str = '''
   | 1234567 1#2#3#4#5#6#7#-   1b2b3b4b5b6b7b- 
     hello
  '''
  composition = test_parses(str,test)
  test.equal(composition.lines[0].kind,"number","should set composition kind to number")
  test.done()

exports.test_measure_pitch_durations = (test) ->
  str = '''
        --S- ---- --r-
  '''
  composition = test_parses(str,test)
  line=first_sargam_line(composition)
  my_pitch=utils.tree_find(line, (item) -> item.source is "S" )
  # TODO:
  #test.equal(my_pitch.fraction_array.join(''), [ '2/4', '4/4', '2/4'].join('') )
  test.done()

exports.test_zzz = (test) ->
  str = '''
                            ..
        CDbDEb EFF#G AbABbB CD
        
                    ..
        SrGmMP dDnN SR 

                    
        सररग़ग़ मम'पधधऩऩ
        _ _      _ __

        '''
  test.done()
