# This is a script to run the bk program from the command line,
# since running it within the IDE fails due to the lanterna library not having
# access to the screen from there.
#
#!/usr/bin/env bash
set -eu

mvn compile

java \
-Dfile.encoding=UTF-8 \
-classpath target/classes:/Users/home/.m2/repository/com/jsbase/java-core/1000/java-core-1000.jar:/Users/home/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:/Users/home/.m2/repository/com/jsbase/java-testutil/1000/java-testutil-1000.jar:/Users/home/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar:/Users/home/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/Users/home/.m2/repository/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar:/Users/home/.m2/repository/com/github/librepdf/openpdf/1.3.27/openpdf-1.3.27.jar:/Users/home/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar bk.Bk \
database database.json \
log_file bk_log.txt \
"$@"
