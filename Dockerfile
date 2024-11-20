# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Set environment variables for Maven
ENV MAVEN_VERSION=3.8.8
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

# Copy the entire project into the container with specific ownership and permissions
COPY --chown=appuser:appgroup --chmod=755 . .

# Build the application
RUN mvn clean package -DskipTests

# Expose the application's port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/loot-1.0.0.jar"]
