'use strict';

angular.module('controllers', ['services'])
    .controller('MainCtrl', ['$scope', 'Account', function ($scope, Account) {
      $scope.accountInfo = Account.info();
    }]);
