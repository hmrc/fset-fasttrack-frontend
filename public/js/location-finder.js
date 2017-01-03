$(function(){

    jQuery.fn.scrollTo = function(elem, speed) {
        $(this).animate({
            scrollTop:  $(this).scrollTop() - $(this).offset().top + $(elem).offset().top
        }, speed == undefined ? 1000 : speed);
        return this;
    };

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
        callback('{ "locations": [ { "name": "foo" }, { "name": "bar" }]}')
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

    var lat = '',
        lng = '',
        myGeoLocation = '';

    var locations = [];

    var sort_by = function(field, reverse, primer){
        var key = function (x) {return primer ? primer(x[field]) : x[field]};

        return function (a,b) {
            var A = key(a), B = key(b);
            return ( (A < B) ? -1 : ((A > B) ? 1 : 0) ) * [-1,1][+!!reverse];
        }
    }

    function getLatLongFromPostCode() {
        $('#loadingLocations').removeClass('toggle-content');
        $('#noLocationsFound').addClass('toggle-content');

        loadJSON(function(response) {
            // Parse JSON string into object
            var locationsResponse = JSON.parse(response);
            locations = locationsResponse.locations;
        });

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

    // locations.sort(sort_by('name', true, function(a){return a.toUpperCase()}));
    // http://stackoverflow.com/questions/979256/sorting-an-array-of-javascript-objects

    function showNearby() {
        $('#listOfLocations input').not(':checked').closest('li').remove();
        $('#loadingLocations').addClass('toggle-content');
        for (var i = 0; i < locations.length; i++) {
            if($('#listOfLocations label:contains("' + locations[i].name + '")').length ) {

            } else {
                $('#listOfLocations').append(
                    '<li class="scheme-container">' +
                    '<span class="selected-preference invisible">N/A</span>' +
                    '<label for="' + locations[i].id + '" class="block-label block-label-slim">' +
                    '<input type="checkbox" id="' + locations[i].id + '" data-schemename>' +
                    '<span class="location-name">' + locations[i].name + '</span>' +
                    // '<span class="location-distance">' + locations[i].distance + ' miles</span>' +
                    '</label>' +
                    '</li>' );
            }
        }
        $('#scrollingList').scrollTo($('#listOfLocations li:first-child'), 400);
    }

    getLatLongFromPostCode();

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

});