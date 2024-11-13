#!/usr/bin/env bash

sbt scalafmtAll clean coverage compile test it/test coverageOff dependencyUpdates coverageReport
