'use strict';

angular.module('controllers')
  .controller('HomeCtrl', ['$rootScope', '$location', 'userInfo',
    function ($rootScope, $location, userInfo) {
      function redirectToFirstService() {
        if ($location.path() == '/home') {
          for (var account in userInfo.accountsState) {
            if (userInfo.accountsState[account]) {
              $location.path('/home/photos/' + account + '/albums');
              break;
            }
          }
        }
      }

      $rootScope.$on('$locationChangeStart', function () {
        redirectToFirstService();
      });

      redirectToFirstService();
    }]);