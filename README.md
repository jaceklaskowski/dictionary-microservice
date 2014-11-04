dictionary-microservice
=======================

My very own first microservice using Spray/Akka.

The purpose of the application is to store a key-value pairs in a database and moreover accept *any* number of fields with just four required ones:

* `node`
* `app`
* `obj`
* `mgr`

`id` is optional and set after an entity is persisted.

Other fields are accepted without any validation.

## Getting the application up and running

Run `./activator` in the project's home directory and once the shell appears, execute `~re-start`.

    > ~re-start
    [info] Application dictionary-spray not yet started
    [info] Starting application dictionary-spray in the background ...
    dictionary-spray Starting main.Main.main()
    [success] Total time: 0 s, completed Nov 4, 2014 10:14:30 PM
    1. Waiting for source changes... (press enter to interrupt)
    dictionary-spray host=localhost port=8080
    dictionary-spray >>> >>> receive called
    dictionary-spray [INFO] [11/04/2014 22:14:31.692] [default-akka.actor.default-dispatcher-3] [akka://default/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
    dictionary-spray [INFO] [11/04/2014 22:14:31.694] [default-akka.actor.default-dispatcher-2] [akka://default/deadLetters] Message [akka.io.Tcp$Bound] from Actor[akka://default/user/IO-HTTP/listener-0#-1254137053] to Actor[akka://default/deadLetters] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.

You should be able to open http://localhost:8080 in a browser.

The list of working routes:

* http://localhost:8080/api/dict/rules to `GET` all the available objects in the store.
* http://127.0.0.1:8080/api/dict/rules/1 to `GET` the object with id = `1`.

Use `curl` or any other REST client to talk to the service.

* `curl -i -X GET http://127.0.0.1:8080/api/dict/rules`
* `curl -i -X GET http://127.0.0.1:8080/api/dict/rules/1`
* `curl -i -X PUT http://127.0.0.1:8080/api/dict/rules -H "Content-Type: application/json" -d '{"node":"N", "app":"A", "obj":"O", "mgr":"MMM"}'`
* `curl -i -X POST http://127.0.0.1:8080/api/dict/rules/1 -H "Content-Type: application/json" -d '{"node":"NNN", "app":"AAA", "obj":"OOO", "mgr":"MMM"}'`
* `curl -i -X DELETE http://127.0.0.1:8080/api/dict/rules/1`
* `curl -i -X GET http://127.0.0.1:8080/api/dict/rules`