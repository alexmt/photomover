'use strict';

angular.module('services')
  .factory('App', ['$resource', function ($resource) {
    return $resource('/services/app/:action', {}, {
      googleAppSettings: {
        method: 'GET',
        url: '/services/app/google/settings'
      },
      presentationSettings: {
        method: 'GET',
        url: 'services/app/settings/presentation'
      }
    });
  }]);