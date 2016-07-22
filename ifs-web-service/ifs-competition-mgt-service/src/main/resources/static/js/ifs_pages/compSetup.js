IFS.competition_management.setup = (function(){
  "use strict";
  var s;
  return {
    settings: {

    },
    init: function(){
        s = this.settings;
        IFS.competition_management.setup.handleCompetitionCode();

        IFS.competition_management.setup.handleAddCoFunder();

        jQuery("body.competition-management.competition-setup").on('change','#competitionTypeId',function(){
          IFS.competition_management.setup.handleStateAid();
        });
        jQuery("body.competition-management.competition-setup").on('change','[name="innovationSectorCategoryId"]',function(){
          IFS.competition_management.setup.handleInnovationSector();
        });
    },
    handleCompetitionCode : function(){
        jQuery(document).on('click','#generate-code',function(){
            var button = jQuery(this);
            var competitionId = button.val();
            var field = button.closest('.form-group').find('input');
            var url = window.location.protocol + "//" + window.location.host+'/management/competition/setup/'+competitionId+'/generateCompetitionCode';
            //todo ajax failure
            jQuery.ajaxProtected({
              type: "GET",
              url: url,
              success: function(data) {
                if(typeof(data) !== 'undefined'){
                  if(data.success === "true"){
                    IFS.core.formValidation.setValid(field,data.message);
                    field.val(data.message);
                  }
                  else {
                    IFS.core.formValidation.setInvalid(field,data.message);
                  }
                }
              }
            });
        });
    },
    handleInnovationSector : function(){
          var sector = jQuery('[name="innovationSectorCategoryId"]').val();
          if(typeof(sector) !=='undefined'){
            var url = window.location.protocol + "//" + window.location.host+'/management/competition/setup/getInnovationArea/'+sector;
            jQuery.ajaxProtected({
              type: "GET",
              url: url,
              success: function(data) {
                  var innovationCategory = jQuery('[name="innovationAreaCategoryId"]');
                  innovationCategory.children().remove();
                  jQuery.each(data,function(){
                      innovationCategory.append('<option value="'+this.id+'">'+this.name+'</option>');
                  });
                  innovationCategory.trigger('change');
              }
          });
          }

    },
    handleStateAid : function(){
       var stateAid =  jQuery('#competitionTypeId').find('[value="'+jQuery('#competitionTypeId').val()+'"]').attr('data-stateaid');
       if(stateAid == 'true'){
         stateAid = 'yes';
       }
       else {
         stateAid = 'no';
       }
       jQuery('#stateAid').attr('aria-hidden','false').find('p').html('<span class="'+stateAid+'">'+stateAid+'</span>');
    },
    handleAddCoFunder: function() {
      jQuery(document).on('click','#add-cofunder',function() {
          var count = parseInt(jQuery('#co-funder-count').val(),10);
          jQuery('<div class="grid-row" id="co-funder-row-'+ count +'"><div class="column-half"><div class="form-group"><input type="text" maxlength="255" data-maxlength-errormessage="Co-funders has a maximum length of 255 characters" class="form-control width-x-large" id="' + count +'-funder" name="coFunders['+ count +'].coFunder" value=""></div> </div>' +
              '<div class="column-half"><div class="form-group"><input type="number" min="0" class="form-control width-x-large" id="' + count +'-funderBudget" name="coFunders['+ count +'].coFunderBudget" value=""></div> </div></div>')
              .insertBefore('#dynamic-row-pointer');

          jQuery('#co-funder-count').val(count + 1);
          return false;
      });

    }

  };
})();
