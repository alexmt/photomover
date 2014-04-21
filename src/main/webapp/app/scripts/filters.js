'use strict';

angular.module('appFilters')
  .filter('capitalizeFirst', function() {
    return function(str) {
      return str ? (str.charAt(0).toUpperCase() + str.slice(1)) : "";
    }
  });