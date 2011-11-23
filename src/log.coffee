root = exports ? this

log = (x) ->
  return if !@debug?
  return if !@debug
  return if !JSON?
  console.log(JSON.stringify(arg,null," ")) for arg in arguments
root.log=log
