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
echo "done"
