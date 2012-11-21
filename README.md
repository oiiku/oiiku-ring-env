# oiiku-ring-env

Utility for creating ring handlers that use a specific provided config/env.

Does not use singletons or global vars to achieve this, the end result is fully functional.

It's made to work with lein-ring, both in development and production.

## Installing

It's deployed to clojars, use it by adding the following dependency to your leiningen config:

    [oiiku-ring-env "0.2.0"]

## Using

You will do most of the work in creating the function that returns a configured ring handler yourself. Manual work requires about as many lines as it would do if we provided an API for it.

    (defn make-app-handler
      [env]
      (let [env (oiiku-ring-env/evaluate-env
                 env #{:db :cookie-secret :app-title})]
        (fn [req] (actual-app-handler (assoc req :env env)))))

This function has the following properties:

1. You call it with `(make-app-handler {:db ... :cookie-secret "abc123" :app-title "foo"})`
2. If you provide an environment with a key not present in the set in the 2nd argument to `evaluate-env`, you'll get an exception.
3. Values in the provided `env` can be `delay`s, which is nice for lazy evaluation of config values.
4. The actual environment will be made available as `:env` on the request map.

## You don't have to assoc the env on the request.

Here's another example:

    (def ^:dynamic *env* nil)
    
    (defn make-app-handler
      [env]
      (let [env (oiiku-ring-env/evaluate-env
                 env #{:db :cookie-secret :app-title})]
        (fn [req]
          (binding [*env* env]
            (actual-app-handler env)))))

Your library author prefers the assoc method as it makes the end result completely functional, but using `binding` is safe in most/all environments as the bindings are thread local, and you normally have one thread per request.

## Making a lazy handler with config files

In development and production (more info in separate sections) you typically want to use config files.

[TODO: Improve this section]

## Development use

[TODO: Improve this section]

## Production use

Make sure the config files you use in development is not added to the classpath when you run `lein with-profile production ring war` or `lein with-profile production ring uberwar`.

Configure your app server (we use Jetty) to add a folder that contains your config files to the classpath of your running .war web apps.
