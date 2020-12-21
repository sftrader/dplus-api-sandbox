FROM ubuntu:latest

# Install some needed software into the image
RUN apt-get update && yes | apt-get install git
RUN apt-get update && yes | apt-get install openjdk-8-jdk-headless
RUN git clone https://github.com/sftrader/dplus-api-sandbox.git

# Write out a script for the image to run
RUN echo "#!/bin/bash" > /startscript.sh
RUN echo "set -x" >> /startscript.sh
RUN echo "cd dplus-api-sandbox" >> /startscript.sh
RUN echo "./gradlew clean build" >> /startscript.sh
RUN echo "java -jar Sandbox/build/libs/Sandbox*all.jar" >> /startscript.sh

RUN chmod +x /startscript.sh

# Note that, when the image is run, the gradlew command will cause downloads into the image each time.
# This is expected.
CMD /startscript.sh
