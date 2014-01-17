angular.module('services', ['ngResource'])
    .factory('Account', ['$resource', function ($resource) {
      return $resource('services/account/:action', {}, {
        info: {
          method: 'GET',
          params: {
            action: 'info'
          }
        }
      });
    }]);


