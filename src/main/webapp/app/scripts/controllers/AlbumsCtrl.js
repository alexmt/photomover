'use strict';

angular.module('controllers')
  .controller('AlbumsCtrl', ['$scope', '$routeSegment', 'Photo', function ($scope, $routeSegment, Photo) {
    $scope.service = $routeSegment.$routeParams.service;
    $scope.albums = Photo.albums( { service: $routeSegment.$routeParams.service });
  }]);