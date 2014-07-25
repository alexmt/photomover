'use strict';

/**
 * Contains information about selected albums and photos.
 */
angular.module('services').service('SelectionSrv', [function() {

  var selectionPerService = {};

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
      selectedPhotoIds: []
    }, persistSelection);
  }

  function getServiceSelection(service) {
    var selectionPerAlbum = getKeyValue(selectionPerService, service, {}, false);
    var result = [];
    for (var albumId in selectionPerAlbum) {
      var albumSelection = selectionPerAlbum[albumId];
      if (albumSelection.selectAll) {
        result.push({
          albumId: albumId
        });
      } else {
        for (var photoId in albumSelection.selectedPhotoIds) {
          result.push({
            albumId: albumId,
            photoId: photoId
          });
        }
      }
    }
    return result;
  }

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
      return !selection.selectAll && selection.selectedPhotoIds.indexOf(params.photoId) > -1;
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
      var photoIdIndex = selection.selectedPhotoIds.indexOf(params.photoId);
      var isPhotoSelected = photoIdIndex > -1;
      if (isPhotoSelected != isSelected) {
        if (isSelected) {
          selection.selectedPhotoIds.push(params.photoId);
        } else {
          selection.selectedPhotoIds.splice(photoIdIndex, 1);
        }
      }
    } else {
      selection.selectAll = isSelected;
      selection.selectedPhotoIds = [];
    }
  };
}]);
