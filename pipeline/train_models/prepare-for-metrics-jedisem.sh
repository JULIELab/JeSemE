##parameters as suggested by Hamilton et al., Diachronic Word Embeddings Reveal Statistical Laws of Semantic Change, ACL 2016
#assumes python 2 and correctly installed hyperwords with my customn extensions
HYPERWORD_PATH="/home/hellrich/hyperwords/omerlevy-hyperwords-688addd64ca2"
CONTEXT_WINDOW=4
TARGET="/home/hellrich/tmp/jedisem"
SOURCE="/home/hellrich/historical-emotion"

function prepare {
	local source_path=$1
	local target_path=$2
	local lowercase=$3
	
	# (lowercase) , remove non alphanumeric, replace multi space, remove empty lines
	sedstring="s/[^[:alnum:]]*[ \t\n\r][^[:alnum:]]*/ /g;s/[^a-z0-9]*$/ /g;s/  */ /g;/^\s*$/d"
	if [[ "$lowercase" == "lowercase" ]] ; then
		sedstring="s/[[:upper:]]*/\L&/g;"$sedstring
	fi

	rm -f $target_path/*	
	sed sedstring < $source_path > $target_path/clean
	python $HYPERWORD_PATH/hyperwords/corpus2counts.py --win $CONTEXT_WINDOW $target_path/clean > $target_path/counts
}

function prepare_dta {
	_target=$TARGET"/dta"
	_source=$SOURCE"/dta/jedisem"
	for source_path in $_source/*_*
	do
		echo "$source_path"
		name=$(basename $source_path)
		target_path=$_target/$name
		mkdir -p $target_path
		prepare $source_path $target_path "keep case"
	done
	echo "finished DTA"
}

function prepare_coha {
	_target=$TARGET"/coha"
	_source=$SOURCE"/coha/decades"
	for source_path in $_source/${x}*0
	do
		echo "$source_path"
		name=$(basename $source_path)
		target_path=$_target/$name
		mkdir -p $target_path
		prepare $source_path $target_path "lowercase"
	done
	echo "finished COHA"
}


prepare_dta & 
prepare_coha &