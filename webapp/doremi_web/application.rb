require 'sinatra'
require 'json'

set :comp, "#{Dir.pwd}/public/compositions"
set :port,80  if `hostname`.chomp == 'ragapedia'

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


before do
  puts '[Params]'
  p params
end

get '/' do
  File.read(File.join('public', 'index.html'))
end

get '/list_samples' do
  ary=[]
  Dir.chdir("public/samples") do
    ary=Dir.glob("*.{txt,sargam}") 
  end
  content_type :json
  ary.to_json
end

post '/generate_html_page' do
  # params are html_to_use and filename
  filename=params["filename"] || ""
  fname="#{sanitize_filename(filename)}_#{Time.new.to_i}.html"
  comp=settings.comp
  dir=File.join('public','compositions')
  fp= "#{comp}/#{fname}"
  File.open("#{fp}", 'w') {|f| f.write(params["html_to_use"]) }
  fname
end

post '/lilypond.txt' do
  comp=settings.comp
  dir=File.join('public','compositions')
  puts params.inspect      
  return "no data param" if !params["data"]
  data=params["data"]
  doremi_script_source=params["doremi_script_source"]
  puts "params.data is #{params['data']}" 
  filename=params["fname"] || ""
  fname="#{sanitize_filename(filename)}_#{Time.new.to_i}"
  fp= "#{comp}/#{fname}"
  `rm #{fp}-page*png`
  File.open("#{fp}.ly", 'w') {|f| f.write(data) }
  File.open("#{fp}.txt", 'w') {|f| f.write(doremi_script_source) }
  result=`lilypond --png  -o #{fp} #{fp}.ly  2>&1`
  # lilypond will create files like untitled_1319780034-page1.png
  # ... page2.png etc  if pdf is multi page
  # For now, if it does it, only display first page
  # TODO:
  # using imagemagick, combine the pngs as follow
  #  convert untitled_1319780082-page* -append last.montage.png
  ####################################3
  #
  # COMBINE MULTI-PAGE PNGs if multiple pages
  # REQUIRES IMAGEMAGICK
  #
  ###################################
  page1="#{fp}-page1.png"
  if File.file? page1
    `convert #{fp}-page*.png -append #{fp}.png`
  end
  result=`lilypond --pdf  -o #{fp} #{fp}.ly  2>&1`
  if $?.exitstatus > 0 # failed
    return {:error => true,:midi => "",:lilypond_output => result, :png => ""}.to_json
  end
  puts "result of running lilypond is <<#{result}>>"
  `cp #{fp}.ly #{comp}/last.ly`
  # puts "result2.length is #{result2[0..20]}..."
  #midi=`openssl enc -base64 -in #{fp}.midi`
  #puts "midi is #{midi[0..20]}..."
  content_type :json
  {:error => false, 
   :fname => fname,
   :lilypond_output => result,
  }.to_json
end
