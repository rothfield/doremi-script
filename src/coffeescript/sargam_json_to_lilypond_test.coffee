root = exports ? this

debug=false

log = (x) ->
  return if !console
  console.log(x) if debug

sys = require('sys')
utils=require './tree_iterators.js'
log = require('./log.js').log
require './sargam_parser.js'

to_lilypond=require('./sargam_json_to_lilypond.js').to_lilypond

parser=SargamParser

test_parses = (str,test,msg="") ->
  composition=parser.parse(str)
  log(composition) if composition?
  test.ok(composition?,"#{str} didn't parse!!. #{msg}")
  return composition

exports.test_combines_whole_beat_rests_within_a_measure = (test) ->
  debug=true
  str = 'S - -'
  composition=test_parses(str,test)
  log("composition is",composition,debug)
  test.done()

