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

/**
    Runs when the page is first loaded. Does the following:
    - Sets up form submission event.
    - Sets up map.
    - For testing purposes right now adds hard coded landmarks to map.
 */

var sharedMap = null;
var places;
var autoComplete;
var autoCompleteService;
var countryRestrict = {'country': 'us'};
    
    
function onPageLoad() {
  attachSearchFormSubmissionEvent();
  map = initMap();
  sharedMap = map;
  getInitialContent();
  initAutoComplete();
}

/** 
    Makes it so the search form uses our custom JS and not the default HTML functionality.
 */
function attachSearchFormSubmissionEvent() {
  const searchForm = document.getElementById('search-form');
  searchForm.addEventListener('submit', event => {
    event.preventDefault();
    doSearch(event.target);
  });
}

/**
    Clears all previous children of the article list.
 */
function clearArticleList() {
    const articleList = document.getElementById("articles-list");
    while(articleList.firstChild) {
        articleList.removeChild(articleList.firstChild);
    }
}


/**
    Begin regional news pipeline functions.
    Two possible paths.
    - Searches invoked by the search button in the form.
    - Searched invoked by clicking on a suggested location.
 */

/**
    Starting point of a region search that is invoked from the search button. Submits query to the servlet and then passes its responses to the getRegionArticles function.
 */
function doSearch(form) {
    let region = form.elements["region-search-field"].value;
    if (region != "") {
        let topic = form.elements["topic-search-field"].value;
        request = {'input' : region};
        autoCompleteService.getPlacePredictions(request, ((predictions) => finishSearch(predictions, topic)));
    } else {
        /**
            If we add a warning message later for invalid locations it could go here.
         */
    }
}

/**
    Starting point of a region search that is not invoked by the search button. Submits query to the servlet and then passes its responses to the getRegionArticles function.
 */
function doSearchNotFromForm() {
    let region = document.getElementById("region-search-field").value;
    if (region != "") {
        let topic = document.getElementById("topic-search-field").value;
        console.log("region: " + region + " topic: " + topic);
        let fetchParameter = "/region-news?region=" + region + "&topic=" + topic;
        const response = fetch(fetchParameter);
        response.then(getRegionArticles);
    } 
}

/**
    Finishes the search using autocomplete suggested results.
 */
function finishSearch(suggestions, topic) {
    let region = suggestions[0].description;
    document.getElementById("region-search-field").value = region;
    console.log("region: " + region + " topic: " + topic);
    let fetchParameter = "/region-news?region=" + region + "&topic=" + topic;
    const response = fetch(fetchParameter);
    response.then(getRegionArticles);
    const response2 = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + region +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
    response2.then(getJSONOfGeoCoding);
}

/**
    Get the region response geo coding.
 */
function getJSONOfGeoCoding(response) {
    const json = response.json();
    return json.then(moveToLocation);
}

/**
    Moves the screen to the provided locaiton.
 */
function moveToLocation(json) {
    if (json.results[0].geometry) {
        sharedMap.panTo(json.results[0].geometry.location);
        sharedMap.setZoom(10);
    }
}

/**
    Retrieves the json from the region-news servlet response.
 */
function getRegionArticles(response) {
    const json = response.json();
    json.then(displayArticlesFromJSON);
}


/**
    Displays the articles contained in the json to the webpage.
 */
function displayArticlesFromJSON(json) {
    clearArticleList();
    for (index in json) {
        let articleObj = json[index];
        addArticle(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url);
    }
    openNav();
}

/**
    End regional news pipeline functions.
 */

/**
    Begin world news pipeline functions.
 */

/**
    Retrieves the json from the world-news servlet response.
 */
function getWorldArticles(response) {
    const json = response.json();
    json.then(configureWorldArticles);
}

/**
    Sorts the world news articles, saves them and adds pins to the map for them.
 */
function configureWorldArticles(json) {
    let articleMap = new Map();
    for (index in json) {
        let articleObj = json[index];
        if (articleMap[articleObj.location] == null) {
            articleMap[articleObj.location] = [articleObj];
        } else {
            articleMap[articleObj.location].push(articleObj);
        }
    }
    for (key in articleMap) {
        console.log(key, articleMap[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMap[key]));
    }
}

/**
    Get the region response geo coding.
 */
function getRegionJSONOfGeoCoding(articles, response) {
    const json = response.json();
    return json.then(placeArticlesPinOnMap.bind(null, articles));
}

/**
    Prints the response.
 */
function placeArticlesPinOnMap(articles, json) {
    let lat = json.results[0].geometry.location.lat;
    let long = json.results[0].geometry.location.lng;
    let title = json.results[0].formatted_address;
    addLandmark(sharedMap, lat, long, title, articles);
    return json;
}

/**
    End world news pipeline functions.
 */

/**
    Fetches the initial articles displayed on the page.
 */
function getInitialContent() {
    /**
        Initial message telling the user to search or click a pin.
     */
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let titleElement = document.createElement('h2');
    titleElement.innerText = "Enter a state or city in the search bar above or click a pin on the map for articles relevant to that location.";
    item.appendChild(titleElement);
    articleList.appendChild(item);
    /**
        Fetches the world news, clusters it, and places the pins corresponding to its included locations.
     */
    let fetchParameter = "/world-news";
    const response = fetch(fetchParameter);
    response.then(getWorldArticles);
}

/**
    Displays the passed list of articles.
 */
function displayArticles(articles) {
    clearArticleList();
    for (i = 0; i < articles.length; i++) {
        articleObj = articles[i];
        addArticle(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url);
    }
    openNav();
}

/**
    Adds an article with the passed attributes to the article list.
 */
function addArticle(title, publisher, content, date, link) {
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let titleElement = document.createElement('h2');
    titleElement.innerText = title;
    let publisherElement = document.createElement('h4');
    publisherElement.innerText = publisher + " - " + date;
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
function addLandmark(map, lat, lng, title, articles) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  marker.addListener('click', () => {
    displayArticles(articles);
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

function initAutoComplete() {

    // Create the autoComplete object and associate it with the UI input control.
    // Restrict the search to the default country, and to place type "cities".
    autoComplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */ (
            document.getElementById('region-search-field')), {
            types: ['(cities)'],
            componentRestrictions: countryRestrict
        });
    places = new google.maps.places.PlacesService(map);
    autoCompleteService = new google.maps.places.AutocompleteService();
    autoComplete.addListener('place_changed', onPlaceChanged);


    // Add a DOM event listener to react when the user selects a country.
    document.getElementById('country').addEventListener(
        'change', setAutoCompleteCountry);

    }

    // When the user selects a city, get the place details for the city and
    // zoom the map in on the city.
function onPlaceChanged() {
    var place = autoComplete.getPlace();
    console.log(place);
    if (place.geometry) {
        sharedMap.panTo(place.geometry.location);
        sharedMap.setZoom(10);
        doSearchNotFromForm();
    } else {
        document.getElementById('region-search-field').placeholder = 'Enter a city';
    }
}

function openNav() {
    document.getElementById("article-list-container").style.width = "30vw";
}

function closeNav() {
    document.getElementById("article-list-container").style.width = "0";
}