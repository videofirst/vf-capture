import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route } from 'react-router-dom';

import './index.css';
import App from './app/App';
import Login from './login/Login';
import registerServiceWorker from './app/registerServiceWorker';

// https://css-tricks.com/react-router-4/

ReactDOM.render(
    <BrowserRouter>
        <App/>
    </BrowserRouter>,
    document.getElementById('root')
);
registerServiceWorker();
