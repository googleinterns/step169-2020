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
var articlesOpen = false;
var smallSearch = false;
var countryRestrict = {'country': 'us'};

// marker collections
var cityMarkers = [];
var subcountryMarkers = [];
var countryMarkers = [];
var sportsMarkersCountry = [];
var sportsMarkersSubcountry = [];
var sportsMarkersCity = [];
var politicsMarkersCountry = [];
var politicsMarkersSubcountry = [];
var politicsMarkersCity = [];
var businessMarkersCountry = [];
var businessMarkersSubcountry = [];
var businessMarkersCity = [];
var miscMarkersCountry = [];
var miscMarkersSubcountry = [];
var miscMarkersCity = [];

// boolean to set visibility 
var showpol =false;
var showmisc =false;
var showbus =false;
var showsports =false;

// Article lists for each category
let articleMapCity = new Map();
let articleMapSubcountry = new Map();
let articleMapCountry = new Map();
let articleMapSportsCountry = new Map();
let articleMapSportsSubcountry = new Map();
let articleMapSportsCity = new Map();
let articleMapBusinessCountry = new Map();
let articleMapBusinessSubcountry = new Map();
let articleMapBusinessCity = new Map();
let articleMapPoliticsCountry = new Map();
let articleMapPoliticsSubcountry = new Map();
let articleMapPoliticsCity = new Map();
let articleMapMiscCountry = new Map();
let articleMapMiscSubcountry = new Map();
let articleMapMiscCity = new Map();


