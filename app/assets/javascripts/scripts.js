GOVUK.shimLinksWithButtonRole.init()

$(function() {

    //Accessibility
    var errorSummary =  $('#error-summary-display'),
        $input = $('input:text');

    //Trim inputs and Capitalize postode
    $('[type="submit"]').click(function(){
        $input.each( function(){
            if($(this).val() && $(this).attr('data-uppercase') === 'true' ){
                $(this).val($(this).val().toUpperCase().replace(/\s\s+/g, ' ').trim())
            }else{
                $(this).val($(this).val().trim())
            }
        });
    });

    //======================================================
    // Move immediate focus to any error summary
    //======================================================
    if ($('.error-summary a').length > 0) {
        $('.error-summary').focus();
    }

    //Add aria-hidden to hidden inputs
    $('[type="hidden"]').attr("aria-hidden", true);

    var showHideContent = new GOVUK.ShowHideContent();
    showHideContent.init()


    $('.form-date label.form-field--error').each(function () {

        $(this).closest('div').addClass('form-field--error')
        var $relocate = $(this).closest('fieldset').find('legend')
        $(this).find('.error-notification').appendTo($relocate)

    });

    var otherReasonErrorSummary = $('#other-reason-error-summary');

    if(otherReasonErrorSummary.length > 0){

        var otherReasonPanel = $('#other-reason-panel'),
            otherCheckBox = $('#other');

        otherCheckBox.attr('checked','checked');
        otherReasonPanel.attr('aria-hidden', 'false').removeClass('js-hidden');
    }

    function nodeListForEach (nodes, callback) {
        if (window.NodeList.prototype.forEach) {
            return nodes.forEach(callback);
        }
        for (var i = 0; i < nodes.length; i++) {
            callback.call(window, nodes[i], i, nodes);
        }
    }

    var $tabs = document.querySelectorAll('[data-module="tabs"]');
    nodeListForEach($tabs, function ($tabs) {
        new window.GOVUKFrontend($tabs).init();
    });

});
