'use strict';

angular.module('controllers')
  .controller('AccountCtrl', ['$rootScope', '$scope', '$window', 'User', 'accountInfo',
    function ($rootScope, $scope, $window, User, accountInfo) {

      function refreshUserInfo() {
        User.info(function(info) {
          $rootScope.$broadcast('userUpdated', info);
        });
      }

      $scope.accountInfo = accountInfo;

      $scope.updateInfo = function () {
        User.updateInfo({
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
        User.removeService(service, function() {
          removeServiceModal.modal('hide');
          refreshUserInfo();
          User.detailedInfo(function(accountInfo) {
            $scope.accountInfo = accountInfo;
          });
        });
      };

      $scope.startDeletingAccount = function() {
        deleteAccountModal.modal('show');
      };

      $scope.confirmDeletingAccount = function() {
        User.deleteAccount(function() {
          $window.location.reload();
        });
      }
    }]);