root = exports ? this

debug=false

sys = require('sys')
log = (x) ->
  return if !console
  console.log(x) if debug
 
x=require './tree_iterators.js'
all_items=x.all_items
tree_select=x.tree_select


my_log = (obj) ->
  sys.inspect(obj,true,null)

exports.test_tree_select = (test) ->
  tree =
    my_type: "line"
    line_number: 1
    items: [ { my_type: "pitch", pitch: "C" }
             { my_type: "pitch", pitch: "D" } ]
  result=tree_select(tree, (item) ->
                     item.my_type is "pitch")
  test.equal(2,result.length)
  test.done()
exports.test_tree_map = (test) ->
  tree =
    my_type: "line"
    line_number: 1
    items: [ { my_type: "pitch", pitch: "C" }
             { my_type: "pitch", pitch: "D" } ]
  result=tree_select(tree, (item) ->
                     item.my_type is "pitch")
  test.equal(2,result.length)
  test.done()

exports.test_empty_object = (test) ->
  result=all_items {}
  test.equal(1,result.length)
  test.done()

exports.test_object_with_items = (test) ->
  tree =
    my_type: "line"
    line_number: 1
    items: [ { my_type: "pitch", pitch: "C" }
             { my_type: "pitch", pitch: "D" } ]
  result=all_items tree
  test.equal(3,result.length)
  test.equal(result[0].my_type,"line")
  test.done()


