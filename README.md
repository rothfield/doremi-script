DoremiScript
============

**DoremiScript** is a small language for creating [letter based musical notation](http://en.wikipedia.org/wiki/Letter_notation). It currently supports western ABC style notation, [Numbered notation](http://http://en.wikipedia.org/wiki/Numbered_musical_notation) using 1234567, [Sargam](http://en.wikipedia.org/wiki/Swara) notation using english letters SrRgGmMPdnN as used at the AACM for notating Ragas, and [Devanagri notation in Bhatkande style](http://en.wikipedia.org/wiki/Musical_notation#India) using Devanagri letters for the sargam letters. **DoremiScript** lets you write letter music notation using an easy-to-read, easy-to-write plain text format, then nicely formats it using css and html. DoRemiScript can also generate standard western notation via a [Lilypond](http://lilypond.org) converter. 

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
    
    
    
                                i            IV  
             3                  +            2          .   
    1)|: (Sr | n) S   (gm Pd) | P - P  P   | P - D  <(nDSn)>) |
               .   
          ban-    su-  ri       ba- ja ra-   hi  dhu- na
    
      0  ~                3            ~       +  .     *  *   
    | P  d   P    d    |  (Pm   PmnP) (g m) | (PdnS) -- g  S | 
      ma-dhu-ra   kan-     nai-        ya      khe-     la-ta
    
       2               0    
                       ~   
    | (d-Pm  g) P  m | r - S :|
       ga-      wa-ta  ho- ri



Sargam HTML Output
------------------
![Screenshot of Sargam rendered in html](https://github.com/rothfield/doremi/raw/master/docs/sargam_screenshot.png "Sargam Screenshot")

Generated Western Staff Notation
--------------------------------
![Converted to western notation](https://github.com/rothfield/doremi/raw/master/docs/western_notation_example.png "")



  - Use a blank line to separate lines of music.
  - ABC style notation is written as follows C C# Db etc
  - Numbered notation is written as follows 1234567 corresponding to the diatonic notes of the C scale, with sharps and flats notated as 2b(Db) and 2#(D#), for example.
  - AACM sargam notation is written as follows SrRgGmMPdDnN for the 12 notes. Sharp and flat notes are also supported as follows: S# R# etc and Pb. This allows proper notation using Sargam of Jazz/Pop/Classical music that uses these notes. For example, in notating the Beatles tune "Yesterday", use P# to indicate the sharpened fifth degree. P# corresponds to G# in the key of C.
  - Note that the AACM style of Sargam notation uses *lowercase* characters to indicate the flattened notes.
  - Devanagri style sargam is written using the devanagri letters सरग़मपधऩस
  for the seven notes SRGmPDN. Use an underline underneath the letter to indicate a kommal note (flat). For sharp Ma, write as म' (note the tick symbol) 
  - left and right parens are used to indicate slurs. Slurs can only be made between pitches. For example, use (SRg)- rather than  (Srg-)
  - Beats are delimited by spaces. If you need more space within the beat(for example, to make room for lyrics), then use angle brackets <> to delimit the beat
  - Dot (.) and asterisk can be used interchangeably to 
indicate octaves. Place dots underneath for lower octave and above for
upper octaves. Use a colon **:** to indicate upper-upper octave or lower-lower octave
  - Rhythm: In this style of notation, derived from the AACM style, the dash (**-**) is used as a rhythmic placeholder. For example, this measure `| C--Eb--G-   A   A   A |` is interpreted as a measure consisting of four beats. The first beat is divided into 8 parts, where the C gets 3/8ths of the beat, Eb gets 3/8ths of a beat and G gets 2/8ths. (Note that 3/8ths of a beat in western 4/4 notation is considered 3/32nds and is written as a dotted 16th. Confused?) The 3 A's are each a single beat. The lilypond generator attempts to support this, but still needs a bit of work.
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
  - DoremiScript grammar is [here](https://github.com/rothfield/doremi/raw/src/grammars/sargam.peg.js)
  - Endings are entered as follows: 1.________ or 2._____ Current lilypond support for endings is poor.
  - It is possible to mix ABC, Devanagri, and AACM notations in the same composition, but each line of notation must be in only one style.
  - Supports ornaments (Sargam only for now). Octaves for ornaments are not supported and the ornament is placed in the column to the RIGHT of the pitch:
  - Code base is written in coffeescript
  - (Programmes only:)Command line tools are available via npm:
    - First install [npm](http://npmjs.org)
    - Then install the **sargam** package:
    - npm install sargam
    - Command line usage: 
      - To generate lilypond:
        - cat composition.txt | sargam 
      - To parse the composition, returning the parsed json:
        - cat composition.txt | sargamparse
      - Run lilypond on composition.txt
        - cat composition.txt | sargam | lilypond -o composition -
  - This project borrows ideas from [ABC musical notation](http://en.wikipedia.org/wiki/ABC_notation), [lilypond](http://lilypond.org), and [markdown](http://en.wikipedia.org/wiki/Markdown).
  - Special thanks to George Ruckert, Ali Akbar Khan and the authors of peg.js, lilypond, and coffeescript.

Ornament example:
```Title: test

   NRSNS
| S```
 
