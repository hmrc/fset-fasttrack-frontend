$(function(){
    $(function()
    {
        var schemePrefArray = ['Empty'],
            firstEmptyPosition = $.inArray('Empty', schemePrefArray),
            getOrdinal = function(n) {
                var s=["th","st","nd","rd"],
                v=n%100;
                return n+(s[(v-20)%10]||s[v]||s[0]);
            };

        $('[data-schemename]').on('change', function()
        {
            var $this = $(this),
                thisScheme = $this.closest('.block-label').text(),
                thisSchemeID = $this.attr('id'),
                thisSchemeValue = $this.attr('value'),
                schemeReq = $this.closest('.scheme-container').find(
                    '[data-scheme-req]').html(),
                specialSchemeReq = $this.closest('.scheme-container').find(
                    '[data-spec-scheme]').html(),
                isSpecial = $this.closest('.scheme-container').find(
                    '[data-spec-scheme]').length,
                specialEligibility = isSpecial == 0 ?
                    '<p class="font-xsmall no-btm-margin">Requires at least ' +
                    schemeReq + '</p>' :
                    '<div class="scheme-warning"><p class="font-xsmall">Requires at least ' +
                    specialSchemeReq + '</p></div>',
                arrayPosition = $.inArray(thisSchemeID, schemePrefArray),
                emptyPosition = $.inArray('Empty', schemePrefArray);
            if (arrayPosition < 0 && $this.is(':checked'))
            {
                if (emptyPosition < 0)
                {
                    schemePrefArray.push(thisSchemeID);
                }
                else
                {
                    schemePrefArray.splice(emptyPosition, 1, thisSchemeID);
                }
                var arrayPositionNow = $.inArray(thisSchemeID,
                    schemePrefArray);

                var hiddenSchemeId = "#schemes_"+arrayPositionNow;
                $(hiddenSchemeId).val(thisSchemeValue);
                $('#selectedPrefList li').eq(arrayPositionNow).after(
                    '<li class="scheme-prefcontainer" data-scheme-id="' +
                    thisSchemeID + '"><span data-schemeorder>' +
                    getOrdinal(arrayPositionNow + 1) +
                    ' preference </span><div class="text scheme-elegrepeat">' +
                    '<span class="bold-small" data-schemenameinlist>' +
                    thisScheme + '</span>' + specialEligibility +
                    '<a href="#" class="link-unimp scheme-remove"><i class="fa fa-times" aria-hidden="true"></i>Remove</a></div>'
                );

                $this.closest('.scheme-container').addClass(
                    'selected-scheme').find('.selected-preference').text(
                    getOrdinal(arrayPositionNow + 1)).removeClass(
                    'invisible');
            }
            if (!$this.is(':checked'))
            {
                var hiddenSchemeId = "#schemes_"+arrayPosition;
                $(hiddenSchemeId).val('');
                schemePrefArray.splice(arrayPosition, 1, 'Empty');
                $('#selectedPrefList').find('[data-scheme-id="' +
                    thisSchemeID + '"]').remove();
                $this.closest('.scheme-container').removeClass(
                    'selected-scheme').find('.selected-preference').text(
                    'N/A').addClass('invisible');
            }
            if ($('input[data-schemename]:checked').length > 0)
            {
                $('[data-scheme-placeholder]').addClass(
                    'toggle-content');
            }
            else
            {
                $('[data-scheme-placeholder]').removeClass(
                    'toggle-content');
            }
        });
        $('#selectedPrefList').on('click', '.scheme-remove', function(e)
        {
            var thisScheme = $(this).closest('.scheme-prefcontainer')
                    .attr('data-scheme-id'),
                schemesAfter = $(this).closest('.scheme-prefcontainer')
                    .nextAll();
            e.preventDefault();
            $('#' + thisScheme).trigger('click').attr('checked',
                false).closest('label').removeClass('selected');
            schemesAfter.each(function()
            {
                var schemeID = $(this).attr('data-scheme-id');
                $('#' + schemeID).trigger('click');
                $('#' + schemeID).trigger('click');
            });
        });

        function displaySelectedSchemes(){
            for(i = 0; i < $("[id^='schemes_']").length; i++){
                var hiddenScheme = $('#schemes_'+i)
                if(hiddenScheme !== "undefined") {
                    var scheme = hiddenScheme.val();
                    var sid = "#scheme-" + scheme;
                    if(scheme !== '' && typeof $(sid) !== "undefined"){
                        var initialStatus = $(sid).is(':checked');
                        if(initialStatus == false){
                            $(sid).click();
                        }
                        $(sid).checked = true;
                        $(sid).trigger('change');
                    }
                }
            }
        }
        displaySelectedSchemes();
    });
});
