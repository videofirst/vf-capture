import React from 'react';
import ReactDOM from 'react-dom';
import { ThemeProvider } from 'styled-components';

import './index.css';
import App from './app/App';
import registerServiceWorker from './app/registerServiceWorker';

const theme = {
    pc1: "#222",
    pc2: "#C2B49A",
    bg: "white"
}

ReactDOM.render(
    <ThemeProvider theme={theme}>
       <App />
    </ThemeProvider>,
    document.getElementById('root')
);
registerServiceWorker();
