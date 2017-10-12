import pandas as pd
import codecs


# function that rescales data
def scaleInRange(x, oldmin, oldmax, newmin, newmax):
    # linear scaling (see koeper 2016). (Softmax makes no sense)
    return ((newmax - newmin) * (x - oldmin)) / (oldmax - oldmin) + newmin


def load_english(path):
    '''
    Warriner et al. 2013
    '''
    warriner13 = pd.read_csv(path, sep=',')
    warriner13 = warriner13[['Word', 'V.Mean.Sum', 'A.Mean.Sum', 'D.Mean.Sum']]
    warriner13.columns = ['Word', 'Valence', 'Arousal', 'Dominance']
    warriner13.set_index('Word', inplace=True)
    # print(warriner13.head())
    # print(warriner13.shape)
    return warriner13


def load_german(path):
    '''
    Schmidtke et al., 2014
    '''

    lemmata_mapping = _read_lemmata_mapping_file()

    schmidtke14 = pd.read_excel(path)
    # schmidtke14=schmidtke14[['Word','Valence','Arousal','Dominance']]
    schmidtke14 = schmidtke14[
        ['G-word', 'VAL_Mean', 'ARO_Mean_(ANEW)', 'DOM_Mean']]
    schmidtke14.columns = ['Word', 'Valence', 'Arousal', 'Dominance']
    schmidtke14['Word'].replace(lemmata_mapping, inplace=True)
    schmidtke14.set_index('Word', inplace=True)
    schmidtke14 = schmidtke14[~schmidtke14.index.duplicated(keep='first')]
    # rescaling valence

    schmidtke14.Valence = [scaleInRange(x=x, oldmin=-3.,
                                        oldmax=3., newmin=1., newmax=9.)
                           for x in schmidtke14.Valence]

    # setting word column to lower case for compatiblity with briesemeister11
    # print(schmidtke14.head())
    # print(schmidtke14.shape)
    return schmidtke14


def _read_lemmata_mapping_file(lemmata_mapping_file="german-dta-normalization.csv"):
    mapping = {}
    with codecs.open(lemmata_mapping_file, "r", "utf-8") as f:
        for line in f:
            word, lemma = line.strip().split(";")
            mapping[word] = lemma
    return mapping
