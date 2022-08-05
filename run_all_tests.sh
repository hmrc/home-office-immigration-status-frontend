#!/usr/bin/env bash

sbt clean scalafmt compile coverage test it:test dependencyUpdates coverageOff coverageReport