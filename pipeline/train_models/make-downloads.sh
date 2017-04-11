cd /home/hellrich/tmp/jedisem
mkdir -p downloads
rm -f downloads/*

for x in coha50 dta50 google_fiction google_german_lemmata royal_society_corpus/models
do
	(
		name=${x%/models}
		zip -q downloads/$name.zip $x/*/{chi,pmi,svd_pmi}.*
		echo "finished $name"
	)&
done
wait

cd downloads
mv coha50.zip coha.zip
mv dta50.zip dta.zip
mv google_german_lemmata.zip google_german.zip
mv royal_society_corpus.zip rsc.zip
echo "done"
