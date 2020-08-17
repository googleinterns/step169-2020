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
var countryRestrict = {'country': 'us'};
var cityMarkers = [];
var subcountryMarkers = [];
var countryMarkers = [];
var sportsMarkers = [];
var politicsMarkers = [];
var businessMarkers = [];
var miscMarkers = [];
var showpol =false;
var showmisc =false;
var showbus =false;
var showsports =false;

function onPageLoad() {
  attachSearchFormSubmissionEvent();
  map = initMap();
  sharedMap = map;
  getInitialContent();
  initAutoComplete();
}

/**
    Prints the nlp test response.
 */
function printNLPTestResponse(reponse) {
    console.log(response);
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
    testNLP();
}

/**
    Finishes the search using autocomplete suggested results.
 */
function finishSearch(suggestions, topic) {
    if (suggestions && suggestions.length > 0) {
        let region = suggestions[0].description;
        document.getElementById("region-search-field").value = region;
        console.log("region: " + region + " topic: " + topic);
        let fetchParameter = "/region-news?region=" + region + "&topic=" + topic;
        const response = fetch(fetchParameter);
        response.then(getRegionArticles);
        const response2 = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + region +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response2.then(getJSONOfGeoCoding);
    }
    testNLP();
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
    let articleMapCity = new Map();
    let articleMapSubcountry = new Map();
    let articleMapCountry = new Map();
    let articleMapSports = new Map();
    let articleMapBusiness = new Map();
    let articleMapPolitics = new Map();
    let articleMapMisc = new Map();

    // Creates and fills maps for each level of geographical divisions
    for (index in json) {
        let articleObj = json[index];
        if (articleMapCity[articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country] == null) {
            articleMapCity[articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country] = [articleObj];
        } else {
            articleMapCity[articleObj.location.city + ", " + articleObj.location.subcountry + ", " + articleObj.location.country].push(articleObj);
        }

        if (articleMapSubcountry[articleObj.location.subcountry + ", " + articleObj.location.country] == null) {
            articleMapSubcountry[articleObj.location.subcountry + ", " + articleObj.location.country] = [articleObj];
        } else {
            articleMapSubcountry[articleObj.location.subcountry + ", " + articleObj.location.country].push(articleObj);
        }

        if (articleMapCountry[articleObj.location.country] == null) {
            articleMapCountry[articleObj.location.country] = [articleObj];
        } else {
            articleMapCountry[articleObj.location.country].push(articleObj);
        }
        
        // CATEGORIZATION FEATURE 
        if (articleObj.theme == "sports"){
            if (articleMapSports[articleObj.location.country] == null) {
                articleMapSports[articleObj.location.country] = [articleObj];
            } else {
                articleMapSports[articleObj.location.country].push(articleObj);
            }
        } else if (articleObj.theme == "politics"){
            if (articleMapPolitics[articleObj.location.country] == null) {
                articleMapPolitics[articleObj.location.country] = [articleObj];
            } else {
                articleMapPolitics[articleObj.location.country].push(articleObj);
            }
        } else if (articleObj.theme == "miscellaneous"){
            if (articleMapMisc[articleObj.location.country] == null) {
                articleMapMisc[articleObj.location.country] = [articleObj];
            } else {
                articleMapMisc[articleObj.location.country].push(articleObj);
            }
        } else if (articleObj.theme == "business"){
            if (articleMapBusiness[articleObj.location.country] == null) {
                articleMapBusiness[articleObj.location.country] = [articleObj];
            } else {
                articleMapBusiness[articleObj.location.country].push(articleObj);
            }
        }
    }
    for (key in articleMapCity) {
        // console.log(key, articleMapCity[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapCity[key], "city"));
    }
    for (key in articleMapSubcountry) {
        // console.log(key, articleMapSubcountry[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapSubcountry[key], "subcountry"));
    }
    for (key in articleMapCountry) {
        // console.log(key, articleMapCountry[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapCountry[key], "country"));
    }    
    for (key in articleMapSports) {
        // console.log(key, articleMapSports[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapSports[key], "sports"));
    }
    for (key in articleMapBusiness) {
        // console.log(key, articleMapBusiness[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapBusiness[key], "business"));
    }
    for (key in articleMapPolitics) {
        // console.log(key, articleMapPolitics[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapPolitics[key], "politics"));
    }
    for (key in articleMapMisc) {
        // console.log(key, articleMapMisc[key].length);
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyDTrfkvl_JKE7dPcK3BBHlO4xF7JKFK4bY");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMapMisc[key], "misc"));
    }
}

/**
    Get the world news response geo coding.
 */
function getRegionJSONOfGeoCoding(articles, label, response) {
    // console.log("Response is: " + response);
    // console.log("label is : " + label);

    const json = response.json();

    return json.then(placeArticlesPinOnMap.bind(null, articles, label));
}

/**
    Prints the response.
 */
