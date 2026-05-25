FROM ubuntu:latest
LABEL authors="IPeredereev"

ENTRYPOINT ["top", "-b"]