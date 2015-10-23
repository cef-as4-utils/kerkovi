/*global define */

'use strict';

define(['angular'], function(angular) {

/* Directives */

angular.module('myApp.directives', []).
  directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
      elm.text(version);
    };
  }])
  .directive('guitarist', ['hisName', function(parm){
    return function(scope, elm, attrs) {
      elm.text(parm + ' is a good guitarist')
    };
  }])
  .directive('lalmeri', ['hisName', function(parm){
    return function(scope, elm, attrs) {
      elm.text(scope.lalmeri)
    };
  }]);

});
