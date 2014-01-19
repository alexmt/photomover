angular.module('services', ['ngResource'])
    .factory('User', ['$resource', function ($resource) {
      return $resource('services/user/:action', {}, {
        info: {
          method: 'GET',
          params: { action: 'info' }
        },
        storeGoogleToken: {
          method: 'POST',
          params: { action: 'storeGoogleToken' }
        }
      });
    }]);


