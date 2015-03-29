from google.appengine.api import urlfetch
from keys import keys
import json
import logging
import urllib2
import webapp2

# https://developer.nest.com/documentation/cloud/api-overview/
# https://developer.nest.com/documentation/cloud/rest-guide

def get_nest_status(access_token):
    # See https://developer.nest.com/documentation/api-reference
    url = "https://developer-api.nest.com/?auth=%s" % access_token
    result = urlfetch.fetch(url)

    logging.warn(result.status_code)
    logging.warn(result.content)

    status = {}
    if result.status_code == 200:
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

        status['thermostat'] = structure['thermostats'][0]
        status['structure'] = structure_names[0]
        status['target_temp'] = thermostat['target_temperature_f']
        status['current_temp'] = thermostat['ambient_temperature_f']
        status['hvac_mode'] = thermostat['hvac_mode']
        status['away_status'] = structure['away']

    return status

class RedirectAuth(webapp2.RequestHandler):
    def get(self):
        self.redirect("https://home.nest.com/login/oauth2?client_id=%s&state=STATE" % keys.NEST_CLIENT_ID)

class AccessTokenHandler(webapp2.RequestHandler):
    def get(self, auth_code):
        # See https://developer.nest.com/documentation/cloud/rest-quick-guide
        url = 'https://api.home.nest.com/oauth2/access_token?client_id=%s&code=%s&client_secret=%s&grant_type=authorization_code'
        result = urlfetch.fetch(url=url % (keys.NEST_CLIENT_ID, auth_code, keys.NEST_CLIENT_SECRET),
            method=urlfetch.POST,
            headers={'Content-Type': 'application/x-www-form-urlencoded'})

        logging.warn(result.status_code)
        logging.warn(result.content)

        token = {}
        if result.status_code == 200:
            response_data = json.loads(result.content)

            if not 'error' in response_data:
                token['expires_in'] = response_data['expires_in']
                token['access_token'] = response_data['access_token']

        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(token))

class NestStatusHandler(webapp2.RequestHandler):
    def get(self, access_token):
        status = get_nest_status(access_token)

        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(status))

class SmartRedirectHandler(urllib2.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, hdrs, newurl):
        # see https://github.com/python-git/python/blob/master/Lib/urllib2.py
        logging.warn('redirect_request: %s' % newurl)
        newurl = newurl.replace(' ', '%20')
        req =  urllib2.Request(newurl,
                               data=req.data,
                               headers=req.headers,
                               origin_req_host=req.get_origin_req_host(),
                               unverifiable=True)
        req.get_method = lambda: 'PUT'

        return req

class NestTargetTemperatureSetHandler(webapp2.RequestHandler):
    def get(self, access_token, thermostat, target_temp):
        # See https://developer.nest.com/documentation/api-reference
        url = "https://developer-api.nest.com/devices/thermostats/%s/target_temperature_f?auth=%s" % (thermostat, access_token)
        request = urllib2.Request(url, data='%d' % int(target_temp))
        request.add_header('Content-Type', 'application/json')
        request.get_method = lambda: 'PUT'

        opener = urllib2.build_opener(SmartRedirectHandler(), urllib2.HTTPCookieProcessor()) #, urllib2.HTTPSHandler(debuglevel=1)
        result = opener.open(request)

        logging.warn(result.getcode())

        # send the full status back
        status = get_nest_status(access_token)

        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(status))

class NestAwaySetHandler(webapp2.RequestHandler):
    def get(self, access_token, structure_id, away):
        # See https://developer.nest.com/documentation/api-reference
        url = "https://developer-api.nest.com/structures/%s?auth=%s" % (structure_id, access_token)
        request = urllib2.Request(url, data='{"away": "%s"}' % away)
        request.add_header('Content-Type', 'application/json')
        request.get_method = lambda: 'PUT'

        opener = urllib2.build_opener(SmartRedirectHandler(), urllib2.HTTPCookieProcessor()) #, urllib2.HTTPSHandler(debuglevel=1)
        result = opener.open(request)

        logging.warn(result.getcode())

        # send the full status back
        status = get_nest_status(access_token)

        self.response.headerlist = [('Content-type', 'application/json')]
        self.response.out.write(json.dumps(status))

app = webapp2.WSGIApplication([
    ('/', RedirectAuth),
    ('/api/accesstoken/(.+?)', AccessTokenHandler),
    ('/api/status/(.+?)', NestStatusHandler),
    ('/api/target/set/([^/]+?)/([^/]+?)/(.+?)', NestTargetTemperatureSetHandler),
    ('/api/away/set/([^/]+?)/([^/]+?)/(.+?)', NestAwaySetHandler),
], debug=False)
