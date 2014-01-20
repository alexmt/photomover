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
          // TODO(amatyushentsev): Implement possible errors handling: "access_denied", "immediate_failed"
        }
      }
    }]);
