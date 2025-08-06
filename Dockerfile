# Use a lightweight Java runtime image
FROM openjdk:21-jdk-slim

# Set working directory inside the container
WORKDIR /loadbalancer

# Copy your JAR file into the container
COPY target/PayrocLoadBalancerTechnicalChallenge-1.0-SNAPSHOT-jar-with-dependencies.jar loadbalancer.jar

# Run the app
ENTRYPOINT ["java", "-jar", "loadbalancer.jar"]