#!/usr/bin/env bash

sbt scalafmtAll scalastyleAll clean coverage compile test it:test coverageOff dependencyUpdates coverageReport