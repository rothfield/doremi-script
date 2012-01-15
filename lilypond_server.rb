require 'sinatra'
require 'json'

class LilypondServer < Sinatra::Application

  set :port,9292
  
  configure do
    mime_type :xml, 'text/plain'
    mime_type :ly, 'text/plain'
  end
  
  set :comp, "#{Dir.pwd}/public/compositions"
  set :lily2image, "#{Dir.pwd}/bin/lily2image"
#  set :lilypond, "/usr/local/bin/lilypond"
  #set :port,80  if `hostname`.chomp == 'ragapedia'
  set :haml, :format => :html5
  
  def sanitize_filename(filename)
    name=filename.strip
     # NOTE: File.basename doesn't work right with Windows paths on Unix
     # get only the filename, not the whole path
     name.gsub! /^.*(\\|\/)/, ''
  
     # Finally, replace all non alphanumeric, underscore 
     # or periods with underscore
     # name.gsub! /[^\w\.\-]/, '_'
     # Basically strip out the non-ascii alphabets too 
     # and replace with x. 
     # You don't want all _ :)
     name.gsub!(/[^0-9A-Za-z.\-]/, '_')
     return name
  end
  
  
  
  get '/' do
    "#{ENV["SHELL"]} - ENV[PATH] is   #{ ENV["PATH"]} - whoami is #{`whoami`} - pwd is #{`pwd`} - Usage: get /lilypond_to_jpg?fname=..&lilypond=...&doremi_script_source="
  end
  
  post '/lilypond_to_jpg' do
    puts "in lilypond_to_jpg, params are #{params.inspect}"
    comp=settings.comp
    dir=File.join('public','compositions')
    return "no lilypond param" if !params["lilypond"]
    return "no fname param" if !params["fname"]
    lilypond=params["lilypond"]
    doremi_script_source=params["doremi_script_source"]
    filename=params["fname"] || ""
    simple_file_name=sanitize_filename(filename)
    fname="#{simple_file_name}"
    archive="#{simple_file_name}_#{Time.new.to_i}"
    
    fp= "#{comp}/#{fname}"
    archive="#{comp}/#{simple_file_name}_backup_#{Time.new.to_i}"
    # The -f stops rm from generating an error message
    `rm -f #{fp}-page*png`
    File.open("#{fp}.ly", 'w') {|f| f.write(lilypond) }
    File.open("#{archive}.txt", 'w') {|f| f.write(doremi_script_source) }
    File.open("#{fp}.txt", 'w') {|f| f.write(doremi_script_source) }
    result=`lilypond -o #{fp} #{fp}.ly  2>&1`
      #########################3
      #
      # Use lily2image to create better images for web
      #  http://code.google.com/p/lily2image/
      #  Requires lilypond 2.12.3  !!!!! and nbm
      #
      ########################
      
      result2= `#{settings.lily2image} -r=72 -f=jpg #{fp}.ly 2>&1`  
      result=result+result2
      # may create files like: bansuriv3-page1.jpeg
      # lilypond will create files like untitled_1319780034-page1.jpeg
      # if piece is long
      ####################################3
      #
      # COMBINE MULTI-PAGE jpegs if multiple pages
      # REQUIRES IMAGEMAGICK
      #
      ###################################
      page1="#{fp}-page1.jpeg"
      if File.file? page1
        puts "converting multiple pages using convert. #{fp}-page*.jpeg"
        `convert #{fp}-page*.jpeg -append #{fp}.jpeg`
      end
      `mv  #{fp}.jpeg #{fp}.jpg`
    error=false
    
    fname = "/compositions/#{fname}.jpg"
    if $?.exitstatus > 0 # failed
      error=true
      fname=""
    end
    `rm #{fp}.ps`
    `cp #{fp}.ly #{comp}/last.ly`
    json={:error => error, 
     :fname => fname,
     :lilypond_output => result
    }.to_json
    callback = params.delete('callback') # jsonp
    if callback
      content_type :js
      response = "#{callback}(#{json})" 
    else
      content_type :json
      response = json
    end
    response
  end
end 
