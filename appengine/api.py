from endpoints_proto_datastore.ndb import EndpointsModel
from google.appengine.api import urlfetch
from google.appengine.ext import ndb
from keys import keys
from protorpc import remote
import endpoints
import json
import logging

AUDIENCES = []
ALLOWED_CLIENT_IDS = [keys.APPENGINE_CLIENT_ID, endpoints.API_EXPLORER_CLIENT_ID]

class AccessToken(EndpointsModel):
    _message_fields_schema = ('auth_code', 'access_token', 'expires_in')

    auth_code = ndb.StringProperty()
    access_token = ndb.StringProperty()
    expires_in = ndb.IntegerProperty()

class NestStatus(EndpointsModel):
    _message_fields_schema = ('access_token', 'target_temp', 'current_temp', 'away_status', 'hvac_mode')

    access_token = ndb.StringProperty()
    target_temp = ndb.IntegerProperty()
    current_temp = ndb.IntegerProperty()
    away_status = ndb.BooleanProperty()
    hvac_mode = ndb.StringProperty()

@endpoints.api(name='nestiq',
               version='v1',
               description='Nest API for ConnectIQ',
               hostname='feedly-iq.appspot.com',
               audiences=AUDIENCES,
               allowed_client_ids=ALLOWED_CLIENT_IDS)
class NestApi(remote.Service):
    @NestStatus.method(path='nest/status/{access_token}',
                       name='nest.status',
                       http_method='GET',
                       request_fields=('access_token',))
    def QueryNestStatus(self, query):
        status = NestStatus()
        status.access_token = query.access_token

        url = "https://developer-api.nest.com/?auth=%s" % query.access_token
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

        status.target_temp = thermostat['target_temperature_f']
        status.current_temp = thermostat['ambient_temperature_f']
        status.hvac_mode = thermostat['hvac_mode']
        status.away_status = not 'home' == structure['away']

        return status

    @AccessToken.method(path='nest/accesstoken/{auth_code}',
                        name='nest.accesstoken',
                        http_method='GET',
                        request_fields=('auth_code',))
    def QueryAccessToken(self, data):
        url = 'https://api.home.nest.com/oauth2/access_token?client_id=%s&code=%s&client_secret=%s&grant_type=authorization_code' % (keys.NEST_CLIENT_ID, data.auth_code, keys.NEST_CLIENT_SECRET)
        result = urlfetch.fetch(url=url,
            method=urlfetch.POST,
            headers={'Content-Type': 'application/x-www-form-urlencoded'})

        logging.warn(result.status_code)
        logging.warn(result.content)

        response_data = json.loads(result.content)

        token = AccessToken()
        if 'error' in response_data:
            token.expires_in = 0
            token.access_token = response_data['error']
        else:
            token.access_token = response_data['access_token']
            token.expires_in = response_data['expires_in']

        return token

application = endpoints.api_server([NestApi], restricted=False)
