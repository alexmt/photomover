'use strict';

angular.module('controllers')
  .controller('PhotosCtrl', ['$scope', '$location', '$routeSegment', '$modal', 'presentationSettings', 'Photo',
    function ($scope, $location, $routeSegment, $modal, presentationSettings, Photo) {

      function reloadPhotos() {
        Photo.albumPhotos({
          service: $routeSegment.$routeParams.service,
          albumId: $routeSegment.$routeParams.id,
          page: $scope.currentPage
        }, function (photos) {
          angular.forEach(photos, function (photo, index) {
            photo.index = index;
          });
          $scope.photos = photos;
        });
      }

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
      };

      $scope.pageChanged = function() {
        $location.path($routeSegment.getSegmentUrl('home.photos.albumPhotos', {
          service: $routeSegment.$routeParams.service,
          id: $routeSegment.$routeParams.id,
          page: $scope.currentPage
        }));
        reloadPhotos();
      };
      $scope.photosPerPage = presentationSettings.photosPerPage;
      $scope.maxPagesCount = presentationSettings.maxPagesCount;

      Photo.albumInfo({
        service: $routeSegment.$routeParams.service,
        albumId: $routeSegment.$routeParams.id
      }, function(info) {
        $scope.albumInfo = info;
        $scope.currentPage = $routeSegment.$routeParams.page || 1;
        reloadPhotos();
      });
    }]);
