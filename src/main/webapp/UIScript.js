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
function showInitialMessageInArticleList(){
    /**
        Initial message telling the user to search or click a pin.
     */
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let titleElement = document.createElement('h2');
    titleElement.innerText = "Enter a state or city in the search bar or click a pin on the map for articles relevant to that location.";
    item.appendChild(titleElement);
    articleList.appendChild(item);
}

/**
    Adds an article with the passed attributes to the article list.
 */
function createArticleHtmlComponent(title, publisher, content, date, link, thumbnail) {
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
        picElement.style = "width:100%;border-radius:10px"
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


// Adds a marker with the articles based on its corresponding label
function addMarkerWithArticles(map, lat, lng, title, articles, label) {

    // Varies marker color based on label
    if (label == "city"){
        newMarker(map, lat, lng, title, "red", markers["city"], articles);
    } else if (label == "subcountry"){
        newMarker(map, lat, lng, title, "red", markers["subCountry"], articles);
    } else if (label == "country"){
        newMarker(map, lat, lng, title, "red", markers["country"], articles);
    }
    else if (label == "sportscountry"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkers["country"], articles);
    }    
    else if (label == "sportssubcountry"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkers["subCountry"], articles);
    }    
    else if (label == "sportscity"){        
        newMarker(map, lat+0.15, lng-0.25, title, "blue", sportsMarkers["city"], articles);
    }
    else if (label == "businesscountry"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkers["country"], articles);
    }    
    else if (label == "businesssubcountry"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkers["subCountry"], articles);
    }
    else if (label == "businesscity"){
        newMarker(map, lat+0.15, lng+0.15, title, "yellow", businessMarkers["city"], articles);
    }
    else if (label == "generalcountry"){
        newMarker(map, lat, lng, title, "white", generalMarkers["country"], articles);
    }
    else if (label == "generalsubcountry"){
        newMarker(map, lat, lng, title, "white", generalMarkers["subCountry"], articles);
    }
    else if (label == "generalcity"){
        newMarker(map, lat, lng, title, "white", generalMarkers["city"], articles);
    }
    else if (label == "healthcountry"){
        newMarker(map, lat-0.15, lng-0.15, title, "green", healthMarkers["country"], articles);
    }
    else if (label == "healthsubcountry"){
        newMarker(map, lat-0.15, lng-0.15, title, "green", healthMarkers["subCountry"], articles);
    }
    else if (label == "healthcity"){
        newMarker(map, lat-0.15, lng-0.15, title, "green", healthMarkers["city"], articles);
    }    
    else if (label == "techcountry"){
        newMarker(map, lat+0.25, lng-0.15, title, "orange", techMarkers["country"], articles);
    }
    else if (label == "techsubcountry"){
        newMarker(map, lat+0.25, lng-0.15, title, "orange", techMarkers["subCountry"], articles);
    }
    else if (label == "techcity"){
        newMarker(map, lat+0.25, lng-0.15, title, "orange", techMarkers["city"], articles);
    }
    else if (label == "sciencecountry"){
        newMarker(map, lat-0.25, lng-0.25, title, "purple", scienceMarkers["country"], articles);
    }
    else if (label == "sciencesubcountry"){
        newMarker(map, lat-0.25, lng-0.25, title, "purple", scienceMarkers["subCountry"], articles);
    }
    else if (label == "sciencecity"){
        newMarker(map, lat-0.25, lng-0.25, title, "purple", scienceMarkers["city"], articles);
    }
   else if (label == "entertainmentcountry"){
        newMarker(map, lat-0.15, lng+0.25, title, "lightblue", entertainmentMarkers["country"], articles);
    }
    else if (label == "entertainmentsubcountry"){
        newMarker(map, lat-0.15, lng+0.25, title, "lightblue", entertainmentMarkers["subCountry"], articles);
    }
    else if (label == "entertainmentcity"){
        newMarker(map, lat-0.15, lng+0.25, title, "lightblue", entertainmentMarkers["city"], articles);
    }
    // Determine visibility
    showBasedOnZoom(); 
}

