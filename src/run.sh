# Uncomment if any changed made and .db files need to be removed before building
# find  . -name "*.db"  | xargs -d'\n' rm

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
lib_dir=./lastfm-lib

# Find the path to the file.jar file within the lastfm-lib directory
jar_path=$(find "$lib_dir" -name "lastfm-java-0.1.3-SNAPSHOT.jar" -print -quit)

# Check if the file.jar file was found
if [[ -n $jar_path ]]; then
  # Run mvn install:install-file with the appropriate arguments
  mvn install:install-file -Dfile="$jar_path" -DgroupId=de.u-mass -DartifactId=lastfm-java -Dversion=0.1.3 -Dpackaging=jar
else
  echo "lastfm-library not found within $lib_dir"
fi
jar_path=$(find "$lib_dir" -name "json-20230227.jar" -print -quit)
if [[ -n $jar_path ]]; then
  # Run mvn install:install-file with the appropriate arguments
  mvn install:install-file -Dfile="$jar_path" -DgroupId=org.json -DartifactId=json -Dversion=20230227 -Dpackaging=jar
else
  echo "json-library not found within $lib_dir"
fi
mvn clean -DskipTests install
cd music-web
mvn jetty:run
