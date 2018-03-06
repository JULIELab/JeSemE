#Assumes presence of https://github.com/JULIELab/dta-converter
JAR="dta-converter-1.4-jar-with-dependencies.jar"
name="dta_kernkorpus_2017-09-01"
wget http://media.dwds.de/dta/download/"$name"_tcf.zip 
unzip "$name"_tcf.zip
java -jar $JAR -i "$name"/full -o "$name"_lemma -m "$name"_meta.csv -l #lemmatized

function subcorpus {
        local from=$1
        local step=$2
        local to=$3
        local target=$4
        rm -rf $target/*
        mkdir -p $target
        for x in $(seq $from $step $to)
        do
                y=$(($x+$step-1))
                rm -f $target/${x}_$y
                for year in $(seq $x $y)
                do
                        for f in "$name"_lemma/*_$year
                        do
                                tr '\n' ' ' < $f >> $target/${x}_$y
                                echo "" >> $target/${x}_$y
                        done
                done
        done
        wc -w $target/*_*
}

subcorpus 1751 30 1900 jeseme_dta_corpus #earlier stays below 10M
