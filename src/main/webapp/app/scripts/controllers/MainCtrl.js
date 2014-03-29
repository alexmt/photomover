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
      $scope.photos = [];
      $scope.albums = User.albums({ service: service });
      $scope.service = service;
    };

    $scope.showPhotos = function(albumId, albumName) {
      $scope.albumName = albumName;
      $scope.albums = [];
      $scope.photos = User.photos({
        service: $scope.service,
        albumId: albumId
      });
    };

    $scope.signInToFlickr = function() {
      window.location.href = 'services/user/flickr/authorize';
    };

    $scope.signInToGoogle = function() {
      App.googleAppSettings(function(settings) {
        Google.authorize(settings).then(function(code) {
          User.authorizeGoogleAccount( { code: code }, function (response) {
            applyUserInfo(response.data);
          });
        });
      });
    }
  }]);
