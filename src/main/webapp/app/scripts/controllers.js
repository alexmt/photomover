'use strict';
function googleSignInCallback(authResult) {}
angular.module('controllers', ['services'])
    .controller('MainCtrl', ['$scope', 'User', function ($scope, User) {
      $scope.userInfo = User.info();
      $scope.googleSignInCallback = "googleSignInCallback";
      window.googleSignInCallback = function(authResult) {
        var authCode = authResult['code'];
        if (authCode) {
          User.authorizeGoogleAccount(authCode, function(response) {
            $scope.userInfo = response.data;
          });
        } else if (authResult['error']) {
          //   "access_denied" – пользователь отказался предоставить приложению доступ к данным
          //   "immediate_failed" – не удалось выполнить автоматический вход пользователя
        }
      }
    }]);
