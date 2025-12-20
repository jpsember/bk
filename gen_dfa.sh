#!/usr/bin/env bash
set -eu

# Compile rxp files to dfa
#
DEST="src/main/resources/bk/"
dfa input descr.rxp output "${DEST}desc.dfa" ids src/main/java/bk/Util.java
