fraction_to_musicxml_step_and_dotes = (frac) ->
  "2/1":"<type>half</type>"
  "3/1":"<type>half</type><dot/>"
  "4/1":"<type>whole</type>"
  "5/1":"<type>whole</type><dot/><dot/>"
  "1/1":"<type>quarter</type>"
  "1/1":"<type>quarter</type>"
  "1/1":"<type>quarter</type>"
  "1/1":"<type>quarter</type>"
  "1/2":"<type>eighth</type>"
  "1/3": "<type>eighth</type>"  # 1/3 1/5 1/7 all 8th notes so one beat will beam together
  "1/9":"<type>eighth</type>"
  "1/11":"<type>eighth</type>"
  "1/13":"<type>eighth</type>"
  "1/5":"sixteenth"
  "2/5":"<type>eighth</type>"
  "3/5":"<type>eighth</type><dot/>" #TODO should be tied
  "4/5":"<type>quarter</type>" #TODO should be tied
  "5/5":"<type>quarter</type>"
  "6/6":"<type>quarter</type>"
  "7/7":"<type>quarter</type>"
  "8/8":"<type>quarter</type>"
  "9/9":"<type>quarter</type>"
  "10/10":"<type>quarter</type>"
  "11/11":"<type>quarter</type>"
  "12/12":"<type>quarter</type>"
  "13/13":"<type>quarter</type>"
  "1/7": "<type>thirtysecond</type>" # ??? correct???hhhhhhhhhh
  "2/7": "<type>sixteenth</type>" # ??? correct???hhhhhhhhhh
  "3/7": "<type>sixteenth</type><dot/>" # ??? correct???hhhhhhhhhh
  "4/7": "<type>eighth</type>" # ??? correct???hhhhhhhhhh
  "5/7": "<type>eighth</type><dot/><dot/>" # ??? correct???hhhhhhhhhh
  "6/7": "<type>eighth</type><dot/><dot/>" # ??? correct???hhhhhhhhhh
  "6/8": "<type>eighth</type><dot/>"
  "2/3": "<type>quarter</type>"
  "2/8": "<type>sixteenth</type>"
  "3/8": "<type>sixteenth</type><dot/>"  # 1/4 + 1/8
  "5/8": "<type>eighth</type>"   # TODO: WRONG
  "4/8": "<type>eighth</type>"
  "7/8": "<type>eighth</type><dot/><dot/>" # 1/2 + 1/4 + 1/8
  "1/6": "<type>sixteenth</type>"
  "2/6": "<type>eighth</type>"
  "3/6": "<type>quarter</type>" # not sure??
  "4/6":"<type>quarter</type>" # NOT SURE ????
  "5/6":"<type>eighth</type><dot/><dot/>" #  WRONGnot sure TODO??
  "2/2":"<type>quarter</type>"
  "3/3":"<type>quarter</type>"
  "4/4":"<type>quarter</type>"
  "8/8":"<type>quarter</type>"
  "1/4":"<type>sixteenth</type>"
  "2/4":"<type>eighth</type>"
  "3/4":"<type>eighth</type><dot/>"
  "3/8":"<type>sixteenth</type><dot/>"
