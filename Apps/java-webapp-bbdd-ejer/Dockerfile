FROM maven as builder
COPY . /code/
WORKDIR /code
RUN mvn -DskipTests=true package

FROM openjdk:8-jre
RUN apt-get update; apt-get install -y netcat
COPY --from=builder /code/target/*.jar /app/
COPY ./run.sh /app/
WORKDIR /app
RUN chmod 777 /app/run.sh
EXPOSE 8080
CMD ["/app/run.sh"]