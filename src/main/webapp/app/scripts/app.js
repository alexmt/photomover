'use strict';

angular.module('controllers', ['services']);
angular.module('services', ['ngResource']);
angular.module('appFilters', []);

angular.module('webApp', [
  'services',
  'controllers',
  'ngRoute',
  'route-segment',
  'view-segment',
  'appFilters'
]).config(function ($routeProvider, $routeSegmentProvider, $httpProvider) {
  $routeSegmentProvider.options.autoLoadTemplates = true;

  var userInfoDep = {
    userInfo: ['User', function (User) {
      return User.info().$promise;
    }]
  };

  $routeSegmentProvider
    .when('/home', 'home.photos')
    .when('/home/photos/:service/albums', 'home.photos.albums')
    .when('/home/photos/:service/albums/:id/albumPhotos', 'home.photos.albumPhotos')
    .when('/account', 'home.account')
    .when('/login', 'login')

    .segment('home', {
      templateUrl: 'views/home.html',
      controller: 'HomeCtrl',
      resolve: userInfoDep
    })
    .within()
      .segment('photos', {
        templateUrl: 'views/photos/services.html'
      })
      .within()
        .segment('albums', {
          templateUrl: 'views/photos/albums.html',
          controller: 'AlbumsCtrl',
          dependencies: ['service']
        })
        .segment('albumPhotos', {
          templateUrl: 'views/photos/albumPhotos.html',
          controller: 'PhotosCtrl',
          dependencies: ['service', 'id']
        })
      .up()
      .segment('account', {
        templateUrl: 'views/account.html',
        controller: 'AccountCtrl',
        resolve: {
          accountInfo : ['User', function(User) {
            return User.detailedInfo();
          }]
        }
      })
    .up()
    .segment('login', {
      templateUrl: 'views/login.html',
      controller: 'LoginCtrl',
      resolve: userInfoDep
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
