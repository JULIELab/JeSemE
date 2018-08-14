import glob
import gzip
import codecs
import re
import sys
import os

with_pos = False

targets = {}
my_buffer = {}

def flush(a_buffer, some_targets, a_year):
        for line in a_buffer[a_year]:
                some_targets[a_year].write(line)
        a_buffer[a_year].clear()


if len(sys.argv) !=  3:
        raise Exception("Provide 2 arguments:\n\t1,Source directory with raw corpus\n\t2,Target directory for transformed corpus")
directory = sys.argv[1]
target = sys.argv[2]
if not os.path.exists(target):
        os.makedirs(target)
for gziped in glob.glob(os.path.join(directory, "googlebooks-*-5gram-20120701-*.gz")):
        print("Processing "+gziped)
        with gzip.open(gziped, 'rb') as unpacked:
                reader = codecs.getreader("utf-8")
                for line in reader(unpacked):
                        text, year, match_count, volume_count = line.split("\t")
                        has_pos = "_" in text
                        if (with_pos and has_pos) or (not with_pos and not has_pos):
                                if year not in targets:
                                        targets[year] = open(os.path.join(target,year),"w",encoding="utf-8") 
                                        my_buffer[year] = []
                                elif len(my_buffer[year]) > 10000:
                                        flush(my_buffer, targets, year)
                                my_buffer[year].append(line)

for year in targets: 
        flush(my_buffer, targets, year)
        targets[year].close()
