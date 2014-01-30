'use strict';
angular.module('controllers', ['services'])
  .controller('AppCtrl', ['$scope', 'User', 'App', function ($scope, User, App) {
    $scope.userInfo = User.info();
    $scope.$on('event:google-plus-signin-success', function (event, authResponse) {
      User.authorizeGoogleAccount(authResponse.code, function (response) {
        $scope.userInfo = response.data;
      });
    });
    App.googleAppSettings().$promise.then(function (value) {
      $scope.googleAppSettings = value;
    });
  }])
  .controller('MainCtrl', ['$scope', 'User', 'App', function ($scope, User, App) {

  }]);
