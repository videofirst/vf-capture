import React, { Component } from 'react';
import styled from 'styled-components';

const Title = styled.h1`
  font-size: 1.5em;
  text-align: center;
  color: ${props => props.theme.pc1};
`;
const Wrapper = styled.section`
  padding: 1em;
  background: ${props => props.theme.bg};
`;

class NewCapture extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <Wrapper>
        <Title>Create a new capture</Title>
      </Wrapper>
    );
  }
}

export default NewCapture;
