$(function(){
    var schemePrefArray = ['Empty'],
        firstEmptyPosition = $.inArray('Empty', schemePrefArray),
        preferencesAs123 = ['1st', '2nd', '3rd', '4th', '5th', '6th', '7th', '8th', '9th', '10th', '11th', '12th', '13th', '14th', '15th', '16th', '17th'],
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

    function loadJSON(callback) {

        /*
        var xobj = new XMLHttpRequest();
        xobj.overrideMimeType("application/json");
        xobj.open('GET', '../_assets/js/locations.json', true); // Replace 'my_data' with the path to your file
        xobj.onreadystatechange = function () {
            if (xobj.readyState == 4 && xobj.status == "200") {
                // Required use of an anonymous callback as .open will NOT return a value but simply returns undefined in asynchronous mode
                callback(xobj.responseText);
            }
        };
        xobj.send(null);
        */
        callback('{ "locations": [] }');
    }

    var locations = [];

    loadJSON(function(response) {
        // Parse JSON string into object
        var locationsResponse = JSON.parse(response);

        locations = locationsResponse.locations;

    });

    $('html').on('change', '[data-schemename]', function() {
        var $this = $(this),
            thisScheme = $this.closest('.block-label').find('.location-name').text(),
            thisSchemeID = $this.attr('id'),
            schemeReq = $this.closest('.scheme-container').find('[data-scheme-req]').html(),
            isSpecial = $this.closest('.scheme-container').find('[data-spec-scheme]').length,
            arrayPosition = $.inArray(thisSchemeID, schemePrefArray),
            emptyPosition = $.inArray('Empty', schemePrefArray);

        if(arrayPosition >= 0) {
            //Do nothing
        } else if($this.is(':checked')) {
            if(emptyPosition < 0) {
                schemePrefArray.push(thisSchemeID);
            } else {
                schemePrefArray.splice(emptyPosition, 1, thisSchemeID);
            }
            var arrayPositionNow = $.inArray(thisSchemeID, schemePrefArray);

            var id = thisSchemeID;
            var result = $.grep(locations, function(e){ return e.id == id; });
            var schemesLength = result[0].schemes.length;
            var schemesAsHTML = '';

            for (var i = 0; i < schemesLength; i++) {
                schemesAsHTML += '<li>' + result[0].schemes[i] + '</li>';
            }

            $('#selectedPrefList > li').eq(arrayPositionNow).after(
                '<li class="location-prefcontainer" data-scheme-id="' + thisSchemeID + '">' +
                '<span data-schemprefinlist>' + preferencesAsText[arrayPositionNow] + '</span>' +
                '<div class="text scheme-elegrepeat">' +
                '<span class="bold-small" data-schemenameinlist>' + thisScheme +
                '</span><a href="#" class="link-unimp scheme-remove">' +
                '<i class="fa fa-times" aria-hidden="true"></i>Remove</a>' +
                '<details class="no-btm-margin"><summary class="no-btm-margin">' +
                schemesLength + ' available schemes</summary>' +
                '<div class="detail-content panel-indent"><ul>' + schemesAsHTML + '</ul></div></details></div>');

            $this.closest('.scheme-container').addClass('selected-scheme').find('.selected-preference').text(preferencesAs123[arrayPositionNow]).removeClass('invisible');
        }

        if(!$this.is(':checked')) {
            schemePrefArray.splice(arrayPosition, 1, 'Empty');
            $('#selectedPrefList').find('[data-scheme-id="' + thisSchemeID + '"]').remove();
            $this.closest('.scheme-container').removeClass('selected-scheme').find('.selected-preference').text('N/A').addClass('invisible');
        }

        var chosenPreferences = $('[data-schemeorder]').map(function() {
            return $( this ).text();
        })
            .get();

        var arrayOfChosen = $.makeArray(chosenPreferences);
        var differenceArray = [],
            initialVal = 0;


        $.grep(preferencesAsText, function(el) {
            if($.inArray(el, arrayOfChosen) == -1) differenceArray.push(el);

            initialVal++;
        });

    });

    $('#selectedPrefList').on('click', '.scheme-remove', function(e) {
        var thisScheme = $(this).closest('.location-prefcontainer').attr('data-scheme-id'),
            schemesAfter = $(this).closest('.location-prefcontainer').nextAll();

        e.preventDefault();

        $('#' + thisScheme).trigger('click').attr('checked', false).closest('label').removeClass('selected');


        schemesAfter.each(function () {
            var schemeID = $(this).attr('data-scheme-id');

            $('#' + schemeID).trigger('click');
            $('#' + schemeID).trigger('click');
        });

    });

});