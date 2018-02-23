# Gradle OSGi Plugin

OSGi made easy. Bring fun to development of applications based on OSGi frameworks.

Assumptions:

* Instance plugin can setup from the scratch Apache Felix Main Distribution (and later even Knoplerfish), task `osgiSetup` (reduces manual setup, fully automatized).
* Bundle plugin can produce **.bundle* file containing bundles (and dependent bundles) and descriptor (metadata.json) which will be read by Gradle to handle transitive dependencies and multiple versions of sample bundle, task `osgiBundle`.
* Instance plugin will deploy on raw OSGi framework only one special *Neva OSGi Framework* bundle which will handle *.bundle files installation (like Apache Felix File Install, but using HTTP protocol), task `osgiDeploy`
* Instance plugin will provide tasks to control running OSGi instance: `osgiUp`, `osgiHalt`, `osgiDestroy` (like Vagrant commands).


Bundle *.bundle file structure:

* osgi/metadata.json
* osgi/bundles/*.jar
* content (e.g JCR content)
* ...

The draft above which allow to easily develop a bundles which will provide complete features, framework and applications - not only separate bundles which we have to collect by our own.
Sample bundles (granularity of bundles will be controlled on demand by developers):

* Netty (containing bundles: common, buffer, transport, handler, codec etc)
* Vert.x framework (containing Vert.x Core, Vertx.x Web)
* JCR Oak (containing oak-core, oak-spi)

or even application related (more recommended, like AEM CRX packages)

* Javarel Framework, containing bundles: Vert.x related, Netty related, JCR Oak (all-in-one, app with dependencies)
* Example Application, containing bundles: core, common (only business project implementation)

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Krystian Panek** - [pun-ky](https://github.com/pun-ky)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details