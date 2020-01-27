#!/usr/bin/env python
# coding: utf-8

# ## Libraries

# In[10]:


#Import All Dependencies
# import cv2, os, bz2, json, csv, difflib, requests, socket, whois, urllib.request, urllib.parse, urllib.error, re, OpenSSL, ssl
import numpy as np
from datetime import datetime
from urllib.parse import urlparse
from urllib.request import Request, urlopen
# from selenium import webdriver
from matplotlib import pyplot as plt
from bs4 import BeautifulSoup
# from timeout import timeout
import requests
import numpy as np
import urllib
import cv2
import re
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.firefox.options import Options
from PIL import Image
from io import BytesIO
import time
import os
import os.path
from os import path
import io
from difflib import SequenceMatcher


# In[11]:


import contextlib 
  
try: 
    from urllib.parse import urlencode           
except ImportError: 
    from urllib import urlencode 
try: 
    from urllib.request import urlopen 
except ImportError: 
    from urllib2 import urlopen 

import sys 


# ### TinyURL

# In[12]:


#Taken from https://www.geeksforgeeks.org/python-url-shortener-using-tinyurl-api/
#Returns the url subtracting the domain name with www and com stuff
#So, http://tinyurl.com/y5bffkh2 ---becomes---> y5bffkh2
def getTinyURL(URL): 
    request_url = ('http://tinyurl.com/api-create.php?' + urlencode({'url':URL}))     
    with contextlib.closing(urlopen(request_url)) as response:                       
        return response.read().decode('utf-8 ')[19:]


# In[13]:


#Returns Beautiful Soup object
def getHTML(URL):
    try:
        hdr = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36'} #Make the user agent verified, that is Mozilla
#         req = Request(URL,headers=hdr)
        req = requests.get(URL, headers=hdr)
        page = req.text #Get URL HTML contents
        soup = BeautifulSoup(page, 'html.parser') #Convert to BeutifulSoup
        print("Built Soup")
        #prettyText = str(soup.prettify()) #Convert the HTML in its form
        return soup
    except Exception as e:
# #         if e.__class__.__name__ == "TimeoutError": raise TimeoutError("")
        return None


# ### XPath

# In[14]:


#https://selenium-python.readthedocs.io/locating-elements.html#locating-by-xpath
#XPath can either id or name relative
def getMyXPath(currentTag): #Original XPath
    returnXPath = ""
    while(currentTag.parent!=None):
        if "id" in (currentTag.attrs):
            print("true")
            returnXPath = currentTag.name + "[@id='" + currentTag.attrs['id'].strip() + "']/" + returnXPath #//form[@id='loginForm']
            break
        if "name" in (currentTag.attrs):
            print("true")
            returnXPath = currentTag.name + "[@name='" + currentTag.attrs['name'].strip() + "']/" + returnXPath #//form[@id='loginForm']
            break 
        returnXPath = currentTag.name + "/" + returnXPath
        currentTag = currentTag.parent
        print(currentTag.attrs)
        
        
    returnXPath = returnXPath.replace("[document]/html","/") #When it reaches the end of document parent, it adds the following it it's start, so we need to delete it
    if not returnXPath.startswith("//"): #the XPath should start with 2 forward slash
        returnXPath = "//" + returnXPath
    # if not returnXPath.startswith("/"): #the XPath should start with 1 forward slash: Update
    #     returnXPath = "/" + returnXPath
    if returnXPath.endswith("/"): #the XPath should not end with forward slash
        returnXPath = returnXPath[:-1]
    
    returnXPath = returnXPath.replace("meta/","").replace("table/tr", "table/tbody/tr") #Few changes to be made while performing XPath
    return (returnXPath) #//div[@id='tab-panel-0-w3']/div/span/h2


# In[ ]:


import itertools

def getStackXPath(element): #XPath code from Stack Overflow
    """
    Generate xpath of soup element
    :param element: bs4 text or node
    :return: xpath as string
    """
    components = []
    child = element if element.name else element.parent
    for parent in child.parents:
        """
        @type parent: bs4.element.Tag
        """
        previous = itertools.islice(parent.children, 0, parent.contents.index(child))
        xpath_tag = child.name
        xpath_index = sum(1 for i in previous if i.name == xpath_tag) + 1
        components.append(xpath_tag if xpath_index == 1 else '%s[%d]' % (xpath_tag, xpath_index))
        child = parent
    components.reverse()
    return '/%s' % '/'.join(components)


# In[54]:


