'use strict';

angular.module('directives')
  .directive('wizard', [function() {
    var tpl = '<div class="wizard" ng-transclude></div>';
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      template: tpl,
      scope: {
        step: '='
      },
      link: function($scope, element) {
        element.wizard();
        $scope.$watch('step', function(value) {
          element.wizard('selectedItem', {
            step: value
          })
        });
      }
    };
  }]);
