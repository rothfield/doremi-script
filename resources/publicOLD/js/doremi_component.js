//
// Code for a doremi_component (UNFINISHED)
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

  var DoremiComponent = createClass({
    getInitialState: function () {
      return {
        txt: "",
        imgURL: "./images/blank.png"
      }
    },
    setText: function (x) {
			var merged= merge(this.state, {
          txt: x,
          basename: doremiTextToHash(
            x)
        })
		  this.setState(merged)
    },
    componentWillMount: function () {
      $.ajax({
        url: this.props.url,
        //dataType: 'txt', //Type of data expected back from server
        type: 'GET',
        error: function () {
          // TODO
        },
        success: function (myData) {
          this.setText(myData)
        }.bind(this)
      })
    },
    displayName: 'DoremiComponent',
    render: function () {
      return div({
          className: "doremi_component"
        },
        pre(null, this.state.txt),
        DoremiStaffNotationImg({
          url: this.state.basename + ".png",
          txt: this.state.txt
        })
      )
    }
  });
  var DoremiStaffNotationImg = createClass({
    displayName: 'DoremiStaffNotationImg',
    render: function () {
				if (debug) {
      console.log("render DoremiStaffNotationImg, this.props.url=",this.props.url)
				}
      return img({
        src: "./compositions/" + this.props.url
      })
    }
  })
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
      //  $(this.getDOMNode()) .autosize();
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


 if (false) {
  window.React.renderComponent(
    DoremiComponent({
      urlBase: "/" // URL to post to server
    }),
    document.getElementById('content'));
 }
}());
