// boot-cljs shim
(function() {
  var shimRegex = new RegExp('(.*)app.js$');

  function findPrefix() {
    var els = document.getElementsByTagName('script');
    for (var i = 0; i < els.length; i++) {
      var src = els[i].getAttribute('src');
      var match = src && src.match(shimRegex);
      if (match) return match[1]; }
    return ''; }

  var prefix = findPrefix();
  var loadedSrcs = {};
  var scripts = document.getElementsByTagName('script');

  for (var i = 0; i < scripts.length; i++)
    if (scripts[i].src !== undefined)
      loadedSrcs[scripts[i].src] = true;

  function writeScript(src) {
    var newElem;
    if (window.__boot_cljs_shim_loaded === undefined)
      document.write(src);
    else {
      newElem = document.createElement('div');
      newElem.innerHTML = src;
      if (newElem.src !== undefined && loaded[newElem.src] === undefined) {
        document.getElementsByTagName('head')[0].appendChild(newElem); }}}

  writeScript("<script src='" + prefix + "out/goog/base.js'></script>");
  writeScript("<script src='" + prefix + "boot-cljs-app.js'></script>");
  writeScript("<script>goog.require('boot.cljs.app');</script>");

  window.__boot_cljs_shim_loaded = true; })();
