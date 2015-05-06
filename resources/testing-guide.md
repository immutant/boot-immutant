To run an application's tests inside of a WildFly container, you can
use the `immutant-test` task.

This task runs WildFly, deploys the project to it, runs all tests
found beneath the test/ directory, undeploys the app, and then shuts
down WildFly. All tests found on the classpath of the project will be
executed.

The task's behavior can be configured with the following options:


* `:cluster` - If true, the application will be deployed to the
  default cluster defined in the WildFly domain configuration (which
  defaults to two nodes), with the tests being run on one of the nodes.
  Defaults to `false`.

* `:debug` - If true, the server will be started with
  remote-debugging enabled. This means it will start, but immediately
  suspend, waiting for a remote debugger to attach to port 8787.

* `:init-fn` - The 'main' function called when the application is
  deployed. This function is responsible for initializing your
  application, and no initialization will occur if it is not provided.

* `:port-offset` - The test WildFly instance(s) is started with offset
  ports so it won't collide with a dev WildFly you may be running on
  the standard ports. By default, the ports are offset by 67.

* `:resource-paths` - A vector of directories containing resources
  that need to be copied to the top-level of the war file. These
  directories are different than the boot-standard `:resource-paths`, as
  those will be included in the war automatically. These directories
  are used to override or add configuration to `WEB-INF/` or
  `META-INF/` dirrectories within the war.

* `:wildfly-home` - Specifies the path to the WildFly install to be
  used. If not provided, the `$WILDFLY_HOME` environment variable
  will be used.

TODO: add a way to run a subset of the tests.
