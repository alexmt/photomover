'use strict';

angular.module('directives')
  .directive('selectable', ['$window', '$rootScope', function ($window, $rootScope) {

    var body = $($window.document.body);
    var activeTarget = null;
    var selectionLink = $('<button type="button" class="btn btn-default select-link select-link">' +
      '<i class="glyphicon glyphicon-ok"></i>' +
      '</button>');
    var offRouteChangeEvent = null;

    function equalOrChild(first, second) {
      return first[0] == second || first.has(second).length > 0;
    }

    function onMouseMove(event) {
      if (activeTarget != null &&
        !equalOrChild(activeTarget, event.target) &&
        !equalOrChild(selectionLink, event.target)) {
        hideSelectionLink();
      }
    }

    function onHover() {
      activeTarget = $(this);
      body.on('mousemove', onMouseMove);
      selectionLink.show().css(activeTarget.offset());
      offRouteChangeEvent = $rootScope.$on('$routeChangeStart', hideSelectionLink);
    }

    function hideSelectionLink() {
      selectionLink.hide();
      activeTarget = null;
      body.off('mousemove', onMouseMove);
      if (offRouteChangeEvent != null) {
        offRouteChangeEvent();
        offRouteChangeEvent = null;
      }
    }

    body.append(selectionLink);
    return {
      restrict: 'A',
      link: function (scope, element) {
        element.hover(onHover)
      }
    }
  }]);
