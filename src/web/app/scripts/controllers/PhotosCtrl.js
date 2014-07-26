'use strict';

angular.module('controllers')
  .controller('PhotosCtrl', [
    '$scope', '$location', '$routeSegment', '$modal', '$rootScope', 'presentationSettings', 'PhotoSrv', 'SelectionSrv',
    function ($scope, $location, $routeSegment, $modal, $rootScope, presentationSettings, PhotoSrv, SelectionSrv) {

      function reloadPhotos() {
        PhotoSrv.albumPhotos({
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
      $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
      $rootScope.$on("selectionChanged", function() {
        $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
        $scope.$apply();
      });
      $scope.showPhoto = function (index) {
        $modal.open({
          templateUrl: 'photoModal.html',
          controller: ['$scope', '$modalInstance', 'data', function($scope, $modalInstance, data) {
            var index = data.index;
            $scope.photo = data.photos[index];

            $scope.closePhoto = function() {
              $modalInstance.dismiss();
            };
            $scope.showNextPhoto = function() {
              index = (index + 1) % data.photos.length;
              $scope.photo = data.photos[index];
            };
            $scope.showPrevPhoto = function() {
              if (index == 0) {
                index = data.photos.length;
              }
              index -= 1;
              $scope.photo = data.photos[index];
            };
          }],
          resolve: {
            data: _.constant({
              photos: $scope.photos,
              index: index
            })
          }
        });
      };

      $scope.pageChanged = function() {
        $location.path($routeSegment.getSegmentUrl('home.photos.albumPhotos', {
          service: $routeSegment.$routeParams.service,
          id: $routeSegment.$routeParams.id,
          page: $scope.currentPage
        }));
      };
      $scope.photosPerPage = presentationSettings.photosPerPage;
      $scope.maxPagesCount = presentationSettings.maxPagesCount;
      $scope.$on('$routeChangeSuccess', function (event, currentRoute) {
        $scope.currentPage = currentRoute.pathParams.page || 1;
        reloadPhotos();
      });

      $scope.albumId = $routeSegment.$routeParams.id;
      PhotoSrv.albumInfo({
        service: $routeSegment.$routeParams.service,
        albumId: $routeSegment.$routeParams.id
      }, function(info) {
        $scope.albumInfo = info;
        $scope.currentPage = $routeSegment.$routeParams.page || 1;
        reloadPhotos();
      });
    }]);
