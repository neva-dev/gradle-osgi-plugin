# OSGi Toolkit

OSGi made easy. Bring fun to development of applications based on OSGi frameworks.

Assumptions:
 
* Reducing to minimum an effort related with creating custom OSGi framework distributions from reusable **feature concentrated OSGi packages** (not just bundles) being a combination of: metadata, artifact and its dependencies.
* Filling a gap which is OSGi related **easy to use tooling** that any Java dev could use and don't be scared of.

Project structure:

* Gradle Plugin - Tool for building bundles, packages and custom distributions.
* Manager Bundle - Bundle responsible for packages deployment on custom distribution (like a Apache Felix Web Console).
* Framework Launcher - Application that launches OSGi framework being a base of distribution (like a Apache Felix Main)
* Distribution Launcher - Application that extracts self-included custom distribution and performs initial setup.

## Versioning

Project is using [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Krystian Panek** - [pun-ky](https://github.com/pun-ky)

## License

**OSGi Toolkit** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)
