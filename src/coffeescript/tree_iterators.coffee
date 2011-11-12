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
  _ = require("underscore")._

tree_filter = (tree,my_function) ->
  _.select(all_items(tree),my_function)

tree_find = (tree,my_function) ->
  _.find(all_items(tree),my_function)


all_items= (tree,items=[]) ->
  # TODO: dry this up
  # return (recursively) items in the tree, delves into the hierarchy
  # looks for an items property and if so, recurses to it.
  # line 
  #   measure
  #     beat
  #       item
  if  (!tree.items)
     return [tree]
  for an_item in tree.items
    do (an_item) =>
      items.push an_item #if !an_item.items?
      items.concat all_items(an_item,items)
  return [tree].concat(items)

tree_each= (tree,fun) ->

root.tree_each=tree_each
root.all_items=all_items
root.tree_filter=tree_filter
root.tree_select=tree_filter
root.tree_find=tree_find
root.tree_detect=tree_find
