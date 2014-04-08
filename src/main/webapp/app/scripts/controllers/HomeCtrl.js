'use strict';

angular.module('controllers')
  .controller('HomeCtrl', ['$scope', '$location', '$routeSegment', function ($scope, $location, $routeSegment) {
    $scope.$watch('userInfo', function(userInfo) {
      if (userInfo && !$routeSegment.$routeParams.service) {
        for (var account in userInfo.accountsState) {
          if (userInfo.accountsState[account]) {
            $location.path('/home/' + account + '/albums');
            break;
          }
        }
      }
    });
  }]);