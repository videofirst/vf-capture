import React, { Component } from 'react';
import { Route } from 'react-router-dom';
import { MuiThemeProvider } from 'material-ui';

import Login from '../login/Login';
import Dashboard from '../dashboard/Dashboard';

//import ListCaptures from '../list-captures/ListCaptures';
//import NewCapture from '../new-capture/NewCapture';
// style components theme - not currently using
//import { ThemeProvider } from 'styled-components';
//<ThemeProvider theme={theme}> - styled components style
//const theme = {
//  pc1: "#222",
//  pc2: "#C2B49A",
//  bg: "white"
//}

class App extends Component {

  render() {
    return (
      <MuiThemeProvider>
        <div id="app">
          <Route path="/" exact component={Login} />
          <Route path="/dashboard" exact component={Dashboard} />
        </div>
      </MuiThemeProvider>
    );
  }
}

export default App;