function placeArticlesPinOnMap(articles, label, json) {
    let lat = json.results[0].geometry.location.lat;
    let long = json.results[0].geometry.location.lng;
    let title = json.results[0].formatted_address;
    addLandmark(sharedMap, lat, long, title, articles, label);
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

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title, articles, label) {
    if (label=="city"){
        newMarker(map, lat, lng, title, "red", cityMarkers, articles);
    } else if (label == "subcountry"){
        newMarker(map, lat, lng, title, "red", subcountryMarkers, articles);
    } else if (label == "country"){
        newMarker(map, lat, lng, title, "red", countryMarkers, articles);
    }
    else if (label == "sports"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkers, articles);
    }
    else if (label == "business"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkers, articles);
    }
    else if (label == "politics"){
        newMarker(map, lat, lng, title, "green", politicsMarkers, articles);
    }
    else if (label == "misc"){
        newMarker(map, lat-0.15, lng-0.15, title, "purple", miscMarkers, articles);
    }
    showBasedOnZoom();
}

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
            for (i = 0; i < countryMarkers.length; i++) {
                countryMarkers[i].setVisible(zoom < 5);
            }
            for (i = 0; i < cityMarkers.length; i++) {
                cityMarkers[i].setVisible(zoom > 8);
            }
            for (i = 0; i < subcountryMarkers.length; i++) {
                subcountryMarkers[i].setVisible((zoom >= 5) && (zoom <= 8));
            }
        }
    });
}

function showPol(){
    showpol = !showpol;
    displayRelevant(politicsMarkers, showpol);
}
function showMisc(){
    showmisc = !showmisc;
    displayRelevant(miscMarkers, showmisc);
}
function showBus(){
    showbus = !showbus;
    displayRelevant(businessMarkers, showbus);
}
function showSports(){
    showsports = !showsports;
    displayRelevant(sportsMarkers, showsports);
}
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

function openNav() {
    articlesOpen = true;
    document.getElementById("article-list-container").style.transform = "translateX(-36vw)";
}

function closeNav() {
    articlesOpen = false;
    document.getElementById("article-list-container").style.transform = "translateX(0)";
}

function toggleNav() {
    if (articlesOpen) {
        closeNav();
    } else {
        openNav();
    }
}

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

/**
    Makes call to the NLP testing servlet.
 */
function testNLP() {
    console.log("-- BEGIN ARTICLE LABELER TESTS --\n-- CORRECT | NLP GUESS --\n");
    for (i = 0; i < labelerTestCases.length; i++) {
        var loc = labelerTestCases[i];
        let fetchParameter = "/nlp-test?url=" + loc.url;
        const response = fetch(fetchParameter);
        let locString = loc.city + "," + loc.subCountry + "," + loc.country;
        response.then((resp) => resp.text().then((text) => console.log(locString + " | " + text)));
    }
}

var labelerTestCases = [
    {
        city: "tampa",
        subCountry: "florida",
        country: "united states",
        url: "https://abcnews.go.com/US/wireStory/officer-injured-wounded-florida-shooting-spree-72100571?cid=clicksource_4380645_2_heads_hero_live_headlines_hed"
    },
    {
        city: "portland",
        subCountry: "oregon",
        country: "united states",
        url: "https://abcnews.go.com/US/portland-nations-hotbed-clashes-protesters-federal-agents/story?id=72050134&cid=clicksource_4380645_13_hero_headlines_bsq_image"
    },
    {
        city: "berkeley",
        subCountry: "california",
        country: "united states",
        url: "https://www.nbcbayarea.com/news/local/east-bay/berkeley-officer-discharges-weapon-during-encounter-with-suspects/2336142/"
    },
    {
        city: "san mateo",
        subCountry: "california",
        country: "united states",
        url: "https://www.nbcbayarea.com/news/local/peninsula/2-arrested-after-30-pounds-of-suspected-meth-found-during-san-mateo-traffic-stop/2333720/"
    },
    {
        city: "san francisco",
        subCountry: "california",
        country: "united states",
        url: "https://www.nbcbayarea.com/news/local/san-francisco/san-francisco-police-sergeant-4-officers-injured-in-separate-incidents/2335962/"
    },
    {
        city: "fort lauderdale",
        subCountry: "florida",
        country: "united states",
        url: "https://www.sun-sentinel.com/local/broward/fort-lauderdale/fl-ne-water-crisis-fort-lauderdale-lawsuit-20200731-mtavzrdjfvfkxn47qksrniijry-story.html"
    },
    {
        city: "florida",
        subCountry: "florida",
        country: "united states",
        url: "https://abcnews.go.com/Politics/fauci-warns-states-coronavirus-numbers-good/story?id=72059455&cid=clicksource_4380645_2_heads_hero_live_hero_hed"
    },
    {
        city: "altanta",
        subCountry: "georgia",
        country: "united states",
        url: "https://abcnews.go.com/Politics/read-president-barack-obamas-eulogy-rep-john-lewis/story?id=72081189&cid=clicksource_4380645_5_three_posts_card_image"
    },
    {
        city: "philadelphia",
        subCountry: "pennsylvania",
        country: "united states",
        url: "https://abcnews.go.com/US/wireStory/philadelphia-trash-piles-pandemic-stymies-removal-72080100?cid=clicksource_4380645_2_heads_hero_live_headlines_hed"
    },
    {
        city: "beirut",
        subCountry: "beyrouth",
        country: "lebanon",
        url: "https://www.nytimes.com/2020/08/05/world/middleeast/beirut-lebanon-explosion.html"
    },
    {
        city: "tokyo",
        subCountry: "tokyo",
        country: "japan",
        url: "https://www.nytimes.com/2020/08/05/business/japan-entry-ban-coronavirus.html?action=click&module=News&pgtype=Homepage"
    },
    {
        city: "nanchang",
        subCountry: "jiangxi sheng",
        country: "china",
        url: "https://www.bbc.com/news/world-asia-china-53666557"
    }
];