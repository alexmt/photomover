'use strict';

angular.module('controllers')
  .controller('PhotoCtrl', ['$scope', '$window', 'User', function ($scope, $window, User) {

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

    $scope.$watch('userInfo', function(userInfo) {
      if (userInfo) {
        for (var account in userInfo.accountsState) {
          if (userInfo.accountsState[account]) {
            $scope.showAlbums(account);
          }
        }
      }
    });
  }]);
