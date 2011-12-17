%w(rubygems oa-oauth dm-core dm-sqlite-adapter dm-migrations sinatra haml).each { |dependency| require dependency }

DataMapper.setup(:default, "sqlite3://#{Dir.pwd}/database.db")

class User
  include DataMapper::Resource
  property :id,         Serial
  property :uid,        String
  property :name,       String
  property :nickname,   String
  property :created_at, DateTime
end

DataMapper.finalize
DataMapper.auto_upgrade!

'''
  Read-only
  About the application permission model
  Consumer key  PBQUrshhiJtwnqRv1Umpg
  Consumer secret   MKNWz4WW79mOMeBBYzFEVPZeMUigaJ9Xe6gBH250ZZA
  '''
Consumer_key = 'PBQUrshhiJtwnqRv1Umpg'
Consumer_secret='MKNWz4WW79mOMeBBYzFEVPZeMUigaJ9Xe6gBH250ZZA'


# You'll need to customize the following line. Replace the CONSUMER_KEY 
#   and CONSUMER_SECRET with the values you got from Twitter 
#   (https://dev.twitter.com/apps/new).
use OmniAuth::Strategies::Twitter, 'PBQUrshhiJtwnqRv1Umpg','MKNWz4WW79mOMeBBYzFEVPZeMUigaJ9Xe6gBH250ZZA' 

class DoremiApp < Sinatra::Application

enable :sessions
set :port,9292

require 'json'
configure do
  mime_type :xml, 'text/plain'
  mime_type :ly, 'text/plain'
end

set :comp, "#{Dir.pwd}/public/compositions"
set :port,80  if `hostname`.chomp == 'ragapedia'
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


get '/auth/:name/callback' do
  auth = request.env["omniauth.auth"]
  user = User.first_or_create({ :uid => auth["uid"]}, { 
    :uid => auth["uid"], 
    :nickname => auth["user_info"]["nickname"], 
    :name => auth["user_info"]["name"], 
    :created_at => Time.now })
  session[:user_id] = user.id
  value="#{user.uid}|#{user.nickname}|#{user.name}"
  myDomain=""
  response.set_cookie("user", 
                      :value => value,
                      :domain => myDomain,
                      :path => "/",
                      :expires => Time.new(2020,1,1))
  redirect "/?name=#{user.name}&uid=#{user.uid}"
end

# any of the following routes should work to sign the user in: 
#   /sign_up, /signup, /sign_in, /signin, /log_in, /login
["/sign_in/?", "/signin/?", "/log_in/?", "/login/?", "/sign_up/?", "/signup/?"].each do |path|
  get path do
    redirect '/auth/twitter'
  end
end

# either /log_out, /logout, /sign_out, or /signout will end the session and log the user out
["/sign_out/?", "/signout/?", "/log_out/?", "/logout/?"].each do |path|
  get path do
    session[:user_id] = nil
    redirect '/'
  end
end


get '/' do
  haml :index
end

get '/list_samples' do
  ary=[]
  Dir.chdir("public/samples") do
    ary=Dir.glob("*.{txt,sargam}") 
  end
  content_type :json
  ary2= ary.collect do |x|
     x =~ /^(.*).(txt|sargam)$/
    "/samples/#{$1}"
  end
  ary2.to_json
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

get %r{/samples/([a-z_A-Z0-9]+)$} do
  haml :index
end

get %r{/compositions/([a-z_A-Z0-9]+)$} do
  haml :index
end


get '/compositions' do
  ary=[]
  @compositions=Dir.chdir("public/compositions") do
    ary=Dir.entries('.').sort {|b,a| File.stat(a).mtime <=> File.stat(b).mtime}
  end
  @compositions= @compositions.find_all {|i|  i =~ /\.txt$/ }
  haml :compositions
end


post '/save' do
  comp=settings.comp
  dir=File.join('public','compositions')
  html=params["html_page"]
  lilypond=params["lilypond"]
  save_to_samples=params['save_to_samples']
  doremi_script_source=params["doremi_script_source"]
  musicxml=params["musicxml"]
  filename=params["fname"] || ""
  simple_file_name=sanitize_filename(filename)
  fname=simple_file_name
  archive="#{simple_file_name}_#{Time.new.to_i}"
  fp= "#{comp}/#{fname}"
  fp_archive="#{comp}/#{archive}"
  `rm #{fp}-page*png`
  File.open("#{fp}.ly", 'w') {|f| f.write(lilypond) }
  File.open("#{fp}.txt", 'w') {|f| f.write(doremi_script_source) }
  File.open("#{archive}.txt", 'w') {|f| f.write(doremi_script_source) }
  File.open("#{fp}.xml", 'w') {|f| f.write(musicxml) }
  File.open("#{fp}.html", 'w') {|f| f.write(html) }
  ary=[]
  if save_to_samples 
    Dir.chdir("public/compositions") do
      ary=Dir.glob("#{fname}*") 
    end
    ary.each do |x| 
     x =~ /^.*\.(.*)$/
     suffix=$1
     fp= "#{comp}/#{x}"
    `cp #{fp} ../../samples/#{simple_file_name}.#{suffix}`
    `cp #{fp} ./samples/#{simple_file_name}.#{suffix}`
    end
  end
  content_type :json
  {:error => false, 
   :fname => "/compositions/#{fname}"
  }.to_json
end

post '/lilypond.txt' do
  comp=settings.comp
  dir=File.join('public','compositions')
  return "no lilypond param" if !params["lilypond"]
  lilypond=params["lilypond"]
  save_to_samples=params['save_to_samples']
  doremi_script_source=params["doremi_script_source"]
  musicxml=params["musicxml"]
  filename=params["fname"] || ""
  simple_file_name=sanitize_filename(filename)
  fname="#{simple_file_name}"
  archive="#{simple_file_name}_#{Time.new.to_i}"
  
  fp= "#{comp}/#{fname}"
  `rm #{fp}-page*png`
  File.open("#{fp}.ly", 'w') {|f| f.write(lilypond) }
  File.open("#{archive}.txt", 'w') {|f| f.write(doremi_script_source) }
  File.open("#{fp}.txt", 'w') {|f| f.write(doremi_script_source) }
  File.open("#{fp}.xml", 'w') {|f| f.write(musicxml) }
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
  `rm #{fp}.ps`
  `cp #{fp}.ly #{comp}/last.ly`
  ary=[]

  if save_to_samples 
    Dir.chdir("public/compositions") do
      ary=Dir.glob("#{fname}*") 
    end
    ary.each do |x| 
     x =~ /^.*\.(.*)$/
     suffix=$1
     fp= "#{comp}/#{x}"
    `cp #{fp} ../../samples/#{simple_file_name}.#{suffix}`
    `cp #{fp} ./samples/#{simple_file_name}.#{suffix}`
    end
  end
  #midi=`openssl enc -base64 -in #{fp}.midi`
  #puts "midi is #{midi[0..20]}..."
  content_type :json
  {:error => false, 
   :fname => "/compositions/#{fname}",
   :lilypond_output => result,
  }.to_json
end

end