/** Adds a marker that shows an info window when clicked. */
function newMarker(map, lat, lng, title, color, markerSet, articles){
    let url;
    if (color==="white"){
        url = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";
    } else if(color==="lightblue"){
        url = "http://maps.google.com/mapfiles/kml/paddle/ltblu-circle.png";
    } else {
        url = "https://maps.google.com/mapfiles/ms/icons/";
        url += color + "-dot.png";
    }
    const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, 
      map: map, 
      title: title,
      icon: { url: url,
            scaledSize: new google.maps.Size(30, 30), // scaled size
            origin: new google.maps.Point(0,0), // origin
            anchor: new google.maps.Point(0, 0) // anchor
      }
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
        if (!showGeneralOn && !showBusinessOn && !showTechOn && !showSportsOn && !showHealthOn && !showEntertainmentOn && !showScienceOn){
            zoomVisibility(markers["country"], (zoom < 4));
            zoomVisibility(markers["city"], (zoom > 6));
            zoomVisibility(markers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showGeneralOn) {
            zoomVisibility(generalMarkers["country"], (zoom < 4));
            zoomVisibility(generalMarkers["city"], (zoom > 6));
            zoomVisibility(generalMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showBusinessOn) {
            zoomVisibility(businessMarkers["country"], (zoom < 4));
            zoomVisibility(businessMarkers["city"], (zoom > 6));
            zoomVisibility(businessMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showTechOn) {
            zoomVisibility(techMarkers["country"], (zoom < 4));
            zoomVisibility(techMarkers["city"], (zoom > 6));
            zoomVisibility(techMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showSportsOn) {
            zoomVisibility(sportsMarkers["country"], (zoom < 4));
            zoomVisibility(sportsMarkers["city"], (zoom > 6));
            zoomVisibility(sportsMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showHealthOn) {
            zoomVisibility(healthMarkers["country"], (zoom < 4));
            zoomVisibility(healthMarkers["city"], (zoom > 6));
            zoomVisibility(healthMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showScienceOn) {
            zoomVisibility(scienceMarkers["country"], (zoom < 4));
            zoomVisibility(scienceMarkers["city"], (zoom > 6));
            zoomVisibility(scienceMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
        }
        if (showEntertainmentOn) {
            zoomVisibility(entertainmentMarkers["country"], (zoom < 4));
            zoomVisibility(entertainmentMarkers["city"], (zoom > 6));
            zoomVisibility(entertainmentMarkers["subCountry"], (zoom >= 4) && (zoom <= 6));
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
function showGeneral(){
    showGeneralOn = !showGeneralOn;
    displayAllRelevantLevels(generalMarkers, showGeneralOn);
}

// Shows or hides markers about business on select or unselect respectively
function showBusiness(){
    showBusinessOn = !showBusinessOn;
    displayAllRelevantLevels(businessMarkers, showBusinessOn);
}

// Shows or hides markers about sports on select or unselect respectively
function showSports(){
    showSportsOn = !showSportsOn;
    displayAllRelevantLevels(sportsMarkers, showSportsOn);
}
// Shows or hides markers about tech on select or unselect respectively
function showTech(){
    showTechOn = !showTechOn;
    displayAllRelevantLevels(techMarkers, showTechOn);
}
// Shows or hides markers about health on select or unselect respectively
function showHealth(){
    showHealthOn = !showHealthOn;
    displayAllRelevantLevels(healthMarkers, showHealthOn);
}
// Shows or hides markers about entertainment on select or unselect respectively
function showEntertainment(){
    showEntertainmentOn = !showEntertainmentOn;
    displayAllRelevantLevels(entertainmentMarkers, showEntertainmentOn);
}
// Shows or hides markers about science on select or unselect respectively
function showScience(){
    showScienceOn = !showScienceOn;
    displayAllRelevantLevels(scienceMarkers, showScienceOn);
}
// Shows or hides relevant markers on select or unselect respectively of the categories
function displayRelevant(markerSet, visible){
    var zoom = sharedMap.getZoom();

    if (showTechOn || showBusinessOn || showScienceOn || showSportsOn || showHealthOn || showEntertainmentOn || showGeneralOn){
        for (i = 0; i < markers["country"].length; i++) {
            markers["country"][i].setVisible(false);
        }
       for (i = 0; i < markers["city"].length; i++) {
            markers["city"][i].setVisible(false);
        }
        for (i = 0; i < markers["subCountry"].length; i++) {
            markers["subCountry"][i].setVisible(false);
        }  
    }
    else {
        for (i = 0; i < markers["country"].length; i++) {
            markers["country"][i].setVisible((zoom < 4));
        }
       for (i = 0; i < markers["city"].length; i++) {
            markers["city"][i].setVisible((zoom > 6));
        }
        for (i = 0; i < markers["subCountry"].length; i++) {
            markers["subCountry"][i].setVisible((zoom >= 4) && (zoom <= 6));
        }  
    }

    for (i = 0; i < markerSet.length; i++) {
        markerSet[i].setVisible(visible);
    }   
}

function displayAllRelevantLevels(markerSet, state){
    var zoom = sharedMap.getZoom();
    displayRelevant(markerSet["country"], state && (zoom < 4));
    displayRelevant(markerSet["subCountry"], state && (zoom >= 4) && (zoom <= 6));
    displayRelevant(markerSet["city"], state && (zoom > 6));
}
/**
    Displays the passed list of articles.
 */
function displayArticles(articles) {
    clearArticleList();
    articles.sort((a, b) => b.date.seconds - a.date.seconds);
    for (i = 0; i < articles.length; i++) {
        articleObj = articles[i];
        createArticleHtmlComponent(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url, articleObj.thumbnailUrl);
    }
    displayArticlePanel();
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

// Open Article list nav
function displayArticlePanel() {
    articlesOpen = true;
    document.getElementById("article-list-container").style.transform = "translateX(-1200px)";
    document.getElementById("article-list-container").style.transition = "all 0.7s ease";
    if (smallSearch){
        closeSmallSearchForm();
    }
}

// Close Article list nav
function hideArticlePanel() {
    articlesOpen = false;
    document.getElementById("article-list-container").style.transform = "translateX(1200px)";
    document.getElementById("article-list-container").style.transition = "all 0.7s ease";

}

// Open or close nav depending on current state
function toggleNav() {
    if (articlesOpen) {
        hideArticlePanel();
    } else {
        displayArticlePanel();
    }
}

// open search mini form 
function expandSearch() {
    hideArticlePanel();
    document.getElementById("small-screen-display").style.display = "grid";
    document.getElementById("search-expand").style.display = "none";
    smallSearch=true;
}

// Closes small form if user clicks away
function closeSmallSearchForm() {
    document.getElementById("small-screen-display").style.display = "none";
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
    // document.getElementById('topic-search-field').focus();
}
