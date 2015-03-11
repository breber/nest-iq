from keys import keys
import webapp2

class RedirectAuth(webapp2.RequestHandler):
    def get(self):
        self.redirect("https://home.nest.com/login/oauth2?client_id=%s&state=STATE" % keys.NEST_CLIENT_ID)

app = webapp2.WSGIApplication([
    ('/', RedirectAuth)
], debug=False)
