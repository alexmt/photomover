'use strict';

angular.module('services')
  .factory('UserSrv', ['$resource', function ($resource) {
    return $resource('/services/user/:action', {}, {
      info: {
        method: 'GET',
        params: { action: 'info' }
      },
      updateInfo: {
        method: 'POST',
        params: { action: 'updateInfo' }
      },
      removeService: {
        method: 'POST',
        params: { action: 'removeService' }
      },
      deleteAccount: {
        method: 'POST',
        params: { action: 'deleteAccount' }
      },
      detailedInfo: {
        method: 'GET',
        params: { action: 'detailedInfo' }
      },
      authorizeOAuth2Account: {
        method: 'POST',
        url: '/services/user/verifyOAuth2'
      }
    });
  }]);
