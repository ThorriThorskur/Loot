# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Set environment variables for Maven
ENV MAVEN_VERSION=3.9.4
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="$MAVEN_HOME/bin:$PATH"

# Install Maven
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /usr/share/ && \
    mv /usr/share/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    rm apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    apt-get clean

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files into the container
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose the application's port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/Loot-0.0.1-SNAPSHOT.jar"]
