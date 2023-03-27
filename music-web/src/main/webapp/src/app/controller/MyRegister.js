'use strict';

/**
 * Settings user edition page controller.
 */
angular.module('music').controller('MyRegister', function($scope, $dialog, $state, $stateParams, Restangular) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.username;
  };
  
  /**
   * In edit mode, load the current user.
   */
  if ($scope.isEdit()) {
    Restangular.one('user', $stateParams.username).get().then(function(data) {
      $scope.user = data;
    });
  }

  /**
   * Update the current user.
   */
  $scope.edit = function() {
    var promise = null;
    
    if ($scope.isEdit()) {
      promise = Restangular
      .one('user', $stateParams.username)
      .post('', $scope.user);
    } else {
      promise = Restangular
      .one('user')
      .put($scope.user);
    }
    
    promise.then(function() {
      $state.transitionTo('login');
    });
  };

});