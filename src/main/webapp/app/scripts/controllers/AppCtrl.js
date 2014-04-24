'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', '$rootScope', '$location', '$window', 'App', 'Google', 'User',
    function ($scope, $rootScope, $location, $window, App, Google, User) {

      function applyInfo(userInfo) {
        $scope.userInfo = userInfo;
        if (userInfo && userInfo.isAnonymous) {
          $location.path("/login");
        }
      }

      $scope.signInToFlickr = function () {
        $window.location.href = 'services/user/flickr/authorizeOAuth';
      };

      $scope.signInToGoogle = function () {
        App.googleAppSettings(function (settings) {
          Google.authorize(settings).then(function (code) {
            User.authorizeGoogleAccount(code, function (response) {
              $scope.userInfo = response.data;
              $location.path('/home/photos/google/albums');
            });
          });
        });
      };

      $scope.logout = function () {
        $window.location.href = "/services/user/logout";
      };

      $scope.isActiveLocation = function(location) {
        return $location.path().indexOf(location) == 0;
      };

      $rootScope.$on('userUpdated', function(event, info) {
        applyInfo(info);
      });
      User.info(applyInfo);
    }]);