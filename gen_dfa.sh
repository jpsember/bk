#!/usr/bin/env bash
set -eu

# Compile rxp files to dfa
#
DEST="src/main/resources/bk/"
dfa descr.rxp "${DEST}desc.dfa" ids src/main/java/bk/Util.java
