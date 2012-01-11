Lilypond Server
============

Lilypond server is a small server implemented in sinatra that provides a web api that accepts lilypond source data. The api then runs lilypond and generates a jpg suitable for web use. 

The server uses [lily2image](http://code.google.com/p/lily2image/) to create web friendly lilypond output. The generated jpg file is saved on the server under the public/compositions directory. The filename to save it under is passed in the params.

Requirements:

- LilyPond 2.12.3
- netpbm
- Ruby
- Sinatra

NOTE: The server serves up jsonp data. Since the lilypond source may be large and the jsonp uses 'GET', this may be a problem if the web server has limits for 'GET' data. For now I'm using the 'thin' server.



On debian, install lilypond from source in order to get the correct version. Install netpbm via apt

Usage:

The api accepts json or jsonp. Here is some sample coffeescript code
that uses jQuery and jsonP:

      url='http://localhost:9292/lilypond_to_jpg'
      my_data =
        fname: "composition_#{self.id()}"
        lilypond: lilypond_source
        doremi_script_source: self.doremi_script_source() 
      obj=
        dataType : "jsonp",
        timeout : 10000
        type:'GET'
        url: url
        data: my_data
        error: (some_data) ->
          alert("Couldn't connect to staff notation generator server at #{url}")
          self.staff_notation_url(NONE_URL)
        success: (some_data,text_status) ->
          self.generating_staff_notation(false)
          self.composition_lilypond_output(some_data.lilypond_output)
          if some_data.error
            self.staff_notation_url(NONE_URL)
            return
          self.staff_notation_url(some_data.fname)
      $.ajax(obj)

