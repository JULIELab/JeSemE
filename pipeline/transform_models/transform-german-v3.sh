SOURCE="/home/hellrich/tmp/jedisem"
TARGET="/home/hellrich/tmp/jedisem/jeseme_v3_import"
EMOPATH="/home/hellrich/oldJeseme/pipeline/transform_models/notgit/"
EN_EMO=$EMOPATH/Ratings_Warriner_et_al.csv
DE_EMO=$EMOPATH/13428_2013_426_MOESM1_ESM.xlsx  

function scale {
        local target=$1
        python scale_emotion_data.py $target/EMOTION.csv
        mv $target/EMOTION.csv $target/EMOTION_raw.csv #did not work in last run due to typo :/
        mv $target/EMOTION_normalized.csv $target/EMOTION.csv
}

#processing
(
	x="dta"
	mkdir -p $TARGET/$x
	python vectors2similarity.py $TARGET/$x 10000 $DE_EMO de $SOURCE/dta_2017/{1751_1780,1781_1810,1811_1840,1841_1870,1871_1900} 
	scale $TARGET/$x
	echo "finished $x"
)&
(       x="google_german"
        mkdir -p $TARGET/$x
        python vectors2similarity.py --exceptions=Romantik $TARGET/$x 10000 $DE_EMO de $SOURCE/google_german_lemmata/{1830,1840,1850,1860,1870,1880,1890,1900,1910,1920,1930,1940,1950,1960,1970,1980,1990,2000} 
	scale $TARGET/$x       
	echo "finished $x" 
)&
wait

echo "done"
