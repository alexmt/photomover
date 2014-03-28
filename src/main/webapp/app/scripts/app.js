'use strict';

angular.module('controllers', ['services']);
angular.module('services', ['ngResource']);

angular.module('webApp', [
    'controllers',
    'ngRoute'
  ]).config(function ($routeProvider, $httpProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
    $httpProvider.defaults.transformRequest = function(data){
      if (data === undefined) {
        return data;
      }
      return $.param(data);
    }
  });
