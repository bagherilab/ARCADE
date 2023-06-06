FROM amazoncorretto:8

# Change working directory
WORKDIR /home

# Install necessary libraries
RUN yum -y install unzip aws-cli
RUN yum -y install tar
RUN yum -y install xz

# Copy model jar
COPY arcade-3.0.jar ./arcade.jar

# Copy entrypoint script and make executable
COPY arcade.sh ./arcade.sh
RUN chmod +x ./arcade.sh

# Create input and output directories
RUN mkdir input output init

# Create non root user
RUN yum -y install shadow-utils
RUN useradd nonroot
RUN chown nonroot /mnt/ input/ output/ init/
USER nonroot

# Set entrypoint scipt
ENTRYPOINT ./arcade.sh
