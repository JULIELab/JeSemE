SOURCE="/home/hellrich/tmp/jedisem"
TARGET="/home/hellrich/tmp/jedisem/jeseme_v2_import"
EMOPATH="/home/hellrich/JeSemE/pipeline/transform_models/notgit"
EN_EMO=$EMOPATH/Ratings_Warriner_et_al.csv
DE_EMO=$EMOPATH/13428_2013_426_MOESM1_ESM.xlsx  

(       x="coha"
        mkdir -p $TARGET/$x
        python vectors2similarity.py $TARGET/$x 10000 $EN_EMO en $SOURCE/coha50/{1830,1840,1850,1860,1870,1880,1890,1900,1910,1920,1930,1940,1950,1960,1970,1980,1990,2000} 
        echo "finished $x" )&
(       x="google_fiction"
        mkdir -p $TARGET/$x
        python vectors2similarity.py $TARGET/$x 10000 $EN_EMO en $SOURCE/$x/{1820,1830,1840,1850,1860,1870,1880,1890,1900,1910,1920,1930,1940,1950,1960,1970,1980,1990,2000} 
        echo "finished $x" ) &
(       x="rsc"
        mkdir -p $TARGET/$x
        python vectors2similarity.py $TARGET/$x 5000 $EN_EMO en $SOURCE/royal_society_corpus/models/{1750,1800,1850}
        echo "finished $x" ) &
(       x="dta"
        mkdir -p $TARGET/$x
        python vectors2similarity.py $TARGET/$x 10000 de $DE_EMO $SOURCE/dta50/{1751_1780,1781_1810,1811_1840,1841_1870,1871_1900} 
        echo "finished $x" ) &
(       x="google_german"
        mkdir -p $TARGET/$x
        python vectors2similarity.py $TARGET/$x 10000 de $DE_EMO $SOURCE/$x/{1830,1840,1850,1860,1870,1880,1890,1900,1910,1920,1930,1940,1950,1960,1970,1980,1990,2000} 
        echo "finished $x" ) &
wait
echo "done"