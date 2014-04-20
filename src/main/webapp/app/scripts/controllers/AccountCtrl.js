'use strict';

angular.module('controllers')
  .controller('AccountCtrl', ['$scope', 'accountInfo', function($scope, accountInfo) {
    $scope.accountInfo = accountInfo;
  }]);