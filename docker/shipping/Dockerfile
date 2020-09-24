FROM openjdk:8-jdk

WORKDIR /usr/src/app
COPY *.jar ./app.jar



RUN	chown -R ${SERVICE_USER}:${SERVICE_GROUP} ./app.jar

USER ${SERVICE_USER}

ARG BUILD_VERSION
ARG COMMIT

LABEL org.label-schema.vendor="Neotys" \
  org.label-schema.version="${BUILD_VERSION}" \
  org.label-schema.name="Socks Shop: Shipping" \
  org.label-schema.description="REST API for Shipping service" \
  org.label-schema.url="https://github.com/neotysdevopsdemo/shipping" \
  org.label-schema.vcs-url="github.com:neotysdevopsdemo/shipping.git" \
  org.label-schema.vcs-ref="${COMMIT}" \
  org.label-schema.schema-version="1.0"

ENTRYPOINT ["java","-jar","./app.jar",  "--port=80"]
