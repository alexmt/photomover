'use strict';

angular.module('webApp', [
      'controllers',
      'ngRoute',
    ]).config(function ($routeProvider) {
  $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
});
