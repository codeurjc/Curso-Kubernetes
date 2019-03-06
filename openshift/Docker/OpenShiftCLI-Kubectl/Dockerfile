FROM ubuntu:18.04

RUN apt update && \
    apt install -y --no-install-recommends \
    curl

RUN curl -Lk https://storage.googleapis.com/kubernetes-release/release/v1.7.16/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl && \
    chmod +x /usr/local/bin/kubectl

RUN curl -Lk https://github.com/openshift/origin/releases/download/v1.5.1/openshift-origin-client-tools-v1.5.1-7b451fc-linux-64bit.tar.gz -o oc.tar.gz && \
    tar xzf oc.tar.gz && \
    mv openshift-origin-client-tools-v1.5.1-7b451fc-linux-64bit/oc /usr/local/bin/oc && \
    chmod +x /usr/local/bin/oc

ENTRYPOINT [ "/bin/bash" ]