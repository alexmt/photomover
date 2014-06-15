photomover [![Build Status](https://travis-ci.org/alexmt/photomover.svg?branch=master)](https://travis-ci.org/alexmt/photomover) [![Analytics](https://ga-beacon.appspot.com/UA-51691703-1/photomover/readme)](https://github.com/igrigorik/ga-beacon)
===========

A Web Application which allows to manage photos/pictures stored in Google Plus/Flickr including following:

- view, delete and organize photos into albums;
- convenient bulk upload;
- move/copy photos between Google Plus/Flickr accounts;
- scheduled operations (e.g. move all photos from Google Plus auto upload album to specified Flickr collection);

CLI Utils:

- Group unsorted photos by month:

  photomover.jar organize -s /Photos -o /Photos/Unsorted -deleteSource True

  -s - source directory
  -o - output directory
  -deleteSource - DELETE source directories if true

- Upload photos to Flickr

  upload -s /home/matyushentsev/Pictures/Google -appKey [yourAppKey] -appSecret [yourAppSecret]

  each directory will be uploaded to set with the same name
  
