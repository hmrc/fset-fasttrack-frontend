$(function(){

    var ENTER_KEY = 13;

    jQuery.fn.scrollTo = function(elem, speed) {
        $(this).animate({
            scrollTop:  $(this).scrollTop() - $(this).offset().top + $(elem).offset().top
        }, speed == undefined ? 1000 : speed);
        return this;
    };

    function loadLocationsJson(callback, latitude, longitude) {
        var locationsUrl = "/fset-fast-track/application/scheme-locations/eligible";
        var latLongParams = "";
        if (typeof latitude !== "undefined" && typeof longitude !== "undefined") {
            latLongParams = "?latitudeOpt=" + latitude + "&longitudeOpt=" + longitude;
        }
        $.getJSON(locationsUrl + latLongParams, function(data) {
            callback(data);
        });
    }

    function hasNumber(myString) {
        return /\d/.test(myString);
    }

    $("#yourPostcode").autocomplete({
        source: function (request, response) {
            locations.sort(sort_by('locationName', true, function(a){return a.toUpperCase()}));
            array = $.map(locations, function (value, key) {
                return {
                    label: value.locationName
                }
            });
            response($.ui.autocomplete.filter(array, request.term));
        },

        select: function(e, ui) {
            if(!$('#listOfLocations li:contains("' + ui.item.label + '")').find('input').is(':checked')) {
                $('#scrollingList').scrollTo($('#listOfLocations li:contains("' + ui.item.label + '")'), 400);
                $('#listOfLocations li:contains("' + ui.item.label + '")').find('input').trigger('click')
                    .prop('checked', true).parent().addClass('selected');
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
        if(e.which == ENTER_KEY && ($('.ui-autocomplete li:contains("' + thisVal + '")').text() == thisVal)) {
            $('.ui-autocomplete li:contains("' + thisVal + '")').trigger('click');
            $('#yourPostcode').val('');
        }
    });

    var locations = [];

    var sort_by = function(field, reverse, primer){
        var key = function (x) {return primer ? primer(x[field]) : x[field]};

        return function (a,b) {
            var A = key(a), B = key(b);
            return ( (A < B) ? -1 : ((A > B) ? 1 : 0) ) * [-1,1][+!!reverse];
        }
    };

    function loadLocationsFromPostCode() {
        $('#loadingLocations').removeClass('toggle-content');
        $('#noLocationsFound').addClass('toggle-content');

        var currentPostcode = $('#yourPostcode').val().toUpperCase().replace(/ /g,'');
        var addressLookupUrl = "/fset-fast-track/address-search/"+currentPostcode;
        var locationsCallback = function(response) {
                                    locations = response
                                    addDistance();
                                }
        $.getJSON(addressLookupUrl, function(data) {
            //console.log("Request succeeded for postcode=" + currentPostcode + ": location=" + data.latitude +"," + data.longitude);
            loadLocationsJson(locationsCallback, data.latitude, data.longitude);
        }).fail(function(xhr, textStatus, error ) {
            console.log( "Request failed for postcode=" + currentPostcode + ": " +  textStatus + ", " + error);
            if ("Bad Request" == error) {
                showInvalidPostcodePanel();
            }
            if(locations.length === 0) loadLocationsJson(locationsCallback);
        }).always(function(){
            $('#loadingLocations').addClass('toggle-content');
        });
    }

    function addDistance() {
        showNearby();
        displaySelectedLocations();
    }

    function showInvalidPostcodePanel() {
        $('#invalidPostcodeWrapper').slideDown(300);
    }

    function hideInvalidPostcodePanel() {
        $('#invalidPostcodeWrapper').slideUp(300);
    }

    function showNearby() {
        $('#listOfLocations input').not(':checked').closest('li').remove();
        $('#loadingLocations').addClass('toggle-content');
        for (var i = 0; i < locations.length; i++) {
            if($('#listOfLocations label:contains("' + locations[i].locationName + '")').length ) {

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
                    '<input type="checkbox" id="' + locations[i].locationId + '" data-schemename value="'+locations[i].locationId+'">' +
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
        setTimeout(loadLocationsFromPostCode(), 300);
        e.preventDefault();
    });

    $('#yourPostcode').on('keyup', function(e) {
        var MIN_LENGTH = 5;
        var searchByPostcode = $(this).val().length > MIN_LENGTH && hasNumber($(this).val());
        if(searchByPostcode) {
            $('#updateLocationWrapper').slideDown(300);
            hideInvalidPostcodePanel();
        }
        if(e.which == ENTER_KEY && searchByPostcode) {
            $('#updateLocationWrapper').slideUp(300);
            setTimeout(loadLocationsFromPostCode, 300);
        }
    });

    $(document).ready(function() {
      $(window).keydown(function(event){
        if(event.keyCode == 13) {
          event.preventDefault();
          return false;
        }
      });
      loadLocationsFromPostCode();
    });

    var schemePrefArray = ['Empty'],
        firstEmptyPosition = $.inArray('Empty', schemePrefArray),
        getOrdinal = function(n) {
            var s=["th","st","nd","rd"],
            v=n%100;
            return n+(s[(v-20)%10]||s[v]||s[0]);
        };

    $('html').on('change', '[data-schemename]', function() {
        var $this = $(this),
            thisScheme = $this.closest('.block-label').find('.location-name').text(),
            thisSchemeID = $this.attr('id'),
            thisSchemeValue = $this.attr('value'),
            schemeReq = $this.closest('.scheme-container').find('[data-scheme-req]').html(),
            isSpecial = $this.closest('.scheme-container').find('[data-spec-scheme]').length,
            arrayPosition = $.inArray(thisSchemeID, schemePrefArray),
            emptyPosition = $.inArray('Empty', schemePrefArray);

        if(arrayPosition < 0 && $this.is(':checked')) {
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
                schemesAsHTML += '<li>' + result[0].schemes[i].name + '</li>';
            }

            var hiddenLocationId = "#locationIds_"+arrayPositionNow;
            $(hiddenLocationId).val(thisSchemeValue);

            $('#selectedPrefList > li').eq(arrayPositionNow).after(
                '<li class="location-prefcontainer" data-scheme-id="' + thisSchemeID + '">' +
                '<span data-schemprefinlist>' + getOrdinal(arrayPositionNow + 1) + ' preference </span>' +
                '<div class="text scheme-elegrepeat">' +
                '<span class="bold-small" data-schemenameinlist>' + thisScheme +
                '</span><a href="#" class="link-unimp scheme-remove">' +
                '<i class="fa fa-times" aria-hidden="true"></i>Remove</a>' +
                '<details class="no-btm-margin"><summary class="no-btm-margin">' +
                schemesLength + ' available schemes</summary>' +
                '<div class="detail-content panel-indent"><ul>' + schemesAsHTML + '</ul></div></details></div>');

            $this.closest('.scheme-container').addClass('selected-scheme').find('.selected-preference').text(getOrdinal(arrayPositionNow + 1)).removeClass('invisible');
        }

        if(!$this.is(':checked')) {
            var hiddenLocationId = "#locationIds_"+arrayPosition;
            $(hiddenLocationId).val('');
            schemePrefArray.splice(arrayPosition, 1, 'Empty');
            $('#selectedPrefList').find('[data-scheme-id="' + thisSchemeID + '"]').remove();
            $this.closest('.scheme-container').removeClass('selected-scheme').find('.selected-preference').text('N/A').addClass('invisible');
        }
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

    function displaySelectedLocations(){
        for(i = 0; i < $("[id^='locationIds_']").length; i++){
            var hiddenLocation = $('#locationIds_'+i)
            if(hiddenLocation !== "undefined") {
                var location = hiddenLocation.val();
                var sid = "#" + location;
                if(location !== '' && typeof $(sid) !== "undefined"){
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
});
