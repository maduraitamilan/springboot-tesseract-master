FROM ubuntu

# Add Maintainer Info
LABEL maintainer="suresh.arumugam@arabbank.com.jo"

# get the software proprerties common git and curl
# We need to install the software properties common before doing add-apt-repository, otherwise it will give an error: add-apt-repository: not found
RUN apt-get update && apt-get install -y \
  software-properties-common \
  git \   
  curl

RUN add-apt-repository ppa:alex-p/tesseract-ocr && apt-get update

# Get tesseract-ocr packages
RUN apt-get install -y \
  libleptonica-dev \
  libtesseract4 \
  libtesseract-dev \
  tesseract-ocr