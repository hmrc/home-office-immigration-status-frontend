#!/usr/bin/env bash

sbt scalafmtAll scalastyleAll clean coverage compile test it/test A11y/test coverageOff dependencyUpdates coverageReport
