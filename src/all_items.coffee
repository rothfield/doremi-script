debug=false

root = exports ? this


root.all_items= (tree,items=[]) ->
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
      items.concat this.all_items(an_item,items)
  return [tree].concat(items)

