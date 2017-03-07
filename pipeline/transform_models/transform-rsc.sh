SOURCE="/home/hellrich/tmp/jedisem/royal_society_corpus/models"
TARGET="/home/hellrich/tmp/jedisem/import_me_rsc/rsc"

python vectors2similarity.py $TARGET 5000 $SOURCE/*
