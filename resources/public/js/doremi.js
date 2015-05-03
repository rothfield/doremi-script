(function () {
  "use strict";
  var debug = false;
  window.loadTimeout = null;
  /** Save typing **/
  var span = window.React.DOM.span;
  var div = window.React.DOM.div;
  var textarea = window.React.DOM.textarea;
  var a = window.React.DOM.a;
  var input = window.React.DOM.input;
  var pre = window.React.DOM.pre;
  var form = window.React.DOM.form;
  var img = window.React.DOM.img;
  var button = window.React.DOM.button;
  var select = window.React.DOM.select;
  var label = window.React.DOM.label;
  var h3 = window.React.DOM.h3;
  var option = window.React.DOM.option;
  var createClass = window.React.createClass;
  var rest = function (x) {
    // Analogous to clojure rest function
    return x.slice(1);
  };
  var extractTitle = function (txt) {
    // extract title from attributes.
    // Title will be found like:
    //  title: my title or Title: my title
    var patt = /^title:( )*([^\n]+)( )*/mi;
    var z = patt.exec(txt);
    if (z) {
      return z[2].trim();
    } else {
      return null;
    }
  }
  var sanitize = function (name) {
      // Sanitize file name
      if (name === null) {
        return null;
      } else {
        return name.replace(/[^0-9A-Za-z.\_]/g, '_')
          .toLowerCase();
      }
    }

  var doremiTextToHash = function (txt) {
    // Returns filename without suffix
    // txt is doremi text
    // A bit of caching. Using the txt, generate the file
    // name on the server. Does MD5 on the txt
		// Uses first 8 chars of MD5
    var title = sanitize(extractTitle(txt));
    var titleSnippet = "";
    if (title) {
      titleSnippet = title + "-";
    }
    return titleSnippet + window.MD5(txt).substring(0,8)
  }

  var first = function (x) {
    // Analogous to clojure function
    return x[0];
  };
  var last = function (x) {
    // Analogous to clojure function
    return x[x.length - 1];
  };

  var second = function (x) {
    // Analogous to clojure function
    return x[1];
  };

  var assert = function (condition, message) {
    if (!condition) {
      throw message || "Assertion failed";
    }
  };

  var isArray = function (x) {
    return (Object.prototype.toString.call(x) ===
      '[object Array]');
  };

  var isA = function (kind, x) {
    // Our data looks like ["note" blah blah blah]
    // Usage is then isA("note",myObject)
    return (isArray(x) && (x[0] === kind));
  };

  var merge = function (o1, o2) {
    if (o1 === null || o2 === null) {
      return o1
    }

    for (var key in o2) {
      if (o2.hasOwnProperty(key)) {
        o1[key] = o2[key];
      }
    }
    return o1;
  }


  var classNameFor = function (item) {
    assert(isArray(item))
    return item[0].replace(/-/g, '_');
  }

  var DoremiBox = createClass({

    startParseTimer: function () {
      setInterval(this.parse, 2000)
    },

    handleDoremiTextChange: function () {
      // don't need to do anything here. There is a timer that
      // periodically parses the text input and renders
      // the composition in HTML 
    },

    sanitize: function (name) {
      // Sanitize file name
      if (name === null) {
        return null;
      } else {
        return name.replace(/[^0-9A-Za-z.\_]/g, '_')
          .toLowerCase();
      }
    },
    baseURL: "compositions/",

    getPathWithoutExtension: function (txt) {
      // A bit of caching. Using the txt, generate the file
      // name on the server. Does MD5 on the txt
      var title = sanitize(extractTitle(txt));
      var titleSnippet = "";
      if (title) {
        titleSnippet = title + "-";
      }
      return this.baseURL + titleSnippet + window.MD5(
        txt)
    },



    ajaxCall: function (txt, flag, kind) {
			this.flag=flag /// FUNKY. TODO get rid of flag altogether??
			this.txt=txt // FUNKY
				// Handle parse and generate staff notation
      var myVerb
			if (flag) {
         myVerb="doremi-server/generate_staff_notation"
			} else {
					myVerb="doremi-server/parse"
			}
      if (debug) {
        console.log("ajaxCall");
      }
      if (this.state.data.ajaxIsRunning) {
        return
      }
      if (txt === "") {
        return
      }
			var z= merge(this.state.data, { ajaxIsRunning:true})
      this.setState({data: z})

      if (debug) {
        console.log("ajax call");
      }
      $.ajax({
        url: this.props.urlBase + myVerb,
        dataType: 'json', //Type of data expected back from server
        type: 'POST',
        data: {
          src: txt,
          kind: kind
        },
        error: function () {
          this.setState({
            data: merge(this.state.data, {ajaxIsRunning: false})
					})
        }.bind(this),
        success: function (myData) {
         var hashed = doremiTextToHash(this.txt);
		var path="./compositions/" + hashed
      var staffNotationPath = path + ".png"
      var midiURL = path + ".mid"
					
          if (debug) {
            console.log("success")
          }
		var merge3={}
					if (this.flag) {
							merge3.staffNotationPath=staffNotationPath
									merge3.midiURL=midiURL
					}
          var merge1 = {}
          var merge2 = {}
          var myMap2
          var myMap = {
            ajaxIsRunning: false,
            parsed: myData.parsed,
            parseErrors: myData.error,
            attributes: myData.attributes
          }
          if (myData.attributes) {
            merge1 = {
              kind: myData.attributes.kind
            }
            if (this.state.data.renderAs === "") {
              merge2 = {
                renderAs: myData.attributes.kind
              }
            }
          }
          myMap2 = merge (merge(myMap, merge(merge1, merge2)), merge3) // TODO
          if (myData.lilypond) {
            myMap2.lilypond = myData.lilypond
          }
          assert(myData.error || !myData.attributes ||
            myData.attributes.kind);
          this.setState({
            data: merge(this.state.data, myMap2)
          })
          if (debug) {
            console.log("in success, state.data",
              this.state.data)
          }
          var curSrc = $('#the_area')
            .val();
          curSrc = curSrc
        }.bind(this)
      });

    },
    displayName: 'DoremiBox',

    handleGenerateStaffNotationButton: function () {
      if (this.state.data.ajaxIsRunning) {
        return
      }
      var txt = $('#the_area')
        .val();
      if (txt === "") {
        return
      }
//      var oldData = this.state.data;
//      var hashed = doremiTextToHash(txt);
//			var path="./compositions/" + hashed
//      oldData.staffNotationPath = path + ".png"
//      oldData.midiURL = path + ".mid"
//      this.setState({
//        data: oldData
//      });
      this.ajaxCall(txt, true, this.state.data.kind);
			// Let react do it??? reloadStaffNotationImage() // or let react do it
      //setTimeout(reloadStaffNotationImage, 5000);
    },
    handleToggleStaffNotation: function () {
      var data = this.state.data;
      data.showStaffNotation = !data.showStaffNotation;
      this.setState({
        data: data
      });
    },
    kinds: ["", "abc-composition", "doremi-composition",
      "hindi-composition",
      "number-composition", "sargam-composition"
    ],
    handleRenderAsChange: function (x) {
      // Force redraw/re-parse by clearing lastTxtParsed
      this.setState({
        data: merge(this.state.data, {
          renderAs: x,
          lastTxtParsed: ""
        })
      })
    },
    //   this.setState({ data: merge(this.state.data, { renderAs: x })
    //
    handleKindChange: function (x) {
      // Force redraw/re-parse by clearing lastTxtParsed
      this.setState({
        data: merge(this.state.data, {
          kind: x,
          lastTxtParsed: "",
          staffNotationPath: "/images/blank.png",  // clear it TODO: improve

        })
      })
    },
    handleShowHideLilypond: function () {
      var data = this.state.data;
      data.showLilypond = !data.showLilypond;
      this.setState({
        data: data
      });
    },
    getInitialState: function () {
      var myState = {
        data: {
          ajaxIsRunning: false,
          midiURL: null,
          displayKind: "",
          kind: "",
          renderAs: "",
          id: null,
          resources: [],
					// TODO: Maybe get rid of staffNotationPath.- 
          staffNotationPath: "/images/blank.png",
          parsed: [],
          file_id: "",
          src: "Key: Ab\n\nHi john\nHow are you\n\n | SSS\n\n" +
            "   .:\n" +
            "SSSSS\n" +
            ".:",
          parseErrors: null,
          lilypond: null,
          showStaffNotation: true,
          showLilypond: false,
          lastTxtParsed: ""
        }
      };
      //  myState.data.staffNotationPath = this.getStaffNotationPath(
      //   $('#the_area')
      //  .val());
      return myState;
    },
    parse: function () {
      var current = $('#the_area')
        .val()
      if (debug) {
        console.log("parse")
      }
      if (this.state.data.ajaxIsRunning) {
        return
      }
      if (this.state.data.lastTxtParsed === current) {
        return
      }
      if (current === "") {
        return
      }
      this.setState({
        data: merge(this.state.data, {
          lastTxtParsed: current
        })
      })
      this.ajaxCall($('#the_area')
        .val(), false, this.state.data.kind);
    },
    componentDidMount: function () {
      if (debug) {
        console.log(
          "componentDidMount, starting parseTimer")
      }
      this.startParseTimer();
    },
    render: function () {
      return (
        div({
            className: "doremiBox"
          },
          Header(null),
          //ExampleBox(null),
          // FileUploadForm({}),
          div({
              className: "controls"
            },
            SelectKindBox({
              kinds: this.kinds,
              kind: this.state.data.kind,
              handleKindChange: this.handleKindChange
            }),
            RenderAsBox({
              kinds: this.kinds,
              renderAs: this.state.data.renderAs,
              handleRenderAsChange: this.handleRenderAsChange
            }),
            GenerateStaffNotationButton({
              ajaxIsRunning: this.state.data.ajaxIsRunning,
              handleGenerateStaffNotationButton: this
                .handleGenerateStaffNotationButton
            }),
            ShowHideLilypond({
              handleShowHideLilypond: this.handleShowHideLilypond
            }),
            Midi({
              midiURL: this.state.data.midiURL
            }),
            ToggleStaffNotation({
              handleToggleStaffNotation: this.handleToggleStaffNotation
            }),
            VisualTestSuite(null),
            Help(null)
          ),
          DoremiTextAreaBox({
            kind: this.state.data.kind,
            handleDoremiTextChange: this.handleDoremiTextChange
          }),
          ParseErrors({
            parseErrors: this.state.data.parseErrors
          }),
          Lilypond({
            lilypond: this.state.data.lilypond,
            showLilypond: this.state.data.showLilypond
          }),
          StaffNotationDisplay({
            staffNotationPath: this.state.data.staffNotationPath,
            //data: this.state.data
          }),
          Composition({
            parsed: this.state.data.parsed,
            kind: this.state.data.renderAs
          })
        )
      );
    }
  });
  var Header = createClass({
    example: "Enter letter music notation using 1234567,CDEFGABC, DoReMi (using drmfslt or DRMFSLT), SRGmPDN, or devanagri: " +
      "सर ग़म म'प धऩ" + "\n\n",

    example2: ["Title: Happy Birthday",
      "Filename: happy_birthday_in_D",
      "Key: c",
      "Author: Traditional",
      "TimeSignature: 3/4",
      "",
      "|: P-P | D P S | N - PP | D P R | S - PP | ",
      "   . .   . .     .   ..   . .         ..  ",
      "ha-ppy birth-day to you ha-ppy birth-day to you ha-ppy",
      "",
      "                                     1.____   2.______",
      "| P G S | (N - - | D) - mm | G S R | S -   :| S -P DP | n",
      "                                                 . ..   .",
      "  birth-day dear Jim ha-ppy birth-day to you  you and ma-ny more",
    ].join("\n"),

    render: function () {
      return h3({
        title: "Example",
      }, this.example)
    }
  })
  var Midi = createClass({
    // Uses jasmid midi player.
    playFile: function () {
				if (debug) {
				console.log("playFile")
				}
      window.play("./" + this.props.midiURL)
    },
    render: function () {
      var className = ""
      if (!this.props.midiURL) {
        className = "hidden"
      }
      return a({
        title: "Plays MIDI file",
        className: className,
        onClick: this.playFile,
        href: "#"
      }, "Play MIDI File(Turn Volume Up!)")
    }
  })

  var Help = createClass({
    render: function () {
      return a({
        title: "Opens in new window",
        target: "_blank",
        href: "https://raw.github.com/rothfield/doremi-script#readme"
      }, "Help")
    }
  })

  var VisualTestSuite = createClass({
    render: function () {
      return a({
        title: "Opens in new window",
        target: "_blank",
        href: "https://rawgithub.com/rothfield/doremi-script/master/test/good_test_results/report.html"
      }, "Visual test suite")
    }
  })

  var SelectKindBox = createClass({
    handleChange: function (x) {
      this.props.handleKindChange(x.target.value)
    },
    displayName: 'SelectKindBox',
    render: function () {
      var options = this.props.kinds.map(
        function (x, index) {
          var y
          if (x === "abc-composition") {
            y = "ABC"
          } else {
            y = x
          }
          if (x === "hindi-composition") {
            y = y + "( स र ग़ म म' प ध ऩ )"
          }
          return option({
            value: x,
            key: index
          }, y.replace("-composition", ""))
        }.bind(this))
      return div({
          className: "selectNotationBox"
        },
        label({
          htmlFor: "selectNotation"
        }, "Enter Notation as: "),
        select({
            id: "selectNotation",
            value: this.props.kind,
            onChange: this.handleChange
          },
          options
        )
      )
    }
  });

  var ParseErrors = createClass({
    displayName: 'ParseErrors',
    render: function () {
      var extra = "";
      if (this.props.parseErrors === null) {
        extra = " hidden";
      }
      return div({
          className: "compositionParseFailed" + extra,
        },
        pre(null, this.props.parseErrors));
    }

  });
  var RenderAsBox = createClass({
    handleChange: function (x) {
      this.props.handleRenderAsChange(x.target.value)
    },
    displayName: 'RenderAsBox',
    render: function () {
      var options = this.props.kinds.map(
        // TODO: dry
        function (x, index) {
          var y
          if (x === "abc-composition") {
            y = "ABC"
          } else {
            y = x
          }
          if (x === "hindi-composition") {
            y = y + "( स र ग़ म म' प ध ऩ )"
          }
          return option({
            value: x,
            key: index
          }, y.replace("-composition", ""))
        }.bind(this))

      return div({
          className: "RenderAsBox"
        },
        label({
          htmlFor: "renderAs"
        }, "Render as:"),
        select({
            id: "renderAs",
            value: this.props.renderAs,
            onChange: this.handleChange
          },
          options
        )
      )
    }
  });


  var Lilypond = createClass({
    displayName: 'Lilypond',
    render: function () {
      if (debug) {
					console.log("Lilypond")
			}
      var extra = "";
      if (!this.props.showLilypond) {
        extra = " hidden";
      }
      return div({
        className: "lilypondDisplay" + extra,
      }, this.props.lilypond);
    }

  });

  // ["ornament",["ornament-pitch","D"],"after"] 
  var OrnamentPitch = createClass({

    class_for_ornament_pitch_octave: function (
      octave_num) {
      if (octave_num === null) {
        return "octave0";
      }
      if (octave_num < 0) {
        return "lower_octave_" + (octave_num * -1);
      }
      if (octave_num > 0) {
        return "upper_octave_" + octave_num
      }
      return "octave0";
    },

    render: function () {
      var octave;
      var item = this.props.item;
      var items = rest(rest(item))
      var filtered = items.filter(function (x) {
        return isA("octave", x)
      })
      if (filtered.length > 0) {
        octave = filtered[0][1]
      } else {
        octave = 0
      }
      var src = renderPitchInKind(item[1], this.props.kind)
      return span({
        className: "ornament_item " +
          this.class_for_ornament_pitch_octave(
            octave),
        dangerouslySetInnerHTML: {
          __html: src
        }
      })
    }
  })
  var Ornament = createClass({

    render: function () {
      var item = this.props.item;
      var placement = last(item)
      var items = rest(item);
      var filtered = items.filter(function (x) {
        return isArray(x)
      })
      var ary = filtered.map(drawItem.bind(this));
      return span({
        className: "upper_attribute" + " " +
          classNameFor(item) + " " + "placement_" +
          placement,
      }, ary)
    }
  });


  var BeginSlur = createClass({
    displayName: 'BeginSlur',

    render: function () {
      return span({
        className: "slur",
        id: this.props.id
      });
    }
  });

  var EndSlur = createClass({
    displayName: 'EndSlur',

    render: function () {
      return span({
        className: "ignore-end-slur"
      });
    }
  });

  var PitchItem = createClass({
    displayName: 'PitchItem',

    render: function () {
      return span({
        className: classNameFor(this.props.item)
      }, this.props.src);
    }
  });

  var PitchSpan = createClass({
    displayName: 'PitchSpan',

    render: function () {
      return span({
        className: "note  pitch",
        dangerouslySetInnerHTML: {
          __html: this.props.src
        }
      });
    }
  });

  var Octave = createClass({
    class_for_octave: function (octave_num) {
      if (octave_num === null) {
        return "octave0";
      }
      if (octave_num < 0) {
        return "lower_octave_" + (octave_num * -1);
      }
      if (octave_num > 0) {
        return "upper_octave_" + octave_num +
          " upper_octave_indicator";
      }
      return "octave0";
    },
    bullet: "&bull;",
    displayName: 'Octave',
    render: function () {
      var item = this.props.item;
      if (debug) {
        console.log(item);
      }
      //assert(isA("octave", item));
      var src = this.bullet;
      return span({
        className: this.class_for_octave(second(item)),
        dangerouslySetInnerHTML: {
          __html: src
        }
      });
    }
  });
  var needs_underline = {
    "Db": true,
    "Eb": true,
    "Ab": true,
    "Bb": true
  }
  var Pitch = createClass({
    displayName: 'Pitch',
    render: function () {
      //  if rendering in devanagri, add underline for flat
      // notes Db Eb Ab Bb
      var pitch = this.props.item;
      // TODO: move this into PitchSpan and style sharp and flat
      assert(isA("pitch", pitch));
      var items = rest(rest(pitch));
      var kommalIndicator = []
      if ((this.props.kind === "hindi-composition") &&
        (needs_underline[second(pitch)])
      ) {
        kommalIndicator = span({
          key: 99,
          className: "kommalIndicator"
        }, "_");
      }

      var beginSlurId = items.filter(function (x) {
        return ("begin-slur-id" === first(x));
      });
      var endSlurId = items.filter(function (x) {
        return ("end-slur-id" === first(x));
      });
      if (beginSlurId.length > 0) {
        this.props.slurId = beginSlurId[0][1];
      }
      if (endSlurId.length > 0) {
        this.props.endSlurId = endSlurId[0][1];
      }
      var ary = items.map(drawItem.bind(this));
      var pitchAry = renderPitchInKind(second(pitch),
        this.props.kind)
      var pitch1 = pitchAry[0]
      var alteration = pitchAry[1]
      var pitchSpan = PitchSpan({
        key: items.length + 1,
        src: pitch1,
      });
      var ary2 = ary.concat([pitchSpan]);
      var ary3
      if (alteration) {
        var alterationSpan = span({
          key: items.length + 2,
          className: "note pitch alteration",
          dangerouslySetInnerHTML: {
            __html: alteration
          }
        })
        ary3 = ary2.concat([alterationSpan])
      } else {
        ary3 = ary2
      }
      var noteWrapperProps = {
        className: "note_wrapper",
        key: this.props.key
      }
      if (endSlurId.length > 0) {
        merge(noteWrapperProps, {
          'data-begin-slur-id': endSlurId[0][1]
        })
      }
      return span(
        noteWrapperProps,
        ary3.concat(kommalIndicator));
    },

  });
  var Beat = createClass({

    displayName: 'Beat',

    render: function () {
      var looped = "";
      var beat = this.props.beat;
      var beatItems = rest(beat);
      var count = beatItems.reduce(function (accum, cur) {
        if (("pitch;dash")
          .indexOf(cur[0]) > -1) {
          return (accum + 1);
        } else {
          return accum;
        }
      }, 0);
      if (count > 1) {
        looped = " looped";
      }
      if (debug) {
        console.log("beat", beat, count);
      }
      var items = rest(beat);
      var ary = items.map(drawItem.bind(this));
      assert(isA("beat", beat));
      return span({
        className: "beat " + looped
      }, ary);
    }
  });


  var Measure = createClass({

    displayName: 'Measure',

    render: function () {
      var measure = this.props.measure;
      var items = rest(measure);
      var ary = items.map(drawItem.bind(this));
      // console.log("render, measure is", measure);
      assert(isA("measure", measure));
      return span({
        className: "measure"
      }, ary);
    }
  });

  if (false) {
    return FileUploadForm
  }
  var FileUploadForm = createClass({

    displayName: 'FileUploadForm',
    render: function () {
      return form({
          action: "/file",
          method: "post",
          encType: "multipart/form-data"
        },
        input({
          name: "file",
          type: "file",
          size: "20"
        }),
        input({
          type: "submit",
          name: "submit",
          value: "submit"
        })
      );
    }
    //   <input name="file" type="file" size="20" />
    //<input type="submit" name="submit" value="submit" />
    //</form>

  });

  var BarlineAux = createClass({

    displayName: 'BarlineAux',

    render: function () {
      return span({
        className: "note barline",
        dangerouslySetInnerHTML: {
          __html: lookupBarlineTable[first(this.props
            .item)]
        }
      })
    }
  })


  var Barline = createClass({

    displayName: 'Barline',

    render: function () {
      var barline = this.props.barline;
      assert(isA("barline", barline));
      var ary = rest(barline)
        .map(drawItem.bind(this));
      return span({
        className: "note_wrapper",
        key: this.props.key
      }, ary);
    }
  });

  /*
 . The Unicode character ♭(U+266D) is the flat sign. Its HTML entity is &#9837;.
In Unicode, the sharp symbol (♯) is at code point U+266F. Its HTML entity is &#9839;. The symbol for double sharp (double sharp) is at U+1D12A (so &#119082;). These characters may not display correctly in all fonts.
*/
  var sharp_symbol = "&#9839;";
  var flat_symbol = "&#9837;";
  var lookup_simple = function (str) {
    var LOOKUP;
    LOOKUP = {
      "b": "b",
      "#": "#",
      ".": "&bull;",
      "*": "&bull;",
      "|:": "|:",
      "~": "~",
      ":|": ":|",
      "|": "|",
      "||": "||",
      "%": "%",
      "|]": "|",
      "[|": "|"
    };
    return LOOKUP[str];
  };
  if (false) {
    console.log(lookup_simple)
  }
  var lookup_html_entity = function (str) {
    var LOOKUP;
    LOOKUP = {
      "b": "&#9837;",
      "#": "&#9839;",
      ".": "&bull;",
      "*": "&bull;",
      "|:": "&#x1d106",
      "~": "&#x1D19D&#x1D19D",
      ":|": "&#x1d107",
      "|": "&#x1d100",
      "||": "&#x1d101",
      "%": "&#x1d10E",
      "|]": "&#x1d102",
      "[|": "&#x1d103"
    };
    return LOOKUP[str];
  };
  if (false) {
    console.log(lookup_html_entity)
  }

  var lookup1 = {
    "Cb": ["S", flat_symbol],
    "C": ["S"],
    "C#": ["S", sharp_symbol],
    "Db": ["r"],
    "D": ["R"],
    "D#": ["R", sharp_symbol],
    "Eb": ["g"],
    "E": ["G"],
    "E#": ["G", sharp_symbol],
    "F": ["m"],
    "F#": ["M"],
    "Gb": ["P", flat_symbol],
    "G": ["P"],
    "G#": ["P", sharp_symbol],
    "Ab": ["d"],
    "A": ["D"],
    "A#": ["D", sharp_symbol],
    "Bb": ["n"],
    "B": ["N"],
    "B#": ["N", sharp_symbol]
  };

  var lookupNumber = {
    "Cb": ["1", flat_symbol],
    "C": ["1"],
    "C#": ["1", sharp_symbol],
    "Db": ["2", flat_symbol],
    "D": ["2"],
    "D#": ["2", sharp_symbol],
    "Eb": ["3", flat_symbol],
    "E": ["3"],
    "E#": ["3", sharp_symbol],
    "F": ["4"],
    "F#": ["4", sharp_symbol],
    "Gb": ["5", flat_symbol],
    "G": ["5"],
    "G#": ["5", sharp_symbol],
    "Ab": ["6", flat_symbol],
    "A": ["6"],
    "A#": ["6", sharp_symbol],
    "Bb": ["7", flat_symbol],
    "B": ["7"],
    "B#": ["7", sharp_symbol]
  };
  var lookupABC = {
    "Cb": ["C", flat_symbol],
    "C": ["C"],
    "C#": ["C", sharp_symbol],
    "Db": ["D", flat_symbol],
    "D": ["D"],
    "D#": ["D", sharp_symbol],
    "Eb": ["E", flat_symbol],
    "E": ["E"],
    "E#": ["E", sharp_symbol],
    "F": ["F"],
    "F#": ["F", sharp_symbol],
    "Gb": ["G", flat_symbol],
    "G": ["G"],
    "G#": ["G", sharp_symbol],
    "Ab": ["A", flat_symbol],
    "A": ["A"],
    "A#": ["A", sharp_symbol],
    "Bb": ["B", flat_symbol],
    "B": ["B"],
    "B#": ["B", sharp_symbol]
  };
  var lookupDoReMi = {
    "Cb": ["D", flat_symbol],
    "C": ["D"],
    "C#": ["D", sharp_symbol],
    "Db": ["R", flat_symbol],
    "D": ["R"],
    "D#": ["R", sharp_symbol],
    "Eb": ["M", flat_symbol],
    "E": ["M"],
    "E#": ["M", sharp_symbol],
    "F": ["F"],
    "F#": ["F", sharp_symbol],
    "Gb": ["S", flat_symbol],
    "G": ["S"],
    "G#": ["S", sharp_symbol],
    "Ab": ["L", flat_symbol],
    "A": ["L"],
    "A#": ["L", sharp_symbol],
    "Bb": ["T", flat_symbol],
    "B": ["T"],
    "B#": ["T", sharp_symbol]
  };

  var s = "स"
  var r = "र"
  var g = "ग़"
  var m = "म"
  var p = "प"
  var d = "ध"
  var n = "ऩ"
  var tick = "'"

  var lookupHindi = {
    "Cb": s + flat_symbol,
    "C": s,
    "C#": s + sharp_symbol,
    "Db": r,
    "D": r,
    "D#": r + sharp_symbol,
    "Eb": g,
    "E": g,
    "E#": g + sharp_symbol,
    "F": m,
    "F#": m + tick,
    "Gb": p + flat_symbol,
    "G": p,
    "G#": p + sharp_symbol,
    "Ab": d,
    "A": d,
    "A#": d + sharp_symbol,
    "Bb": n,
    "B": n,
    "B#": n + sharp_symbol
  }


  /*
reverse-final-barline = <'[' "|">
final-barline = <'|' ']'>
double-barline = <'|' '|'>
single-barline = <'|' ! ('|' | ']' | ':')>
left-repeat = <"|:">
right-repeat = <":|">
*/
  var lookupBarlineTable = {
    "single-barline": "&#x1d100",
    "double-barline": "&#x1d101",
    "left-repeat": "&#x1d106",
    "mordent": "&#x1D19D&#x1D19D",
    "right-repeat": "&#x1d107",
    "final-barline": "&#x1d102",
    "reverse-final-barline": "&#x1d103"
  };



  var renderPitchInKind = function (pitch, kind) {
    switch (kind) {
    case "sargam-composition":
      return lookup1[pitch]
    case "number-composition":
      return lookupNumber[pitch]
    case "abc-composition":
      return lookupABC[pitch]
    case "doremi-composition":
      return lookupDoReMi[pitch]
    case "hindi-composition":
      return lookupHindi[pitch]
    default:
      return lookup1[pitch]
    }
  }

  var drawItem = function (item, index) {
    if (debug) {
      console.log("drawItem, item=", item);
    }
    assert(isArray(item));
    assert(this.props.kind);
    var key = item[0];
    switch (key) {
    case "single-barline":
    case "left-repeat":
    case "right-repeat":
    case "reverse-final-barline":
    case "final-barline":
    case "double-barline":
      return BarlineAux({
        item: item,
        key: index
      })
    case "ornament-pitch":
      return OrnamentPitch({
        kind: this.props.kind,
        item: item,
        key: index
      });
    case "ornament":
      return Ornament({
        kind: this.props.kind,
        item: item,
        key: index
      });

    case "lyrics-section":
      return LyricsSection({
        lyricsSection: item,
        key: index
      });
    case "stave":
      return NotesLine({
        kind: this.props.kind,
        item: item[1],
        key: index
      });
    case "end-slur-id":
    case "begin-slur":
    case "attribute-section":
      return null;
    case "octave":
      if (second(item) === 0) {
        return null;
      } else {
        return Octave({
          key: index,
          item: item,
          kind: this.props.kind
        });
      }
      break;
    case "begin-slur-id":
      return BeginSlur({
        key: index,
        item: item,
        id: this.props.slurId // Passed from Pitch!
      });
    case "end-slur":
      return EndSlur({
        key: index,
        item: item,
      });

    case "chord":
    case "tala":
    case "ending":
      return PitchItem({
        key: index,
        item: item,
        src: item[1],
        kind: this.props.kind
      });
    case "mordent":
      return PitchItemWithEntity({
        key: index,
        item: item,
        src: "&#x1D19D&#x1D19D",
        kind: this.props.kind
      });

    case "syl":
      return PitchItem({
        key: index,
        item: item,
        src: item[1],
        kind: this.props.kind
      });
      //case "upper-line-dot":
      //case "mordent":
    case "line-number":
      return LineItem({
        key: index,
        item: item,
        src: item[1] + ")",
        kind: this.props.kind
      });
    case "barline":
      return Barline({
        key: index,
        barline: item,
        kind: this.props.kind
      });
    case "pitch":
      return Pitch({
        item: item,
        key: index,
        kind: this.props.kind
      });
    case "dash":
      return LineItem({
        item: item,
        src: "-",
        key: index,
        kind: this.props.kind
      });
    case "barline":
      return LineItem({
        item: item,
        key: index,
        src: "|", // TODO
        kind: this.props.kind,
      });
    case "beat":
      return Beat({
        beat: item,
        key: index,
        kind: this.props.kind
      });
    case "measure":
      return Measure({
        measure: item,
        key: index,
        kind: this.props.kind
      });
    default:
      return null;
      //	span({
      //       key: index
      //    },
      //   JSON.stringify(item));
    }
  }
  var PitchItemWithEntity = createClass({
    displayName: 'PitchItemWithEntity',

    render: function () {
      var item = this.props.item;
      return span({
        className: classNameFor(item),
        dangerouslySetInnerHTML: {
          __html: this.props.src
        }
      });
    }
  });

  var NotesLine = createClass({
    displayName: 'NotesLine',

    componentDidMount: function () {
      window.dom_fixes($(this.getDOMNode()));
    },
    componentDidUpdate: function () {
      window.dom_fixes($(this.getDOMNode()));
    },

    render: function () {
      assert(this.props.kind);
      var item = this.props.item;
      var items = rest(item);
      if (debug) {
        console.log("props", this.props);
      }
      if (debug) {
        console.log("item", item);
      }
      if (debug) {
        console.log("items", items);
      }
      var ary = items.map(drawItem.bind(this));
      return div({
        className: "stave sargam_line",
        key: this.props.key
      }, ary);
    }
  });



  var LineItem = createClass({
    displayName: 'LineItem',

    render: function () {
      var item = this.props.item;
      var src = this.props.src;
      var className = item[0].replace(/-/g, '_');
      return span({
        className: "note_wrapper",
        key: this.props.key
      }, span({
        className: "note " + className
      }, src));
    }
  });

  /*
draw_lyrics_section=(lyrics_section) ->
  # For now, display hyphens
  without_dashes=lyrics_section.unhyphenated_source.replace(/-/g,'')
  #without_dashes=lyrics_section.source
  x="<div title='Lyrics Section' class='stave lyrics_section unhyphenated'>#{lyrics_section.unhyphenated_source}</div>"
  x+"<div title='Lyrics Section' class='stave lyrics_section hyphenated'>#{lyrics_section.hyphenated_source}</div>"
*/

  var LyricsSection = createClass({
    displayName: 'LyricsSection',

    render: function () {
      var lyricsSection = this.props.lyricsSection;
      assert(isA("lyrics-section", lyricsSection))
      var lyricLines = (rest(lyricsSection))
        .map(function (x) {
          assert(isA("lyrics-line", x))
          return rest(x)
            .join(" ");
        });
      var str = lyricLines.join("\n");
      return div({
          title: 'Lyrics Section',
          className: 'stave lyrics_section unhyphenated'
        },
        str);
    }
  });

  var Composition = createClass({
    displayName: 'Composition',
    render: function () {
      if (debug) {
        console.log("parsed=", this.props.parsed);
      }
      var ary;
      var items;
      if (!this.props.parsed) {
        ary = [];
      } else {
        items = rest(this.props.parsed);
        if (debug) {
          console.log("in Composition, items=", items);
        }
        ary = items.map(drawItem.bind(
          this));
      }
      return div({
        className: "composition doremiContent",
      }, ary);
    }
  });


  var StaffNotationDisplay = createClass({

    displayName: 'StaffNotationDisplay',
    imgLoad: function () {
      // TODO: better to use callback to set state
      $('#staff_notation')
        .removeClass("hidden");
      window.dom_fixes();
    },

    imgError: function () {
      $('#staff_notation')
        .addClass("hidden");
     // setTimeout(reloadStaffNotationImage, 5000);
    },
    render: function () {
      var extra = "";
      if (!this.props.showStaffNotation) {
        extra = " hidden";
      }
    var tmp = new Date();
      return img({
        src: this.props.staffNotationPath + "?" + tmp.getTime(),
        onLoad: this.imgLoad,
        onError: this.imgError,
        id: "staff_notation",
        name: "",
        className: "" + extra,
      });
    }
  });


  var GenerateStaffNotationButton = createClass({
    displayName: 'GenerateStaffNotationButton',
    handleClick: function (event) {
      if (this.props.ajaxIsRunning) {
        alert(
          "Please wait for current update to complete");
        return
      }
      this.props.handleGenerateStaffNotationButton(
        event.target
        .value);
    },
    render: function () {
      //Don't disable because then it flickers
      return button({
        name: "generateStaffNotation",
        title: "Generates staff notation and MIDI file using Lilypond",
        onClick: this.handleClick,
      }, "Generate Staff Notation/ MIDI/ Lilypond");
    }
  });

  var ShowHideLilypond = createClass({
    displayName: 'ShowHideLilypond',
    handleClick: function (event) {
      this.props.handleShowHideLilypond(event.target.value);
    },
    render: function () {
      return button({
        name: "lilypondToggle",
        className: "toggleButton",
        onClick: this.handleClick,
      }, "Lilypond");
    }
  });

  var ToggleStaffNotation = createClass({
    displayName: 'ToggleStaffNotation',
    handleClick: function (event) {
      this.props.handleToggleStaffNotation(event.target
        .value);
    },
    render: function () {
      return button({
        name: "staffNotationToggle",
        className: "toggleButton",
        onClick: this.handleClick,
      }, "Staff Notation Hide/Show");
    }
  });


  var DoremiTextAreaBox = createClass({
    displayName: 'DoremiTextAreaBox',
    render: function () {
      return div({
          className: "entryAreaBox doremiContent"
        },
        DoremiTextArea({
          kind: this.props.kind,
          handleDoremiTextChange: this.props.handleDoremiTextChange
        }))
    }
  });


  /*
	var RenderedExample= createClass({
    render: function () {
     return div({
				 className: "exampleBox"
		 },Example())
	}
	})
*/

  var ExampleBox = createClass({
    render: function () {
      return div({
        className: "exampleBox"
      }, Example())
    }
  })



  var Example = createClass({
    sampleText: (
      [
        "",
        "",
        "|: 1 -2 3- -1 | 3 1 3 - | 2 -3 44 32 | 4 - - -|",
        "  doe a deer a fe-male deer re a drop of gol-den sun",
        "",
        "| 3 -4 5 3 |"
      ].join("\n")),

    render: function () {
      return pre({
        className: "exampleTxt"
      }, this.sampleText)
    }

  })


  var DoremiTextArea = createClass({
    getInitialState: function () {
      return {
        value: ""
      };
    },
    displayName: 'DoremiTextArea',
    handleTextChange: function (event) {

      this.setState({
        value: event.target.value
      });
      this.props.handleDoremiTextChange(event);
    },
    componentDidMount: function () {
       $(this.getDOMNode()) .autosize();
    },
    render: function () {
      var value = this.state.value;
      return textarea({
        value: value,
        id: "the_area",
        name: "src",
        className: "entryArea",
        onChange: this.handleTextChange,
        placeholder: "Enter letter music notation using 1234567,CDEFGABC, DoReMi (using drmfslt or DRMFSLT), SRGmPDN, or devanagri: " +
          "सर ग़म म'प धऩ" +
          "   Example:  | 1 -2 3- -1 | 3 1 3 - | \n\n",
        ref: "src"
      });
    }
  });



  window.React.renderComponent(
    DoremiBox({
      urlBase: "/" // URL to post to server
    }),
    document.getElementById('content'));
	if (false) {
			ExampleBox()
	}

  var reloadStaffNotationImage = function () {
    var tmp = new Date();
    tmp = '?' + tmp.getTime();
    tmp = '';
    if (document.images.staff_notation) {
      document.images.staff_notation.src = document.images
        .staff_notation
        .src + tmp;
    }
  }


}());
