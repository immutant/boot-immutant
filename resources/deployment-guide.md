To deploy an Immutant application to WildFly, you'll need to generate
a war file based on a fileset that has passed through the `gird` task.

The task's behavior can be configured with the following options:

* `:context-path` - The context path to attach the application to. By
  default, the application will use a context based on the name of the
  war file, so a war file named `foo.war` will be hosted under `/foo`.
  To override that set `:context-path` to the desired context.  If you
  want the root ('/') context, you can either set `:context-path` to
  "/" *or* name the war file `ROOT.war`.

  This value is written to `WEB-INF/jboss-web.xml` inside the war, and
  a copy of the file is written to `:target-path`.

* `:dev` - Tells the task to create a development war with the
  following characteristics:

  - The application source and resources are referenced where they are
    on disk.
  - The application's dependencies are also not included, and are
    referenced from `~/.m2/`.
  - An nREPL endpoint is started on a random port on localhost.

  The development war allows the `ring.middleware.reload` middleware
  to reload changed namespaces on disk, and doesn't require you to
  regenerate the war file after making source changes. You will still
  need to redeploy the application to see any changes that the reload
  middleware can't load, and you'll need to regenerate the war if you
  change any dependencies.

  When using this option, you should *not* include the application's
  sources or dependencies in the war (so don't use it in conjunction
  with the `uber`, `aot`, or `sift --to-resource` tasks).

  Defaults to `false`.

* `:init-fn` - The 'main' function called when the application is
  deployed. This function is responsible for initializing your
  application, and no initialization will occur if it is not provided.

* `:nrepl-host` - The host to bind nrepl to. Defaults to "localhost".

* `:nrepl-port` - The port to bind to. Defaults to `0`, which means a
  random port.

* `:nrepl-port-file` - The file where the nREPL port is written for
  tooling to pick up. Can be relative to the app root or absolute,
  and *must* be absolute when used with an uberwar. Defaults to nil.

* `:nrepl-start` - Controls if an nREPL endpoint is started or not. For
  development wars, this is `true` by default, false otherwise.

* `:virtual-host` - The name of a host defined in the WildFly
  configuration that has virtual aliases assigned. This likely *won't*
  be the actual hostname. See the
  [WildFly docs](https://docs.jboss.org/author/display/WFLY8/Undertow+%28web%29+subsystem+configuration)
  for more detail.

  This value is written to `WEB-INF/jboss-web.xml` inside the war, and
  a copy of the file is written to `:target-path`.

### Example

`gird` just needs to be included in a standard war-building
pipeline. For a non-dev war, that would look like:

```
(require '[boot.immutant :refer :all])

(deftask build-war []
  (comp
    (uber :as-jars true)
    (aot :all true)
    (gird :init-fn 'my-app.core/init)
    (war)
    (target)))
```

For a dev war, you wouldn't need toe `uber` or `aot` tasks:

```
(deftask build-dev-war []
  (comp
    (gird :dev true :init-fn 'my-app.core/init)
    (war)
    (target)))
```

### Notes

We generate a `WEB-INF/web.xml` that sets a `ServletListener` that
acts as an entry point in to your application. If you need to modify
`web.xml`, you can grab a copy from `target/` after executing the
`target` task after `gird`. You'll want to place your copy under
`WEB-INF/web.xml` somewhere in your `:resource-paths` so it will get
added to the top-level of the war. We also generate a
`WEB-INF/jboss-deployment-structure.xml` that specifies what WildFly
modules the application depends on, and a `WEB-INF/jboss-web.xml` if
you specify a `:context-path` or `:virtual-host`. If you need to
modify those, the same deal applies.
