# This is a script to run the bk program from the command line,
# since running it within Eclipse fails due to the lanterna library not having
# access to the screen from there.
#

java \
-Dfile.encoding=UTF-8 \
-classpath /Users/home/github_projects/bk/target/classes:\
/Users/home/github_projects/java-core/target/classes:\
/Users/home/.m2/repository/junit/junit/4.12/junit-4.12.jar:\
/Users/home/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:\
/Users/home/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:\
/Users/home/github_projects/java-testutil/target/classes:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.jupiter.api_5.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.jupiter.engine_5.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.jupiter.migrationsupport_5.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.jupiter.params_5.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.commons_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.engine_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.launcher_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.runner_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.suite.api_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.suite.engine_1.8.1.v20211028-1957.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.platform.suite.commons_1.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit.vintage.engine_5.8.1.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.opentest4j_1.2.0.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.apiguardian_1.1.2.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.junit_4.13.2.v20211018-1956.jar:\
/Applications/Eclipse.app/Contents/Eclipse/plugins/org.hamcrest.core_1.3.0.v20180420-1519.jar:\
/Users/home/.m2/repository/com/github/jpsember/java-core/1.3.11/java-core-1.3.11.jar:\
/Users/home/.m2/repository/com/github/jpsember/java-testutil/1.3.1/java-testutil-1.3.1.jar:\
/Users/home/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar:\
/Users/home/.m2/repository/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar:\
/Users/home/.m2/repository/com/github/librepdf/openpdf/1.4.2/openpdf-1.4.2.jar bk.Bk \
database example/database.json \
log_file bk_log.txt \
"$@"
