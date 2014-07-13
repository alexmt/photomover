'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', '$rootScope', '$location', '$window', 'App', 'Google', 'User',
    function ($scope, $rootScope, $location, $window, App, Google, User) {

      function applyInfo(userInfo) {
        $scope.userInfo = userInfo;
        $scope.hasUnlinkedServices = false;
        for (var service in userInfo.accountsState) {
          if (!userInfo.accountsState[service]) {
            $scope.hasUnlinkedServices = true;
            break;
          }
        }
        if (userInfo && userInfo.isAnonymous) {
          $location.path("/login");
        }
      }

      $scope.signInToOAuthService = function (service) {
        $window.location.href = 'services/user/' + service + '/authorizeOAuth';
      };

      $scope.signInToGoogle = function () {
        App.googleAppSettings(function (settings) {
          Google.authorize(settings).then(function (code) {
            User.authorizeOAuth2Account({ service: 'google', code: code }, function (response) {
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
