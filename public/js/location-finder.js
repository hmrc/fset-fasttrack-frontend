$(function(){

    jQuery.fn.scrollTo = function(elem, speed) {
        $(this).animate({
            scrollTop:  $(this).scrollTop() - $(this).offset().top + $(elem).offset().top
        }, speed == undefined ? 1000 : speed);
        return this;
    };

    function loadLocationsJson(callback, hasALevels, hasStemALevels, latitude, longitude) {

        var latLongParams = "";
        if (typeof latitude !== "undefined" && typeof longitude !== "undefined") {
            latLongParams = "&latitudeOpt=" + latitude + "&longitudeOpt=" + longitude;
        }

        $.getJSON("/fset-fast-track/application/schemes/by-eligibility?hasALevels=" + hasALevels + "&hasStemALevels=" + hasStemALevels + latLongParams, function(data) {
            callback(data);
        });
    }

    function hasNumber(myString) {
        return /\d/.test(myString);
    }

    $("#yourPostcode").autocomplete({
        source: function (request, response) {
            //data :: JSON list defined
            locations.sort(sort_by('name', true, function(a){return a.toUpperCase()}));
            array = $.map(locations, function (value, key) {
                return {
                    label: value.name
                }
            });
            response($.ui.autocomplete.filter(array, request.term));
        },

        select: function(e, ui) {
            if(!$('#listOfLocations li:contains("' + ui.item.label + '")').find('input').is(':checked')) {
                $('#scrollingList').scrollTo($('#listOfLocations li:contains("' + ui.item.label + '")'), 400);
                $('#listOfLocations li:contains("' + ui.item.label + '")').find('input').trigger('click').prop('checked', true).parent().addClass('selected');
            } else {
                $('#scrollingList').scrollTo($('#listOfLocations li:contains("' + ui.item.label + '")'), 400);
            }
        },
        close: function() {
            var theVal = $('#yourPostcode').val();

            if(!hasNumber(theVal) && theVal.length >= 4) {
                $('#yourPostcode').val('');
            }

        }
    }).on('keyup', function(e) {
        var thisVal = $(this).val();
        if(e.which == 13 && ($('.ui-autocomplete li:contains("' + thisVal + '")').text() == thisVal)) {
            $('.ui-autocomplete li:contains("' + thisVal + '")').trigger('click');
            $('#yourPostcode').val('');
        }
    });

    var lat, lng;

    var locations = [];

    var sort_by = function(field, reverse, primer){
        var key = function (x) {return primer ? primer(x[field]) : x[field]};

        return function (a,b) {
            var A = key(a), B = key(b);
            return ( (A < B) ? -1 : ((A > B) ? 1 : 0) ) * [-1,1][+!!reverse];
        }
    };

    var sic = $('#scheme-input-container');

    if (sic.length) {
        getLatLongFromPostCode(
         sic.attr('data-hasALevels'),
         sic.attr('data-hasStemALevels'),
         lat,
         lng
        )
    }

    function getLatLongFromPostCode(hasALevels, hasStemALevels, latitude, longitude) {
        $('#loadingLocations').removeClass('toggle-content');
        $('#noLocationsFound').addClass('toggle-content');

        loadLocationsJson(function(response) {
            // Parse JSON string into object
            locations = response
        }, hasALevels, hasStemALevels, latitude, longitude);

        /* Post code lookup
        var currentPostcode = $('#yourPostcode').val().toUpperCase(),
            parts = currentPostcode.match(/^([A-Z]{1,2}\d{1,2}[A-Z]?)\s*(\d[A-Z]{2})$/);

        if(parts != null) {
            parts.shift();

            var postcodesJsonURL = 'http://api.geonames.org/postalCodeLookupJSON?postalcode=' + parts[0] + '&country=GB&username=henrycharge&style=full';

            $.getJSON( postcodesJsonURL, function( data ) {

                if(jQuery.isEmptyObject(data.postalcodes[0])) {
                    $('#loadingLocations').addClass('toggle-content');
                    $('#noLocationsFound').removeClass('toggle-content');
                } else {
                    lat = data.postalcodes[0].lat,
                        lng = data.postalcodes[0].lng;

                    myGeoLocation = new GeoPoint(lat, lng);

                    setTimeout(addDistance, 0);
                }

            });
        } else {
            $('#loadingLocations').addClass('toggle-content');
            // $('#noLocationsFound').removeClass('toggle-content');
        }
        */
        setTimeout(addDistance, 0)

    }

    function addDistance() {
        locations.sort(sort_by('distance', true, parseInt));

        setTimeout(showNearby, 0);
    }

    function showNearby() {
        $('#listOfLocations input').not(':checked').closest('li').remove();
        $('#loadingLocations').addClass('toggle-content');
        for (var i = 0; i < locations.length; i++) {
            if($('#listOfLocations label:contains("' + locations[i].name + '")').length ) {

            } else {

                var distanceText = "";
                if (typeof locations[i].distanceKm !== 'undefined') {
                    var distanceToMax2DP = +parseFloat(locations[i].distanceKm).toFixed(2)
                    distanceText = '<span class="location-distance">' + distanceToMax2DP + ' miles</span>';
                }

                $('#listOfLocations').append(
                    '<li class="scheme-container">' +
                    '<span class="selected-preference invisible">N/A</span>' +
                    '<label for="' + locations[i].locationId + '" class="block-label block-label-slim">' +
                    '<input type="checkbox" id="' + locations[i].locationId + '" data-schemename>' +
                    '<span class="location-name">' + locations[i].locationName + '</span>' +
                    distanceText +
                    '</label>' +
                    '</li>' );
            }
        }
        $('#scrollingList').scrollTo($('#listOfLocations li:first-child'), 400);
    }

    $('#updateLocation').on('click', function(e) {

        $('#updateLocationWrapper').slideUp(300);

        setTimeout(getLatLongFromPostCode(), 300);

        e.preventDefault();
    });

    $('#yourPostcode').on('keyup', function(e) {
        if($(this).val().length > 5 && hasNumber($(this).val())) {
            $('#updateLocationWrapper').slideDown(300);
        }

        if(e.which == 13) {
            $('#updateLocationWrapper').slideUp(300);

            setTimeout(getLatLongFromPostCode(), 300);
        }
    });

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
            var result = $.grep(locations, function(e){ return e.locationId == id; });
            var schemesLength = result[0].schemes.length;
            var schemesAsHTML = '';

            for (var i = 0; i < schemesLength; i++) {
                schemesAsHTML += '<li>' + result[0].schemes[i] + '</li>';
            }

            $('#selectedPrefList > li').eq(arrayPositionNow).after(
                '<li class="location-prefcontainer" data-scheme-id="' + thisSchemeID + '">' +
                '<span data-schemprefinlist>' + preferencesAsText[arrayPositionNow] + '</span>' +
                '<input type="hidden" name="locationIds[' + arrayPositionNow +']" value="' + id + '" />' +
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
