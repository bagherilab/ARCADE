FROM amazoncorretto:8

RUN yum -y install unzip aws-cli
RUN yum -y install tar
RUN yum -y install xz
RUN mkdir input output init

COPY arcade-3.0.jar /arcade.jar

COPY arcade.sh /arcade.sh
RUN chmod +x /arcade.sh

ENTRYPOINT /arcade.sh