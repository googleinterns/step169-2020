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
  window.addEventListener("resize", clearSmallForm);
  attachSearchFormSubmissionEvent();
  attachSmallSearchFormSubmissionEvent();
  map = initMap();
  sharedMap = map;
  getInitialContent();
  initAutoComplete("");
  initAutoComplete("small-");
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
    doSearch(event.target,"");
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
    doSearch(event.target,"small-");
    closeSmallSearchForm();
    displayArticlePanel();
  });
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
function doSearch(form, barLabel) {
    let region = form.elements[barLabel + "region-search-field"].value;
    if (region != "") {
        let topic = form.elements[barLabel + "topic-search-field"].value;
        /*
        types: ['(cities)'],
            componentRestrictions: countryRestrict
        */
        request = {'input' : region, 'types' : ['(cities)']/*, 'componentRestrictions' : countryRestrict*/};
        autoCompleteService.getPlacePredictions(request, ((predictions) => finishSearch(predictions, topic, barLabel)));
    } else {
        /**
            If we add a warning message later for invalid locations it could go here.
         */
    }
}

/**
    Starting point of a region search that is not invoked by the search button. Submits query to the servlet and then passes its responses to the getRegionArticles function.
 */
function doSearchNotFromForm(barLabel) {
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
function finishSearch(suggestions, topic, barLabel) {
    if (suggestions && suggestions.length > 0) {
        let region = suggestions[0].description;
        document.getElementById(barLabel + "region-search-field").value = region;
        console.log("region: " + region + " topic: " + topic);
        let fetchParameter = "/region-news?region=" + region + "&topic=" + topic;
        const response = fetch(fetchParameter);
        response.then(getRegionArticles);
        const response2 = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + region +"&key=AIzaSyCZTgWP9rvo_ICsAcVXukYQ860eg3BS1wU");
        response2.then(getJSONOfGeoCoding);
    }
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
    json.sort((a, b) => b.date.seconds - a.date.seconds);
    for (index in json) {
        let articleObj = json[index];
        createArticleHtmlComponent(articleObj.title, articleObj.publisher, articleObj.description, articleObj.date, articleObj.url, articleObj.thumbnailUrl);
    }
    displayArticlePanel();
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
        fillArticleMaps(articleMap, articleObj);

       
        // CATEGORIZATION FEATURE 
        if (articleObj.theme == "sports"){
            fillArticleMaps(articleMapSports, articleObj);
        } else if (articleObj.theme == "health"){
            fillArticleMaps(articleMapHealth, articleObj);
        } else if (articleObj.theme == "general"){
            fillArticleMaps(articleMapGeneral, articleObj);
        } else if (articleObj.theme == "business"){
            fillArticleMaps(articleMapBusiness, articleObj);        
        } else if (articleObj.theme == "science"){
            fillArticleMaps(articleMapScience, articleObj);      
        } else if (articleObj.theme == "tech"){
            fillArticleMaps(articleMapTech, articleObj);      
        } else if (articleObj.theme == "entertainment"){
            fillArticleMaps(articleMapEntertainment, articleObj);
        }
    }

    fetchAddressGeocode(articleMap, "");
    fetchAddressGeocode(articleMapSports, "sports");
    fetchAddressGeocode(articleMapBusiness, "business");
    fetchAddressGeocode(articleMapGeneral, "general");
    fetchAddressGeocode(articleMapHealth, "health");
    fetchAddressGeocode(articleMapTech, "tech");
    fetchAddressGeocode(articleMapEntertainment, "entertainment");
    fetchAddressGeocode(articleMapScience, "science");
}


function fetchAddressGeocode(articleMap, label){
    for (key in articleMap["city"]) {
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyCZTgWP9rvo_ICsAcVXukYQ860eg3BS1wU");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMap["city"][key], label + "city"));
    }
    for (key in articleMap["subCountry"]) {
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyCZTgWP9rvo_ICsAcVXukYQ860eg3BS1wU");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMap["subCountry"][key], label + "subcountry"));
    }
    for (key in articleMap["country"]) {
        response = fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" + key +"&key=AIzaSyCZTgWP9rvo_ICsAcVXukYQ860eg3BS1wU");
        response.then(getRegionJSONOfGeoCoding.bind(null, articleMap["country"][key], label + "country"));
    }
}

function fillArticleMaps(articleMap, articles){
    
    if (articleMap["country"][articles.location.country] == null) {
        articleMap["country"][articles.location.country] = [articles];
    } else {
        articleMap["country"][articles.location.country].push(articles);
    }
    if (articleMap["subCountry"][articles.location.subcountry + ", " + articles.location.country] == null) {
        articleMap["subCountry"][articles.location.subcountry + ", " + articles.location.country] = [articles];
    } else {
        articleMap["subCountry"][articles.location.subcountry + ", " + articles.location.country].push(articles);
    }
    if (articleMap["city"][articles.location.city + ", " + articles.location.subcountry + ", " + articles.location.country] == null) {
        articleMap["city"][articles.location.city + ", " + articles.location.subcountry + ", " + articles.location.country] = [articles];
    } else {
        articleMap["city"][articles.location.city + ", " + articles.location.subcountry + ", " + articles.location.country].push(articles);
    }
}

/**
    Get the world news response geo coding.
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
        addMarkerWithArticles(sharedMap, lat, long, title, articles, label);
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
    showInitialMessageInArticleList();
    /**
        Fetches the world news, clusters it, and places the pins corresponding to its included locations.
     */
    let fetchParameter = "/world-news";
    const response = fetch(fetchParameter);
    response.then(getWorldArticles);
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

function initAutoComplete(barLabel) {
    // Create the autoComplete object and associate it with the UI input control.
    // Restrict the search to the default country, and to place type "cities".
    autoComplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */ (
            document.getElementById(barLabel + 'region-search-field')), {
            types: ['(cities)']/*,
            componentRestrictions: countryRestrict*/
        });
    console.log(barLabel);
    console.log(barLabel + 'region-search-field');

    places = new google.maps.places.PlacesService(map);
    autoCompleteService = new google.maps.places.AutocompleteService();
    autoComplete.addListener('place_changed', onPlaceChanged);


    // Add a DOM event listener to react when the user selects a country.
    // document.getElementById('country').addEventListener(
    //     'change', setAutoCompleteCountry);
}

// When the user selects a city, get the place details for the city and
// zoom the map in on the city.
function onPlaceChanged() {
    var place = autoComplete.getPlace();
    console.log(place);
    var scrWidth = document.documentElement.clientWidth;
    if (scrWidth>1200){
        if (place.geometry) {
            sharedMap.panTo(place.geometry.location);
            sharedMap.setZoom(10);
            doSearchNotFromForm("");
        } else {
            document.getElementById('region-search-field').placeholder = 'Enter a city';
        }
    }else{
        if (place.geometry) {
            sharedMap.panTo(place.geometry.location);
            sharedMap.setZoom(10);
            doSearchNotFromForm("small-");
        } else {
            document.getElementById('small-region-search-field').placeholder = 'Enter a city';
        }
    }
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