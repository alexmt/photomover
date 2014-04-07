'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', '$rootScope', 'App', 'Google', 'User',
    function ($scope, $rootScope, App, Google, User) {

      function applyUserInfo(info) {
        $scope.userInfo = info;
        $rootScope.$broadcast("userInfoUpdated", info);
      }

      $scope.signInToFlickr = function () {
        window.location.href = 'services/user/flickr/authorize';
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