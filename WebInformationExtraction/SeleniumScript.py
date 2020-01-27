#!/usr/bin/env python
# coding: utf-8

# In[2]:


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
import json


# In[3]:


fireFoxBinaryPath = r'C:\Program Files (x86)\Mozilla Firefox\firefox.exe'
geckoDriverPath = "C:/Users/Yash/Documents/geckodriver.exe"

googleChomreDriverPath = r"C:\Users\Yash\Documents\chromedriver.exe"

windowSizeXAxis = 1024
windowSizeYAxis = 768


# In[4]:


# Uses Firefox to launch Selenium Headlessly: One needs to have:
# firefox binary path(located in Program Files folder(Windows)) and 
# geckoDriver, which acts as an intermediate between selenium and firefox, and can download it from: https://github.com/mozilla/geckodriver/releases
def getSeleniumDriverForFirefox(fireFoxBinaryPath, geckoDriverPath, windowSizeXAxis, windowSizeYAxis):
    options = Options()
    options.set_headless(headless=True)
    options.binary = binary
    options.add_argument('--ignore-certificate-errors')
    
    cap = DesiredCapabilities().FIREFOX
    cap["marionette"] = True #optional
    
    driver = webdriver.Firefox(firefox_options=options, capabilities=cap, executable_path=geckodriver)
    driver.set_window_size(windowSizeXAxis, windowSizeYAxis) # set the window size that you need 
    return driver

def getSeleniumDriverForChrome(googleChomreDriverPath, headlessOrNot):
    options = webdriver.ChromeOptions()
    options.headless = headlessOrNot

    driver = webdriver.Chrome(options=options, executable_path=googleChomreDriverPath)
    return driver

# Saves the screenshot with the last 20 characters of the URL as the file name
# Firefox doesn't properly returns the whole page's screenshot, so use Chrome
def takeScreenshotWithFirefox(URL):
    driver2 = getSeleniumDriverForChrome(googleChomreDriverPath)
    driver2.get(URL)
    driver2.save_screenshot("screenshots/" + getTinyURL(URL)+".png")
    driver2.quit()
    
def takeScreenshotWithChrome(URL, driver):
    driver.get(URL)

    S = lambda X: driver.execute_script('return document.body.parentNode.scroll'+X)
    driver.set_window_size(S('Width'),S('Height')) # May need manual adjustment
    driver.find_element_by_tag_name('body').screenshot("screenshots/" + getTinyURL(URL)+".png")


# In[8]:


# URL = "https://www.ebay.com/itm/Apple-iPhone-XR-64GB-Factory-Unlocked-Smartphone-4G-LTE-iOS-Smartphone/143200193783?var=442132772989&_trkparms=%26rpp_cid%3D5da6205bfe6ec928a2999518%26rpp_icid%3D5da6205bfe6ec928a2999517&_trkparms=pageci%3Afd7f6834-f568-11e9-86d0-74dbd180c416%7Cparentrq%3Af7934d2616d0a9cb8b4f0daffffc86e1%7Ciid%3A1&frcectupt=true"


# In[7]:


# #Get Selenium Object for that XPath
# driver2 = getSeleniumDriverForChrome(googleChomreDriverPath, False)
# driver2.get(URL)
# # driver2.implicitly_wait(10)
# driver2.maximize_window()


# In[ ]:




