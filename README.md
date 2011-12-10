DoremiScript
============

**DoremiScript** is a small language for creating [letter based musical notation](http://en.wikipedia.org/wiki/Letter_notation). It currently supports western ABC style notation, [Numbered notation](http://http://en.wikipedia.org/wiki/Numbered_musical_notation) using 1234567, [Sargam](http://en.wikipedia.org/wiki/Swara) notation using english letters SrRgGmMPdnN as used at the AACM for notating Ragas, and [Devanagri notation in Bhatkande style](http://en.wikipedia.org/wiki/Musical_notation#India) using Devanagri letters for the sargam letters. **DoremiScript** lets you write letter music notation using an easy-to-read, easy-to-write plain text format, then nicely formats it using css and html. DoremiScript can also generate standard western notation via a [Lilypond](http://lilypond.org) converter. 

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
    
    
              
                                   i            IV         . 
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
![Screenshot of DoremiScript rendered in html](https://github.com/rothfield/doremi-script/raw/master/docs/bansuri_in_html_screenshot.png "Sargam Screenshot")

Generated Western Staff Notation
--------------------------------
![Converted to western notation](https://github.com/rothfield/doremi-script/raw/master/docs/bansuri_in_western_notation.png "")



  - Use a blank line to separate lines of music.
  - Start lines of music with a barline "|", for example,  | C D E F | . Otherwise be sure to include a barline or dash in the main line of music.
  - ABC style notation is written as follows C C# Db etc
  - Numbered notation is written as follows 1234567 corresponding to the diatonic notes of the C scale, with sharps and flats notated as 2b(Db) and 2#(D#), for example.
  - AACM sargam notation is written as follows SrRgGmMPdDnN for the 12 notes. Sharp and flat notes are also supported as follows: S# R# etc and Pb. This allows proper notation using Sargam of Jazz/Pop/Classical music that uses these notes. For example, in notating the Beatles tune "Yesterday", use P# to indicate the sharpened fifth degree. P# corresponds to G# in the key of C.
  - Note that the AACM style of Sargam notation uses *lowercase* characters to indicate the flattened notes.
  - Devanagri style sargam is written using the devanagri letters सरग़मपधऩस
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
  - Chord symbols are supported
  - Syllables must be entered directly under a pitch.
  - For syllables to show up properly in generated western notation, slurs
should be used to indicate the melismas
  - Use | for single barline, || for double barline, |: and :| for left and right repeats
  - If need be, you can have multiple upper or lower lines.
  - Asterisk (*) can be used instead of a . to indicate octave
  - Use the tilde character **~** to indicate an ornament which is displayed as 
# &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    &#x1D19D;&#x1D19D;
  - Supports tala markings +023456 for notating classical North Indian rhythmical cycles such as teental. The **+** indicates (usually) the first beat of the rhythmical cycle. The tala markings indicate the claps(taali) and non-claps(kaali) that delimit the rhythmic cycle.  
  - Generates [lilypond](http://lilypond.org) as an interchange format, which can be used to generate western staff notation and midi
  - Parser is easy to modify and is generated from a PEG grammar using a parsing expression grammar [peg.js])https://github.com/dmajda/pegjs)
  - DoremiScript grammar is [here](https://raw.github.com/rothfield/doremi-script/master/src/grammars/doremiscript.peg.js)
  - Endings are entered as follows: 1.________ or 2._____ Current lilypond support for endings is poor.
  - It is possible to mix ABC, Devanagri, and AACM notations in the same composition, but each line of notation must be in only one style.
  - Supports ornaments (Sargam only for now). Ornaments can be entered either to RIGHT or LEFT of the note to be ornamented(on one of the lines above the note). You can put dots above/below the ornament pitch to indicate the octave. Lilypond renderer displays the (right hand) ornaments as looped 32th grace notes. Be sure to include a "|"(barline) or "-"(dash) in the main line of music, otherwise the parser has problems.
  - Since DoReMiScript also supports chords such as G, G7, this may cause problems for ornaments that start with an ABC letter. In this case you can delimit the ornament as follows: <GRmGPmGm>. Chord symbols take precedence, so that Dm above the main line is interpreted as the chord Dm rather than a Dm ornament.
  - Code base is written in coffeescript
  - (Programmes only:)Command line tools are available via npm:
    - First install [npm](http://npmjs.org)
    - Then install the **doremi-script** package:
    - npm install doremi-script
    - Command line usage: 
      - To generate lilypond:
        - cat composition.txt | doremi2ly
      - To parse the composition, returning the parsed json:
        - cat composition.txt | doremiparse
      - Run lilypond on composition.txt
        - cat composition.txt | doremi2ly | lilypond -o composition -
      - Create a standalone html page composition.html from composition.txt:
        - cat composition.txt | doremi2htmldoc > composition.html
  - This project borrows ideas from [ABC musical notation](http://en.wikipedia.org/wiki/ABC_notation), [lilypond](http://lilypond.org), and [markdown](http://en.wikipedia.org/wiki/Markdown).
  - Special thanks to George Ruckert, Ali Akbar Khan and the authors of peg.js, lilypond, and coffeescript.
  - The online version is best used with Firefox.


 
