$(function(){
    $(function()
    {
        var schemePrefArray = ['Empty'],
            firstEmptyPosition = $.inArray('Empty', schemePrefArray),
            preferencesAs123 = ['1st', '2nd', '3rd', '4th', '5th', '6th',
                '7th', '8th', '9th', '10th', '11th', '12th', '13th', '14th',
                '15th', '16th', '17th'
            ],
            preferencesAsText = [
                '1st preference',
                '2nd preference',
                '3rd preference',
                '4th preference',
                '5th preference',
                '6th preference',
                '7th preference',
                '8th preference',
                '9th preference',
                '10th preference',
                '11th preference',
                '12th preference',
                '13th preference',
                '14th preference',
                '15th preference',
                '16th preference',
                '17th preference'
            ];
        $('[data-schemename]').on('change', function()
        {
            var $this = $(this),
                thisScheme = $this.closest('.block-label').text(),
                thisSchemeID = $this.attr('id'),
                schemeReq = $this.closest('.scheme-container').find(
                    '[data-scheme-req]').html(),
                isSpecial = $this.closest('.scheme-container').find(
                    '[data-spec-scheme]').length,
                specialEligibility = isSpecial == 0 ?
                    '<p class="font-xsmall no-btm-margin">Requires at least ' +
                    schemeReq + '</p>' :
                    '<div class="scheme-warning"><p class="font-xsmall">Requires at least ' +
                    schemeReq + '</p></div>',
                arrayPosition = $.inArray(thisSchemeID, schemePrefArray),
                emptyPosition = $.inArray('Empty', schemePrefArray);
            if (arrayPosition >= 0)
            {
                //Do nothing
            }
            else if ($this.is(':checked'))
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

                $('#selectedPrefList li').eq(arrayPositionNow).after(
                    '<li class="scheme-prefcontainer" data-scheme-id="' +
                    thisSchemeID + '"><span data-schemeorder>' +
                    preferencesAsText[arrayPositionNow] +
                    '</span><div class="text scheme-elegrepeat">' +
                    '<input type="hidden" name="schemeNames[' + arrayPositionNow + ']" value="' + thisScheme + '" />' +
                    '<span class="bold-small" data-schemenameinlist>' +
                    thisScheme + '</span>' + specialEligibility +
                    '<a href="#" class="link-unimp scheme-remove"><i class="fa fa-times" aria-hidden="true"></i>Remove</a></div>'
                );

                $this.closest('.scheme-container').addClass(
                    'selected-scheme').find('.selected-preference').text(
                    preferencesAs123[arrayPositionNow]).removeClass(
                    'invisible');
            }
            if (!$this.is(':checked'))
            {
                schemePrefArray.splice(arrayPosition, 1, 'Empty');
                $('#selectedPrefList').find('[data-scheme-id="' +
                    thisSchemeID + '"]').remove();
                $this.closest('.scheme-container').removeClass(
                    'selected-scheme').find('.selected-preference').text(
                    'N/A').addClass('invisible');
            }
            var chosenPreferences = $('[data-schemeorder]').map(
                function()
                {
                    return $(this).text();
                })
                .get();
            var arrayOfChosen = $.makeArray(chosenPreferences);
            var differenceArray = [],
                initialVal = 0;
            $.grep(preferencesAsText, function(el)
            {
                if ($.inArray(el, arrayOfChosen) == -1)
                    differenceArray.push(el);
                initialVal++;
            })
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
    });
});
