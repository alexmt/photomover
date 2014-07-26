'use strict';

angular.module('controllers')
  .controller('AlbumsCtrl', ['$rootScope', '$scope', '$routeSegment', 'PhotoSrv',
    function ($rootScope, $scope, $routeSegment, PhotoSrv) {
      $scope.service = $routeSegment.$routeParams.service;
      $scope.albums = PhotoSrv.albums({ service: $routeSegment.$routeParams.service });
      $rootScope.$broadcast('serviceChanged', $scope.service);
    }]);
