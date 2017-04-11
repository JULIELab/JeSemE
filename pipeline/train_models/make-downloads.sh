cd /home/hellrich/tmp/jedisem
mkdir -p downloads
rm downloads/*

for x in coha50 dta50 google_fiction google_german_lemmata royal_society_corpus/models
	do zip -q downloads/$x.zip $x/*/{chi,pmi,svd_pmi}.* &
done

wait
echo "done"
