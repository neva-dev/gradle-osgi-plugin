# Gradle OSGi Plugin

OSGi made easy. Bring fun to development of applications based on OSGi frameworks.

Assumptions:

* Instance plugin can setup from the scratch Apache Felix Main Distribution (and later even Knoplerfish), task `osgiSetup` (reduces manual setup, fully automatized).
* Package plugin can produce **.jar* file containing bundles (and dependent bundles) and descriptor (metadata.json) which will be read by Gradle to handle transitive dependencies and multiple versions of sample bundle, task `osgiPackage`.
* Instance plugin will deploy on raw OSGi framework only one special *Neva OSGi Framework* bundle which will handle package files installation (like Apache Felix File Install, but using HTTP protocol), task `osgiDeploy`
* Instance plugin will provide tasks to control running OSGi instance: `osgiUp`, `osgiHalt`, `osgiDestroy` (like Vagrant commands).
* Instance plugin will be able extend package to be self-extractable, instance: `osgiBuild` will create a JAR with main class that will extract instance and run it.

OSGi package file structure:

* osgi/metadata.json (required, only jars having that file will be treated as OSGi packages)
* osgi/dependencies/*.jar (optional, complete embedded set of jars used to create package artifact, both compile and runtime bundles to be placed onto Felix)
* osgi/artifact/*.jar (optional, bundle file could be only collection of plain old OSGi bundles)
* osgi/distribution (unpacked Apache Felix Main Distribution, main class will extract that files, then deploy itself assuming that distro will contain Neva OSGi Framework bundle)
* content (e.g JCR content)
* ...

The draft above which allow to easily develop a bundles which will provide complete features, framework and applications - not only separate bundles which we have to collect by our own.
Sample packages (granularity of packages will be controlled on demand by developers):

* Netty (containing bundles: common, buffer, transport, handler, codec etc)
* Vert.x framework (containing Vert.x Core, Vertx.x Web)
* JCR Oak (containing oak-core, oak-spi)

or even application related (more recommended, like AEM CRX packages)

* Javarel Framework, containing bundles: Vert.x related, Netty related, JCR Oak (all-in-one, app with dependencies)
* Example Application, containing bundles: core, common (only business project implementation)

## Versioning

Project is using [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Krystian Panek** - [pun-ky](https://github.com/pun-ky)

## License

**Gradle OSGi Plugin** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)
