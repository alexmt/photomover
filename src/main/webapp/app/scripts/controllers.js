'use strict';
function googleSignInCallback(authResult) {}
angular.module('controllers', ['services'])
    .controller('MainCtrl', ['$scope', 'User', function ($scope, User) {
      $scope.userInfo = User.info();
      $scope.googleSignInCallback = "googleSignInCallback";
      window.googleSignInCallback = function(authResult) {
        var authCode = authResult['code'];
        if (authCode) {
          User.storeGoogleToken(authCode, function() {
            alert("It looks like response")
          });
        } else if (authResult['error']) {
          //   "access_denied" – пользователь отказался предоставить приложению доступ к данным
          //   "immediate_failed" – не удалось выполнить автоматический вход пользователя
        }
      }
    }]);
