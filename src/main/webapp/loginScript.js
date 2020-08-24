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