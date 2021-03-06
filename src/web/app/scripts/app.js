'use strict';

angular.module('controllers', ['services']);
angular.module('services', ['ngResource']);
angular.module('appFilters', []);
angular.module('directives', []);

angular.module('webApp', [
  'services',
  'controllers',
  'ngRoute',
  'route-segment',
  'view-segment',
  'appFilters',
  'ui.bootstrap',
  'directives'
]).config(function ($routeProvider, $routeSegmentProvider) {
  $routeSegmentProvider.options.autoLoadTemplates = true;

  var userInfoDep = {
    userInfo: ['UserSrv', function (UserSrv) {
      return UserSrv.info().$promise;
    }]
  };

  $routeSegmentProvider
    .when('/home', 'home.photos')
    .when('/home/photos/:service/albums', 'home.photos.albums')
    .when('/home/photos/:service/albums/:id/albumPhotos/:page?', 'home.photos.albumPhotos')
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
          dependencies: ['service', 'id'],
          resolve: {
            presentationSettings: ['AppSrv', function(AppSrv) {
              return AppSrv.presentationSettings().$promise;
            }]
          }
        })
      .up()
      .segment('account', {
        templateUrl: 'views/account.html',
        controller: 'AccountCtrl',
        resolve: {
          accountInfo : ['UserSrv', function(UserSrv) {
            return UserSrv.detailedInfo();
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
});
