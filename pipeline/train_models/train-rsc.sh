###parameters as suggested by Hamilton et al., Diachronic Word Embeddings Reveal Statistical Laws of Semantic Change, ACL 2016
#assumes python 2 and correctly installed hyperwords with my customn extensions
HYPERWORD_PATH="/home/hellrich/hyperwords/omerlevy-hyperwords-688addd64ca2"
SMOOTHING="0.75"
DIM="500"
CONTEXT_WINDOW=4
TARGET="/home/hellrich/tmp/jedisem/royal_society_corpus/models"
SOURCE="/home/hellrich/tmp/jedisem/royal_society_corpus/raw"

function prepare {
	local source_path=$1
	local target_path=$2
	local lowercase=$3
	rm -f $target_path/*			
	
	# (lowercase) , remove non alphanumeric, replace multi space, remove empty lines
	if [[ "$lowercase" == "lowercase" ]] ; then
		sed "s/[[:upper:]]*/\L&/g;s/[^[:alnum:]]*[ \t\n\r][^[:alnum:]]*/ /g;s/[^a-z0-9]*$/ /g;s/  */ /g;/^\s*$/d" < $source_path > $target_path/clean
	else
		sed "s/[^[:alnum:]]*[ \t\n\r][^[:alnum:]]*/ /g;s/[^a-z0-9]*$/ /g;s/  */ /g;/^\s*$/d" < $source_path > $target_path/clean
	fi

	python $HYPERWORD_PATH/hyperwords/corpus2counts.py --win $CONTEXT_WINDOW $target_path/clean > $target_path/counts
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
	python $HYPERWORD_PATH/hyperwords/counts2vocab.py $path/counts

	#CHI
	python $HYPERWORD_PATH/hyperwords/counts2chi.py $path/counts $path/chi
	echo "finished $path chi1"

	#CHI SMOOTHED
	python $HYPERWORD_PATH/hyperwords/counts2chi.py --cds $SMOOTHING $path/counts $path/chi_smooth
	echo "finished $path chi2"

	#PMI
	python $HYPERWORD_PATH/hyperwords/counts2pmi.py --cds $SMOOTHING $path/counts $path/pmi
	echo "finished $path pmi"

	#PMI SVD
	python $HYPERWORD_PATH/hyperwords/pmi2svd.py --dim $DIM $path/pmi $path/svd_pmi
	copy_vocab $path pmi svd_pmi
	echo "finished $path svd"
}



for source_path in $SOURCE/*
do
	echo "$source_path"
	name=$(basename $source_path)
	target_path=$TARGET/$name
	mkdir -p $target_path
	(
	prepare $source_path $target_path "lowercase"
	train $target_path
	echo "finished $name" 
	) &
done
wait
echo "done"
