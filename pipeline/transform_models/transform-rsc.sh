SOURCE="/home/hellrich/tmp/jedisem/royal_society_corpus/models"
TARGET="/home/hellrich/tmp/jedisem/import_me_rsc"

for x in royal_society_corpus
do 
	python vectors2similarity.py $TARGET/$x 5000 $SOURCE/$x/*
done
