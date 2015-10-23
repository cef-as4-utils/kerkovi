/*global define */

'use strict';

define(['angular'], function(angular) {

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', ['ngResource'])
  .value('version', '0.2')
  .value('hisName', 'Ewan Dobson')
  .factory('Phone', ['$resource',
    function($resource){
      return $resource('phones/:phoneId.json', {}, {
        query: {method:'GET', params:{phoneId:'phones'}, isArray:true}
      });
    }]);

;

});