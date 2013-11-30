plus2flickr
===========

Move Google+ photos to Flickr

1. Download photos at https://www.google.com/settings/takeout/custom
2. Group unsorted photos by month:

  plus2flickr.jar organize -s /Photos -o /Photos/Unsorted -deleteSource True

  -s - source directory
  -o - output directory
  -deleteSource - DELETE source directories if true
