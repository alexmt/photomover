'use strict';

angular.module('controllers')
  .controller('MainCtrl', ['$scope', '$window', 'User', 'App', 'Google', function ($scope, $window, User, App, Google) {

    function applyUserInfo(info) {
      $scope.userInfo = info;
      for (var account in info.accountsState) {
        if (info.accountsState[account]) {
          $scope.showAlbums(account);
        }
      }
    }

    User.info(applyUserInfo);

    $scope.showAlbums = function(service) {
      $scope.albums = User.albums(service);
    };

    $scope.signInToFlickr = function() {
      window.location.href = 'services/user/flickr/authorize';
    };

    $scope.signInToGoogle = function() {
      App.googleAppSettings(function(settings) {
        Google.authorize(settings).then(function(code) {
          User.authorizeGoogleAccount(code, function (response) {
            applyUserInfo(response.data);
          });
        });
      });
    }
  }]);
