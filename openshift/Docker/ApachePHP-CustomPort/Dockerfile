FROM ubuntu:18.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && \
    apt install -qqy --no-install-recommends \
    apache2 \
    php7.2

COPY apache2.conf /etc/apache2/apache2.conf
COPY ports.conf   /etc/apache2/ports.conf
COPY index.php    /var/www/html/

EXPOSE 8080

RUN chown -R www-data.www-data /var/www/html/
RUN chmod -R 777 /var/log/apache2
RUN chmod -R 777 /var/run/apache2

USER 1001

CMD ["/usr/sbin/apachectl", "-DFOREGROUND", "-k", "start"]
