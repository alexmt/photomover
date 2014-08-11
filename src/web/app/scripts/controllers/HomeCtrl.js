'use strict';

angular.module('controllers')
  .controller('HomeCtrl', ['$rootScope', '$scope', '$modal', '$location', 'userInfo', 'SelectionSrv',
    function ($rootScope, $scope, $modal, $location, userInfo, SelectionSrv) {

      function redirectToFirstService() {
        if ($location.path() == '/home') {
          for (var account in userInfo.accountsState) {
            if (userInfo.accountsState[account]) {
              $location.path('/home/photos/' + account + '/albums');
              break;
            }
          }
        }
      }

      function showSelection() {
        $modal.open({
          size: 'lg',
          templateUrl: 'selectionModal.html',
          controller: ['$scope', '$modalInstance', 'data', function($scope, $modalInstance, data) {
            $scope.service = data.service;
            $scope.selectionStats = SelectionSrv.getSelectionStats(data.service);
            $scope.selectedItems = SelectionSrv.getServiceSelection(data.service);
            $scope.close = function() {
              $modalInstance.dismiss();
            };
            $scope.removeFromSelection = function(item) {
              SelectionSrv.setSelected({
                service: $scope.service,
                albumId: item.albumId,
                photoId: item.photoId
              }, false);
              $scope.selectionStats = SelectionSrv.getSelectionStats(data.service);
              $scope.selectedItems = SelectionSrv.getServiceSelection(data.service);
              $rootScope.$broadcast('selectionChanged');
              if ($scope.selectionStats.isEmpty) {
                $scope.close();
              }
            };
          }],
          resolve: {
            data: _.constant({
              service: $scope.service
            })
          }
        });
      }

      $rootScope.$on('serviceChanged', function(event, service) {
        $scope.selectionStats = SelectionSrv.getSelectionStats(service);
        $rootScope.$on("selectionChanged", function() {
          $scope.service = service;
          $scope.selectionStats = SelectionSrv.getSelectionStats(service);
          $scope.$apply();
        });
      });
      $rootScope.$on('userUpdated', function(event, info) {
        userInfo = info;
      });
      $rootScope.$on('$locationChangeStart', redirectToFirstService);
      $rootScope.$on('showSelectionClick', showSelection);
      redirectToFirstService();
    }]);
