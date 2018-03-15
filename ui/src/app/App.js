import React, { Component } from 'react';
import base64 from 'base-64';
import axios from 'axios';

import Header from './Header';
import ListCaptures from '../list-captures/ListCaptures';
import NewCapture from '../new-capture/NewCapture';

// How do these get set?
const username = 'test';
const password = 'password';
const url = 'http://localhost:1357/api';
const headers = {
  'Authorization' : 'Basic ' + base64.encode(username + ":" + password)
};

class App extends Component {

  constructor() {
    super();
    this.state = { 
      info: {}, 
      defaults: {},
    };
  }

  componentDidMount() {
    axios.get(url, {headers}) 
    .then(response => {
        this.setState({
          defaults: response.data.defaults, 
          info: response.data.info
        });
    });
  }

  render() {
    return (
      <div>
        <Header/>
        <NewCapture defaults={this.state.defaults} info={this.state.info}/>
        <ListCaptures/>
      </div>
    );
  }
}

export default App;
