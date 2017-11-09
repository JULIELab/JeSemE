TARGET="/home/hellrich/tmp/jedisem/jeseme_v2_import"

function scale {
        local target=$1
        python scale_emotion_data.py $target/EMOTION.csv
        mv $target/EMOTION.csv $target/EMOTION_raw.csv #did not work in last run due to typo :/
        mv $target/EMOTION_normalized.csv $target/EMOTION.csv
}

for x in "coha" "google_fiction" "rsc" "dta" "google_german"
do
        scale $TARGET/$x 
        echo "finished $x"
done
echo "done"
