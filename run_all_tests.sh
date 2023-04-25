#!/usr/bin/env bash

sbt scalafmtAll scalastyleAll clean coverage compile Test/test IntegrationTest/test coverageOff dependencyUpdates coverageReport
