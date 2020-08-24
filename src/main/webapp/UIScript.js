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
        if (!showPolitics && !showBusiness && !showmisc && !showsports){
            zoomVisibility(countryMarkers, (zoom < 5));
            zoomVisibility(cityMarkers, (zoom > 8));
            zoomVisibility(subcountryMarkers, (zoom >= 5) && (zoom <= 8));
        }
        if (showPolitics) {
            zoomVisibility(politicsMarkersCountry, (zoom < 5));
            zoomVisibility(politicsMarkersCity, (zoom > 8));
            zoomVisibility(politicsMarkersSubcountry, (zoom >= 5) && (zoom <= 8));
        }
        if (showBusiness) {
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
    showPolitics = !showPolitics;
    var zoom = sharedMap.getZoom();

    displayRelevant(politicsMarkersCountry, showPolitics && (zoom < 5));
    displayRelevant(politicsMarkersSubcountry, showPolitics && (zoom >= 5) && (zoom <= 8));
    displayRelevant(politicsMarkersCity, showPolitics && (zoom > 8));
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
    showBusiness = !showBusiness;
    var zoom = sharedMap.getZoom();

    displayRelevant(businessMarkersCountry, showBusiness && (zoom < 5));
    displayRelevant(businessMarkersSubcountry, showBusiness && (zoom >= 5) && (zoom <= 8));
    displayRelevant(businessMarkersCity, showBusiness && (zoom > 8));
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
    if (showPolitics || showBusiness || showmisc || showsports){
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
/**
    Displays the passed list of articles.
 */
function displayArticles(articles) {
    clearArticleList();
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
    document.getElementById('topic-search-field').focus();

}
