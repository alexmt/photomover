'use strict';

angular.module('controllers')
  .controller('AlbumsCtrl', ['$rootScope', '$scope', '$routeSegment', 'Photo', 'SelectionSrv',
    function ($rootScope, $scope, $routeSegment, Photo, SelectionSrv) {
      $scope.service = $routeSegment.$routeParams.service;
      $scope.albums = Photo.albums({ service: $routeSegment.$routeParams.service });
      $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
      $rootScope.$on("selectionChanged", function() {
        $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
        $scope.$apply();
      });
    }]);
