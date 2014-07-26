'use strict';

angular.module('services')
  .service('GoogleSrv', ['$window','$q', function($window, $q) {
    var isLoading = false;
    var pendingCallbacks = [];
    $window.__onGoogleLoadCallback = function() {
      for (var i in pendingCallbacks) {
        var callback = pendingCallbacks[i];
        callback($window.gapi);
      }
      pendingCallbacks = [];
    };

    function onGoogleLoaded(callback) {
      if (angular.isDefined($window.gapi)) {
        callback($window.gapi);
      } else {
        if (!isLoading) {
          isLoading = true;
          var po = document.createElement('script');
          po.type = 'text/javascript';
          po.async = true;
          po.src = 'https://apis.google.com/js/client:plusone.js?onload=__onGoogleLoadCallback';
          var s = document.getElementsByTagName('script')[0];
          s.parentNode.insertBefore(po, s);
        }
        pendingCallbacks.push(callback);
      }
    }

    this.authorize = function(settings) {
      var deferred = $q.defer();
      onGoogleLoaded(function(gapi) {
        gapi.auth.signIn({
          'approvalprompt' : 'force',
          'clientid' : settings.clientId,
          'requestvisibleactions' : 'http://schemas.google.com/CommentActivity http://schemas.google.com/ReviewActivity',
          'cookiepolicy' : 'single_host_origin',
          'redirecturi': 'postmessage',
          'scope': settings.scopes.join(' '),
          'accesstype': 'offline',
          'callback': function(authResult) {
            if (authResult && !authResult.error) {
              deferred.resolve(authResult.code);
            } else {
              deferred.reject(authResult.error);
            }
          }
        });
      });
      return deferred.promise;
    }
  }]);
