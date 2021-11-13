# see https://raw.githubusercontent.com/clj-python/libpython-clj/master/dockerfiles/Py39Dockerfile

# We will use Ubuntu for our image
FROM ubuntu:latest

# Updating Ubuntu packages

ARG CLOJURE_TOOLS_VERSION=1.10.3.1029
ARG DEBIAN_FRONTENT=noninteractive

# Note: if you are getting errors here, try settings DNS to 1.1.1.1
#   ..: https://stackoverflow.com/questions/24991136/docker-build-could-not-resolve-archive-ubuntu-com-apt-get-fails-to-install-a

RUN apt-get -qq update
RUN apt-get install -y software-properties-common 
RUN add-apt-repository -y ppa:deadsnakes/ppa
RUN apt-get -qq update && apt-get -qq -y install curl wget bzip2 openjdk-8-jdk-headless python3.9 libpython3.9 python3-pip python3-distutils\
    && curl -o install-clojure https://download.clojure.org/install/linux-install-${CLOJURE_TOOLS_VERSION}.sh \
    && chmod +x install-clojure \
    && ./install-clojure && rm install-clojure \
    && wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
    && chmod a+x lein \
    && mv lein /usr/bin \
    && apt-get -qq -y autoremove \
    && apt-get autoclean \
    && rm -rf /var/lib/apt/lists/* /var/log/dpkg.log

# additional dependencies for GraalVM
RUN apt-get update -y
RUN apt-get install -y python3-venv
RUN apt-get install -y python3.9-venv
RUN apt-get install -y python3-dev
RUN apt-get install -y python3.9-dev
RUN apt-get install -y direnv
RUN apt-get install -y zip
RUN apt-get install -y unzip
RUN apt-get install -y sudo
RUN apt-get install -y git
RUN python3 -m pip install virtualenv
RUN python3.9 -m pip install virtualenv

# the show
WORKDIR /app
RUN git clone https://github.com/jjtolton/TheDevilsInterop.git
WORKDIR /app/TheDevilsInterop
RUN ./scripts/build
RUN env/bin/python -m pip install dist/*.whl ipython

CMD env/bin/ipython



