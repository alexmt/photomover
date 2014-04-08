'use strict';

angular.module('controllers')
  .controller('LoginCtrl', ['$location', 'userInfo', function ($location, userInfo) {
    if(!userInfo.isAnonymous) {
      $location.path("/home");
    }
  }]);
