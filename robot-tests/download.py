#!/usr/bin/python

import subprocess
import re
import sys
import urllib
from urlparse import urlparse
from HTMLParser import HTMLParser
 

# Implement bash commands inside python
def shell(command):
  process = subprocess.Popen(command.split(), stdout=subprocess.PIPE)
  output = process.communicate()[0]
  return output

# Work out the base url from the download url 
def getBaseUrl(downloadUrl):
  o = urlparse(downloadUrl)
  baseUrl = o.scheme + "://" + o.netloc
  #print "*******************************************************************"
  #print baseUrl
  #print "*******************************************************************"
  return baseUrl

# Go to the base url. Follow any redirects. Find the final auth base url. Find the relative url to post credentials to
def getBaseAuthUrlAndPortUrl(baseUrl):
  curlCommand = "curl -L --insecure -w marker%{url_effective}marker " + baseUrl
  #print "*******************************************************************"
  #print curlCommand
  #print "*******************************************************************"
  loginHtml = shell(curlCommand)
  #print "*******************************************************************"
  #print loginHtml
  #print "*******************************************************************"
  loginPostUrl = re.findall('<form action="(.*)" method="post">', loginHtml, re.MULTILINE)[0]
  #print "*******************************************************************"
  #print loginPostUrl
  #print "*******************************************************************"
  redirectUrl = re.findall("marker(.*)marker", loginHtml, re.MULTILINE)[0]
  o = urlparse(redirectUrl)
  baseAuthUrl = o.scheme + "://" + o.netloc
  #print "*******************************************************************"
  #print baseAuthUrl
  #print "*******************************************************************"
  return loginPostUrl, baseAuthUrl

# Post the credentials to the base auth url and get SAML credentials which are needed to continue the authentication process
def postCredentialsAndGetShibbolethParameters(user, password, baseAuthUrl, loginPostUrl):
  # Note that python will put quotes around the individual parameters automatically. In particular the authUrl, and with out these the curl command would not work.
  curlCommand = "curl --insecure -L --data j_username=" + urllib.quote(user) + "&j_password=" + urllib.quote(password) + "&_eventId_proceed= " + baseAuthUrl + loginPostUrl
  #print "*******************************************************************"
  #print curlCommand
  #print "*******************************************************************"
  postLoginPage = shell(curlCommand)
  #print "*******************************************************************"
  #print postLoginPage
  #print "*******************************************************************"
  sAMLResponse = re.findall('name="SAMLResponse" value="(.*)"', postLoginPage, re.MULTILINE)[0]
  relayState = re.findall('RelayState" value="(.*?)"', postLoginPage, re.MULTILINE)[0]
  return sAMLResponse, relayState

# Post the SAML credentials and get back a session cookie. The user is now logged in and the session cookie is all that is required going forward.
def postShibbolethParametersForSession(sAMLResponse, relayState, baseUrl):
  h = HTMLParser()
  curlCommand = "curl --insecure -L -c - --data SAMLRequest=" + urllib.quote(h.unescape(sAMLResponse)) + "&RelayState=" + urllib.quote(h.unescape(relayState)) + " " + baseUrl + "/Shibboleth.sso/SAML2/POST"
  #print "*******************************************************************"
  #print curlCommand
  #print "*******************************************************************"
  exchange = shell(curlCommand)
  #print "*******************************************************************"
  #print exchange
  #print "*******************************************************************"
  session = re.findall('(_shibsession_([^\s-]*))\s*(.*)', exchange, re.MULTILINE)
  shibCookieName = session[0][0]
  shibCookieValue = session[0][2]
  return shibCookieName, shibCookieValue

# Download a file
def downloadFile(shibCookieName, shibCookieValue, downloadUrl, downloadFileLocation):
  curlCommand = "curl --insecure --cookie " + shibCookieName + "=" + shibCookieValue + " " + downloadUrl + " -o " + downloadFileLocation
  shell(curlCommand)

# Log a user in and download a file.
# Example use case:
# ./download.py john.doe@innovateuk.test Passw0rd https://ifs-local-dev/management/competition/1/download /tmp/file.xlsx
# Note that repeated use in quick succession on various non developer environments will cause failure due to - most likely - to automatic lockup. 
def main():
  user = sys.argv[1] # e.g. john.doe@innovateuk.test
  password = sys.argv[2] # e.g. Passw0rd
  downloadUrl = sys.argv[3] # e.g. https://ifs-local-dev/management/competition/1/download
  downloadFileLocation = sys.argv[4] # e.g. /tmp/file.xlsx
  
  baseUrl = getBaseUrl(downloadUrl) 
  loginPostUrl, baseAuthUrl = getBaseAuthUrlAndPortUrl(baseUrl)
  sAMLResponse, relayState = postCredentialsAndGetShibbolethParameters(user, password, baseAuthUrl, loginPostUrl)
  shibCookieName, shibCookieValue = postShibbolethParametersForSession(sAMLResponse, relayState, baseUrl)
  downloadFile(shibCookieName, shibCookieValue, downloadUrl, downloadFileLocation)

if __name__ == '__main__':
  main()
