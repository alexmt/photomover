'use strict';

describe('Controller: AppCtrl', function () {

  beforeEach(module('webApp'));

  var AppCtrl, scope, rootScope, location, wnd, app, google, user, userInfo, q;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope, $location, App, Google, User, $q) {
    rootScope = $rootScope;
    scope = $rootScope.$new();
    location = $location;
    app = App;
    google = Google;
    user = User;
    wnd = {
      location: {}
    };
    userInfo = {
      name: "test user"
    };
    q = $q;
    spyOn(user, "info").andCallFake(function(callback) {
      callback(userInfo);
    });
    AppCtrl = $controller('AppCtrl', {
      $scope: scope,
      $rootScope: rootScope,
      $location: location,
      $window: wnd,
      App: app,
      Google: google,
      User: user
    });
  }));

  it('should define signInToFlickr function', function () {
    expect(scope.signInToFlickr).toBeDefined();
  });

  it('should redirect to flickr login page to sign in into flickr', function() {
    scope.signInToFlickr();
    expect(wnd.location.href).toBe('services/user/flickr/authorize');
  });

  it('should load user info on start', function() {
    expect(scope.userInfo).toBe(userInfo);
  });
});
