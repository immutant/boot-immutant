To run an application's tests inside of a WildFly container, you can
use the `test-in-container` task.

This task runs WildFly, deploys the project to it, runs all tests
found beneath the test/ directory, undeploys the app, and then shuts
down WildFly. All tests found on the classpath of the project will be
executed.

An Immutant war must be available in the fileset.

The task's behavior can be configured with the following options:

* `:cluster` - If true, the application will be deployed to the
  default cluster defined in the WildFly domain configuration (which
  defaults to two nodes), with the tests being run on one of the nodes.
  Defaults to `false`.

* `:debug` - If true, the server will be started with
  remote-debugging enabled. This means it will start, but immediately
  suspend, waiting for a remote debugger to attach to port 8787.

* `:port-offset` - The test WildFly instance(s) is started with offset
  ports so it won't collide with a dev WildFly you may be running on
  the standard ports. By default, the ports are offset by 67.

* `:war-name` - The name of the war in the fileset to test. Defaults
  to `"project.war"`.

* `:wildfly-home` - Specifies the path to the WildFly install to be
  used. If not provided, the `$WILDFLY_HOME` environment variable
  will be used.

TODO: add a way to run a subset of the tests.

### Example

To use `test-in-container`, you'll need to add an Immutant war to the
fileset:

```
(require '[boot.immutant :refer :all])

(deftask build-war []
  (comp
    (uber :as-jars true)
    (aot :all true)
    (gird :init-fn 'my-app.core/init)
    (war)
    (target)))

(deftask run-tests []
  (comp
    (build-war)
    (test-in-container)))
```
