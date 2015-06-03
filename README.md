DoremiScript
============
**DoremiScript** is a small language for creating [letter based musical notation](http://en.wikipedia.org/wiki/Letter_notation). It currently supports western ABC style notation, [Numbered notation](http://en.wikipedia.org/wiki/Numbered_musical_notation) using 1234567, [Sargam](http://en.wikipedia.org/wiki/Swara) notation using english letters SrRgGmMPdnN as used at the AACM for notating Ragas, and [Devanagri notation in Bhatkande style](http://en.wikipedia.org/wiki/Musical_notation#India) [(See also)](http://www.omenad.net/page.php?goPage=http%3A%2F%2Fwww.omenad.net%2Farticles%2Fomeswarlipi.htm) using Devanagri letters for the sargam letters. **DoremiScript** lets you write letter music notation using an easy-to-read, easy-to-write plain text format, then nicely formats it using css and html. DoremiScript can also generate standard western notation via a [Lilypond](http://lilypond.org) converter. 

Try the [online version](http://ragapedia.com)

Sample Input in AACM Sargam Style
---------------------------------
    Rag:Bhairavi
    Tal:Tintal
    Title:Bansuri
    Author:Traditional
    Source:AAK
    Mode: phrygian
    Filename: bansuri.sargam
    Time: 4/4
    Key: d
    
    
              
                                   [i]          [IV]         . 
             3S             n      +            2         DSnDn
    1)|: (Sr | n) S   (gm <P d)> | P - P  P   | P - D    n     |
               .
          ban-    su-  ri          ba- ja ra-   hi  dhu- na
    
      0  ~                 3           mgm        +  .     *  *   
    | P  d   P    d    |  (Pm   PmnP)    (g m) | (PdnS) -- g  S |
      ma-dhu-ra   kan-     nai-           ya      khe-     la-ta
    
       2               0     
                    Pm ~
    | (d-Pm  g) P  m | r - S :|
       ga-      wa-ta  ho- ri
    
DoremiScript HTML Output(screen snapshot) 
------------------
![Screenshot of DoremiScript rendered in html](https://github.com/rothfield/doremi-script/raw/master/doc/bansuri_in_html_screenshot.png "Sargam Screenshot")

Generated Western Staff Notation
--------------------------------
![Converted to western notation](https://github.com/rothfield/doremi-script/raw/master/doc/bansuri_in_western_notation.png "")

Examples from existing works
--------------------------------

  - From Bhatkhande's 4 volume Hindustani Sangita Paddhati(1913)
   
![Bhatkande (1913)](https://github.com/rothfield/doremi-script/raw/master/doc/bhatkande.png "")




  - Use a blank line to separate lines of music.
  - Start lines of music with a barline "|", for example,  | C D E F | . Otherwise be sure to include a barline or dash in the main line of music.
  - ABC style notation is written as follows C C# Db etc
	- Doremi style notation is written as DRMFSLT with accidentals after the note name - (S# being Gb and Mb being Eb)
  - Numbered notation is written as follows 1234567 corresponding to the diatonic notes of the C scale, with sharps and flats notated as 2b(Db) and 2#(D#), for example.
  - AACM sargam notation is written as follows SrRgGmMPdDnN for the 12 notes. Sharp and flat notes are also supported as follows: S# R# etc and Pb. This allows proper notation using Sargam of Jazz/Pop/Classical music that uses these notes. For example, in notating the Beatles tune "Yesterday", use P# to indicate the sharpened fifth degree. P# corresponds to G# in the key of C.
  - Note that the AACM style of Sargam notation uses *lowercase* characters to indicate the flattened notes.
  - Devanagri style sargam is written using the devanagri letters सरग़मपधऩस
  for the seven notes SRGmPDN. Use an underline underneath the letter to indicate a kommal note (flat). For sharp Ma, write as म' (note the tick symbol) 
  - left and right parens are used to indicate slurs. Slurs can only be made between pitches. For example, use (SRg)- rather than  (Srg-)
  - Beats are delimited by spaces. You can also use angle brackets <> to delimit the beat
  - Dot (.) and asterisk can be used interchangeably to 
indicate octaves. Place dots underneath for lower octave and above for
upper octaves. Use a colon **:** to indicate upper-upper octave or lower-lower octave
  - Lyrics: Enter lyrics for each line underneath the notes. Syllable DO NOT need to be directly under their corresponding note, as the parser can figure out which syllables go with which notes by counting pitches and slurred phrases. 
  - Rhythm: In this style of notation, derived from the AACM style, the dash (**-**) is used as a rhythmic placeholder. For example, this measure `| C--Eb--G-   A   A   A |` is interpreted as a measure consisting of four beats. The first beat is divided into 8 parts, where the C gets 3/8ths of the beat, Eb gets 3/8ths of a beat and G gets 2/8ths. (Note that 3/8ths of a beat in western 4/4 notation is considered 3/32nds and is written as a dotted 16th. The 3 A's are each a single beat. The lilypond generator attempts to support this, but still needs a bit of work.
  - Underlines are supported for Devanagri to indicate flat notes
  - Mode,Time, and Key in header are used in generating western notation
  - Chord symbols are supported. Write them using brackets and align left bracket over the note. Example [Fm7b5]
  - Syllables do not need to be entered underneath the corresponding pitch. Doremi-script matches syllables to notes based on slurs. 
  - For syllables to show up properly in generated western notation, slurs
should be used to indicate the melismas
  - Use | for single barline, || for double barline, |: and :| for left and right repeats
  - If need be, you can have multiple upper or lower lines.
  - Asterisk (*) can be used instead of a . to indicate octave
  - Use the tilde character **~** to indicate an ornament which is displayed as 
# &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    &#x1D19D;&#x1D19D;
  - Supports tala markings +023456 for notating classical North Indian rhythmical cycles such as teental. The **+** indicates (usually) the first beat of the rhythmical cycle. The tala markings indicate the claps(taali) and non-claps(kaali) that delimit the rhythmic cycle.  
  - Generates [lilypond](http://lilypond.org) as a high quality interchange format, which can be used to generate western staff notation and midi.
  - TODO: Generates [musicxml](http://en.wikipedia.org/wiki/MusicXML) as a common music notation format. MusicXML can be imported into hundreds of notation packages. Note that the lilypond export is of higher quality. Currently supports the following: lyrics, before grace notes,title.
  - Parser is easy to modify and is generated from an Instaparse grammar.
	
  - DoremiScript grammar is [here](https://raw.github.com/rothfield/doremi-script/master/src/grammars/doremiscript.peg.js)
  - Endings are entered as follows: 1.________ or 2._____ Current lilypond support for endings is poor.
  - It is possible to mix ABC, Devanagri, and AACM notations in the same composition, but each line of notation must be in only one style.
  - Supports ornaments (Sargam only for now). Ornaments can be entered either to RIGHT or LEFT of the note to be ornamented(on one of the lines above the note). You can put dots above/below the ornament pitch to indicate the octave. Lilypond renderer displays the (right hand) ornaments as looped 32th grace notes. **Important note**: Support for ornaments to the 'right' of a note seems to be very poor among software vendors, probably because the 'standard' way to right ornaments is *before* a note. Therefore it is probably advisable to not use the right hand ornaments as it may cause data interchange problems down the line. 
  - For best results it is best to start each main line of music with the barline character ("|")
  - Since DoReMiScript also supports chords such as G, G7, this may cause problems for ornaments that start with an ABC letter. In this case you can delimit the ornament with angle brackets **<>**. Chord symbols take precedence, so that Dm above the main line is interpreted as the chord Dm rather than a Dm ornament.
  - Code base is written in Clojure. Parser is written using Instaparse
	- Since doremi-script is written in Clojure, you can use it from the command line.
    - Command line usage: 
      - To generate lilypond: 
			  - Run doremi-script from the command line to create and open a png file with staff notation
        - Example:
        - echo "12 34# 5 - |" | java -jar target/doremi-script-standalone.jar | lilypond --png -o tmp - ; chromium tmp.png
      - To parse the composition, returning the parsed json:
        - (TODO)
      - Run lilypond on composition.txt
        - (TODO) lilypond -o composition -
      - Create a standalone html page composition.html from composition.txt:
        - (Not implemented)
      - Open DoremiScript file in musescore:
        - (Not implemented) cat composition.txt | doremi2musicxml  > composition.xml ; mscore composition.xml
	- Tip: Align chords and talas over notes, dashes, or barlines. Otherwise they are ignored
	- Tip: You can underline devanagri notes RGD and N to get a flat note. This is standard in Bhatkande's notation.
	- Tip: Doremi-script will automatically align syllables based on notes and slurs.
  - This project borrows ideas from [ABC musical notation](http://en.wikipedia.org/wiki/ABC_notation), [lilypond](http://lilypond.org), and [markdown](http://en.wikipedia.org/wiki/Markdown).
  - Special thanks to George Ruckert, Ali Akbar Khan and the authors of Clojure, Instaparse, react.js, re-frame, and lilypond.

Release Notes:

6/1/2015 Re-write is complete. Javascript/Coffe-script front end has been retired and replaced with a Clojurescript front end. 

5/29/2015 Re-wrote using re-frame framework and refactored app.cljs into views, handlers, and subscriptions. 

5/7/2015  Milestone. Re-written. Front end is an html app written in Clojurescript, Reagent,Bootstrap, Instaparse/cljs. Back end was cleaned up and old gui removed. Front end uses async js and css loading. Async css loading implemented using loadCss method. New version should support offline letter notation rendering via instaparse/cljs. Codebase uses cljc to share code between Clojure on server and Clojurescript in the browser. TODO: port musicXML generation from old js version. Simplified lilypond generation somewhat. 
The clojurescript app uses boot, the Clojure ring app uses leiningen.

4/17/2015 You can add annotations above notes (not barlines yet). You can use alphanumeric plus spaces. Use brackets:  like [hello there]

4/15/2015 Now generates mp3's and plays them using html5 audio tag
 
4/14/2015 New clojure-script front end is useable. http://ragapedia.com points to the new front end [doremi-script-app](http://github.com/rothfield/doremi-script-app) The new front end is a separate github project. You can access the old app [here](http://ragapedia.com:4000)  


[Integration test results](https://rawgithub.com/rothfield/doremi-script/master/test/good_test_results/report.html?https://raw.github.com/user/repo/master/)

   3/14/2014 Note that lilypond 2.12.3 is required in order to get web-friendly staff notation via the lily2image program.
	 I found the old version at this page: http://www.lilypond.org/old-downloads.html  

   3/14/2014  Merged doremi-script with doremi-script-base. doremi-script-base is the older version written in Javascript. The legacy directory contains the older code.

	3/13/2014  Staff notation generation moved from shell script to a java shell call and ajax updated.

   MILESTONE RELEASE Feb 28,2014 - New GUI and multi notation system support
   
     - New GUI is written and up at [http://ragapedia.com](http://ragapedia.com)
	   - GUI is written using [react.js](http://facebook.github.io/react/)
	   - Implemented multi notation system support
		 - Added choice of which system to render notation in. For example, enter notation in numbers and display in Hindi.
	   - Added doremi to notation systems
	   - Chords are now entered in brackets. [Dm7]
	   - Old gui (written using knockout.js) is [here](http://ragapedia.com/doremi-script-gui/index.html#/root)

   MILESTONE RELEASE Feb 12,2014 - Tests look good
   
	   - Status: Lilypond conversion is working. TODO: MusicXML output as
	 in old version.
     - DoremiScript is implented in Clojure and uses Instaparse for parsing.
		 - Front end is written using html, css, and React.js

