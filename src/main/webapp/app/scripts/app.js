'use strict';

angular.module('controllers', ['services']);
angular.module('services', ['ngResource']);

angular.module('webApp', [
  'services',
  'controllers',
  'ngRoute',
  'route-segment',
  'view-segment'
]).config(function ($routeProvider, $routeSegmentProvider, $httpProvider) {
  $routeSegmentProvider.options.autoLoadTemplates = true;

  $routeSegmentProvider
    .when('/home', 'home')
    .when('/home/:service/albums', 'home.albums')
    .when('/home/:service/albums/:id/photos', 'home.photos')
    .when('/login', 'login')
    .segment('home', {
      templateUrl: 'views/home.html',
      controller: 'HomeCtrl'
    })
    .within()
      .segment('albums', {
        templateUrl: 'views/albums.html',
        controller: 'AlbumsCtrl',
        dependencies: ['service']
      })
      .segment('photos', {
        templateUrl: 'views/photos.html',
        controller: 'PhotosCtrl',
        dependencies: ['service', 'id']
      })
    .up()
    .segment('login', {
      templateUrl: 'views/login.html',
      controller: 'LoginCtrl',
      resolve: {
        userInfo: function(User) {
          return User.info().$promise;
        }
      }
    });
  $routeProvider.otherwise({
    redirectTo: '/home'
  });
  $httpProvider.defaults.transformRequest = function (data) {
    if (data === undefined) {
      return data;
    }
    return $.param(data);
  }
});
