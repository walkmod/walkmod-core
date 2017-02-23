# walkmod-core

[![Build Status](https://travis-ci.org/walkmod/walkmod-core.svg?branch=master)](https://travis-ci.org/walkmod/walkmod-core)
[![Chat](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/{project-full-path}?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Windows Build Status](https://ci.appveyor.com/api/projects/status/2q35s9gt9bqaw558/branch/dev?svg=true)](https://ci.appveyor.com/project/rpau/walkmod-core)

<p align="center">
  <img src="http://walkmod.com/public/docs/assets/img/demo/logo-mask.png" alt="WalkMod logo"/>
</p>

This project, called [http://www.walkmod.com](walkmod), is an open source tool to fix coding style issues. Walkmod can support with any programming language if a set of interfaces are implemented as a third party plugin. The first (and current)
supported language is Java. So, walkmod is completely modularized and you can extend their features (like Maven), 
by plugins (see [http://walkmod.com/pluginslist](plugins]) ). 

This repository contains the core library to load the configuration files and apply a chain of code transformations ( file by file ) 
and required to build plugins. Anyway, you can use this library to help us to create a Walkmod ecosystem where other development tools
(i.e Eclipse, Maven) can execute walkmod in an embedded mode instead of forcing a manual installation.

The [https://github.com/rpau/walkmod-cmd](walkmod-cmd) project is just the responsible to build the tool - a zip file which all walkmod libraries, the default 
configuration and the scripts to execute walkmod. 

## Usage

You just need the following dependency:

```
<dependency>
    <groupId>org.walkmod</groupId>
    <artifactId>walkmod-core</artifactId>
    <version>3.0.4</version>
</dependency>
```

The `org.walkmod.WalkmodFacade` can help you to invoke the walkmod commands by API.

## Contributing

If you want to hack on this, fork it, improve it and send me a pull request.

To get started using it, just clone it and call mvn install. 


