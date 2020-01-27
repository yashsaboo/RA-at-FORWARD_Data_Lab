#!/usr/bin/env python
# coding: utf-8

# In[1]:


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


# In[2]:


#Taken from https://www.geeksforgeeks.org/python-url-shortener-using-tinyurl-api/
#Returns the url subtracting the domain name with www and com stuff
#So, http://tinyurl.com/y5bffkh2 ---becomes---> y5bffkh2
def getTinyURL(URL): 
    request_url = ('http://tinyurl.com/api-create.php?' + urlencode({'url':URL}))     
    with contextlib.closing(urlopen(request_url)) as response:                       
        return response.read().decode('utf-8 ')[19:]


# In[4]:


# getTinyURL("URL")


# In[ ]:




