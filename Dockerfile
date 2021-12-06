FROM openjdk:8
COPY target/senseair-s8-1.0-SNAPSHOT.jar /senseair-s8.jar
CMD ["java", "-jar", "/senseair-s8.jar"]
