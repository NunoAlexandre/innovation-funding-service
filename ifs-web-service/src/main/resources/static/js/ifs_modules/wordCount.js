/* jshint strict: true, undef: true, unused: true */
/* globals  jQuery : false, window: false, setTimeout : false, clearTimeout: false */

var ifs_wordCount = (function(){
    "use strict";
    var s; 
    return {
        settings : {
            wordcountEl : ".word-count textarea",
            typeTimeout : 500
        },
        init : function(){
            s = this.settings; 
            jQuery('body').on('change', s.wordcountEl, function(e){ 
              ifs_wordCount.updateWordCount(e.target);
            });
            //wait until the user stops typing 
            jQuery('body').on('keyup', s.wordcountEl, function(e) { 
               clearTimeout(window.ifs_wordcount_timer);
               window.ifs_wordcount_timer = setTimeout(function(){ifs_wordCount.updateWordCount(e.target); }, s.typeTimeout);
            });
        },
        updateWordCount : function(textarea){
              var field = jQuery(textarea);
              var value = field.val();

              //regex = replace newlines with space \r\n, \n, \r 
              value = value.replace(/(\r\n|\n|\r)/gm," ");
              //remove markdown lists ('* ','1. ','2. ') from markdown as it influences word count
              value = value.replace(/([[0-9]+\.\ |\*\ )/gm,"");

              var words = jQuery.trim(value).split(' ');
              var count = 0;
              //for becuase of ie7 performance. 
              for (var i = 0; i < words.length; i++) {
                if(words[i].length > 0){
                  count++;
                }
              }
              var delta = field.attr('data-max_words') - count;
              var countDownEl = field.parents(".word-count").find(".count-down");
              countDownEl.html(delta);
              if(delta < 0){
                  countDownEl.removeClass("positive").addClass("negative");
              }else{
                  countDownEl.removeClass("negative").addClass("positive");
              }
        }  
    };
})();
