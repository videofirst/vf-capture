import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import Notifications, { notify } from 'react-notify-toast';

import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import AppBar from 'material-ui/AppBar';
import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';

import { api } from '../utils/Api';

//import Dashboard from '../dashboard/Dashboard';

//import styled from 'styled-components';
/*const Title = styled.h1`
  font-size: 1.5em;
  text-align: center;
  color: ${props => props.theme.pc1};
`;
const Wrapper = styled.section`
  padding: 1em;
  background: ${props => props.theme.bg};
`;*/
//<Notifications />

class Login extends Component {
  
  constructor(props) {
    super(props);
    this.state= {
      username:'',
      password:'',
      redirect: false
    }
  }

  handleClick(event) {
    event.preventDefault();
    api.login(this.state.username, this.state.password)
    .then(response => {
      console.log (response);
      this.setState ({redirect: true})
    })
    .catch(error => {
      this.setState ({redirect: false})
      console.log("error " + error, "error");
    });
  }

  render() {
    return this.state.redirect ? <Redirect to="dashboard"/> :     
    (
      <div>
        <AppBar title="VFT Capture"/>
        <TextField
          hintText="Enter your Username"
          floatingLabelText="Username"
          value={this.state.username}
          onChange = {(event,newValue) => this.setState({username:newValue})}
          />
        <br/>
          <TextField
            type="password"
            hintText="Enter your Password"
            floatingLabelText="Password"
            value={this.state.password}
            onChange = {(event,newValue) => this.setState({password:newValue})}
            />
          <br/>
          <RaisedButton label="Login" primary={true} style={style} 
            onClick={(event) => this.handleClick(event)}
            disabled={this.state.username === '' || this.state.password === ''}/>
        </div>
    );
  }
}

const style = {
  margin: 15,
 };

export default Login;
