FROM hseeberger/scala-sbt:11.0.10_1.4.9_2.11.12 as build

RUN mkdir /app
COPY /build.sbt /app
COPY /project /app/project
COPY /rhapsody/src /app/rhapsody/src
WORKDIR /app
RUN sbt "project rhapsody" package


FROM jetty:9.4.41-jre11
COPY --from=build /app/rhapsody/target/scala-2.11/rhapsody_2.11-1.0.1-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war
