# appengine_config.py
from google.appengine.ext import vendor

print("in here")
# Add any libraries install in the "lib" folder.
vendor.add('lib')