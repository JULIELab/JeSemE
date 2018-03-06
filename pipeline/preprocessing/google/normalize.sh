function getTokens {
	python gather_tokens.py $corpus $token_path
	echo "Gathered unique tokens: $(wc -l $token_path)"
}

function query {
	mkdir -p $split_path $normalized_path
	cd $split_path
	split --line-bytes 512k $token_path
	for split in *
	do
		echo "Querying $split"
		curl -sSfF "qd=<$split" "http://www.deutschestextarchiv.de/demo/cab/query?a=norm1&fmt=tj" > $normalized_path/$split
		sleep 2
	done
	echo "All queried"
	cd -
}

function normalize {
	python parse_normalized.py $normalized_path $path/normalized.csv
	echo "Normalized"
}

#parsing paths
if (( $# != 2 )); then
    echo "Provide exactly 2 arguments:"
    echo "	1, Path to gziped corpus"
    echo "	2, Path to store results in"
    exit 1
fi

corpus=$(cd $1; pwd) #get absolute path if necessary

mkdir -p $2
path=$(cd $2; pwd) #get absolute path if necessary

token_path="$path/tokens"
split_path="$path/splits"
normalized_path="$path/normalized"


#main
source activate gensimtest
getTokens
query
normalize
source deactivate