'use strict';

angular.module('directive.google-plus-signin', []).
  directive('googlePlusSignin', ['$window', '$rootScope', function ($window, $rootScope) {
    var pendingItems = [];

    function renderElement(element, settings) {
      $window.gapi.signin.render(element[0], {
        clientId: settings.clientId,
        redirecturi: 'postmessage',
        scope: settings.scopes.join(' '),
        accesstype: 'offline',
        cookiepolicy: 'single_host_origin',
        callback: '__gSigninCallback'
      });
    }

    function tryRenderElement(element, settings) {
      if (angular.isDefined($window.gapi) && $window.gapi.login && settings) {
        renderElement(element, settings);
      } else {
        pendingItems.push({ element: element, settings: settings});
      }
    }

    $window.__gSigninCallback = function (authResult) {
      if (authResult && !authResult.error) {
        $rootScope.$broadcast('event:google-plus-signin-success', authResult);
      }
      else {
        $rootScope.$broadcast('event:google-plus-signin-failure', authResult);
      }
    };

    $window.__gOnLoadCallback = function () {
      for (var i in pendingItems) {
        var item = pendingItems[i];
        renderElement(item.element, item.settings);
      }
      pendingItems = [];
    };

    return {
      restrict: 'E',
      template: '<span></span>',
      replace: true,
      link: function ($scope, element, attrs) {
        $scope.$watch(attrs.settings, function (settings) {
          if (angular.isDefined(settings)) {
            tryRenderElement(element, settings);
          }
        });
        // load google plus script once
        if (!$window.___gcfg) {
          $window.___gcfg = {
            parsetags: 'explicit'
          };
          (function () {
            var po = document.createElement('script');
            po.type = 'text/javascript';
            po.async = true;
            po.src = 'https://apis.google.com/js/plusone.js?onload=__gOnLoadCallback';
            var s = document.getElementsByTagName('script')[0];
            s.parentNode.insertBefore(po, s);
          })();
        }
      }
    };
  }]);