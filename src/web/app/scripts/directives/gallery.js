'use strict';

angular.module('directives')
  .directive('gallery', function () {
    var tpl = '<div class="Collage" ng-transclude></div>';
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      template: tpl,
      scope: {
        pictures: '=pictures',
        onPictureClick: '&'
      },
      link: function($scope, element) {
        $scope.$watch('pictures', function() {
          element.addClass('no-img-padding');
          window.setTimeout(function() {
            $(element).waitForImages(function() {
              $(element).collagePlus({
                'effect' : 'effect-2',
                'allowPartialLastRow' : true
              });
              window.setTimeout(function() {
                element.removeClass('no-img-padding');
              });
            })
          });
        })
      }
    };
  });
