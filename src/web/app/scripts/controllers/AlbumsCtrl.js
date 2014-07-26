'use strict';

angular.module('controllers')
  .controller('AlbumsCtrl', ['$rootScope', '$scope', '$routeSegment', 'PhotoSrv', 'SelectionSrv',
    function ($rootScope, $scope, $routeSegment, PhotoSrv, SelectionSrv) {
      $scope.service = $routeSegment.$routeParams.service;
      $scope.albums = PhotoSrv.albums({ service: $routeSegment.$routeParams.service });
      $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
      $rootScope.$on("selectionChanged", function() {
        $scope.selectionStats = SelectionSrv.getSelectionStats($scope.service);
        $scope.$apply();
      });
    }]);
