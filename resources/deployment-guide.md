To deploy an Immutant application to WildFly, you'll need to generate
a war file via the `immutant-war` task.

The task's behavior can be configured with the following options:

* `:context-path` - The context path to attach the application to. By
  default, the application will use a context based on the name of the
  war file, so a war file named `foo.war` will be hosted under `/foo`.
  To override that set `:context-path` to the desired context.  If you
  want the root ('/') context, you can either set `:context-path` to
  "/" *or* name the war file `ROOT.war`.

  This value is written to `WEB-INF/jboss-web.xml` inside the war, and
  a copy of the file is written to `:target-path`.

* `:destination` - The directory where the war file should be placed.
  To ease deployment to WildFly, you can specify the root of your
  WildFly installation and the archive will be placed within the
  `standalone/deployments/` directory under that root instead of at
  the root. Defaults to `:target-path`.

* `:dev` - Tells the task to create a development war with the
  following characteristics:

  - The application source and resources aren't included in the
    archive, and instead are referenced where they are on disk.
  - The application's dependencies are also not included, and are
    referenced from `~/.m2/`.
  - An nREPL endpoint is started on a random port on localhost.

  The development war allows the `ring.middleware.reload` middleware
  to reload changed namespaces on disk, and doesn't require you to
  regenerate the war file after making source changes. You will still
  need to redeploy the application to see any changes that the reload
  middleware can't load, and you'll need to regenerate the war if you
  change any dependencies.

  Defaults to `false`, which results in an uberwar containing all of
  the application's code, resources, and dependencies.

* `:init-fn` - The 'main' function called when the application is
  deployed. This function is responsible for initializing your
  application, and no initialization will occur if it is not provided.

* `:name` - Specifies the name of the war file (without the .war
  suffix).  Defaults to "project".

* `:nrepl-host` - The host to bind nrepl to. Defaults to "localhost".

* `:nrepl-port` - The port to bind to. Defaults to `0`, which means a
  random port.

* `:nrepl-port-file` - The file where the nREPL port is written for
  tooling to pick up. Can be relative to the app root or absolute,
  and *must* be absolute when used with an uberwar. Defaults to nil.

* `:nrepl-start` - Controls if an nREPL endpoint is started or not. For
  development wars, this is `true` by default, false otherwise.

* `:resource-paths` - A vector of directories containing resources
  that need to be copied to the top-level of the war file. These
  directories are different than the boot-standard `:resource-paths`, as
  those will be included in the war automatically. These directories
  are used to override or add configuration to `WEB-INF/` or
  `META-INF/` dirrectories within the war.

* `:virtual-host` - The name of a host defined in the WildFly
  configuration that has virtual aliases assigned. This likely *won't*
  be the actual hostname. See the
  [WildFly docs](https://docs.jboss.org/author/display/WFLY8/Undertow+%28web%29+subsystem+configuration)
  for more detail.

  This value is written to `WEB-INF/jboss-web.xml` inside the war, and
  a copy of the file is written to `:target-path`.


### Notes

When generating an uberwar, we generate an uberjar using the standard
boot `uber` and `jar` tasks (`(comp (uber) (jar))`), so any options
for those tasks set via `task-options!` will be applied.

For both developer and uber wars, we generate a `WEB-INF/web.xml` that
acts as an entry point in to your application. As a convenience, we
drop a copy of that `web.xml` in to `:target-path` in case you need to
modify it. You'll want to place your copy in a directory in your
application root and point the `:resource-paths` task option at dir so
it will get added to the top-level of the war. We also generate a
`WEB-INF/jboss-deployment-structure.xml` that specifies what WildFly
modules the application depends on, and drop a copy in
`:target-path`. We do the same for `WEB-INF/jboss-web.xml` if you
specify a `:context-path` or `:virtual-host`.
