'use strict';

angular.module('controllers')
  .controller('LoginCtrl', ['$scope', '$rootScope' ,'$location', function ($scope, $rootScope, $location) {
    function checkIsLoginCompleted(userInfo) {
      if (userInfo && !userInfo.isAnonymous) {
        $location.path("/photos");
      }
    }

    $scope.$watch('userInfo', checkIsLoginCompleted);
    $rootScope.$on('userInfoUpdated', function(event, info) {
      checkIsLoginCompleted(info);
    });
  }]);
