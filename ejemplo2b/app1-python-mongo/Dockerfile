FROM python:2.7
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt
ADD . /todo
COPY ./run /run.sh
WORKDIR /todo
CMD ["/run.sh"]