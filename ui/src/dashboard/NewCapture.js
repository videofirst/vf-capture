import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';

import FlatButton from 'material-ui/FlatButton';
import FontIcon from 'material-ui/FontIcon';

import { api } from '../utils/Api';

class NewCapture extends Component {

  constructor(props) {
    super(props);
    this.startCapture = this.startCapture.bind(this);
    this.recordCapture = this.recordCapture.bind(this);
    this.stopCapture = this.stopCapture.bind(this);
    this.failedCapture = this.failedCapture.bind(this);
    this.passedCapture = this.passedCapture.bind(this);
    // this.finishCapture = this.finishCapture.bind(this);  NOT WORKING
  }

  startCapture() {
    const params = {
      categories : {
        organisation: "Google",
        product: "Train Search",
      },
      feature: "Advanced Search",
      scenario: "Search in London",
      record: "false",
    };
    api.startCapture(params);
  }

  recordCapture() {
    api.recordCapture();
  }

  stopCapture() {
    api.stopCapture();
  }

  passedCapture() {
    const params = {
      testStatus: "pass"
    };
    api.finishCapture(params);
  }

  failedCapture() {
    const params = {
      testStatus: "fail"
    };
    api.finishCapture(params);
  }

  logOut(history) {
    api.logout();
    history.push('/');
  }

  render() {

    const LogoutButton = withRouter(({ history }) => (
      <FlatButton
          label="Logout"
          backgroundColor="#cccc"
          hoverColor="#ddd"
          onClick={this.logOut(history)} />
    ));

    return (
      <div>
        <div>
          Capture State: <strong>{this.props.captureStatus.state}</strong>
        </div>

        <FlatButton
          label="Start"
          backgroundColor="#a4c639"
          hoverColor="#8AA62F"
          onClick={this.startCapture}
          icon={<FontIcon className="muidocs-icon-custom-github" />}
        />
        <br/>

        <FlatButton
          label="Record"
          backgroundColor="#a4c639"
          hoverColor="#8AA62F"
          onClick={this.recordCapture}
          icon={<FontIcon className="muidocs-icon-custom-github" />}
        />

        <FlatButton
          label="Stop"
          backgroundColor="#a4c639"
          hoverColor="#8AA62F"
          onClick={this.stopCapture}
          icon={<FontIcon className="muidocs-icon-custom-github" />}
        />

        <br/>

        <FlatButton
          label="Pass"
          backgroundColor="#a4c639"
          hoverColor="#8AA62F"
          onClick={this.passedCapture}
          icon={<FontIcon className="muidocs-icon-custom-github" />}
        />

        <FlatButton
          label="Fail"
          backgroundColor="#a4c639"
          hoverColor="#8AA62F"
          onClick={this.failedCapture}
          icon={<FontIcon className="muidocs-icon-custom-github" />}
        />
        <br/>
        <br/>

        <LogoutButton/>
        
      </div>
    );
  }
}

export default NewCapture;
