'use strict';

/**
 * Contains information about selected albums and photos.
 */
angular.module('services').service('SelectionSrv', [function() {

  var selectionPerService = {};

  function getPhotoIndex(photos, photoId) {
    for (var i in photos) {
      if (photos[i].photoId == photoId) {
        return i;
      }
    }
    return -1;
  }

  function getKeyValue(map, key, defaultValue, persistDefault) {
    var result = map[key];
    if (result == null) {
      result = defaultValue;
      if (persistDefault) {
        map[key] = result;
      }
    }
    return result;
  }

  function getAlbumSelection(service, albumId, persistSelection) {
    var selectionPerAlbum = getKeyValue(selectionPerService, service, {}, persistSelection);
    return getKeyValue(selectionPerAlbum, albumId, {
      selectAll: false,
      selectedPhotos: []
    }, persistSelection);
  }

  function getServiceSelection(service) {
    var selectionPerAlbum = getKeyValue(selectionPerService, service, {}, false);
    var result = [];
    for (var albumId in selectionPerAlbum) {
      var albumSelection = selectionPerAlbum[albumId];
      if (albumSelection.selectAll) {
        result.push({
          albumId: albumId,
          thumbnailUrl: albumSelection.thumbnailUrl
        });
      } else {
        for (var i in albumSelection.selectedPhotos) {
          var photo = albumSelection.selectedPhotos[i];
          result.push({
            albumId: albumId,
            photoId: photo.photoId,
            thumbnailUrl: photo.thumbnailUrl
          });
        }
      }
    }
    return result;
  }

  this.getServiceSelection = getServiceSelection;
  this.getSelectionStats = function(service) {
    var selectionStats = {
      albums: 0,
      photos: 0,
      isEmpty: true
    };
    var items = getServiceSelection(service);
    for (var i in items) {
      var selectedItem = items[i];
      if (selectedItem.photoId) {
        selectionStats.photos += 1;
      } else {
        selectionStats.albums += 1;
      }
      selectionStats.isEmpty = false;
    }
    return selectionStats;
  };

  /**
   * @param params The selection parameters.
   * @param params.service The service name.
   * @param params.albumId The albumId.
   * @param [params.photoId] Optional photo id.
   * @returns {boolean} whether specified photo ( or album if photo id is not specified ) is selected or not.
   */
  this.isSelected = function(params) {
    var selection = getAlbumSelection(params.service, params.albumId, false);
    if (params.photoId) {
      return !selection.selectAll && getPhotoIndex(selection.selectedPhotos, params.photoId) > -1;
    } else {
      return selection.selectAll;
    }
  };

  /**
   * Sets selection state for specified photo or album if photo id is not specified. If album is selected then all
   * previously selected photos of that album are deselected. If photo is selected then photo's album is deselected.
   *
   * @param params The selection parameters.
   * @param params.service The service name.
   * @param params.albumId The albumId.
   * @param [params.photoId] Optional photo id.
   * @param isSelected The required selection state.
   */
  this.setSelected = function(params, isSelected) {
    var selection = getAlbumSelection(params.service, params.albumId, true);
    if (params.photoId) {
      selection.selectAll = false;
      var photoIndex = getPhotoIndex(selection.selectedPhotos, params.photoId);
      var isPhotoSelected = photoIndex > -1;
      if (isPhotoSelected != isSelected) {
        if (isSelected) {
          selection.selectedPhotos.push({
            thumbnailUrl: params.thumbnailUrl,
            photoId: params.photoId
          });
        } else {
          selection.selectedPhotos.splice(photoIndex, 1);
        }
      }
    } else {
      selection.selectAll = isSelected;
      selection.thumbnailUrl = params.thumbnailUrl;
      selection.selectedPhotos = [];
    }
  };
}]);
