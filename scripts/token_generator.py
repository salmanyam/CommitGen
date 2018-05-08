#!/usr/bin/python

import sys
import pandas as pd
import numpy as np
import re
import string
import numpy as np
import matplotlib.pyplot as plt
from pylab import rcParams
import json

def main():
    # print command line arguments
    #for arg in sys.argv[1:]:
    #print sys.argv[1]
    
    #if len(sys.argv) < 4:
    #    print('usage: python token_generator.py <number of files> <file base name>'
    
    
    entries = dict()
    total_files = sys.argv[1]
    filename = sys.argv[3]


    for i in range(total_files):
        with open(filename + '-diffs-' + str(i) + '.txt') as f:
            count = 0
            is_rev = False
            is_msg = False
            is_diff = False
            msg_tokens = list()
            diff_tokens = list()

            revision = ''

            for line in f:
                #if count > 5:
                 #   break

                if line == '\n':
                    continue

                if 'revi####commit##gen####vt####se###spring####2018' in line:

                    if is_diff == True:
                        entries[count] = (revision, len(msg_tokens), len(diff_tokens))
                        #print(revision, len(msg_tokens), len(diff_tokens))

                        msg_tokens = list()
                        diff_tokens = list()
                        revision = ''

                        count += 1

                    is_rev = True
                    is_msg = False
                    is_diff = False

                    continue

                if 'comm####commit##gen####vt####se###spring####2018' in line:
                    is_rev = False
                    is_msg = True
                    is_diff = False
                    continue

                if 'diff####commit##gen####vt####se###spring####2018' in line:
                    is_rev = False
                    is_msg = False
                    is_diff = True
                    continue


                if is_rev == True:
                    revision = line.strip()
                elif is_msg == True:
                    msg_tokens.extend(re.findall(r"[^,.:;' ]+|[,.:;']", line.strip()))
                else:
                    diff_tokens.extend(re.findall(r"[^,.:;' ]+|[,.:;']", line.strip()))

    with open(filename + '-tokens.txt', 'w') as file:
        file.write(json.dumps(entries)) # use `json.loads` to do the reverse
    

if __name__ == "__main__":
    main()