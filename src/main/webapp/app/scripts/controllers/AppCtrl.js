'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', 'User', 'App', function ($scope, User, App) {
    $scope.userInfo = User.info();
    App.googleAppSettings(function (value) {
      $scope.googleAppSettings = value;
    });
  }]);
