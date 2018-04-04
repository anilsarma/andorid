# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""This sample shows how to connect to PostgreSQL running on Cloud SQL.

See the documentation for details on how to setup and use this sample:
    https://cloud.google.com/appengine/docs/flexible/python\
    /using-cloud-sql-postgres
"""

import pandas as pd
import datetime
import logging
import os
import socket

from flask import Flask, request, redirect
from flask_sqlalchemy import SQLAlchemy
import sqlalchemy
import json
#import tzlocal
#import urllib
from  urllib.request import urlopen
from bs4 import BeautifulSoup


app = Flask(__name__)


def is_ipv6(addr):
    """Checks if a given address is an IPv6 address."""
    try:
        socket.inet_pton(socket.AF_INET6, addr)
        return True
    except socket.error:
        return False


# [START example]
# Environment variables are defined in app.yaml.
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ['SQLALCHEMY_DATABASE_URI']
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)


class Routes(db.Model):
    route_id = db.Column(db.Integer, primary_key=True)
    agency_id = db.Column(db.Integer)
    route_long_name = db.Column(db.String(46))

    def __init__(self, route_id):
        self.route_id = route_id

# in eastern time
def now():
    #tz = tzlocal.get_localzone()
    #
    #t0 = pd.Timestamp('now')
    t0 = pd.Timestamp(datetime.datetime.utcnow())
    t1 = t0.tz_localize('utc').tz_convert("US/Eastern")#.tz_localize(None)
    print(" Time:", t0, t1)
    return t1

@app.route('/')
def index():
    return redirect("http://www.google.com", code=302)
# [END example]



def execute(sql):
    result = db.engine.execute(sql)
    output = []
    columns = [x.name for x in result.cursor.description]
    for row in result:
        d = dict(zip(columns, row))
        output.append(d)
        # n#ames.append(str(row[0]) + str(row[1]))
    return output



@app.route('/route_names', methods=['GET'])
def route_names():
    longname = request.args.get("route")
    if longname is not None:
        result = db.engine.execute( "select * from routes where UPPER(route_long_name) like UPPER('%%{}%%') ".format(longname ) )
        #print(sql)
    else:
        sql = 'select * from routes'
        result = db.engine.execute(sql)
    names = []

    columns = [x.name for x in result.cursor.description]
    for row in result:
        d = dict(zip(columns, row))
        names.append(d)
        # n#ames.append(str(row[0]) + str(row[1]))

    output = json.dumps(names)
    return output, 200, {'Content-Type': 'application/json; charset=utf-8'}

@app.route('/get_routes', methods=['GET'])
def get_routes():
    start_stn = request.args.get("from")
    stop_stn = request.args.get("to")
    travel_date = request.args.get("date")
    travel_time = request.args.get("time")
    r = _get_routes(start_stn, stop_stn, travel_date, travel_time )
    return json.dumps(r), 200, {'Content-Type': 'application/json; charset=utf-8'}

@app.route('/train_status', methods=['GET'])
def train_status():
    station = request.args.get("station")
    data = _train_status(station)
    return json.dumps(data), 200, {'Content-Type': 'application/json; charset=utf-8'}


def _train_status(station='New York'):
    station = _get_station_code(station)
    if len(station) == 0:
        return []
    req = 'http://dv.njtransit.com/mobile/tid-mobile.aspx?sid={}'.format(station[0]['station_code']) # urllib2.Request()
    res = urlopen(req)
    soup = BeautifulSoup(res)  # .read()
    table = soup.find('table', {'id': "GridView1"})
    header = None
    data = []
    for row in table.find_all('table'):
        if header is None:
            header = row
            continue
        td = row.find_all('td')
        time = td[0].get_text().strip()
        to = td[1].get_text().strip()
        track = td[2].get_text().strip()
        line = td[3].get_text().strip()
        block_id = td[4].get_text().strip()
        status = td[5].get_text().strip()
        if len(track) == 0:
            if len(status) == 0:
                continue

        entry = {"time": time, "to": to, "track": track, "line": line, "status": status, 'block_id': block_id}
        data.append(entry)
    return data


def _get_station(station_name):
    sql = "select * from stops where upper(stop_name) like upper('%%" + station_name + "%%')";
    return execute(sql)

def _get_station_code(station_name):
    sql = "select * from station_codes where upper(station_name) like upper('%%{station_name}%%')".format(station_name=station_name)
    print ("SQL:", sql)
    return execute(sql)


def _get_routes(stn_from_name, stn_to_name, travel_date=None, travel_time=None):
    start_stop_id = _get_station(stn_from_name)
    stop_stop_id =  _get_station(stn_to_name)
    if len(start_stop_id)==0:
        return []

    if len(stop_stop_id)==0:
        return []

    sql = '''
    with trip_stops as ( select trips.block_id as block_id, trips.trip_id as trip_id, stop_times.stop_sequence as stop_sequence, stop_times.arrival_time as arrival_time,
                                stop_times.departure_time as departure_time, stops.stop_name as stop_name,
                                stops.stop_id as stop_id, trips.service_id as service_id
                    	from stop_times INNER join stops on stop_times.stop_id = stops.stop_id  inner join trips on trips.trip_id = stop_times.trip_id 
                   ), 
     start_station as ( select * from trip_stops where stop_name in ( '{start_name}' ) ),
     stop_station  as( select * from trip_stops where stop_name in ( '{stop_name}' ) ), 
     services as ( select service_id from calendar_dates where date = '{travel_date}' ),
     start_stop_station as (  select   s1.block_id, s1.service_id, s1.trip_id, s1.stop_name as start_name, s2.stop_name as stop_name, s1.departure_time as departure_time, s2.arrival_time as arrival_time
                    from (select * from start_station) as s1, (select * from stop_station) as s2 where s1.trip_id = s2.trip_id and s1.stop_sequence < s2.stop_sequence 
                 )     
     select * from start_stop_station where departure_time > '{travel_time}' and service_id in ( select service_id from services ) order by departure_time limit 500
'''

    if travel_time is None:
        travel_time = now()
        travel_time = travel_time.strftime("%H:%M:%S")
    if travel_date is None:
        travel_date = now()
        travel_date = travel_date.strftime("%Y%m%d")
    print("Time is ", travel_time, travel_date)
    sql = sql.format(start_name=start_stop_id[0]["stop_name"], stop_name=stop_stop_id[0]["stop_name"], travel_date=travel_date, travel_time=travel_time )
    data =  execute(sql)
    status = _train_status(stn_from_name)
    dstatus ={}
    for s in status:
         dstatus[s['block_id']] = s

    xdata = []
    for d in data:
        t = d['block_id']
        d['track'] = ''
        d['status'] = ''
        if t in dstatus:
            d['track'] = dstatus[t]['track']
            d['status'] = dstatus[t]['status']
        xdata.append(d)

    return xdata

def _get_route_stations(route_name):
    sql_stations = "select * from stops where stop_id in (select  distinct stop_id from stop_times where trip_id in ( select distinct trip_id from trips where route_id  = {route_id} ) );";
    sql_route = "select * from routes where route_long_name like '%%{route_name}%%';".replace("{route_name}", route_name);
    #System.out.println("SQL:" + sql_route);
    route_ids = execute(sql_route)# , "route_id")
    sql_stations = sql_stations.format( {'route_id':route_ids[0]['route_id'] } )
    #System.out.println("SQL:" + sql_stations );
    sql_stations = execute(sql_stations)#get_values( db, sql_stations, "stop_name");
    stations=[]
    for row in sql_stations:
        stations.append( row['stop_name'].upper())

    return stations

@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)
