#!/usr/bin/env bash

sbt scalafmtAll clean coverage # Keep these separate, the optimisations break the compilation
sbt compile test it/test coverageOff dependencyUpdates coverageReport
