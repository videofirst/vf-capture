import axios from 'axios';
import base64 from 'base-64';

// How do we inject these in?
const apiUrl = 'http://localhost:1357/api';
const capturesUrl = apiUrl + "/captures";
const capturesStartUrl = capturesUrl + "/start";
const capturesRecordUrl = capturesUrl + "/record";
const capturesStopUrl = capturesUrl + "/stop";
const capturesFinishUrl = capturesUrl + "/finish";

/**
 * Wrapper class for API access to server.
 */
class Api {

  constructor() {
    console.log("constructor");
    // Try and read from local storage
  }

  async login (username, password) {
    this.setCredentials (username, password);
    console.log("URL - " + apiUrl);
    await axios.get(apiUrl, {headers: this.headers })
    .then(response => { return response })
    .catch(error => {
      this.setCredentials('', '')
      throw error;
    });
  }

  logout() {
    console.log("User logged out"); // FIXME - do better solution
    localStorage.removeItem('auth');
  }

  getInfo() {
    checkIsLoggedIn();
    return this.username != '' ?
      axios.get(apiUrl, { headers: this.headers }) : [];
  }

  startCapture(params) {
    checkIsLoggedIn();
    return this.username != '' ?
      axios.post(capturesStartUrl, params, { headers: this.headers }) : [];
  }

  recordCapture() {
    checkIsLoggedIn();
    return this.username != '' ?
      axios.post(capturesRecordUrl, {}, { headers: this.headers }) : [];
  }

  stopCapture() {
    checkIsLoggedIn();
    return this.username != '' ?
      axios.post(capturesStopUrl, {}, { headers: this.headers }) : [];
  }

  finishCapture(params) {
    checkIsLoggedIn();
    return this.username != '' ?
      axios.post(capturesFinishUrl, params, { headers: this.headers } ) : [];
  }

  setCredentials(username, password) {
    this.username = username;
    this.password = password;
    const vftAuth = base64.encode(username + ":" + password);
    localStorage.setItem('vftAuth', token);

    setHeaders ();

    console.log("Set credentials - " + JSON.stringify(this.headers));
  }

  setHeaders () {
    this.headers = {
      'Authorization' : 'Basic ' + localStorage.getItem('vftAuth'),
      'X-Requested-With', 'XMLHttpRequest'
    };
  }

  checkIsLoggedIn() {
    if (localStorage.getItem('auth') === null) {
      console.log("Not logged in !!!");   // FIXME - do a better solution
    }
  }

}

export const api = new Api();