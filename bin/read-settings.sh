mkdir src/main/resources/
heroku config:get FLICKR_APP_SETTINGS > src/main/resources/flickr_app.json
heroku config:get GOOGLE_APP_SETTINGS > src/main/resources/client_secret.json
