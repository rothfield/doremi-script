root = exports ? this
to_western_rhythm=(require './to_western_rhythm.js').to_western_rhythm


test_data = [
]
#exports.test_all = (test) ->

dots_should_be_undefined = (test,obj) ->
  test.equal(obj.dots,undefined,"dots should be undefined")

exports.test_half_of_beat_is_an_eighth_note = (test) ->
  result= to_western_rhythm(1,2)
  test.equal(result[0].note,"eighth")
  dots_should_be_undefined(test,result[0])
  test.done()
exports.test_third_of_beat_is_an_eigthth_note = (test) ->
  result= to_western_rhythm(1,3)
  test.equal(result[0].note,"eighth")
  dots_should_be_undefined(test,result[0])
  test.done()
exports.test_quarter_of_beat_is_a_sixteenth_note = (test) ->
  result= to_western_rhythm(1,4)
  test.equal(result[0].note,"sixteenth")
  dots_should_be_undefined(test,result[0])
  test.done()

exports.test_3_fourths_of_a_beat = (test) ->
  result= to_western_rhythm(3,4)
  test.equal(result[0].note,"eighth")
  test.equal(result[0].dots,dots=1,"should have #{dots} dots")
  test.done()

exports.test_4_beats = (test) ->
  result= to_western_rhythm(4,1)
  test.equal(result[0].note,"whole")
  dots_should_be_undefined(test,result[0])
  test.done()

exports.test_2_beats = (test) ->
  result= to_western_rhythm(2,1)
  test.equal(result[0].note,"half")
  dots_should_be_undefined(test,result[0])
  test.done()

exports.test_3_beats = (test) ->
  result= to_western_rhythm(3,1)
  test.equal(result[0].note,"half")
  test.equal(result[0].dots,dots=1,"should have #{dots} dots")
  test.done()

