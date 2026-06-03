FROM eclipse-temurin:11-jre-alpine

LABEL maintainer="jellystudy"
LABEL description="JellyStudy Question Provider Service with Redis Cache"

WORKDIR /app

COPY target/jellystudy-question-provider-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 20882 8082

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1