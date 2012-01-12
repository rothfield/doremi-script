%w(rubygems omniauth dm-core dm-sqlite-adapter 
   dm-migrations sinatra haml).each { |dependency| require dependency }

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

use OmniAuth::Strategies::Twitter,
      ENV['DOREMI_SCRIPT_TWITTER_CONSUMER_KEY'],
      ENV['DOREMI_SCRIPT_TWITTER_CONSUMER_SECRET']

class DoremiApp < Sinatra::Application

  enable :sessions
  set :port,9292
  disable :logging  # since rack by default also logs

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

  not_found do
    '404 Not found'
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
    ary2.sort.to_json
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


  get %r{^/compositions(/)?$} do
    ary=[]
    @compositions=Dir.chdir("public/compositions") do
      ary=Dir.entries('.').sort {|b,a| File.stat(a).mtime <=> File.stat(b).mtime}
    end
    @compositions= @compositions.find_all {|i|  i =~ /\.txt$/ }
    @compositions.sort!
    haml :compositions
  end


  post '/save' do
    comp=settings.comp
    dir=File.join('public','compositions')
    samples_dir=File.join('public','samples')
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
        `cp #{fp} #{samples_dir}/#{simple_file_name}.#{suffix}`
      end
    end
    if save_to_samples
      name="/compositions/#{fname}"
    else
      name="/samples/#{fname}"
    end
    content_type :json
    {:error => false, 
     :fname => name
    }.to_json
  end

  post '/lilypond.txt' do
    puts "post lilypond.txt"
    comp=settings.comp
    dir=File.join('public','compositions')
    return "no lilypond param" if !params["lilypond"]
    lilypond=params["lilypond"]
    save_to_samples=params['save_to_samples']
    puts "class is #{save_to_samples.class.to_s}"
    puts "save_to_samples is <#{save_to_samples}>"
    doremi_script_source=params["doremi_script_source"]
    musicxml=params["musicxml"]
    filename=params["fname"] || ""
    simple_file_name=sanitize_filename(filename)
    fname="#{simple_file_name}"
    archive="#{simple_file_name}_#{Time.new.to_i}"

    fp= "#{comp}/#{fname}"
    `rm -i #{fp}-page*jpeg`
    File.open("#{fp}.ly", 'w') {|f| f.write(lilypond) }
    File.open("#{archive}.txt", 'w') {|f| f.write(doremi_script_source) }
    File.open("#{fp}.txt", 'w') {|f| f.write(doremi_script_source) }
    File.open("#{fp}.xml", 'w') {|f| f.write(musicxml) }
    #########################3
    #
    # Use lily2image to create better images for web
    #  http://code.google.com/p/lily2image/
    #
    ########################
    result= `./lily2image -r=72 -f=jpeg #{fp}.ly 2>&1`  
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
    `cp #{fp}.ly #{comp}/last.ly`
    ary=[]
    if (save_to_samples == true) 
      puts "*****saving_to_samples"
      Dir.chdir("public/compositions") do
        ary=Dir.glob("#{fname}*") 
      end
      ary.each do |x| 
        x =~ /^.*\.(.*)$/
        suffix=$1
        fp= "#{comp}/#{x}"
        `cp #{fp} ./public/samples/#{simple_file_name}.#{suffix}`
      end
    end
    dir= "compositions"
    dir="samples" if save_to_samples
    content_type :json
    {:error => false, 
     :fname => "/#{dir}/#{fname}",
    :lilypond_output => result,
    }.to_json
  end

end
