root = exports ? this
###
   Quick test of fractions.js
###
debug=false

if module? and module.exports?
  sys = require('sys')
  Fraction=require('./third_party/fraction.js').Fraction

exports.test_fractions_constructor_reduces_improper_fractions = (test) ->
  fraction=new Fraction(2,4)
  test.equal(fraction.numerator,1)
  test.equal(fraction.denominator,2)
  test.done()
