#!/usr/bin/env bash

sbt scalafmtAll scalastyleAll clean coverage compile Test/test it/Test/test coverageOff dependencyUpdates coverageReport
