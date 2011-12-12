root = exports ? this
root.get_composition_attribute= (composition,key) ->
  return null if !composition.attributes
  if composition.attributes.items?
    att=_.detect(composition.attributes.items, (item) ->
      item.key is key
      )
    return null if !att
    return att.value

root.log= (x) ->
  return if !@debug?
  return if !@debug
  console.log arguments... if console

root.running_under_node= ->
  module? && module.exports

root.trim = (val) ->
  if String::trim? then val.trim() else val.replace /^\s+|\s+$/g, ""

root.my_inspect= (obj) ->
  return if ! debug?
  return if !debug
  return if !console?
  if running_under_node()
    console.log(util.inspect(obj,false,null))
    return
  console.log obj

root.item_has_attribute = (item,attr_name) ->
  return false if  !item.attributes?
  _.detect item.attributes,  (attr) ->
    return false if !attr.my_type?
    attr.my_type is attr_name

root.my_clone = (obj) ->
  if not obj? or typeof obj isnt 'object'
    return obj
  newInstance = new obj.constructor()
  for key of obj
    newInstance[key] = root.my_clone obj[key]
  newInstance

root.get_title = (composition) ->
  get_composition_attribute(composition,"Title")

root.get_mode = (composition) ->
  mode = get_composition_attribute(composition,'Mode')
  mode or= "major"

root.get_time = (composition) ->
  get_composition_attribute(composition,"TimeSignature")

root.get_ornament = (pitch) ->
  return null if !pitch.attributes?
  _.detect(pitch.attributes, (attribute) -> attribute.my_type is "ornament")
  
# TODO: make more regular data structure between composition.attributes
# and item.attributes
root.get_item_attribute = (item,key) ->
  return null if !item.attributes?
  _.detect(item.attributes, (attribute) -> attribute.my_type is key)

root.has_mordent = (pitch) ->
  return false if !pitch.attributes?
  _.detect(pitch.attributes, (attribute) -> attribute.my_type is "mordent")

# TODO: move into lilypond file
root.get_chord= (item) ->
  if e =_.detect(item.attributes, (x) -> x.my_type is "chord_symbol")
    return """
    ^"#{e.source}"
    """
  ""

root.get_ending= (item) ->
  if e =_.detect(item.attributes, (x) -> x.my_type is "ending")
    return """
    ^"#{e.source}"
    """
  ""
 
root.is_sargam_line= (line) ->
  return false if !line.kind?
  line.kind.indexOf('sargam') > -1

root.notation_is_in_sargam= (composition) ->
  @log "in notation_is_in_sargam"
  _.detect(composition.lines, (line) -> is_sargam_line(line))

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
      items.concat root.all_items(an_item,items)
  return [tree].concat(items)



