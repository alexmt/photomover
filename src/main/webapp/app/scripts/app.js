'use strict';

angular.module('controllers', ['services']);
angular.module('services', ['ngResource']);

angular.module('webApp', [
    'controllers',
    'ngRoute'
  ]).config(function ($routeProvider, $httpProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl'
      })
      .when('/photos', {
        templateUrl: 'views/main.html',
        controller: 'PhotoCtrl'
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
