# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="suresh.arumugam@arabbank.com.jo"

# Add a volume pointing to /tmp
VOLUME /tmp

# add required tessdata
RUN mkdir -p /Tesseract/owp/resources/tessdata/
RUN mkdir -p /Tesseract/owp/resources/uploaded/

RUN curl -LO https://github.com/maduraitamilan/Tesseract/raw/master/owp/resources/tessdata/eng.traineddata && \
	mv eng.traineddata /Tesseract/owp/resources/tessdata/
	
RUN curl -LO https://github.com/maduraitamilan/Tesseract/raw/master/owp/resources/tessdata/ara.traineddata && \
	mv ara.traineddata /Tesseract/owp/resources/tessdata/

RUN curl -LO https://github.com/maduraitamilan/Tesseract/raw/master/owp/resources/tessdata/osd.traineddata && \
	mv osd.traineddata /Tesseract/owp/resources/tessdata/	
	

RUN curl -LO https://github.com/maduraitamilan/Tesseract/raw/master/owp/resources/OWPFormPattern.json && \
	mv OWPFormPattern.json /Tesseract/owp/resources/
	
RUN curl -LO https://github.com/maduraitamilan/Tesseract/raw/master/owp/resources/owp-words.txt && \
	mv owp-words.txt /Tesseract/owp/resources/	

# Make port 8080 available to the world outside this container
#EXPOSE 8443

# The application's jar file
ARG JAR_FILE=target/tesseractmaster-v0.0.1.jar

# Add the application's jar to the container
ADD ${JAR_FILE} tesseractmaster-v0.0.1.jar

# Run the jar file 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/tesseractmaster-v0.0.1.jar"]