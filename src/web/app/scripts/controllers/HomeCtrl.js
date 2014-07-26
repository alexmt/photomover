'use strict';

angular.module('controllers')
  .controller('HomeCtrl', ['$rootScope', '$scope', '$location', 'userInfo', 'SelectionSrv',
    function ($rootScope, $scope, $location, userInfo, SelectionSrv) {

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

      $rootScope.$on('serviceChanged', function(event, service) {
        $scope.selectionStats = SelectionSrv.getSelectionStats(service);
        $rootScope.$on("selectionChanged", function() {
          $scope.selectionStats = SelectionSrv.getSelectionStats(service);
          $scope.$apply();
        });
      });
      $rootScope.$on('userUpdated', function(event, info) {
        userInfo = info;
      });
      $rootScope.$on('$locationChangeStart', redirectToFirstService);
      redirectToFirstService();
    }]);
