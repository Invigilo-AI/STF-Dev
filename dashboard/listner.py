import json
import pandas as pd
import requests
from flask import Flask, request, app
from waitress import serve

def writing(dt,lat,lon,base):
    df = pd.read_csv('a.csv')
    dict = {"Date/Time" : dt,
            "Lat" : lat,
            "Lon" : lon,
            "Base" : base,
            }
    print(dict)
    df = df.append(dict, ignore_index = True)
    #df.to_csv('a.csv')
    print(df)

app = Flask(__name__)
@app.route("/main", methods = ['POST'])
def main():
    if request.method == 'POST':
        payload = json.loads(request.data)
        dt = payload['dt']
        lat = payload['lat']
        lon = payload['lon']
        base = payload['base']
        writing(dt,lat,lon,base)
    return {"Resp" : True}

if __name__ == '__main__':
    serve(app,host='0.0.0.0', port=11000)