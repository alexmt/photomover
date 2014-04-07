'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', '$rootScope', '$location', '$window', 'App', 'Google', 'User',
    function ($scope, $rootScope, $location, $window, App, Google, User) {

      function applyUserInfo(userInfo) {
        $scope.userInfo = userInfo;
        $rootScope.$broadcast("userInfoUpdated", userInfo);
        if (userInfo && userInfo.isAnonymous) {
          $location.path("/login");
        }
      }

      $scope.signInToFlickr = function () {
        $window.location.href = 'services/user/flickr/authorize';
      };

      $scope.signInToGoogle = function () {
        App.googleAppSettings(function (settings) {
          Google.authorize(settings).then(function (code) {
            User.authorizeGoogleAccount({ code: code }, function (response) {
              applyUserInfo(response.data);
            });
          });
        });
      };

      User.info(applyUserInfo);
    }]);