'use strict';

/**
 * Contains information about selected albums and photos.
 */
angular.module('services').service('SelectionSrv', [function() {
  var keysPerService = {};

  function formatSelectionKey(key) {
    var parts = [];
    if (key.albumId) {
      parts.push(key.albumId);
    }
    if (key.photoId) {
      parts.push(key.photoId);
    }
    return parts.join(':');
  }

  this.isSelected = function(params) {
    var keys = keysPerService[params.service];
    return keys && keys.indexOf(formatSelectionKey(params)) > -1;
  };

  this.setSelected = function(params, isSelected) {
    var keys = keysPerService[params.service];
    if (!keys) {
      keys = [];
      keysPerService[params.service] = keys;
    }
    var key = formatSelectionKey(params);
    if (!isSelected) {
      var position = keys.indexOf(key);
      if (position > -1) {
        keys.splice(position, 1);
      }
    } else {
      keys.push(key);
    }
  };
}]);
