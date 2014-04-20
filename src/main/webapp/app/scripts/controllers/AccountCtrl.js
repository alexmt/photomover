'use strict';

angular.module('controllers')
  .controller('AccountCtrl', ['$scope', 'User', 'accountInfo',
    function ($scope, User, accountInfo) {

      $scope.accountInfo = accountInfo;

      $scope.updateInfo = function () {
        User.updateInfo({
          firstName: $scope.accountInfo.firstName,
          lastName: $scope.accountInfo.lastName,
          email: $scope.accountInfo.email
        });
      };

    }]);