def getMyStackXPath(element): #My XPath code modified with Stack Overflow's one
    """
    Generate xpath of soup element
    :param element: bs4 text or node
    :return: xpath as string
    """
    components = []
    child = element if element.name else element.parent
    for parent in child.parents:
        """
        @type parent: bs4.element.Tag
        """
        
        if "id" in (child.attrs):
            print("true")
            components.append(child.name + "[@id='" + child.attrs['id'].strip() + "']")    #//form[@id='loginForm']
            break
        if "name" in (child.attrs):
            print("true")
            components.append(child.name + "[@name='" + child.attrs['name'].strip() + "']") #//form[@id='loginForm']
            break 
        
        previous = itertools.islice(parent.children, 0, parent.contents.index(child))
        xpath_tag = child.name
        xpath_index = sum(1 for i in previous if i.name == xpath_tag) + 1
        components.append(xpath_tag if xpath_index == 1 else '%s[%d]' % (xpath_tag, xpath_index))
        print("xpath_tag:",xpath_tag)
        print("xpath_tag.attrs:",child.attrs)
        print("xpath_index:",xpath_index)
        print("components:",components)
        child = parent
    components.reverse()
    return '/%s' % '/'.join(components)


# #### Price

# In[55]:


def getTheTagElementForPrice(gShopPriceUpdated, soup):
    returnPriceElementTag = None
    try:
        dummyVar = soup(text=re.compile(gShopPriceUpdated))
    #     print(dummyVar)
        for elem in dummyVar:
#             print("elem.parent",elem.parent)
#             print("elem.parent.name",elem.parent.name)
            if returnPriceElementTag == None: #The first element is the return value unless we encounter a heading tag
                returnPriceElementTag = elem.parent
            if "h" in elem.parent.name: #Found the heading tag, so return this tag and break the loop
                returnPriceElementTag = elem.parent
                return returnPriceElementTag
            if "span" in elem.parent.name: #Found the span tag, so return this tag and break the loop
                returnPriceElementTag = elem.parent
                return returnPriceElementTag
    except Exception as e:
        print("Error in getTheTagElementForPrice(gShopPriceUpdated, soup)")
    return returnPriceElementTag

def findPriceElementTag(gShopPrice, soup): #gShopPrice = $379.00
    gShopPrice = gShopPrice.replace("now","").strip() #GShop soemtimes gives prices with now suffix like "$0.00 now"
    print(gShopPrice)
    
    if "$" in gShopPrice:
        gShopPriceUpdated = gShopPrice.replace("$", "\$") #gShopPriceUpdated = \$379.00; Required because $ is reserved keyword for regex
        print(gShopPriceUpdated)
        returnPriceElementTag = getTheTagElementForPrice(gShopPriceUpdated, soup)
        if returnPriceElementTag != None and len(str(returnPriceElementTag))<400:
            return returnPriceElementTag
    
    gShopPriceUpdated = gShopPrice.replace("$","") #gShopPriceUpdated = 379.00
    print(gShopPriceUpdated)
    returnPriceElementTag = getTheTagElementForPrice(gShopPriceUpdated, soup)
    if returnPriceElementTag != None and len(str(returnPriceElementTag))<400:
        return returnPriceElementTag
        
    gShopPriceUpdated = gShopPrice.replace("$", "\$").split(".")[0] #gShopPriceUpdated = \$379
    print(gShopPriceUpdated)
    returnPriceElementTag = getTheTagElementForPrice(gShopPriceUpdated, soup)
    if returnPriceElementTag != None and len(str(returnPriceElementTag))<400:
        return returnPriceElementTag
        
    gShopPriceUpdated = gShopPrice.replace("$","").split(".")[0] #gShopPriceUpdated = 379
    print(gShopPriceUpdated)
    returnPriceElementTag = getTheTagElementForPrice(gShopPriceUpdated, soup)
    if returnPriceElementTag != None and len(str(returnPriceElementTag))<400:
        return returnPriceElementTag
        
    return None


# ### Testing

# In[56]:


# Unit Testing for XPath


# In[61]:


URL = "https://www.walmart.com/ip/Farberware-3-2-Quart-Digital-Oil-Less-Fryer-White/264698854?athcpid=264698854&athpgid=athenaHomepage&athcgid=null&athznid=BestInDeals&athieid=v1&athstid=CS020&athguid=466001f5-46cfa622-5eb821569a18a716&athancid=null&athena=true"


# In[62]:


priceOfCurrentScreenshot = "39"


# In[63]:


# soup = getHTML(URL)
# returnPriceElementTag = findPriceElementTag(priceOfCurrentScreenshot, soup)


# In[64]:


# getMyStackXPath(returnPriceElementTag)


# In[ ]:




