'use strict';

angular.module('services')
  .factory('PhotoSrv', ['$resource', function ($resource) {
    return $resource('/', {}, {
      albums: {
        method: 'GET',
        url: '/services/photos/:service/albums',
        isArray: true
      },
      albumInfo: {
        method: 'GET',
        url: '/services/photos/:service/albums/:albumId/info'
      },
      albumPhotos: {
        method: 'GET',
        url: '/services/photos/:service/albums/:albumId/photos',
        isArray: true
      }
    });
  }]);
