# oiiku-ring-env

Creates factory functions for creating ring handlers that operate in a specific environment.

Does not use singletons or global vars to achieve this, the end result is fully functional.

It's made to work with lein-ring, both in development and production.

## Installing

It's deployed to clojars, use it by adding the following dependency to your leiningen config:

    [oiiku-ring-env "0.1.1"]

## Using

First, create your handler factory, in `core.clj` or whereever.

    (def make-app-handler
      (oiiku-ring-env/handler-factory
        (fn [env]
          ;; Return any ring handler function here
          (fn [req]
            {:status 200 :body (str "Hello from " (get-in req [:env :app-title]))}))
        :all-keys [:db :cookie-secret :app-title]))

To create a new handler, call the factory with your environment.

    (make-app-handler {:db "an actual db object" :cookie-secret "123abcdef456" :app-title "My App"})

A request to your app will now respond with 200 OK and the response body `"Hello from My App"`.

The actual environment is added to the request map, with the key `env`. You can also specify the name of this key by providing the option `:env-key :my-key` below the `:all-keys` option.

## Making a lazy handler with config files

In development and production (more info in separate sections) you typically want to use config files.

[TODO: Improve this section]

## Development use

[TODO: Improve this section]

## Production use

Make sure the config files you use in development is not added to the classpath when you run `lein with-profile production ring war` or `lein with-profile production ring uberwar`.

Configure your app server (we use Jetty) to add a folder that contains your config files to the classpath of your running .war web apps.
