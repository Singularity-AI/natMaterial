import React from 'react';
import ReactDOM from 'react-dom';

alert("test.js");
window.renderApp = function(id){
  ReactDOM.render(
    <h1>Hello, world!</h1>,
    document.getElementById(id)
  );
};