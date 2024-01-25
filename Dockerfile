FROM eclipse-temurin:21.0.1_12-jre-alpine

RUN apk add --no-cache bash

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG UTILS_VERSION
RUN test -n "$UTILS_VERSION"
COPY utils/$UTILS_VERSION/run-with-redaction.sh ./utils/
COPY utils/$UTILS_VERSION/redactor              ./utils/

COPY build/libs/*.jar app.jar
ENTRYPOINT ["./utils/run-with-redaction.sh", "java","-Dlog4j2.formatMsgNoLookups=true","-jar","/app.jar"]