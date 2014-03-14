# Uses module pattern, exports the following:
#
# tree_map tree_find etc
#
#Try and support things like: 
#Collections
#each, map, reduce, reduceRight, detect, select, reject, all, any, include, invoke, pluck, max, min, sortBy, groupBy, sortedIndex, toArray, size 
#



root = exports ? this
if (typeof require != 'undefined')
  global.all_items= require('./all_items.js').all_items
  _ = require("underscore")._

tree_filter = (tree,my_function) ->
  _.select(all_items(tree),my_function)

tree_find = (tree,my_function) ->
  _.find(all_items(tree),my_function)


tree_each= (tree,fun) ->

root.tree_each=tree_each
root.tree_filter=tree_filter
root.tree_select=tree_filter
root.tree_find=tree_find
root.tree_detect=tree_find
