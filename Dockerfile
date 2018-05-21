# This Docker file defines a production container for the Link Checking Service
FROM identifiersorg/linux-java8
LABEL maintainer="Manuel Bernal Llinares <mbdebian@gmail.com>"

# Environment - defaults
ENV WS_LINK_CHECKER_JVM_MEMORY_MAX 768m

# Prepare the application folder
RUN mkdir -p /home/app

# Add the application structure
ADD target/app/. /home/app
