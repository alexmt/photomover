'use strict';
angular.module('controllers', ['services'])
  .controller('AppCtrl', ['$scope', 'User', 'App', function ($scope, User, App) {
    $scope.userInfo = User.info();
    $scope.$on('event:google-plus-signin-success', function (event, authResponse) {
      User.authorizeGoogleAccount(authResponse.code, function (response) {
        $scope.userInfo = response.data;
      });
    });
    App.googleAppSettings(function (value) {
      $scope.googleAppSettings = value;
    });
  }])
  .controller('MainCtrl', ['$scope', 'User', function ($scope, User) {
    $scope.switchTo = function(service) {
      $scope.albums = User.albums(service);
    };
  }]);
