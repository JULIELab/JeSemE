import glob
import gzip
import codecs
import re
import sys
import os

with_pos = False


if len(sys.argv) !=  3:
	raise Exception("Provide 2 arguments:\n\t1,Source directory with raw corpus\n\t2,Result file")
directory = sys.argv[1]
result_path = sys.argv[2]

tokens = set()
for gziped in glob.glob(os.path.join(directory, "googlebooks-*-5gram-20120701-*.gz")):
	print("Processing "+gziped)
	with gzip.open(gziped, 'rb') as unpacked:
		reader = codecs.getreader("utf-8")
		for line in reader(unpacked):
			text, year, match_count, volume_count = line.split("\t")
			has_pos = "_" in text
			if (with_pos and has_pos) or (not with_pos and not has_pos):
				for token in text.split(" "):
					tokens.add(token)

with codecs.open(result_path, mode="w", buffering=10000, encoding="utf-8") as result:
	for token in tokens:
		result.write(token+"\n")