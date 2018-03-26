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

class ListCaptures extends Component {
  render() {
    return (
      <Wrapper>
        <Title>List Capture</Title>
      </Wrapper>
    );
  }
}

export default ListCaptures;