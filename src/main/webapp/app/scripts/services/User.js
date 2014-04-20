'use strict';

angular.module('services')
  .factory('User', ['$resource', function ($resource) {
    return $resource('/services/user/:action', {}, {
      info: {
        method: 'GET',
        params: { action: 'info' }
      },
      detailedInfo: {
        method: 'GET',
        params: { action: 'detailedInfo' }
      },
      authorizeGoogleAccount: {
        method: 'POST',
        url: '/services/user/google/verify'
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