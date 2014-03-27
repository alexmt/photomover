'use strict';

angular.module('controllers')
  .controller('MainCtrl', ['$scope', '$window', 'User', 'App', 'Google', function ($scope, $window, User, App, Google) {
    $scope.userInfo = User.info();
    $scope.switchTo = function(service) {
      $scope.albums = User.albums(service);
    };
    $scope.signInToFlickr = function() {
      window.location.href = 'services/user/flickr/authorize';
    };
    $scope.signInToGoogle = function() {
      App.googleAppSettings(function(settings) {
        Google.authorize(settings).then(function(code) {
          User.authorizeGoogleAccount(code, function (response) {
            $scope.userInfo = response.data;
          });
        });
      });
    }
  }]);
