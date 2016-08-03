IFS.core.autoSave = (function(){
    "use strict";
    var s; // private alias to settings
    var promiseList = {};
    var autoSave_timer;
    var serverSideValidationErrors = []; // we store the last validation message as deleting of messages is done by content as unique identifier.
                             // So if we have multiple messages it will only delete the one which contains the message that has been resolved.
    return {
        settings : {
            inputs : '[data-autosave] input:not([type="button"],[readonly="readonly"],[type="hidden"],[data-autosave-disabled])',
            select : '[data-autosave] select:not([readonly="readonly"],[data-autosave-disabled])',
            textareas : '[data-autosave] textarea:not([readonly="readonly"],[data-autosave-disabled])',
            typeTimeout : 500,
            minimumUpdateTime : 800,// the minimum time between the ajax request, and displaying the result of the ajax call.
            ajaxTimeOut : 15000
        },
        init : function(){
            s = this.settings;
            jQuery('body').on('change keyup', s.textareas, function(e){
                if(e.type == 'keyup'){
                  //wait until the user stops typing
                  clearTimeout(autoSave_timer);
                  autoSave_timer = setTimeout(function(){ IFS.core.autoSave.fieldChanged(e.target); }, s.typeTimeout);
                }
                else {
                    IFS.core.autoSave.fieldChanged(e.target);
                }
            });
            jQuery('body').on('change', s.inputs+','+s.select, function(e){
                IFS.core.autoSave.fieldChanged(e.target);
            });
        },
        fieldChanged : function (element){
          var promiseListName;
          var field = jQuery(element);
          //make sure repeating rows process sequential per row
          if(field.closest('[data-repeatable-row]').length){
            promiseListName = field.closest('[data-repeatable-row]').attr('id');
          }
          else {
            promiseListName =field.attr('name');
          }
          //per field we handle the request on a promise base, this means that ajax calls should be per field sequental
          //this menas we can still have async as two fields can still be processed at the same time
          //http://www.jefferydurand.com/jquery/sequential/javascript/ajax/2015/04/13/jquery-sequential-ajax-promise-deferred.html
          if(typeof(promiseList[promiseListName]) == 'undefined'){
            promiseList[promiseListName] = jQuery.when({}); //fire first promise :)
          }

          promiseList[promiseListName] = promiseList[promiseListName].then(IFS.core.autoSave.processAjax(field));
        },
        getPostObject : function(field,form){
              //traversing from field as we might get the situation in the future where we have 2 different type autosaves on 1 page within two seperate <form>'s
              var applicationId = jQuery("#application_id").val();
              var saveType = form.attr('data-autosave');
              var jsonObj;

              switch(saveType){
                  case 'application':
                        var dateField = field.is('[data-date]');
                        if(dateField){
                          var fieldInfo = field.closest('.date-group').find('input[type="hidden"]');
                          jsonObj = {
                            applicationId: applicationId,
                            value: field.attr('data-date'),
                            formInputId: fieldInfo.attr('id'),
                            fieldName:  fieldInfo.attr('name')
                          };
                        }
                        else {
                          jsonObj = {
                            applicationId: applicationId,
                            value: field.val(),
                            formInputId: field.attr('id').replace('form-textarea-',''),
                            fieldName: field.attr('name')
                          };
                        }
                        break;
                  case 'fundingDecision':
                        jsonObj = {
                          applicationId: field.attr('name'),
                          fundingDecision: field.val()
                        };
                        break;
                  case 'assessorFeedback':
                        jsonObj = {
                          value : field.val()
                        };
                        break;
                  default :
                        jsonObj = false;
              }
              return jsonObj;
        },
        getUrl : function(field, form){
            var saveType = form.attr('data-autosave');
            var url;
            switch(saveType){
                case 'application':
                  var applicationId = jQuery("#application_id").val();
                  url ='/application/'+applicationId+'/form/saveFormElement';
                  break;
                case 'fundingDecision':
                  var competitionId = field.attr('data-competition');
                  url = '/management/funding/' + competitionId;
                  break;
                case 'assessorFeedback':
                  var formInputId = field.closest('.question').attr('id').replace('form-input-','');
                  var assessmentId = form.attr('action').split('/')[2];
                  url = '/assessment/'+assessmentId+'/formInput/'+formInputId;
                  break;
                default:
                  url = false;
            }
            return url;
        },
        processAjax : function(field){
          return function(){
            var form = field.closest('[data-autosave]');
            var data = IFS.core.autoSave.getPostObject(field,form);
            var url = IFS.core.autoSave.getUrl(field,form);
            var defer = jQuery.Deferred();

            if(data === false || url === false){
              defer.resolve();
              return defer.promise();
            }

            var name = field.attr('name');
            var formGroup = field.closest('.form-group');
            var autoSaveInfo = formGroup.find('.autosave-info');
            var startAjaxTime= new Date().getTime();

            if(autoSaveInfo.length === 0){
              formGroup.find('.textarea-footer').append('<span class="autosave-info" />');
              autoSaveInfo = formGroup.find('.autosave-info');
            }

            jQuery.ajaxProtected({
                type: 'POST',
                url: url,
                data: data,
                dataType: "json",
                beforeSend: function() {
                   autoSaveInfo.html('Saving...');
               },
               timeout: s.ajaxTimeOut
            })
            .done(function(data){
                var doneAjaxTime = new Date().getTime();
                var remainingWaitingTime = (IFS.core.autoSave.settings.minimumUpdateTime-(doneAjaxTime-startAjaxTime));

                //transform name of costrow for persisting to database
            	  if(typeof(data.field_id) !== 'undefined') {
                  jQuery('body').trigger('persistUnsavedRow',[name,data.field_id]);
                }
                // set the form-saved-state
                jQuery('body').trigger('updateSerializedFormState');

                //save message
                setTimeout(function(){
                    IFS.core.autoSave.clearServerSideValidationErrors(field);
                    autoSaveInfo.html('Saved!');

                    if(typeof(data.validation_errors) !== 'undefined'){
                      jQuery.each(data.validation_errors, function(index, value){
                          IFS.core.formValidation.setInvalid(field,value);
                          serverSideValidationErrors.push(value);
                      });
                    }
                }, remainingWaitingTime);

            }).fail(function(data) {
                if(autoSaveInfo.length){
                    var errorMessage = IFS.core.autoSave.getErrorMessage(data);
                    autoSaveInfo.html('<span class="error-message">'+errorMessage+'</span>');
                }
            }).always(function(){
                defer.resolve();
           });
           return defer.promise();
          };
        },
        getErrorMessage : function(data){
          //when something goes wrong server side this will not show validation messages but system errors like timeouts
          var errorMessage;
          if((typeof(data.responseJson) !== 'undefined') && (typeof(data.responseJson.errorMessage) !== 'undefined')){
              errorMessage = data.responseJson.errorMessage;
          }
          else if(data.statusText == 'timeout'){
              errorMessage = "The server is slow responding, your data is not saved";
          }
          else {
              errorMessage = "Something went wrong when saving your data";
          }
          return errorMessage;
        },
        clearServerSideValidationErrors : function(field){
            for (var i = 0; i < serverSideValidationErrors.length; i++){
                 IFS.core.formValidation.setValid(field,serverSideValidationErrors[i]);
            }
        }

    };
})();
