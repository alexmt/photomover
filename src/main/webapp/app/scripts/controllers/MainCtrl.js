'use strict';

angular.module('controllers')
  .controller('MainCtrl', ['$scope', '$window', 'User', function ($scope, $window, User) {
    $scope.$on('event:google-plus-signin-success', function (event, authResponse) {
      User.authorizeGoogleAccount(authResponse.code, function (response) {
        $scope.userInfo = response.data;
      });
    });
    $scope.switchTo = function(service) {
      $scope.albums = User.albums(service);
    };
    $scope.signInToFlickr = function() {
      window.location.href = 'services/user/flickr/authorize';
    };
  }]);
