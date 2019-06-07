import os
import urllib2
from flask import Flask, redirect, url_for, request, render_template

app = Flask(__name__)

@app.route('/info')
def info():
    return "{ status: \"ok\"}";

@app.route('/internalvalue')
def internalvalue():
    return "{ value: 0 }";

@app.route('/servicebvalue-internal')
def servicebvalue_internal():
    return urllib2.urlopen("http://serviceb-service:5000/internalvalue").read();

@app.route('/servicebvalue-external')
def servicebvalue_external():
    return urllib2.urlopen("http://serviceb-service:5000/externalvalue").read();

@app.route('/externalvalue')
def externalvalue():
    return urllib2.urlopen("https://www.googleapis.com/books/v1/volumes?q=isbn:0747532699").read();

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)