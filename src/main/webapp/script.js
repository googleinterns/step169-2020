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
function onPageLoad() {
  attachSearchFormSubmissionEvent();
  map = initMap();
  getInitialContent();
  addLandmark(map, 40.7128, -74.0060, "New York City");
  addLandmark(map, 41.8781, -87.6298, "Chicago");
  addLandmark(map, 34.0522, -118.2437, "Los Angeles");
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
    Starting point of a region search. Submits query to the servlet and then passes its responses to the getRegionArticles function.
 */
function doSearch(form) {
    let region = form.elements["region-search-field"].value;
    if (region != "") {
        let topic = form.elements["topic-search-field"].value;
        console.log("region: " + region + " topic: " + topic);
        let fetchParameter = "/region-news?region=" + region + "&topic=" + topic;
        const response = fetch(fetchParameter);
        response.then(getRegionArticles);
    } 
}

/**
    Retrieves the json from the servlet response.
 */
function getRegionArticles(response) {
    const json = response.json();
    json.then(displayArticles);
}

/**
    Displays the articles contained in the json to the webpage.
 */
function displayArticles(json) {
    clearArticleList();
    for (index in json) {
        let articleObj = json[index];
        addArticle(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url);
    }
}

/**
    Fetches the initial articles displayed on the page.
 */
function getInitialContent() {
    const articleList = document.getElementById("articles-list");
    let item = document.createElement('li');
    let titleElement = document.createElement('h2');
    titleElement.innerText = "Enter a state or city in the search bar above or click a pin on the map for articles relevant to that location.";
    item.appendChild(titleElement);
    articleList.appendChild(item);
}

/**
    Function for testing that adds articles with a custom title.
 */
function testAddArticles(title) {
    clearArticleList();
    for (i = 1; i <= 10; i++) {
        addArticle(title + " " + i, "Publisher " + i, "Content " + i, "07/23/2020", "https://www.google.com");
    }
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
    var map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 40.674, lng: -73.945},
    zoom: 6,
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
