#produces lemmata!
import json
import glob
import codecs
import sys
import os

if len(sys.argv) !=  3:
	raise Exception("Provide 2 arguments:\n\t1,Source directory with CAB responses\n\t2,Result file")
cab_files = sys.argv[1]
result_path = sys.argv[2]

with codecs.open(result_path, mode="w", buffering=10000, encoding="utf-8") as result:
	for cab_file in glob.glob(os.path.join(cab_files, "*")):
		with codecs.open(cab_file, mode="r", encoding="utf-8") as cab:
			for line in cab:
				if "\t" in line:
					word, analysis = line.split("\t")
					lemma = json.loads(analysis)["moot"]["lemma"] 
					if word.lower() != lemma.lower() and not ( ";" in word or ";" in lemma):
						result.write(word+";"+lemma+"\n")
