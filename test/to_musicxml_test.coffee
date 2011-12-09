root = exports ? this

debug=false
global._console ||= require('./underscore.logger.js') if global?
Logger=global._console.constructor

_ = require("underscore")._ if require?



require './doremi_script_parser.js'
sys = require('sys')
utils=require './tree_iterators.js'
_console.level  = Logger.INFO
_.mixin(_console.toObject())

`_.mixin({
  each_slice: function(obj, slice_size, iterator, context) {
    var collection = obj.map(function(item) { return item; });
    
    if (typeof collection.slice !== 'undefined') {
      for (var i = 0, s = Math.ceil(collection.length/slice_size); i < s; i++) {
        iterator.call(context, _(collection).slice(i*slice_size, (i*slice_size)+slice_size), obj);
      }
    }
    return; 
  }
});`

to_musicxml=require('./to_musicxml.js').to_musicxml

parser=DoremiScriptParser

test_to_musicxml = (str,test,msg="") ->
  composition=parser.parse(str)
  _.debug "test_to_musicxml:composition is #{composition}"
  composition.source=str
  _.debug("test_to_musicxml, str is \n#{str}\n")
  to_musicxml(composition)

exports.ztest_single_note = (test) ->
  input="S"
  musicxml=test_to_musicxml(input,test)
  """
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <!DOCTYPE score-partwise PUBLIC
    "-//Recordare//DTD MusicXML 3.0 Partwise//EN"
    "http://www.musicxml.org/dtds/partwise.dtd">
                        <score-partwise version="3.0">
            <score-header>
<work>
  <work-number></work-number>
    <work-title>null</work-title>
    </work>

            </score-header>

  <part-list>
    <score-part id="P1">
      <part-name>Music</part-name>
    </score-part>
  </part-list>
  <part id="P1">
   
<measure number="1">
    <attributes>
    <divisions>1</divisions>
    <key>
      <fifths>0</fifths>
    </key>
    <clef>
  <sign>C</sign>
  <line>2</line>
</clef>
    <time>
      <beats>4</beats>
      <beat-type>4</beat-type>
    </time>
  </attributes>
  <note>
    <pitch>
      <step>C</step>
      <octave>4</octave>
    </pitch>
    <duration>4</duration>
    <type>whole</type>
  </note>
</measure>
  </part>
</score-partwise>

  """
  words= [
     "score-partwise"
     "</score-partwise>"
     "<part id="
     "</part>"
     "<note>"
     "</note"
  ]
  contains_word_helper(test,input,musicxml,words)
  test.done()

exports.test_title = (test) ->
  title="The Entertainer"
  input="Title: #{title}\n\n\nS"
  musicxml=test_to_musicxml(input,test)
  contains_word_helper(test,input,musicxml,[title])
  test.done()

contains_word_helper= (test,input,musicxml,words) ->
  for word in words
    msg= "#{input} --> (musicxml output) should include #{word}"
    failure_msg= "#{msg}\n output was:\n#{musicxml}"
    success_msg= "✔ #{msg}"
    test.ok(musicxml.indexOf(word) > -1,failure_msg)
    console.log success_msg
    
exports.test_pitches = (test) ->
  input="SRGmPDN"
  find= "CDEFGAB"
  # TODO:
  input="S"
  find="C"
  list=find.split('')
  words= ("<step>#{x}</step>" for x in list)
  musicxml=test_to_musicxml(input,test)
  contains_word_helper(test,input,musicxml,words)
  test.done()

