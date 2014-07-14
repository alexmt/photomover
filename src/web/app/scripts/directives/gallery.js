'use strict';

angular.module('directives')
  .directive('gallery', function () {
    var tpl = '<div class="Collage">' +
        '<img selectable ng-src="{{picture.largeUrl}}" ng-click="onPictureClick({index: picture.index})"' +
          ' ng-repeat="picture in pictures">' +
      '</div>';
    return {
      restrict: 'E',
      replace: true,
      template: tpl,
      scope: {
        pictures: '=pictures',
        onPictureClick: '&'
      },
      link: function($scope, element) {
        $scope.$watch('pictures', function() {
          window.setTimeout(function() {
            $(element).waitForImages(function() {
              $(element).collagePlus({
                'effect' : 'effect-2',
                'allowPartialLastRow' : true
              });
            })
          });
        })
      }
    };
  });
