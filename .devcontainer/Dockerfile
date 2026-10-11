FROM ubuntu:24.04

ARG BUILDPLATFORM
ARG DEBIAN_FRONTEND=noninteractive

USER root
WORKDIR /root

RUN apt update && apt install -y \
  apt-transport-https ca-certificates gnupg curl wget git zip unzip less zsh net-tools iputils-ping jq lsof

ENV HOME="/root"

# --------------------------------------
# Git
# --------------------------------------
# Need to add the devcontainer workspace folder as a safe directory to enable git 
# version control system to be enabled in the containers file system.
RUN git config --global --add safe.directory "/workspaces/plugin-superhuman-docs"
# --------------------------------------

# --------------------------------------
# Oh my zsh
# --------------------------------------
RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)" -- \
  -t robbyrussell \
  -p git -p node -p npm

ENV SHELL=/bin/zsh
# --------------------------------------

# --------------------------------------
# Java
# --------------------------------------
ARG OS_ARCHITECTURE

RUN mkdir -p /usr/java
RUN echo "Building on platform: $BUILDPLATFORM"
RUN case "$BUILDPLATFORM" in \
  "linux/amd64"|"darwin/amd64") OS_ARCHITECTURE="x64" ;; \
  "linux/arm64"|"darwin/arm64") OS_ARCHITECTURE="aarch64" ;; \
  *) echo "Unsupported BUILDPLATFORM: $BUILDPLATFORM" && exit 1 ;; \
  esac && \
  wget -O openjdk-25.tar.gz "https://api.adoptium.net/v3/binary/latest/25/ga/linux/${OS_ARCHITECTURE}/jdk/hotspot/normal/eclipse"
RUN mkdir -p /usr/java/jdk-25 && \
  tar -xzvf openjdk-25.tar.gz --strip-components=1 -C /usr/java/jdk-25 && \
  rm openjdk-25.tar.gz
ENV JAVA_HOME=/usr/java/jdk-25
ENV PATH="$PATH:$JAVA_HOME/bin"
# Will load a custom configuration file for Micronaut
ENV MICRONAUT_ENVIRONMENTS=local,override
# Sets the path where you save plugins as Jar and is loaded during the startup process
ENV KESTRA_PLUGINS_PATH="/workspaces/plugin-superhuman-docs/local/plugins"
# --------------------------------------

# --------------------------------------
# SSH 
# --------------------------------------
RUN mkdir -p ~/.ssh
RUN touch ~/.ssh/config
RUN echo "Host github.com" >> ~/.ssh/config \
  && echo "    IdentityFile ~/.ssh/id_ed25519" >> ~/.ssh/config
RUN touch ~/.ssh/id_ed25519
# --------------------------------------
