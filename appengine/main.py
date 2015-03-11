from google.appengine.api import urlfetch
from keys import keys
import json
import logging
import webapp2

class RedirectAuth(webapp2.RequestHandler):
    def get(self):
        self.redirect("https://home.nest.com/login/oauth2?client_id=%s&state=STATE" % keys.NEST_CLIENT_ID)

class AccessTokenHandler(webapp2.RequestHandler):
    def get(self, auth_code):
        url = 'https://api.home.nest.com/oauth2/access_token?client_id=%s&code=%s&client_secret=%s&grant_type=authorization_code'
        result = urlfetch.fetch(url=url % (keys.NEST_CLIENT_ID, auth_code, keys.NEST_CLIENT_SECRET),
            method=urlfetch.POST,
            headers={'Content-Type': 'application/x-www-form-urlencoded'})

        logging.warn(result.status_code)
        logging.warn(result.content)

        response_data = json.loads(result.content)

        token = {}

        if 'error' in response_data:
            token['expires_in'] = 0
            token['access_token'] = response_data['error']
        else:
            token['expires_in'] = response_data['expires_in']
            token['access_token'] = response_data['access_token']


        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(token))

class NestStatusHandler(webapp2.RequestHandler):
    def get(self, access_token):
        url = "https://developer-api.nest.com/?auth=%s" % access_token
        result = urlfetch.fetch(url)

        logging.warn(result.status_code)
        logging.warn(result.content)

        # Parse the result as json
        response_data = json.loads(result.content)

        # Get a list of all the structures
        structures_json = response_data['structures']
        structure_names = [p for p in structures_json]

        # Just use the first structure
        structure = structures_json[structure_names[0]]

        # Find all the devices
        devices = response_data['devices']

        # Get the thermostat corresponding to the first thermostat
        # in the structures list
        thermostat = devices['thermostats'][structure['thermostats'][0]]

        status = {}
        status['target_temp'] = thermostat['target_temperature_f']
        status['current_temp'] = thermostat['ambient_temperature_f']
        status['hvac_mode'] = thermostat['hvac_mode']
        status['away_status'] = not 'home' == structure['away']

        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(status))

app = webapp2.WSGIApplication([
    ('/', RedirectAuth),
    ('/api/accesstoken/(.+?)', AccessTokenHandler),
    ('/api/status/(.+?)', NestStatusHandler),
], debug=False)
