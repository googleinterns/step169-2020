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
var showPolitics =false;
var showmisc =false;
var showBusiness =false;
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

