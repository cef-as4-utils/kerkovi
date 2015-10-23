/*global define */

'use strict';

define(function () {

  /* Controllers */

  var controllers = {};

  controllers.MyCtrl1 = function ($scope, Phone) {
    $scope.phones = Phone.query()
    $scope.orderProp = "age"
  }

  controllers.MyCtrl1.$inject = ['$scope', 'Phone'];

  controllers.MyCtrl2 = function () {
  }

  controllers.MyCtrl3 = function ($scope, $http, $routeParams) {
    $scope.phoneId = $routeParams.phoneId;
  }

  controllers.MyCtrl3.$inject = ['$scope', '$http', '$routeParams'];

  return controllers;

});
