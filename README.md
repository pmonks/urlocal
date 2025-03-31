| | | | |
|---:|:---:|:---:|:---:|
| [**release**](https://github.com/pmonks/urlocal/tree/release) | [![CI](https://github.com/pmonks/urlocal/actions/workflows/ci.yml/badge.svg?branch=release)](https://github.com/pmonks/urlocal/actions?query=workflow%3ACI+branch%3Arelease) | [![Dependencies](https://github.com/pmonks/urlocal/actions/workflows/dependencies.yml/badge.svg?branch=release)](https://github.com/pmonks/urlocal/actions?query=workflow%3Adependencies+branch%3Arelease) | [![Vulnerabilities](https://github.com/pmonks/urlocal/actions/workflows/vulnerabilities.yml/badge.svg?branch=release)](https://pmonks.github.io/urlocal/nvd/dependency-check-report.html) |
| [**dev**](https://github.com/pmonks/urlocal/tree/dev)  | [![CI](https://github.com/pmonks/urlocal/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/pmonks/urlocal/actions?query=workflow%3ACI+branch%3Adev) | [![Dependencies](https://github.com/pmonks/urlocal/actions/workflows/dependencies.yml/badge.svg?branch=dev)](https://github.com/pmonks/urlocal/actions?query=workflow%3Adependencies+branch%3Adev) | [![Vulnerabilities](https://github.com/pmonks/urlocal/actions/workflows/vulnerabilities.yml/badge.svg?branch=dev)](https://github.com/pmonks/urlocal/actions?query=workflow%3Avulnerabilities+branch%3Adev) |

[![Latest Version](https://img.shields.io/clojars/v/com.github.pmonks/urlocal)](https://clojars.org/com.github.pmonks/urlocal/) [![Open Issues](https://img.shields.io/github/issues/pmonks/urlocal.svg)](https://github.com/pmonks/urlocal/issues) [![License](https://img.shields.io/github/license/pmonks/urlocal.svg)](https://github.com/pmonks/urlocal/blob/release/LICENSE)


<img alt="urlocal logo: a generated image of a local pub, as one might find in Europe" align="right" width="25%" src="https://raw.githubusercontent.com/pmonks/urlocal/release/urlocal-logo.png">

# urlocal

A Clojure micro-library for cached (ETag based) URL downloads.  At its core, this library provides a single fn (`urlocal.api/input-stream`) for reading the content of a URL, and will transparently cache downloaded content locally on disk (as per the [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)), serving subsequent requests for that same content out of that cache whenever possible.  Because this content is persisted on disk, the cache survives restarts of the JVM.

Cached content is checked for staleness periodically via HTTP ETag GET requests, which are more efficient than a regular HTTP GET request in the event the cache is up to date, and the same speed if not.  Within the checking interval, previously cached content is served straight from disk, with no network I/O at all.  The staleness checking interval is configurable, and for applications that cannot tolerate any staleness, it can be set to 0 (meaning "make an ETag request to check for staleness on every request of the URL's content").  Despite not eliminating network I/O, this configuration is still more efficient than regular HTTP GET requests, especially for content that changes infrequently.

The library only has one non-core dependency - on `clojure.tools.logging`, and is compatible with JVMs 1.8 and above (it uses the crusty old Java 1.1 HTTP client, rather than the vastly improved Java 11+ HTTP client).

While ETag-based caching logic is simple, well understood, and widely documented, the author thought it might be useful to centralise it in the interests of avoiding reinventing (small) wheels.

## Installation

`urlocal` is available as a Maven artifact from [Clojars](https://clojars.org/com.github.pmonks/urlocal).

### Trying it Out

#### Clojure CLI

```shell
$ clj -Sdeps '{:deps {com.github.pmonks/urlocal {:mvn/version "RELEASE"}}}'
```

#### Leiningen

```shell
$ lein try com.github.pmonks/urlocal
```

#### deps-try

```shell
$ deps-try com.github.pmonks/urlocal
```

### Demo

```clojure
(require '[urlocal.api :as url])


(def cache-dir (io/file (str (System/getenv "HOME") "/.cache/urlocal")))
;=> #'user/cache-dir

(.exists cache-dir)
;=> false

(time (url/input-stream "https://spdx.org/licenses/licenses.json"))
;=> "Elapsed time: 298.22525 msecs"
;=> #object[java.io.BufferedInputStream 0x4373f66f "java.io.BufferedInputStream@4373f66f"]

(.exists cache-dir)
;=> true

(map #(.getName %) (file-seq cache-dir))
;=> ("urlocal" "aHR0cHM6Ly9zcGR4Lm9yZy9saWNlbnNlcy9saWNlbnNlcy5qc29u.content" "aHR0cHM6Ly9zcGR4Lm9yZy9saWNlbnNlcy9saWNlbnNlcy5qc29u.metadata.edn")

(time (url/input-stream "https://spdx.org/licenses/licenses.json"))
;=> "Elapsed time: 1.294875 msecs"
;=> #object[java.io.BufferedInputStream 0x161dd92a "java.io.BufferedInputStream@161dd92a"]
```

### API Documentation

[API documentation is available here](https://pmonks.github.io/urlocal/), or [here on cljdoc](https://cljdoc.org/d/com.github.pmonks/urlocal/).

## Contributor Information

[Contributing Guidelines](https://github.com/pmonks/urlocal/blob/release/.github/CONTRIBUTING.md)

[Bug Tracker](https://github.com/pmonks/urlocal/issues)

[Code of Conduct](https://github.com/pmonks/urlocal/blob/release/.github/CODE_OF_CONDUCT.md)

### Developer Workflow

This project uses the [git-flow branching strategy](https://nvie.com/posts/a-successful-git-branching-model/), and the permanent branches are called `release` and `dev`.  Any changes to the `release` branch are considered a release and auto-deployed (JARs to Clojars, API docs to GitHub Pages, etc.).

For this reason, **all development must occur either in branch `dev`, or (preferably) in temporary branches off of `dev`.**  All PRs from forked repos must also be submitted against `dev`; the `release` branch is **only** updated from `dev` via PRs created by the core development team.  All other changes submitted to `release` will be rejected.

### Build Tasks

`urlocal` uses [`tools.build`](https://clojure.org/guides/tools_build). You can get a list of available tasks by running:

```
clojure -A:deps -T:build help/doc
```

Of particular interest are:

* `clojure -T:build test` - run the unit tests
* `clojure -T:build lint` - run the linters (clj-kondo and eastwood)
* `clojure -T:build ci` - run the full CI suite (check for outdated dependencies, run the unit tests, run the linters)
* `clojure -T:build install` - build the JAR and install it locally (e.g. so you can test it with downstream code)

Please note that the `release` and `deploy` tasks are restricted to the core development team (and will not function if you run them yourself).

## License

Copyright © 2023 Peter Monks

Distributed under the [Mozilla Public License, version 2.0](https://www.mozilla.org/en-US/MPL/2.0/).

SPDX-License-Identifier: [`MPL-2.0`](https://spdx.org/licenses/MPL-2.0)
