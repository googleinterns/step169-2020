// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
    var map, places;
    var autocomplete;
    var countryRestrict = {'country': 'us'};
    
    
function onPageLoad() {
  attachSearchFormSubmissionEvent();
  map = initMap();
  getInitialContent();
  addLandmark(map, 40.7128, -74.0060, "New York City");
  addLandmark(map, 41.8781, -87.6298, "Chicago");
  addLandmark(map, 34.0522, -118.2437, "Los Angeles");
  initAuto();

}

function attachSearchFormSubmissionEvent() {
  const searchForm = document.getElementById('search-form');
  searchForm.addEventListener('submit', event => {
    event.preventDefault();
    doSearch(event.target);
  });
}

function doSearch(form) {
  // TODO implement the search
}

/**
    Fetches the initial articles displayed on the page.
 */
function getInitialContent() {
    for (i = 1; i <= 10; i++) {
        addArticle("Title " + i, "Publisher " + i, "Content " + i, "https://www.google.com");
    }
}

/**
    Function for testing that adds articles with a custom title.
 */
function testAddArticles(title) {
    const articleList = document.getElementById("articles-list");
    while(articleList.firstChild) {
        articleList.removeChild(articleList.firstChild);
    }
    for (i = 1; i <= 10; i++) {
        addArticle(title + " " + i, "Publisher " + i, "Content " + i, "https://www.google.com");
    }
}

/**
    Adds an article with the passed attributes to the article list.
 */
function addArticle(title, publisher, content, link) {
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let titleElement = document.createElement('h2');
    titleElement.innerText = title;
    let publisherElement = document.createElement('h4');
    publisherElement.innerText = publisher;
    let contentElement = document.createElement('p'); 
    contentElement.innerText = content + "\n";
    let linkElement = document.createElement('a');
    linkElement.innerText = "Read More"
    linkElement.href = link;
    contentElement.appendChild(linkElement);
    item.appendChild(titleElement);
    item.appendChild(publisherElement);
    item.appendChild(contentElement);
    articleList.appendChild(item);
}

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  marker.addListener('click', () => {
    testAddArticles(title);
  });
}


/** Creates a map and adds it to the page. */
function initMap() {
    // Styles a map in night mode.
    map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 40.674, lng: -73.945},
    zoom: 6,
    disableDefaultUI:true,
    zoomControl:true,

    styles: [
            {
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#212121"
                }
                ]
            },
            {
                "elementType": "labels",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "elementType": "labels.icon",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#757575"
                }
                ]
            },
            {
                "elementType": "labels.text.stroke",
                "stylers": [
                {
                    "color": "#212121"
                }
                ]
            },
            {
                "featureType": "administrative",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#757575"
                },
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "administrative.country",
                "elementType": "labels.text",
                "stylers": [
                {
                    "visibility": "on"
                }
                ]
            },
            {
                "featureType": "administrative.country",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#9e9e9e"
                }
                ]
            },
            {
                "featureType": "administrative.land_parcel",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "administrative.locality",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#bdbdbd"
                }
                ]
            },
            {
                "featureType": "administrative.neighborhood",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "administrative.province",
                "elementType": "labels.text",
                "stylers": [
                {
                    "visibility": "on"
                }
                ]
            },
            {
                "featureType": "poi",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "poi",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#757575"
                }
                ]
            },
            {
                "featureType": "poi.park",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#181818"
                }
                ]
            },
            {
                "featureType": "poi.park",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#616161"
                }
                ]
            },
            {
                "featureType": "poi.park",
                "elementType": "labels.text.stroke",
                "stylers": [
                {
                    "color": "#1b1b1b"
                }
                ]
            },
            {
                "featureType": "road",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "road",
                "elementType": "geometry.fill",
                "stylers": [
                {
                    "color": "#2c2c2c"
                }
                ]
            },
            {
                "featureType": "road",
                "elementType": "labels.icon",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "road",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#8a8a8a"
                }
                ]
            },
            {
                "featureType": "road.arterial",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#373737"
                }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#3c3c3c"
                }
                ]
            },
            {
                "featureType": "road.highway.controlled_access",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#4e4e4e"
                }
                ]
            },
            {
                "featureType": "road.local",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#616161"
                }
                ]
            },
            {
                "featureType": "transit",
                "stylers": [
                {
                    "visibility": "off"
                }
                ]
            },
            {
                "featureType": "transit",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#757575"
                }
                ]
            },
            {
                "featureType": "water",
                "elementType": "geometry",
                "stylers": [
                {
                    "color": "#000000"
                }
                ]
            },
            {
                "featureType": "water",
                "elementType": "labels.text.fill",
                "stylers": [
                {
                    "color": "#3d3d3d"
                }
                ]
            }
            ]
        });
    return map;
}

function initAuto() {

    // Create the autocomplete object and associate it with the UI input control.
    // Restrict the search to the default country, and to place type "cities".
    autocomplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */ (
            document.getElementById('region-search-field')), {
            types: ['(cities)'],
            componentRestrictions: countryRestrict
        });
    places = new google.maps.places.PlacesService(map);

    autocomplete.addListener('place_changed', onPlaceChanged);

    // Add a DOM event listener to react when the user selects a country.
    document.getElementById('country').addEventListener(
        'change', setAutocompleteCountry);
    }

    // When the user selects a city, get the place details for the city and
    // zoom the map in on the city.
function onPlaceChanged() {
    var place = autocomplete.getPlace();
    if (place.geometry) {
        map.panTo(place.geometry.location);
        map.setZoom(10);
        search();
    } else {
        document.getElementById('region-search-field').placeholder = 'Enter a city';
    }
}
