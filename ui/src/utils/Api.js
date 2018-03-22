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
    // this.username = 
    // https://www.npmjs.com/package/local-storage-es6
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

  getInfo() {
    console.log("Get info " + apiUrl + " - " + JSON.stringify(this.headers));
    return this.username != '' ?
      axios.get(apiUrl, { headers: this.headers }) : [];
  }

  startCapture(params) {
    return this.username != '' ? 
      axios.post(capturesStartUrl, params, { headers: this.headers }) : [];
  }

  recordCapture() {
    return this.username != '' ? 
      axios.post(capturesRecordUrl, {}, { headers: this.headers }) : [];
  }

  stopCapture() {
    return this.username != '' ? 
      axios.post(capturesStopUrl, {}, { headers: this.headers }) : [];
  }

  finishCapture(params) {
    return this.username != '' ? 
      axios.post(capturesFinishUrl, params, { headers: this.headers } ) : [];
  }

  setCredentials(username, password) {
    this.username = username;
    this.password = password;
    this.headers = {
      'Authorization' : 'Basic ' + base64.encode(username + ":" + password)
    };
    console.log("Set credentials - " + JSON.stringify(this.headers));
  }

}

export const api = new Api();