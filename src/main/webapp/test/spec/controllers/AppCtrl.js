'use strict';

describe('Controller: MainCtrl', function () {

  // load the controller's module
  beforeEach(module('webApp'));

  var MainCtrl;
  var scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    MainCtrl = $controller('AppCtrl', {
      $scope: scope
    });
  }));

  it('should define signInToFlickr function', function () {
    expect(scope.signInToFlickr).toBeDefined()
  });
});
