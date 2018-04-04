import React, { Component } from 'react';

import NewCapture from './NewCapture';
import { api } from '../utils/Api';

import AppBar from 'material-ui/AppBar';

const REFRESH_DELAY_MILLIS = 2500; // call API every 2.5 seconds

class Dashboard extends Component {

  constructor() {
    super();
    this.state = { 
      info: {}, 
      defaults: {},
      captureStatus: {},
      uploads: {}
    };
    this.tick = this.tick.bind(this);
  }

  tick () {
    api.getInfo()
    .then(response => {
      this.setState ({
        info: response.data.info,
        defaults: response.data.defaults, 
        captureStatus: response.data.captureStatus,
        uploads: response.data.uploads,
      })
    })
    .catch(error => {
      console.log("Error talking to API - " + error);
    });
  }

  componentDidMount() {
    this.interval = setInterval(this.tick, REFRESH_DELAY_MILLIS); // call every few seconds
  }
  
  componentWillUnmount () {
    clearInterval(this.interval);
  }

  render() {
    return (
      <div id="dashboard">
        <AppBar title="VFT Capture"/>
        <h1>Dashboard</h1>
        <p>Uptime: {this.state.info.uptimeSeconds} secs</p>
        <NewCapture info={this.state.info}
           defaults={this.state.defaults}
           captureStatus={this.state.captureStatus}/>
      </div>
    );
  }
}

export default Dashboard;
