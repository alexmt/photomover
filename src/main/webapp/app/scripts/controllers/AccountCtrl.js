'use strict';

angular.module('controllers')
  .controller('AccountCtrl', ['$scope', '$window', 'User', 'accountInfo',
    function ($scope, $window, User, accountInfo) {

      $scope.accountInfo = accountInfo;

      $scope.updateInfo = function () {
        User.updateInfo({
          firstName: $scope.accountInfo.firstName,
          lastName: $scope.accountInfo.lastName,
          email: $scope.accountInfo.email
        });
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