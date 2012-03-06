Lilypond Server
============

Lilypond server is a small server implemented in sinatra that provides a web api that accepts lilypond source data. The api then runs lilypond and generates a jpg suitable for web use. 

The server uses [lily2image](http://code.google.com/p/lily2image/) to create web friendly lilypond output. The generated jpg file is saved on the server under the public/compositions directory. The filename to save it under is passed in the params.

For security reasons the server now does not accept the ly parameter from the browser. The ly parameter is the lilypond source which is input to lilypond. The server will generate the ly file using doremi2ly which should result in a safe lilypond file.

Requirements:

- LilyPond 2.12.3
- netpbm
- Ruby
- Sinatra
- doremi-script-base -
- doremi-script from npm


On debian, install lilypond from source in order to get the correct version. Install netpbm via apt

Usage:

The API will soon be changed to the following:

The api accepts json as follows
  {
    doremi_source: "| S"
    dont_generate_staff_notation: "true"
  }



