Please notice that the master branch might be unstable, preferably use a release from https://github.com/JULIELab/JeSemE/releases

# JeSemE
JeSemE (Jena Semantic Explorer) allows you to explore the semantic development of words over time based on distributional semantics. JeSemE is described in detail in our ACL 2017 paper ["Exploring Diachronic Lexical Semantics with JESEME"]( https://aclanthology.info/pdf/P/P17/P17-4006.pdf) and our COLING 2018 paper ["JeSemE: A Website for Exploring Diachronic Changes in Word Meaning and Emotion"](https://arxiv.org/abs/1807.04148)

# Dependencies
Modified version of Omar Levy's [hyperwords](https://github.com/hellrich/hyperwords)

# Starting JeSemE
* Use maven to build an executable JAR (with dependencies) by executing mvn package in the folder "website" 
* Configuration is done via config.yaml, you must set correct paths for your system!
* Requires a Postgres Server, enter details in config
* Mapping between words and lemmata (German only, fit for historic texts) via normalized.csv (mappingPath in config)
* Files with trained models and derived emotions can be found online on JeSemE's [help page](https://jeseme.coling.uni-jena.de/)
* Use the JAR to execute the commands "initialize" first (creates necessary tables), then "import" (takes some hours & approx 30 GB), and finally start JeSemE via "server"

# External Emotion Lexicons

* English: Warriner, A.B., Kuperman, V., & Brysbaert, M. (2013). Norms of valence, arousal, and dominance for 13,915 English lemmas. Behavior Research Methods, 45, 1191-1207. Available: http://crr.ugent.be/archives/1003

* German: Schmidtke, D. S., Schröder, T., Jacobs, A. M., & Conrad, M. (2014). ANGST: Affective norms for German sentiment terms, derived from the affective norms for English words. Behavior research methods, 46(4), 1108-1118. Available: https://link.springer.com/article/10.3758%2Fs13428-013-0426-y


