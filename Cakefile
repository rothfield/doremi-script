{exec} = require 'child_process'
spawn = require('child_process').spawn
task "watch","Watch coffeescript directory for changes", =>
  
  cmd= "kill -9 $(ps ux | awk '/coffee/ && !/awk/ {print $2}')"
  console.log "killing old watch.sh"
  exec cmd, (err, stdout, stderr) ->
          #throw err if err
    console.log stdout + stderr
    console.log("running watch.sh in background")
    exec "./watch.sh &"


task 'build', 'Build project from src/*.coffee to lib/*.js', ->
  GRAMMAR='sargam.peg.js'
  console.log "Running pegjs on #{GRAMMAR} to create parser in ./lib/sargam "
  cmd= "pegjs -e SargamParser ./src/grammars/#{GRAMMAR}  ./lib/sargam/sargam_parser.js"
  exec cmd, (err, stdout, stderr) ->
    throw err if err
    console.log stdout + stderr

  exec 'coffee --compile --output lib/sargam src/coffeescript', (err, stdout, stderr) ->
    throw err if err
    console.log stdout + stderr
