/* jshint strict: false, undef: true, unused: true */

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

if (typeof (IFS) === 'undefined') { var IFS = {} } // jshint ignore:line
IFS.application = {}
IFS.application.loadOrder = {
  common: {
    init: function () {},
    finalize: function () {
      IFS.application.progressiveSelect.init()
    }
  },
  'app-form': {
    init: function () {
      IFS.core.unsavedChanges.init()
      IFS.application.repeatableRows.init()
      IFS.application.financeSpecifics.init()
    }
  },
  'app-details': {
    init: function () { IFS.application.applicationPage.init() }
  },
  'app-invite': {
    init: function () { IFS.application.invites.init() }
  },
  'app-invite-display': {
    init: function () { IFS.application.inviteDisplay.init() }
  },
  'competition-management': {
    init: function () { IFS.application.competitionManagement.init() }
  }
}
