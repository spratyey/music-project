'use strict';

/**
 * Playlist controller.
 */
angular.module('music').controller('Playlist', function($scope, $state, $stateParams, Restangular, Playlist, NamedPlaylist) {
  // Load playlist
  Restangular.one('playlist', $stateParams.id).get().then(function(data) {
    $scope.playlist = data;
  });

  // Play a single track
  $scope.playTrack = function(track) {
    Playlist.removeAndPlay(track);
  };

  // Add a single track to the playlist
  $scope.addTrack = function(track) {
    Playlist.add(track, false);
  };

  // Add all tracks to the playlist in a random order
  $scope.shuffleAllTracks = function() {
    Playlist.addAll(_.shuffle(_.pluck($scope.playlist.tracks, 'id')), false);
  };

  // Play all tracks
  $scope.playAllTracks = function() {
    Playlist.removeAndPlayAll(_.pluck($scope.playlist.tracks, 'id'));
  };

  // Add all tracks to the playlist
  $scope.addAllTracks = function() {
    Playlist.addAll(_.pluck($scope.playlist.tracks, 'id'), false);
  };

  // Like/unlike a track
  $scope.toggleLikeTrack = function(track) {
    Playlist.likeById(track.id, !track.liked);
  };

  // Remove a track
  $scope.removeTrack = function(order) {
    NamedPlaylist.removeTrack($scope.playlist, order).then(function(data) {
      $scope.playlist = data;
    });
  };

  // Delete the playlist
  $scope.remove = function() {
    NamedPlaylist.remove($scope.playlist).then(function() {
      $state.go('main.default');
    });
  };

  // Update UI on track liked
  $scope.$on('track.liked', function(e, trackId, liked) {
    var track = _.findWhere($scope.playlist.tracks, { id: trackId });
    if (track) {
      track.liked = liked;
    }
  });

  // Configuration for track sorting
  $scope.trackSortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    handle: '.handle',
    containment: 'parent',
    helper: function(e, ui) {
      ui.children().each(function() {
        $(this).width($(this).width());
      });
      return ui;
    },
    stop: function (e, ui) {
      // Send new positions to server
      $scope.$apply(function () {
        NamedPlaylist.moveTrack($scope.playlist, ui.item.attr('data-order'), ui.item.index());
      });
    }
  };

  $scope.spotifyrecommend = function() {
    //$scope.query_result = query_str; //+ " result";
    Restangular.one('playlist', $stateParams.id).one('spotifyrecommendation').get()
      .then(function (data) {
        $scope.spotifyrecommendations = data;
        if ($scope.spotifyrecommendations.length === 0) {
          toaster.pop('warning', 'Search', 'No tracks found');
        }
      });
  };

  $scope.lastfmrecommend = function() {
    //$scope.query_result = query_str; //+ " result";
    Restangular.one('playlist', $stateParams.id).one('lastfmrecommendation').get()
      .then(function (data) {
        $scope.lastfmrecommendations = data;
        if ($scope.lastfmrecommendations.length === 0) {
          toaster.pop('warning', 'Search', 'No tracks found');
        }
      });
  };

  $scope.changeVisibility = function(value)  {
    //$scope.query_result = query_str; //+ " result";
    //console.log("type:", visibility_type);

    $scope.visibility_type = value;
    console.log('value changed to', value);
  

    Restangular.one('playlist', $stateParams.id).post('visibility', {visibility: value})
      .then(function () {
        // console.log(data);
        //$scope.visibility = value;
        console.log("sucess");
      });
  };


});