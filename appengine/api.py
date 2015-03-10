from endpoints_proto_datastore.ndb import EndpointsModel
from google.appengine.ext import ndb
from protorpc import remote
import endpoints
import json
import keys
import logging

AUDIENCES = []
ALLOWED_CLIENT_IDS = [keys.APPENGINE_CLIENT_ID, endpoints.API_EXPLORER_CLIENT_ID]

class NestStatus(EndpointsModel):
    _message_fields_schema = ('status',)

    status = ndb.StringProperty()

class UserCode(EndpointsModel):
    _message_fields_schema = ('user_code', 'verification_url', 'device_code')

    user_code = ndb.StringProperty()
    verification_url = ndb.StringProperty()
    device_code = ndb.StringProperty()

class AccessToken(EndpointsModel):
    _message_fields_schema = ('device_id', 'access_token', 'expires_in')

    device_id = ndb.StringProperty()
    access_token = ndb.StringProperty()
    expires_in = ndb.IntegerProperty()

@endpoints.api(name='nestiq',
               version='v1',
               description='Nest API for ConnectIQ',
               hostname='feedly-iq.appspot.com',
               audiences=AUDIENCES,
               allowed_client_ids=ALLOWED_CLIENT_IDS)
class NestApi(remote.Service):
    @NestStatus.method(user_required=True,
                       path='nest/status',
                       name='nest.status',
                       http_method='GET')
    def QueryNestStatus(self, query):
        user = endpoints.get_current_user()

        if user:
            status = NestStatus()
            status.status = 'good!'
            return status
        else:
            raise endpoints.UnauthorizedException('Unknown user.')

    @UserCode.method(path='nest/usercode',
                     name='nest.usercode',
                     http_method='GET')
    def QueryUserCode(self, data):
        # see https://developers.google.com/accounts/docs/OAuth2
        # see https://developers.google.com/accounts/docs/OAuth2ForDevices

        import urllib
        from google.appengine.api import urlfetch

        form_fields = {
          "client_id": keys.APPENGINE_CLIENT_ID,
          "scope": "email profile"
        }
        form_data = urllib.urlencode(form_fields)

        url = 'https://accounts.google.com/o/oauth2/device/code'
        result = urlfetch.fetch(url=url,
            payload=form_data,
            method=urlfetch.POST,
            headers={'Content-Type': 'application/x-www-form-urlencoded'})

        logging.warn(result.status_code)
        logging.warn(result.content)

        response_data = json.loads(result.content)

        user_code = UserCode()
        user_code.user_code = response_data['user_code']
        user_code.verification_url = response_data['verification_url']
        user_code.device_code = response_data['device_code']

        return user_code

    @AccessToken.method(path='nest/accesstoken/{device_id}',
                        name='nest.accesstoken',
                        http_method='GET',
                        request_fields=('device_id',))
    def QueryAccessToken(self, data):
        import urllib
        from google.appengine.api import urlfetch

        form_fields = {
          "client_id": keys.APPENGINE_CLIENT_ID,
          "client_secret": keys.APPENGINE_SECRET,
          "grant_type": "http://oauth.net/grant_type/device/1.0",
          "code": data.device_id
        }
        form_data = urllib.urlencode(form_fields)

        url = 'https://www.googleapis.com/oauth2/v3/token'
        result = urlfetch.fetch(url=url,
            payload=form_data,
            method=urlfetch.POST,
            headers={'Content-Type': 'application/x-www-form-urlencoded'})

        logging.warn(result.status_code)
        logging.warn(result.content)

        response_data = json.loads(result.content)

        token = AccessToken()
        token.device_id = data.device_id
        if 'error' in response_data:
            token.expires_in = 0
            token.access_token = response_data['error']
        else:
            token.access_token = response_data['access_token']
            token.expires_in = response_data['expires_in']

        return token

application = endpoints.api_server([NestApi], restricted=False)
