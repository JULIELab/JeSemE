import sys

start=sys.argv[3]
end=sys.argv[4]

word2id = {}
with open(sys.argv[2]) as f:
 for line in f:
  if "," in line:
   w_string, w_id = line.strip().split(",")
   word2id[w_id] = w_string

word2year2embedding={}
with open(sys.argv[1]) as f:
 for line in f:
  if "," in line:
   line = line.strip().split(",")
   w=line[0]
   y=line[1]
   d=line[2].split(" ")
   if not w in word2year2embedding:
    word2year2embedding[w] = {}
   word2year2embedding[w][y] = d


word2sim = {}
for w in word2year2embedding:
 embedding1 = word2year2embedding[w][start]
 embedding2 = word2year2embedding[w][end]
 sim = sum([float(embedding1[i]) * float(embedding2[i]) for i in range(len(embedding1))])
 word2sim[w] = sim


words_by_sim = sorted(word2sim.items(), key=lambda kv: kv[1])

print("hoch")
for w,s in words_by_sim[:10]:
 print(word2id[w],s)

print("gering")
for w,s in words_by_sim[-10:]:
 print(word2id[w],s)


