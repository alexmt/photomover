'use strict';

angular.module('controllers')
  .controller('AccountCtrl', ['$rootScope', '$scope', '$window', 'UserSrv', 'accountInfo',
    function ($rootScope, $scope, $window, UserSrv, accountInfo) {

      function refreshUserInfo() {
        UserSrv.info(function(info) {
          $rootScope.$broadcast('userUpdated', info);
        });
      }

      $scope.accountInfo = accountInfo;

      $scope.updateInfo = function () {
        UserSrv.updateInfo({
          firstName: $scope.accountInfo.firstName,
          lastName: $scope.accountInfo.lastName,
          email: $scope.accountInfo.email
        }, refreshUserInfo);
      };

      var removeServiceModal = $('#removeServiceModal');
      var deleteAccountModal = $("#deleteAccountModal");
      $scope.startRemoveService = function(service) {
        $scope.removingService =  {
          name: service
        };
        removeServiceModal.modal('show');
      };

      $scope.confirmRemovingService = function(service) {
        UserSrv.removeService(service, function() {
          removeServiceModal.modal('hide');
          refreshUserInfo();
          UserSrv.detailedInfo(function(accountInfo) {
            $scope.accountInfo = accountInfo;
          });
        });
      };

      $scope.startDeletingAccount = function() {
        deleteAccountModal.modal('show');
      };

      $scope.confirmDeletingAccount = function() {
        UserSrv.deleteAccount(function() {
          $window.location.reload();
        });
      }
    }]);
