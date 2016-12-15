IFS.competitionManagement.setup = (function() {
  "use strict";
  var s;
  return {
    init: function() {
      s = this.settings;
      IFS.competitionManagement.setup.handleCompetitionCode();

      jQuery("body.competition-management.competition-setup").on('change', '#competitionTypeId', function() {
        IFS.competitionManagement.setup.handleStateAid();
      });

      IFS.competitionManagement.setup.handleInnovationSector(true);
      jQuery("body.competition-management.competition-setup").on('change', '[name="innovationSectorCategoryId"]', function() {
        IFS.competitionManagement.setup.handleInnovationSector(false);
      });

      IFS.competitionManagement.setup.disableAlreadySelectedOptions();
      jQuery(".competition-management.competition-setup").on('change', '[name^="innovationAreaCategoryIds"]', function() {
        IFS.competitionManagement.setup.disableAlreadySelectedOptions();
      });

    },
    handleCompetitionCode : function() {
      jQuery(document).on('click', '#generate-code', function() {
        var button = jQuery(this);
        var competitionId = button.val();
        var field = button.closest('.form-group').find('input');
        var url = window.location.protocol + "//" + window.location.host+'/management/competition/setup/'+competitionId+'/generateCompetitionCode';
        //todo ajax failure
        jQuery.ajaxProtected({
          type: "GET",
          url: url
        }).done(function(data) {
          if(typeof(data) !== 'undefined'){
            if(data.success === "true"){
              IFS.core.formValidation.setValid(field, IFS.core.formValidation.getErrorMessage(field, 'required'));
              field.val(data.message);
              jQuery('body').trigger('updateSerializedFormState');
            }
            else {
              IFS.core.formValidation.setInvalid(field, data.message);
            }
          }
        });
        return false;
      });
    },
    disableAlreadySelectedOptions : function() {
      var disabledSections = {};
      jQuery('[name^="innovationAreaCategoryIds"]').each(function() {
        var inst = jQuery(this);
        var value = inst.val();
        var name = inst.prop('name');
        if(value !== null){
          disabledSections[name] = value;
        }
      });
      jQuery('[name^="innovationAreaCategoryIds"]').find('[disabled]:not([value=""])').removeAttr('disabled').removeAttr('aria-hidden');
      for(var section in disabledSections){
        jQuery('[name^="innovationAreaCategoryIds"]:not([name="'+section+'"]) option[value="'+disabledSections[section]+'"]').attr({'disabled':'disabled', 'aria-hidden':'true'});
      }
    },
    handleInnovationSector : function(pageLoad) {
      var sector = jQuery('[name="innovationSectorCategoryId"]').val();
      if(sector === null){
        jQuery('[data-add-row="innovationArea"]').attr('aria-hidden', 'true');
        var innovationCategory = jQuery('[name^="innovationAreaCategoryIds"]');
        innovationCategory.html('<option value="innovation sector" disabled="disabled" selected="selected">Please select an innovation sector first &hellip;</option>');
      }
      else {
        jQuery('[data-add-row="innovationArea"]').attr('aria-hidden', 'false');
        var url = window.location.protocol + "//" + window.location.host+'/management/competition/setup/getInnovationArea/'+sector;
        jQuery.ajaxProtected({
          type: "GET",
          url: url
        }).done(function(areas) {
          if(pageLoad){
            IFS.competitionManagement.setup.filterInnovationAreasPageLoad(areas);
          }
          else {
            IFS.competitionManagement.setup.fillInnovationAreas(areas);
            jQuery(innovationCategory).trigger('ifsValidate');
          }
        });
      }
    },
    fillInnovationAreas : function(currentAreas) {
      var innovationAreasFields = jQuery('[name^="innovationAreaCategoryIds"]');
      jQuery.each(innovationAreasFields, function() {
        var innovationAreasField = jQuery(this);
        innovationAreasField.children().remove();
        innovationAreasField.append('<option value="" disabled="disabled" selected="selected">Please select &hellip;</option>');
        jQuery.each(currentAreas, function() {
          innovationAreasField.append('<option value="'+this.id+'">'+this.name+'</option>');
        });
        IFS.core.autoSave.fieldChanged(this);
      });
    },
    filterInnovationAreasPageLoad : function(currentAreas) {
      currentAreas = jQuery.map(currentAreas, function(area) {
        return '[value="'+area.id+'"]';
      });
      currentAreas.push('[value=""]');
      currentAreas = currentAreas.join(',');
      var innovationAreas = jQuery('[name^="innovationAreaCategoryIds"] option');
      innovationAreas.not(currentAreas).remove();
    },
    handleStateAid : function() {
      var stateAid =  jQuery('#competitionTypeId').find('[value="'+jQuery('#competitionTypeId').val()+'"]').attr('data-stateaid');
      if(stateAid == 'true'){
        stateAid = 'yes';
      }
      else {
        stateAid = 'no';
      }
      jQuery('#stateAid').attr('aria-hidden', 'false').find('p').html('<span class="'+stateAid+'">'+stateAid+'</span>');
    }
  };
})();
