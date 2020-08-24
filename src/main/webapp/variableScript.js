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

var sharedMap = null;
var places;
var autoComplete;
var autoCompleteService;
var articlesOpen = false;
var smallSearch = false;
var countryRestrict = {'country': 'us'};

// marker collections
var markers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var sportsMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var generalMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var businessMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var healthMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var entertainmentMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var techMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };
var scienceMarkers = {
    "city" : [], 
    "subCountry" : [], 
    "country" : []
    };


// boolean to set visibility 
var showGeneralOn =false;
var showHealthOn =false;
var showBusinessOn =false;
var showSportsOn =false;
var showEntertainmentOn =false;
var showTechOn =false;
var showScienceOn =false;

// Article lists for each category
var articleMap = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapSports = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapGeneral= {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapBusiness = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapHealth = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapEntertainment = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapTech = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };
var articleMapScience = {
    "city" : new Map(), 
    "subCountry" : new Map(), 
    "country" : new Map()
    };

