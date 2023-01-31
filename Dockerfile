FROM amazoncorretto:8

WORKDIR /home

RUN yum -y install unzip aws-cli
RUN yum -y install tar
RUN yum -y install xz
RUN mkdir input output init

COPY arcade-3.0.jar ./arcade.jar

COPY arcade.sh ./arcade.sh
RUN chmod +x ./arcade.sh

RUN yum -y install shadow-utils
RUN useradd nonroot
RUN chown nonroot /mnt/ input/ output/ init/
USER nonroot

ENTRYPOINT ./arcade.sh
