#!/usr/bin/python

#import simplejson as json
import json

import urllib
import sys

# fetch article of title live from wikipedia en
def doit(txt):
    o = json.loads(txt)
    #print o
    print json.dumps(o, sort_keys=True, indent=4)

def main():
    if len(sys.argv[1:]) != 1:
        sys.stderr.write("Usage: " + sys.argv[0] + " file\n")
        sys.exit(-1)

    f = open(sys.argv[1], 'r')

    for x in f:
        doit(x)

if __name__ == '__main__':
    main()
