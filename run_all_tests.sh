#!/usr/bin/env bash

sbt scalafmtAll clean coverage compile test it/test A11y/test coverageOff dependencyUpdates coverageReport
