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
    expect(wnd.location.href).toBe('services/user/flickr/authorizeOAuth');
  });

  it('should load user info on start', function() {
    expect(scope.userInfo).toBe(userInfo);
  });

  it('should use Google and User services to sign in into google', function() {
    var googleAppSettingsCallback = null;
    var promise = q.defer().promise;
    var thenPromiseCallback = null;
    var testSettings = {};
    var testCode = 'test_code';
    var authorizeGoogleAccountCallback = null;

    spyOn(app, 'googleAppSettings').andCallFake(function(callback) {
      googleAppSettingsCallback = callback;
    });
    spyOn(google, 'authorize').andCallFake(function(settings) {
      expect(settings).toBe(testSettings);
    }).andReturn(promise);
    spyOn(promise, 'then').andCallFake(function(callback) {
      thenPromiseCallback = callback;
    });
    spyOn(user, 'authorizeGoogleAccount').andCallFake(function(data, callback) {
      expect(data).toBe(testCode);
      authorizeGoogleAccountCallback = callback;
    });

    scope.signInToGoogle();
    expect(googleAppSettingsCallback).not.toBeNull();
    googleAppSettingsCallback(testSettings);
    expect(thenPromiseCallback).not.toBeNull();
    thenPromiseCallback(testCode);
    expect(authorizeGoogleAccountCallback).not.toBeNull();
    var authorizedUserInfo = {
      isAnonymous: false
    };
    authorizeGoogleAccountCallback({ data: authorizedUserInfo });
    expect(scope.userInfo).toBe(authorizedUserInfo);
  });

  it('should redirect to "/services/user/logout" on logout', function() {
    scope.logout();
    expect(wnd.location.href).toBe('/services/user/logout');
  });
});
