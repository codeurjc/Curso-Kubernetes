FROM python:2.7
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt
ADD . /todo
WORKDIR /todo
CMD ["python", "-u", "app.py"]