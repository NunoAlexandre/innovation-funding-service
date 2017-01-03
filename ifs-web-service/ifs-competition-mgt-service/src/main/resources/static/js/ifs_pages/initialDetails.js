IFS.competitionManagement.initialDetails = (function () {
  'use strict'
  var s
  return {
    init: function () {
      s = this.settings

      jQuery('body.competition-management.competition-setup').on('change', '#competitionTypeId', function() {
        IFS.competitionManagement.initialDetails.handleStateAid()
      })
      IFS.competitionManagement.initialDetails.handleInnovationSector(true)
      jQuery('body.competition-management.competition-setup').on('change', '[name="innovationSectorCategoryId"]', function () {
        IFS.competitionManagement.initialDetails.handleInnovationSector(false)
      })
      IFS.competitionManagement.initialDetails.enableDisableMultipleInnovationAreas('[name="competitionTypeId"]')
      jQuery('.competition-management.competition-setup').on('change', '[name="competitionTypeId"]', function () {
        IFS.competitionManagement.initialDetails.enableDisableMultipleInnovationAreas(this)
      })

      IFS.competitionManagement.initialDetails.disableAlreadySelectedOptions()
      jQuery('.competition-management.competition-setup').on('change', '[name^="innovationAreaCategoryIds"]', function () {
        IFS.competitionManagement.initialDetails.disableAlreadySelectedOptions()
        IFS.competitionManagement.initialDetails.autosaveInnovationAreaIds()
      })
    },
    handleInnovationSector: function (pageLoad) {
      var sector = jQuery('[name="innovationSectorCategoryId"]').val()
      if (typeof (sector) === 'undefined' || sector === null) {
        var innovationCategory = jQuery('[name^="innovationAreaCategoryIds"]')
        innovationCategory.html('<option value="innovation sector" disabled="disabled" selected="selected">Please select an innovation sector first &hellip;</option>')
      } else {
        var url = window.location.protocol + '//' + window.location.host + '/management/competition/setup/getInnovationArea/' + sector
        jQuery.ajaxProtected({
          type: 'GET',
          url: url
        }).done(function (areas) {
          if (pageLoad) {
            IFS.competitionManagement.initialDetails.filterInnovationAreasPageLoad(areas)
          } else {
            IFS.core.autoSave.fieldChanged('[name="innovationSectorCategoryId"]')
            IFS.competitionManagement.initialDetails.fillInnovationAreas(areas)
            jQuery(innovationCategory).trigger('ifsValidate')
          }
        })
      }
    },
    disableAlreadySelectedOptions: function () {
      var disabledSections = {}
      jQuery('[name^="innovationAreaCategoryIds"]').each(function () {
        var inst = jQuery(this)
        var value = inst.val()
        var name = inst.prop('name')
        if (value !== null) {
          disabledSections[name] = value
        }
      })
      jQuery('[name^="innovationAreaCategoryIds"]').find('[disabled]:not([value=""])').removeAttr('disabled').removeAttr('aria-hidden')
      for (var section in disabledSections) {
        jQuery('[name^="innovationAreaCategoryIds"]:not([name="' + section + '"]) option[value="' + disabledSections[section] + '"]').attr({'disabled':'disabled', 'aria-hidden':'true'});
      }
    },
    enableDisableMultipleInnovationAreas: function (el) {
      var isSectorCompetition = (jQuery(el).val() == 5)
      if (isSectorCompetition) {
        jQuery('[data-add-row="innovationArea"]').attr('aria-hidden', 'false')
      } else {
        // remove all the other sections and save
        jQuery('[id*="innovation-row"]').not('#innovation-row-0').remove()
        IFS.competitionManagement.initialDetails.autosaveInnovationAreaIds()
        // hide the add button
        jQuery('[data-add-row="innovationArea"]').attr('aria-hidden', 'true')
      }
    },
    fillInnovationAreas: function (currentAreas) {
      var innovationAreasFields = jQuery('[name^="innovationAreaCategoryIds"]')
      jQuery.each(innovationAreasFields, function () {
        var innovationAreasField = jQuery(this)
        innovationAreasField.children().remove()
        innovationAreasField.append('<option value="" disabled="disabled" selected="selected">Please select &hellip;</option>')
        jQuery.each(currentAreas, function () {
          innovationAreasField.append('<option value="' + this.id + '">' + this.name + '</option>')
        })
      })
      IFS.competitionManagement.initialDetails.autosaveInnovationAreaIds()
    },
    autosaveInnovationAreaIds: function () {
      // gets called whenever something changed in the list
      var saveField =  jQuery('[name="autosaveInnovationAreaIds"]')
      var innovationAreas = jQuery.unique(jQuery.map(jQuery('[name*="innovationAreaCategoryIds"]'), function (el) { return jQuery(el).val() }))
      saveField.val(innovationAreas.join())
      IFS.core.autoSave.fieldChanged(saveField)
    },
    filterInnovationAreasPageLoad : function (currentAreas) {
      currentAreas = jQuery.map(currentAreas, function (area) {
        return '[value="' + area.id + '"]'
      })
      currentAreas.push('[value=""]')
      currentAreas = currentAreas.join(',')
      var innovationAreas = jQuery('[name^="innovationAreaCategoryIds"] option')
      innovationAreas.not(currentAreas).remove()
    },
    handleStateAid: function () {
      var stateAid = jQuery('#competitionTypeId').find('[value="' + jQuery('#competitionTypeId').val() + '"]').attr('data-stateaid')
      if (stateAid === 'true') {
        stateAid = 'yes'
      } else {
        stateAid = 'no'
      }
      jQuery('#stateAid').attr('aria-hidden', 'false').find('p').html('<span class="' + stateAid + '">' + stateAid + '</span>')
    }
  }
})()
