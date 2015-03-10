#!/usr/bin/env python

import webapp2
import json
import logging
import time

from datetime import datetime, timedelta

from google.appengine.ext import ndb
from google.appengine.api import urlfetch

from auth import NEST_LOGIN, NEST_PASSWORD, NEST_SERIAL_NUM


class Nest(ndb.Model):
    time = ndb.DateTimeProperty(required=True)
    ac = ndb.BooleanProperty(required=True)
    fan = ndb.BooleanProperty(required=True)
    heat = ndb.BooleanProperty(required=True)
    aux_heat = ndb.BooleanProperty(required=True)
    temperature = ndb.FloatProperty(required=True)

    @property
    def airwave(self):
        if self.fan == True and self.ac == False and self.heat == False and self.aux_heat == False:
            return True
        elif self.fan == True and (self.ac == True or self.heat == True or self.aux_heat == True):
            return False
        elif self.fan == False:
            return False
        else:
            return None

    @classmethod
    def current(cls):
        query = cls.query().order(-cls.time)
        current = query.get()
        if current is None:
            return Nest()
        else:
            return query.get()

    @classmethod
    def runtimes(cls, since):
        runtimes = {
            'fan': timedelta(),
            'ac': timedelta(),
            'airwave': timedelta(),
            'heat': timedelta(),
            'aux_heat': timedelta()
        }

        starttimes = {
            'fan': None,
            'ac': None,
            'airwave': None,
            'heat': None,
            'aux_heat': None
        }

        query = cls.query(cls.time > since).order(cls.time)

        for entity in query:
            for property in runtimes.keys():
                run_status = getattr(entity, property)
                if (run_status == True and starttimes[property] is None):
                    starttimes[property] = entity.time
                elif (run_status == False and starttimes[property] is not None):
                    runtime = entity.time - starttimes[property]
                    runtimes[property] = runtimes[property] + runtime
                    starttimes[property] = None

        for property in runtimes.keys():
            if (starttimes[property] is not None):
                runtime = datetime.now() - starttimes[property]
                runtimes[property] = runtimes[property] + runtime

        return runtimes


class UpdateHandler(webapp2.RequestHandler):
    def get(self):
        nest_login_payload = "username=" + NEST_LOGIN + "&password=" + NEST_PASSWORD

        try:
            nest_login = urlfetch.fetch("https://home.nest.com/user/login", method='POST', payload=nest_login_payload)
        except urlfetch.DeadlineExceededError:
            logging.warn("Nest login timeout.");
        else:
            if nest_login.status_code == 200:
                #self.response.headers.add_header("Access-Control-Allow-Origin", "*")
                #self.response.headers["Content-Type"] = "application/json"
                #self.response.write(nest_login.content)

                nest_login_data = json.loads(nest_login.content)

                nest_auth_headers = {
                  'Authorization': "Basic " + nest_login_data['access_token'],
                  'X-nl-user-id': nest_login_data['userid']
                }

                try:
                    nest = urlfetch.fetch(nest_login_data['urls']['transport_url'] + "/v2/mobile/" + nest_login_data['user'], headers=nest_auth_headers)
                except urlfetch.DeadlineExceededError:
                    logging.warn("Nest query timeout.");
                else:
                    if nest.status_code == 200:
                        self.response.headers.add_header("Access-Control-Allow-Origin", "*")
                        self.response.headers["Content-Type"] = "application/json"
                        self.response.write(nest.content)

                        nest_data = json.loads(nest.content)

                        # The Nest only updates when something changes, as such there's no need to add data
                        # to the datastore unless there's been a change.  To facilitate this we use the nest
                        # timestamp as a key_name and get that first.  Only if it doesn't exist to we add
                        # the entity.
                        timestamp_key = ndb.Key(Nest, str(nest_data['shared'][NEST_SERIAL_NUM]['$timestamp']))

                        if timestamp_key.get() is None:
                            Nest(
                                key = timestamp_key,
                                time = datetime.utcfromtimestamp(nest_data['shared'][NEST_SERIAL_NUM]['$timestamp'] / 1000),
                                ac = nest_data['shared'][NEST_SERIAL_NUM]['hvac_ac_state'],
                                fan = nest_data['shared'][NEST_SERIAL_NUM]['hvac_fan_state'],
                                heat = nest_data['shared'][NEST_SERIAL_NUM]['hvac_heater_state'],
                                aux_heat = nest_data['shared'][NEST_SERIAL_NUM]['hvac_aux_heater_state'],
                                temperature = round(float(nest_data['shared'][NEST_SERIAL_NUM]['current_temperature']) * 9 / 5 + 32, 3)
                            ).put()
                    else:
                        logging.warn("Nest query returned %s", nest.status_code);
            else:
                logging.warn("Nest login returned %s", nest_login.status_code);


class DataHandler(webapp2.RequestHandler):
    def get(self):
        now = datetime.now()
        now_epoch = time.mktime(now.timetuple())

        try:
            from_time = datetime.fromtimestamp(float(self.request.get('from')))
        except:
            from_time = now - timedelta(hours=24)

        try:
            to_time = datetime.fromtimestamp(float(self.request.get('to')))
        except:
            to_time = now

        query = Nest.query(Nest.time >= from_time, Nest.time <= to_time)

        output_data = {
            'time': [],
            'ac': [],
            'fan': [],
            'heat': [],
            'aux_heat': [],
            'temperature': []
        }

        for nest in query:
            output_data['time'].append(int(time.mktime(nest.time.timetuple())) * 1000)
            output_data['ac'].append(nest.ac)
            output_data['fan'].append(nest.fan)
            output_data['heat'].append(nest.heat)
            output_data['aux_heat'].append(nest.aux_heat)
            output_data['temperature'].append(nest.temperature)

        self.response.headers.add_header("Access-Control-Allow-Origin", "*")
        self.response.headers["Content-Type"] = "application/json"
        self.response.write(json.dumps(output_data))


app = webapp2.WSGIApplication([
    ('/nest/update', UpdateHandler),
    ('/nest/data', DataHandler)
], debug=True)
