/*global define */

'use strict';

define(['angular'], function(angular) {

/* Filters */

angular.module('myApp.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    }
  }])
  .filter('checkmark', function() {
    return function(input) {
      return input.toLocaleLowerCase().includes('x') ? '\u2713' : '\u2718';
    }});

});