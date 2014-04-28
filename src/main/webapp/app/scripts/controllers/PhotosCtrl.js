'use strict';

angular.module('controllers')
  .controller('PhotosCtrl', ['$scope', '$routeSegment', '$modal', 'Photo',
    function ($scope, $routeSegment, $modal, Photo) {

      Photo.albumPhotos({
        service: $routeSegment.$routeParams.service,
        albumId: $routeSegment.$routeParams.id
      }, function (photos) {
        angular.forEach(photos, function (photo, index) {
          photo.index = index;
        });
        $scope.photos = photos;
      });

      Photo.albumInfo({
        service: $routeSegment.$routeParams.service,
        albumId: $routeSegment.$routeParams.id
      }, function(info) {
        $scope.albumInfo = info;
      });

      $scope.service = $routeSegment.$routeParams.service;
      $scope.showPhoto = function (index) {
        $modal.open({
          templateUrl: 'photoModal.html',
          controller: ['$scope', 'data', function($scope, data) {
            $scope.photo = data.photo;
          }],
          resolve: {
            data: _.constant({
              photo: $scope.photos[index]
            })
          }
        })
      }
    }]);
