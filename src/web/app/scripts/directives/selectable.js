'use strict';

angular.module('directives')
  .directive('selectable', ['$window', '$rootScope', 'SelectionSrv', function ($window, $rootScope, SelectionSrv) {

    var SELECTED_LINK_CLASS = 'btn-primary';

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
      if (SelectionSrv.isSelected(getSelectionTarget(activeTarget))) {
        selectionLink.addClass(SELECTED_LINK_CLASS);
      } else {
        selectionLink.removeClass(SELECTED_LINK_CLASS);
      }
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

    function onSelectionLinkClick() {
      if (activeTarget != null) {
        selectionLink.toggleClass(SELECTED_LINK_CLASS);
        var isSelected = selectionLink.hasClass(SELECTED_LINK_CLASS);
        SelectionSrv.setSelected(getSelectionTarget(activeTarget), isSelected);
        updateTargetSelectedClass(activeTarget, isSelected);
      }
    }

    function getSelectionTarget(element) {
      return {
        albumId: element.attr('albumId'),
        photoId: element.attr('photoId')
      }
    }

    function updateTargetSelectedClass(element, isSelected) {
      if (isSelected) {
        element.addClass('selected');
      } else {
        element.removeClass('selected');
      }
    }

    body.append(selectionLink);
    selectionLink.on('click', onSelectionLinkClick);
    return {
      restrict: 'A',
      link: function (scope, element) {
        element.hover(onHover);
        updateTargetSelectedClass(element, SelectionSrv.isSelected(element));
      }
    }
  }]);
