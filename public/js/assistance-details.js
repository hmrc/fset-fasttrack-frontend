$(function(){
    function hideShowControls() {
        var target = "needsSupportForOnlineAssessment";
        // If no disability selected then display the top level "support for online tests" question
        if ($("#hasDisability-no").is(":checked") || $("#hasDisability-preferNotToSay").is(":checked")) {
            $('#' + target).show();
        } else if ($("#hasDisability-yes").is(":checked") && $("#guaranteedInterview-Yes").is(":checked")) {
            // If yes disability selected and gis is selected then hide the "support for online tests" question
            $('#' + target).hide();
        }
    }
    hideShowControls();

    function hasDisabilityClick() {
        $('html').on('click', '#hasDisability_field .block-label input[type=radio]', function() {
            hideShowControls();
        });
    }
    hasDisabilityClick();

    function guaranteedInterviewClick() {
        $('html').on('click', '#guaranteedInterview_field .block-label input[type=radio]', function() {
            var target = "needsSupportForOnlineAssessment";

            if ($("#guaranteedInterview-Yes").is(":checked")) {
                $('#' + target).hide();
                // Set the "need support for online tests" to No so we have no problems with the adjustments which are
                // mandatory when the controlling question has been answered with Yes
                $('input:radio[name='+target+'][value=No]').trigger("click");
            } else {
                $('#' + target).show();
            }
        });
    }
    guaranteedInterviewClick();
});
