# boot-immutant

A [Boot](http://boot-clj.com/) plugin for using Immutant 2.x
applications with a [WildFly](http://wildfly.org/) application server.

**Note:** you *only* need this plugin if you are deploying your
applications to a WildFly container.


## Installation

The current version is:

    [boot-immutant "0.5.0"]

You'll need to add it as a test-scoped dependency in your
`build.boot`:

```
(set-env!
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [org.immutant/web "2.1.3"]
                  [boot-immutant "0.5.0" :scope "test"]]
  :source-paths #{"src" "test"})
```

                  
## Usage

The plugin currently provides two subtasks:

* `boot.immutant/gird` - This adds files to the fileset necessary for
  activating an Immutant application in WildFly. When used in
  conjunction with the built-in `war` task, it generates a `.war` file
  suitable for deploying to WildFly. See the
  [deployment guide](resources/deployment-guide.md) for details.

* `boot.immutant/test-in-container` - This deploys the application to WildFly, and
  runs the tests. See the [testing guide](resources/testing-guide.md)
  for details.


## License

Copyright (C) 2015-2016 Red Hat, Inc.

Licensed under the Eclipse Public License v1.0
