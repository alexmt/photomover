'use strict';

angular.module('controllers')
  .controller('AlbumsCtrl', ['$scope', '$routeSegment', 'User', function ($scope, $routeSegment, User) {
    $scope.service = $routeSegment.$routeParams.service;
    $scope.albums = User.albums({ service: $routeSegment.$routeParams.service });
  }]);