function onPageLoad() {
  window.addEventListener("resize", clearSmallForm);
  attachSearchFormSubmissionEvent();
  attachSmallSearchFormSubmissionEvent();
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
    Makes it so the search form uses our custom JS and not the default HTML functionality. On small screen
 */
function attachSmallSearchFormSubmissionEvent() {
  const smallSearchForm = document.getElementById('small-search-form');
  document.getElementById("small-screen-display").style.display = "none";
  smallSearchForm.addEventListener('submit', event => {
    event.preventDefault();
    doSearch(event.target);
    document.getElementById("small-screen-display").style.display = "none";
    document.getElementById("search-expand").style.display = "block";
    smallSearch = false;
    openNav();
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
        /*
        types: ['(cities)'],
            componentRestrictions: countryRestrict
        */
        request = {'input' : region, 'types' : ['(cities)']/*, 'componentRestrictions' : countryRestrict*/};
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
        addArticle(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url, articleObj.thumbnailUrl);
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


    // Creates and fills maps for each level of geographical divisions and each category
    for (index in json) {
        let articleObj = json[index];
        fillArticleMaps(articleMapCity, articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        fillArticleMaps(articleMapSubcountry, articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        fillArticleMaps(articleMapCountry, articleObj.location.country, articleObj);
       
        // CATEGORIZATION FEATURE 
        if (articleObj.theme == "sports"){
            fillArticleMaps(articleMapSportsCountry, articleObj.location.country, articleObj);
            fillArticleMaps(articleMapSportsSubcountry, articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
            fillArticleMaps(articleMapSportsCity, articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        } else if (articleObj.theme == "politics"){
            fillArticleMaps(articleMapPoliticsCountry, articleObj.location.country, articleObj);
            fillArticleMaps(articleMapPoliticsSubcountry, articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
            fillArticleMaps(articleMapPoliticsCity, articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        } else if (articleObj.theme == "miscellaneous"){
            fillArticleMaps(articleMapMiscCountry, articleObj.location.country, articleObj);
            fillArticleMaps(articleMapMiscSubcountry, articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
            fillArticleMaps(articleMapMiscCity, articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        } else if (articleObj.theme == "business"){
            fillArticleMaps(articleMapBusinessCountry, articleObj.location.country, articleObj);
            fillArticleMaps(articleMapBusinessSubcountry, articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
            fillArticleMaps(articleMapBusinessCity, articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country, articleObj);
        }
    }

    fetchAddressGeocode(articleMapCity, "city");
    fetchAddressGeocode(articleMapSubcountry, "subcountry");
    fetchAddressGeocode(articleMapCountry, "country");
    fetchAddressGeocode(articleMapSportsCountry, "sportscountry");
    fetchAddressGeocode(articleMapSportsSubcountry, "sportssubcountry");
    fetchAddressGeocode(articleMapSportsCity, "sportscity");
    fetchAddressGeocode(articleMapBusinessCountry, "businesscountry");
    fetchAddressGeocode(articleMapBusinessSubcountry, "businesssubcountry");
    fetchAddressGeocode(articleMapBusinessCity, "businesscity");
    fetchAddressGeocode(articleMapPoliticsCountry, "politicscountry");
    fetchAddressGeocode(articleMapPoliticsSubcountry, "politicssubcountry");
    fetchAddressGeocode(articleMapPoliticsCity, "politicscity");
    fetchAddressGeocode(articleMapMiscCountry, "misccountry");
    fetchAddressGeocode(articleMapMiscSubcountry, "miscsubcountry");
    fetchAddressGeocode(articleMapMiscCity, "misccity");
}


function fetchAddressGeocode(articleMap, label){
    for (key in articleMap) {
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMap[key], label));
    }
}

function fillArticleMaps(articleMap, key, articles){
    if (articleMap[key] == null) {
        articleMap[key] = [articles];
    } else {
        articleMap[key].push(articles);
    }
}

/**
    Get the region response geo coding.
 */
function getRegionJSONOfGeoCoding(articles, label, response) {
    const json = response.json();

    return json.then(placeArticlesPinOnMap.bind(null, articles, label));
}

/**
    Prints the response.
 */
function placeArticlesPinOnMap(articles, label, json) {
    if (json.results[0] !== undefined) {
        let lat = json.results[0].geometry.location.lat;
        let long = json.results[0].geometry.location.lng;
        let title = json.results[0].formatted_address;
        addLandmark(sharedMap, lat, long, title, articles, label);
        return json;
    }
    return;
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
    titleElement.innerText = "Enter a state or city in the search bar or click a pin on the map for articles relevant to that location.";
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
        addArticle(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url, articleObj.thumbnailUrl);
    }
    openNav();
}

/**
    Adds an article with the passed attributes to the article list.
 */
function addArticle(title, publisher, content, date, link, thumbnail) {
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let linkElement = document.createElement('a');

    let titleElement = document.createElement('h2');
    titleElement.innerText = title;
    let picElement;
    if (thumbnail!==null && thumbnail!=undefined){
        picElement = document.createElement('img');
        picElement.className="thumbnail";
        picElement.src = thumbnail;
        picElement.style = "width:100%;"
        picElement.alt = 'pic';
    }
    let publisherElement = document.createElement('h4');
    publisherElement.innerText = publisher + " - " + formatTimestamp(date);

    // Style header
    const divElement = document.createElement('div');
    divElement.className = 'header-content'
    const divThumbnailElement = document.createElement('div');
    divThumbnailElement.className = 'thumbnail-content';
    const divTitleElement = document.createElement('div');
    divTitleElement.className = 'title-content';
    divTitleElement.appendChild(titleElement);
    divTitleElement.appendChild(publisherElement);
    if (thumbnail!==null && thumbnail!=undefined){
        divThumbnailElement.appendChild(picElement);
    }
    divElement.appendChild(divTitleElement);
    divElement.appendChild(divThumbnailElement);

    let contentElement = document.createElement('p'); 
    contentElement.innerText = content + "\n";
    linkElement.href = link;
    linkElement.target = "_blank";
    item.appendChild(divElement);
    item.appendChild(contentElement);
    linkElement.appendChild(item);
    articleList.appendChild(linkElement);
}

function formatTimestamp(timestamp) {
  if (timestamp === undefined) {
    return "Unknown Date";
  }

  const date = new Date(timestamp.seconds * 1000);
  const dateFormat = new Intl.DateTimeFormat('en',
   {month: 'long', day: 'numeric', year: 'numeric'});
  return dateFormat.format(date);
}

// Adds a landmark based on its corresponding label
function addLandmark(map, lat, lng, title, articles, label) {

    // Varies marker color based on label
    if (label=="city"){
        newMarker(map, lat, lng, title, "red", cityMarkers, articles);
    } else if (label == "subcountry"){
        newMarker(map, lat, lng, title, "red", subcountryMarkers, articles);
    } else if (label == "country"){
        newMarker(map, lat, lng, title, "red", countryMarkers, articles);
    }
    else if (label == "sportscountry"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkersCountry, articles);
    }    
    else if (label == "sportssubcountry"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkersSubcountry, articles);
    }    
    else if (label == "sportscity"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkersCity, articles);
    }
    else if (label == "businesscountry"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkersCountry, articles);
    }    
    else if (label == "businesscsubcountry"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkersSubcountry, articles);
    }
    else if (label == "businesscity"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkersCity, articles);
    }
    else if (label == "politicscountry"){
        newMarker(map, lat, lng, title, "green", politicsMarkersCountry, articles);
    }
    else if (label == "politicssubcountry"){
        newMarker(map, lat, lng, title, "green", politicsMarkersSubcountry, articles);
    }
    else if (label == "politicscity"){
        newMarker(map, lat, lng, title, "green", politicsMarkersCity, articles);
    }
    else if (label == "misccountry"){
        newMarker(map, lat-0.15, lng-0.15, title, "purple", miscMarkersCountry, articles);
    }
    else if (label == "miscsubcountry"){
        newMarker(map, lat-0.15, lng-0.15, title, "purple", miscMarkersSubcountry, articles);
    }
    else if (label == "misccity"){
        newMarker(map, lat-0.15, lng-0.15, title, "purple", miscMarkersCity, articles);
    }

    // Determine visibility
    showBasedOnZoom(); 
}

/** Adds a marker that shows an info window when clicked. */
function newMarker(map, lat, lng, title, color, markerSet, articles){
    let url = "https://maps.google.com/mapfiles/ms/icons/";
    url += color + "-dot.png";
    const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, 
      map: map, 
      title: title,
      icon: { url: url}
        });

    markerSet.push(marker);
    marker.addListener('click', () => {
        displayArticles(articles);
  });
  marker.setVisible(false);
}

// Decides which articles to show depending on the zoom 
function showBasedOnZoom(){
    /* Change markers on zoom */
    google.maps.event.addListener(sharedMap, 'zoom_changed', function() {
        var zoom = sharedMap.getZoom();
        // iterate over markers and call setVisible
        if (!showpol && !showbus && !showmisc && !showsports){
            zoomVisibility(countryMarkers, (zoom < 5));
            zoomVisibility(cityMarkers, (zoom > 8));
            zoomVisibility(subcountryMarkers, (zoom >= 5) && (zoom <= 8));
        }
        if (showpol) {
            zoomVisibility(politicsMarkersCountry, (zoom < 5));
            zoomVisibility(politicsMarkersCity, (zoom > 8));
            zoomVisibility(politicsMarkersSubcountry, (zoom >= 5) && (zoom <= 8));
        }
        if (showbus) {
            zoomVisibility(businessMarkersCountry, (zoom < 5));
            zoomVisibility(businessMarkersCity, (zoom > 8));
            zoomVisibility(businessMarkersSubcountry, (zoom >= 5) && (zoom <= 8));
        }
        if (showmisc) {
            zoomVisibility(miscMarkersCountry, (zoom < 5));
            zoomVisibility(miscMarkersCity, (zoom > 8));
            zoomVisibility(miscMarkersSubcountry, (zoom >= 5) && (zoom <= 8));
        }
        if (showsports) {
            zoomVisibility(sportsMarkersCountry, (zoom < 5));
            zoomVisibility(sportsMarkersCity, (zoom > 8));
            zoomVisibility(sportsMarkersSubcountry, (zoom >= 5) && (zoom <= 8));
        }
    });
}

// Sets visibility on true or false
function zoomVisibility(markerSet, visible){
    for (i = 0; i < markerSet.length; i++) {
        markerSet[i].setVisible(visible);
    }
}

// Shows or hides markers about politics on select or unselect respectively
function showPol(){
    showpol = !showpol;
    var zoom = sharedMap.getZoom();

    displayRelevant(politicsMarkersCountry, showpol && (zoom < 5));
    displayRelevant(politicsMarkersSubcountry, showpol && (zoom >= 5) && (zoom <= 8));
    displayRelevant(politicsMarkersCity, showpol && (zoom > 8));
}

// Shows or hides markers about miscellaneous on select or unselect respectively
function showMisc(){
    showmisc = !showmisc;
    var zoom = sharedMap.getZoom();

    displayRelevant(miscMarkersCountry, showmisc && (zoom < 5));
    displayRelevant(miscMarkersSubcountry, showmisc && (zoom >= 5) && (zoom <= 8));
    displayRelevant(miscMarkersCity, showmisc && (zoom > 8));
}

// Shows or hides markers about business on select or unselect respectively
function showBus(){
    showbus = !showbus;
    var zoom = sharedMap.getZoom();

    displayRelevant(businessMarkersCountry, showbus && (zoom < 5));
    displayRelevant(businessMarkersSubcountry, showbus && (zoom >= 5) && (zoom <= 8));
    displayRelevant(businessMarkersCity, showbus && (zoom > 8));
}

// Shows or hides markers about sports on select or unselect respectively
function showSports(){
    showsports = !showsports;
    var zoom = sharedMap.getZoom();

    displayRelevant(sportsMarkersCountry, showsports && (zoom < 5));
    displayRelevant(sportsMarkersSubcountry, showsports && (zoom >= 5) && (zoom <= 8));
    displayRelevant(sportsMarkersCity, showsports && (zoom > 8));
}

// Shows or hides relevant markers on select or unselect respectively of the categories
function displayRelevant(markerSet, visible){
    if (showpol || showbus || showmisc || showsports){
        for (i = 0; i < countryMarkers.length; i++) {
            countryMarkers[i].setVisible(false);
        }
       for (i = 0; i < cityMarkers.length; i++) {
            cityMarkers[i].setVisible(false);
        }
        for (i = 0; i < subcountryMarkers.length; i++) {
            subcountryMarkers[i].setVisible(false);
        }  
    }
    else {
        for (i = 0; i < countryMarkers.length; i++) {
            countryMarkers[i].setVisible(true);
        }
       for (i = 0; i < cityMarkers.length; i++) {
            cityMarkers[i].setVisible(false);
        }
        for (i = 0; i < subcountryMarkers.length; i++) {
            subcountryMarkers[i].setVisible(false);
        }  
    }

    for (i = 0; i < markerSet.length; i++) {
        markerSet[i].setVisible(visible);
    }   
}

/** Creates a map and adds it to the page. */
function initMap() {
    // Styles a map in night mode.
    map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 40.674, lng: -73.945},
    zoom: 6,
    disableDefaultUI:true,
    zoomControl:true,
    minZoom:3,
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
            "elementType": "geometry.fill",
            "stylers": [
            {
                "color": "#2c2c2c"
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
            types: ['(cities)']/*,
            componentRestrictions: countryRestrict*/
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

// Open Article list nav
function openNav() {
    articlesOpen = true;
    document.getElementById("article-list-container").style.transform = "translateX(-1200px)";
    document.getElementById("article-list-container").style.transition = "all 0.7s ease";
    if (smallSearch)
    endSearch();
}

// Close Article list nav
function closeNav() {
    articlesOpen = false;
    document.getElementById("article-list-container").style.transform = "translateX(1200px)";
    document.getElementById("article-list-container").style.transition = "all 0.7s ease";

}

// Open or close nav depending on current state
function toggleNav() {
    if (articlesOpen) {
        closeNav();
    } else {
        openNav();
    }
}

// open search mini form 
function expandSearch() {
    closeNav();
    
    document.getElementById("small-screen-display").style.display = "grid";
    document.getElementById("search-expand").style.display = "none";
    smallSearch=true;
}

// Closes small form if user clicks away
function endSearch() {
    document.getElementById("small-screen-display").style.display = "none";
    // console.log(document.getElementsByClassName("themes").style);
    // if (document.getElementById("themes").style.display === "none"){
    document.getElementById("search-expand").style.display = "block";
    smallSearch=false;
}

function clearSmallForm() {
  var scrWidth = document.documentElement.clientWidth;
  if (scrWidth>1200){
    document.getElementById("small-screen-display").style.display = "none";
    document.getElementById("search-expand").style.display = "none";
  }
  else{
    document.getElementById("search-expand").style.display = "block";
    document.getElementById("small-screen-display").style.display = "none";
  }
}

// Resets the region search bar
function clearSearchRegion() {
    const clearIcon = document.querySelector(".clear-region-icon");
    const searchBar = document.querySelector(".searchRegion");

    searchBar.addEventListener("keyup", () => {
        if(searchBar.value && clearIcon.style.visibility != "visible"){
        clearIcon.style.visibility = "visible";
        } else if(!searchBar.value) {
        clearIcon.style.visibility = "hidden";
        }
    });
    searchBar.value = "";
    clearIcon.style.visibility = "hidden";
}

// Resets the topic search bar
function clearSearchTopic() {
    const clearIcon = document.querySelector(".clear-topic-icon");
    const searchBar = document.querySelector(".searchTopic");

    searchBar.addEventListener("keyup", () => {
        if(searchBar.value && clearIcon.style.visibility != "visible"){
        clearIcon.style.visibility = "visible";
        } else if(!searchBar.value) {
        clearIcon.style.visibility = "hidden";
        }
    });
    searchBar.value = "";
    clearIcon.style.visibility = "hidden";
}

// Toggles initial user message upon entry
function disableTutorial(){
    document.getElementById('tutorial').style.display = "none";
}

// Retrieves login link from DataServletLogin.java
function getInLink(){
  fetch('/login').then(response => response.text()).then((log) => {
    const logElement = document.getElementById('login');
    logElement.href =log;
    logElement.text = "LOGIN";
 });
}

// Decides if the content of the screen should be shown or a link to login
function showContent(){
  fetch('/show').then(response => response.text()).then((show) => {
    if (show.localeCompare("yes")){
        document.getElementById('login').style.display = "-webkit-inline-box";
        document.getElementById('logout').style.display = "none";
        getInLink();
    }
    if (show.localeCompare("no")){
        document.getElementById('login').style.display = "none";
        document.getElementById('logout').style.display = "-webkit-inline-box";
        getOutLink();
    }
  });
}

// Retrieves logout link from DataServletLogout.java
function getOutLink(){
  fetch('/logout').then(response => response.text()).then((log) => {
    // Returns the element
    const logElement = document.getElementById('logout');
    logElement.href =log;
    logElement.text = "LOGOUT";
 });
}
