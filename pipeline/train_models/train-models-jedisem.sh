#parameters as suggested by Hamilton et al., Diachronic Word Embeddings Reveal Statistical Laws of Semantic Change, ACL 2016
#assumes python 2 and correctly installed hyperwords with my customn extensions
HYPERWORD_PATH="/home/hellrich/hyperwords/omerlevy-hyperwords-688addd64ca2"
SMOOTHING="0.75"
DIM="500"
PATH="/home/hellrich/tmp/jedisem"

function copy_vocab {
	local path=$1
	local from_name=$2
	local to_name=$3

	cp $path/${from_name}.words.vocab $path/${to_name}.words.vocab
	cp $path/${from_name}.contexts.vocab $path/${to_name}.contexts.vocab
}

function train {
	local path=$1

	#CHI
	python $HYPERWORD_PATH/hyperwords/counts2chi.py -$path/counts $path/chi
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

function train_dta {
	_path=$PATH"/dta"
	for path in $_path/*
	do
		echo "$path"
		train $path
	done
	echo "finished dta"
}

function train_coha {
	_path=$PATH"/coha"
	for path in $_path/*
	do
		echo "$path"
		train $path
	done
	echo "finished coha"
}

train_dta & 
train_coha &