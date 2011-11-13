root = exports ? this

debug=false

log = (x) ->
  return if !console
  console.log(x) if debug

sys = require('sys')
utils=require './tree_iterators.js'
require './sargam_parser.js'
to_lilypond=require('./sargam_json_to_lilypond.js').to_lilypond
parser=SargamParser

exports.test_ = (test) ->
  str = ':'
  test.equal(1,1)
  test.done()

