# boot-immutant

A [Boot](http://boot-clj.com/) plugin for using Immutant 2.x
applications with a [WildFly](http://wildfly.org/) application server.

**Note:** you *only* need this plugin if you are deploying your
applications to a WildFly container.

## Installation

The current version is:

    [boot-immutant "0.3.0"]

## Usage

The plugin currently provides two subtasks:

* `boot.immutant/immutant-war` - This generates a `.war` file suitable
  for deploying to WildFly. See the
  [deployment guide](resources/deployment-guide.md) for details.

* `boot.immutant/immutant-test` - This deploys the application to WildFly, and
  runs the tests. See the [testing guide](resources/testing-guide.md)
  for details.

## License

Copyright (C) 2015 Red Hat, Inc.

Licensed under the Eclipse Public License v1.0
