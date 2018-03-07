SOURCE="/home/hellrich/tmp/jedisem"
TARGET="/home/hellrich/tmp/jedisem/jeseme_v2_import"
EMOPATH="/home/hellrich/JeSemE/pipeline/transform_models/notgit"
EN_EMO=$EMOPATH/Ratings_Warriner_et_al.csv
DE_EMO=$EMOPATH/13428_2013_426_MOESM1_ESM.xlsx  

x="dta"
mkdir -p $TARGET/$x
python vectors2similarity.py $TARGET/$x 10000 $DE_EMO de $SOURCE/dta50/{1751_1780,1781_1810,1811_1840,1841_1870,1871_1900} 
echo "done"

./scale_emotion.sh
