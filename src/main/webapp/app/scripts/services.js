'use strict';
angular.module('services', ['ngResource'])
    .factory('User', ['$resource', function ($resource) {
      return $resource('services/user/:action', {}, {
        info: {
          method: 'GET',
          params: { action: 'info' }
        },
        authorizeGoogleAccount: {
          method: 'POST',
          url: 'services/user/google/verify'
        },
        albums: {
          method: 'POST',
          params: { action: 'albums' },
          isArray: true
        }
      });
    }]).factory('App', ['$resource', function ($resource) {
      return $resource('services/app/:action', {}, {
        googleAppSettings: {
          method: 'GET',
          url: 'services/app/google/settings'
        }
      });
    }]);


