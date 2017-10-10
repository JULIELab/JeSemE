from docopt import docopt
import numpy as np
from collections import defaultdict
from representations.embedding import SVDEmbedding
from representations.explicit import PositiveExplicit
from os.path import join, basename, normpath
import pandas as pd


def main():
    args = docopt("""
    Usage:
        vectors2similarity.py <target> <limit> <folders>...
""")
    id2freq, ppmi, chi, svd_similarity = [], [], [], []
    folders = args["<folders>"]
    limit = int(args["<limit>"])

    word2id = read_vocabs(limit, folders)
    for folder in folders:
        name = basename(normpath(folder))
        if "_" in name:
            year = name.split("_")[1]
        else:
            year = name
        id2freq.append((year, read_freq(folder, word2id)))
        ppmi.append((year, read_generic(folder2ppmi(folder), word2id)))
        chi.append((year, read_generic(folder2chi(folder), word2id)))
        svd_similarity.append(
            (year, read_generic(folder2svd(folder), word2id, True)))
    store_results(args["<target>"], ("WORDIDS", iterate(word2id, is_word2id=True)), ("FREQUENCY", iterate(
        id2freq, is_frequency=True)), ("PPMI", iterate(ppmi)), ("CHI", iterate(chi)), ("SIMILARITY", iterate(svd_similarity)))



def emotion_induction(word2id, method, emotion_lexicon):
    """
    Generator providing the emotion value for each word in the word2id
    dictionary based on a modification of the Algorithm presented in Turney 
    and Littman (2002) (numerical seed values are expected instead of 
    positive/negative paradigm words).

    Args:
    word2id             Dictionary mapping from word to id.
    method              A function mapping from word1,word2 to similarity score.
    emotion_lexicon     Pandas data frame with columns Valence, Arousal, and
                        Dominance and words as index.

    Yields:
    4-Tuple comprising the id of the word and the Valence, Arousal, Dominance 
    scores.
    """
    for target in list(word2id):
    # def __turney_single_word__(target, method, emotion_lexicon):
    #     vad=np.array([.0,.0,.0])
    #     normalization=.0
    #     for word in lexicon.words():
    #         vad+=lexicon.get(entry)*embeddings.similarity(entry,targetWord)
    #         normalization += embeddings.similarity(entry, targetWord)
    #     return vad/normalization
        id=word2id[target]
        vad=np.array([.0,.0,.0])
        denominator=.0
        for entry in emotion_lexicon.index:
            vad+=emotion_lexicon.loc[entry]*method(entry,target)
            denominator+=method(entry, target)
        vad=vad/denominator
        yield tuple([id]+list(vad))
    




def iterate(mapping, is_word2id=False, is_frequency=False):
    if is_word2id and is_frequency:
        raise Exception("Not allowed")
    elif is_word2id:
        for word, value in mapping.items():
            yield [str(x) for x in word, value]
    elif is_frequency:
        for year, info in mapping:
            for word, freq in info.items():
                yield [str(x) for x in word, year, freq]
    else:
        for year, generator in mapping:
            for word, word2, metric in generator:
                yield [str(x) for x in word, word2, year, metric]


def store_results(path, *mappings):
    for name, mapping in mappings:
        with open(join(path, name + ".csv"), "w") as f:
            for l in mapping:
                print >>f, ",".join(l)


def folder2ppmi(folder):
    return PositiveExplicit(join(folder, "pmi")).similarity_first_order


def folder2chi(folder):
    return PositiveExplicit(join(folder, "chi")).similarity_first_order


def folder2chi(folder):
    return PositiveExplicit(join(folder, "chi")).similarity_first_order


def folder2svd(folder):
    return SVDEmbedding(join(folder, "svd_pmi")).similarity


def folder2vocab(folder):
    return join(folder, "counts.words.vocab")


def intersect(words):
    if len(words) == 0:
        return set()
    if len(words) == 1:
        return words[0]
    intersection = words[0]
    for w in words[1:]:
        intersection = intersection.intersection(w)
    return intersection


def read_vocabs(limit, folders):
    words = []
    for vocab in [folder2vocab(folder) for folder in folders]:
        w = set()
        i = 0
        with open(vocab, "r") as v:
            for line in v:
                if i == limit:
                    break
                i += 1
                word, freq = line.strip().split()
                w.add(word)
        words.append(w)
    return {y: x for x, y in enumerate(intersect(words))}


def read_freq(folder, word2id):
    id2freq = {}
    total = 0
    with open(folder2vocab(folder), "r") as v:
        for line in v:
            word, freq = line.strip().split()
            if word in word2id:
                id2freq[word2id[word]] = float(freq)
            total += float(freq)
    total = total / 100
    for i, f in id2freq.items():
        id2freq[i] = f / total
    return id2freq


def read_generic(method, word2id, remove_duplicates=False):
    mapping = defaultdict(dict)
    for word1, id1 in word2id.items():
        for word2, id2 in word2id.items():
            if word1 != word2:
                if not remove_duplicates or (remove_duplicates and id1 < id2):
                    metric = method(word1, word2)
                    if metric != 0.0:
                        yield id1, id2, metric


if __name__ == "__main__":
    main()
