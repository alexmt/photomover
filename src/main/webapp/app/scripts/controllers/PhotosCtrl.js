'use strict';

angular.module('controllers')
  .controller('PhotosCtrl', ['$scope', '$routeSegment', 'User', function ($scope, $routeSegment, User) {
    $scope.photos = User.photos({
      service: $routeSegment.$routeParams.service,
      albumId: $routeSegment.$routeParams.id
    });
  }]);
