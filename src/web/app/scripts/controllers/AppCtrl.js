'use strict';

angular.module('controllers')
  .controller('AppCtrl', ['$scope', '$rootScope', '$location', '$window', 'AppSrv', 'GoogleSrv', 'UserSrv',
    function ($scope, $rootScope, $location, $window, AppSrv, GoogleSrv, UserSrv) {

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
        AppSrv.googleAppSettings(function (settings) {
          GoogleSrv.authorize(settings).then(function (code) {
            UserSrv.authorizeOAuth2Account({ service: 'google', code: code }, function (response) {
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
      UserSrv.info(applyInfo);
    }]);
