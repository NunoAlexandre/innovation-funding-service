// Dom based routing
// ------------------
// Based on Paul Irish' code, please read blogpost
// http://www.paulirish.com/2009/markup-based-unobtrusive-comprehensive-dom-ready-execution///
// Does 2 jobs:
//    * Page dependend execution of functions
//    * Gives a more fine-grained control in which order stuff is executed
//
// Adding a page dependend function:
//    1. add class to body <body class="superPage">
//    2. add functions to the IFSLoader object IFSLoader = { superPage : init : function() {}};
//
// For now this will suffice, if complexity increases we might look at a more complex loader like requireJs.
// Please think before adding javascript, this project should work without any of this scripts.

if (!Object.keys) {
  Object.keys = function (obj) {
    if (obj !== Object(obj)) {
      throw new TypeError('Object.keys called on a non-object')
    }
    var k = []
    var p
    for (p in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, p)) {
        k.push(p)
        return k
      }
    }
  }
}

// util for kicking it off.
var UTIL = (function () {
  'use strict'
  return {
    fire: function (func, funcname, args) {
      var keys = Object.keys(IFS)
      jQuery.each(keys, function () {
        if (typeof (IFS[this].loadOrder) !== 'undefined') {
          var namespace = IFS[this].loadOrder  // indicate your obj literal namespace here
          funcname = (funcname === undefined) ? 'init' : funcname
          if (func !== '' && namespace[func] && typeof namespace[func][funcname] === 'function') {
            namespace[func][funcname](args)
          }
        }
      })
    },
    loadEvents: function () {
      // hit up common first.
      var classNames = document.body.className.split(/\s+/)
      UTIL.fire('common')
      // do all the init functions on classes
      jQuery.each(classNames, function (i, classnm) {
        UTIL.fire(classnm)
      })

      // now all the finalize statements first common, then classes
      UTIL.fire('common', 'finalize')
      jQuery.each(classNames, function (i, classnm) {
        UTIL.fire(classnm, 'finalize')
      })
    }
  }
})()
// kick it all off here
jQuery(document).ready(function () {
  UTIL.loadEvents()
})
