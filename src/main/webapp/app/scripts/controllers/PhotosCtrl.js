'use strict';

angular.module('controllers')
  .controller('PhotosCtrl', ['$scope', '$routeSegment', '$modal', 'User',
    function ($scope, $routeSegment, $modal, User) {

      User.photos({
        service: $routeSegment.$routeParams.service,
        albumId: $routeSegment.$routeParams.id
      }, function (photos) {
        angular.forEach(photos, function (photo, index) {
          photo.index = index;
        });
        $scope.photos = photos;
      });

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
