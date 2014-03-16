plus2flickr [![Build Status](https://travis-ci.org/alexmt/plus2flickr.png?branch=master)](https://travis-ci.org/alexmt/plus2flickr)
===========

A Web Application which allows to manage photos/pictures stored in Google Plus/Flickr including following:

- view, delete and organize photos into albums;
- convenient bulk upload;
- move/copy photos between Google Plus/Flickr accounts;
- scheduled operations (e.g. move all photos from Google Plus auto upload album to specified Flickr collection);

CLI Utils:

- Group unsorted photos by month:

  plus2flickr.jar organize -s /Photos -o /Photos/Unsorted -deleteSource True

  -s - source directory
  -o - output directory
  -deleteSource - DELETE source directories if true

- Upload photos to Flickr

  upload -s /home/matyushentsev/Pictures/Google -appKey [yourAppKey] -appSecret [yourAppSecret]

  each directory will be uploaded to set with the same name
