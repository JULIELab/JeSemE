###parameters as suggested by Hamilton et al., Diachronic Word Embeddings Reveal Statistical Laws of Semantic Change, ACL 2016
#assumes python 2 and correctly installed hyperwords with my customn extensions
# 
#seperate processing of google books in a version only lemmatized yet not lowercased (error in original implementation)
HYPERWORD_PATH="/home/hellrich/hyperwords/omerlevy-hyperwords-688addd64ca2/hyperwords"
SMOOTHING="0.75"
DIM="500"
CONTEXT_WINDOW=4
TARGET="/home/hellrich/tmp/jedisem/google_german_lemmata"
SOURCE="/data/data_hellrich/google_books_parts/lemmata/german/"




function prepare {
	rm -rf $TARGET/*
	mkdir -p $TARGET
	python $HYPERWORD_PATH/google_books_parts2counts.py --win $CONTEXT_WINDOW $SOURCE $TARGET 1730 2009
}

function copy_vocab {
	local path=$1
	local from_name=$2
	local to_name=$3

	cp $path/${from_name}.words.vocab $path/${to_name}.words.vocab
	cp $path/${from_name}.contexts.vocab $path/${to_name}.contexts.vocab
}

function train {
	local path=$1

	#vocab
	python $HYPERWORD_PATH/counts2vocab.py $path/counts

	#CHI
	python $HYPERWORD_PATH/counts2chi.py $path/counts $path/chi
	echo "finished $path chi1"

	#CHI SMOOTHED
	python $HYPERWORD_PATH/counts2chi.py --cds $SMOOTHING $path/counts $path/chi_smooth
	echo "finished $path chi2"

	#PMI
	python $HYPERWORD_PATH/counts2pmi.py --cds $SMOOTHING $path/counts $path/pmi
	echo "finished $path pmi"

	#PMI SVD
	python $HYPERWORD_PATH/pmi2svd.py --dim $DIM $path/pmi $path/svd_pmi
	copy_vocab $path pmi svd_pmi
	echo "finished $path svd"
}

prepare
for path in $TARGET/*
do
	echo "$path"
	train $path
done
echo "finished"



