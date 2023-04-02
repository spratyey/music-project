'use strict';

/**
 * Spotify controller.
 */
angular.module('music').controller('Spotify', function($scope, $stateParams, $state, Restangular, $http, toaster, $dialog) {
//   // Load album
//   Restangular.one('album', $stateParams.id).get().then(function(data) {
//     $scope.album = data;
//     $scope.query = $scope.album.artist.name + " - " + $scope.album.name;
//   });

  // Search with an external API 
  $scope.spotifySearch = function(query_str) {
    //$scope.query_result = query_str; //+ " result";
    Restangular.one('external').one('spotify-search').get({query: query_str})
      .then(function (data) {
        $scope.results = data;
        if ($scope.results.length === 0) {
          toaster.pop('warning', 'Search', 'No tracks found');
        }
      });
  };
});