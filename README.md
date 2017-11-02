[![Build Status](https://travis-ci.org/walkmod/walkmod-core.svg?branch=master)](https://travis-ci.org/walkmod/walkmod-core) [![Windows Build Status](https://ci.appveyor.com/api/projects/status/2q35s9gt9bqaw558/branch/dev?svg=true)](https://ci.appveyor.com/project/rpau/walkmod-core)

walkmod: To Fix Java Static Coding Analysis Issues 
==================================================

WalkMod, is an open source tool to share and apply code conventions by automatic quick fixes for coding style issues. 

WalkMod is *language agnostic* and *platform agnostic*, since it can be extended to support multiple programming languages and runs in any platform, since it is coded in Java. However, the only current supported programming language is Java.

WalkMod began as an open source framework to run code transformations. It benefits directly from the experience
accumulated over several years of large-scale projects to support multiple quick fixes for existing static code analysis tools.

<p align="center">
  <img src="http://walkmod.com/public/docs/assets/img/demo/logo-mask.png" alt="WalkMod logo"/>
</p>

Getting started
===============
Docker can be installed either on your computer or in any CI tool to ensure your code style before merging changes. To get started, [check out the installation instructions in the documentation](https://docs.walkmod.com#installation).

Usage examples
==============

WalkMod can be used to run short-lived commands or by using an static configuration.

You can find a [list of real-world
examples](http://docs.walkmod.com#quickfixes) in the
documentation.

Under the hood
--------------

Under the hood, WalkMod is built on the following components:

* The [ivy](http://ant.apache.org/ivy/) engine to download plugins from Maven repositories.
* The [Spring Framework](http://spring.io/) to support IoC.


Contributing to WalkMod
=======================

Want to hack on WalkMod? Awesome! The `org.walkmod.WalkmodFacade` can help you to 
invoke the available commands by API.

If you want to hack on this, fork it, improve it and send me a pull request.

To get started using it, just clone it and call mvn install. 


