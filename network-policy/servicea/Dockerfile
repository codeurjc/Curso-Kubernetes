FROM python:2.7
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt
ADD . /service
WORKDIR /service
CMD ["python","-u","service.py"]