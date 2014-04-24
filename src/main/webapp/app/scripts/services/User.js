'use strict';

angular.module('services')
  .factory('User', ['$resource', function ($resource) {
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
      authorizeGoogleAccount: {
        method: 'POST',
        url: '/services/user/google/verifyOAuth2'
      },
      albums: {
        method: 'POST',
        params: { action: 'albums' },
        isArray: true
      },
      photos: {
        method: 'POST',
        isArray: true,
        params: { action: 'photos' }
      }
    });
  }]);