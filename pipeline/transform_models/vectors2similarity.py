from docopt import docopt
import numpy as np
from collections import defaultdict
from representations.embedding import SVDEmbedding, Embedding
from representations.explicit import PositiveExplicit
from os.path import join, basename, normpath
import pandas as pd
import emotion_lexicons


def main():
    args = docopt("""
    Usage:
        vectors2similarity.py <target> <limit> <emotion_lexicon_path> <emotion_language> <folders>...
""")

    emotion_lexicon = load_emotions(
        args["<emotion_lexicon_path>"], args["<emotion_language>"])

    id2freq, ppmi, chi, svd_similar, emotions, embeddings = [], [], [], [], [], []
    folders = args["<folders>"]
    limit = int(args["<limit>"])

    word2id = read_vocabs(limit, folders)
    for year, folder in iterate_folder(folders):
        id2freq.append((year, read_freq(folder, word2id)))
        ppmi.append((year, read_generic(folder2ppmi(folder), word2id)))
        chi.append((year, read_generic(folder2chi(folder), word2id)))
        svd_similar.append((year, read_top(folder2svd(folder), word2id)))
        emotions.append(
            (year, emotion_induction(word2id, folder2svd(folder, True), emotion_lexicon)))
    # embeddings with alignment
    years_and_folders = [x for x in iterate_folder(folders)][::-1]  # reverse
    year, folder = years_and_folders[0]
    base_embed = folder2svd(folder, True)
    embeddings.append((year, get_embeddings(base_embed, word2id)))
    for year, folder in years_and_folders[1:]:
        other_embed = smart_procrustes_align(
            base_embed, folder2svd(folder, True))
        embeddings.append((year, get_embeddings(other_embed, word2id)))

    store_results(args["<target>"], ("WORDIDS", word2id), ("FREQUENCY", id2freq),
                  ("PPMI", ppmi), ("CHI", chi), ("SIMILARITY", svd_similar), ("EMOTION", emotions), ("EMBEDDING", embeddings))


def load_emotions(emotion_lexicon_path, emotion_language):
    if emotion_language.lower() == "en":
        return emotion_lexicons.load_english(emotion_lexicon_path)
    elif emotion_language.lower() == "de":
        return emotion_lexicons.load_german(emotion_lexicon_path)
    else:
        raise Exception("Not supported: " + emotion_language)


def get_embeddings(embeddings, word2id):
    for word, _id in word2id.items():
        if word in embeddings.wi:
            yield _id, " ".join([str(x) for x in embeddings.represent(word)])


def iterate_folder(folders):
    for folder in folders:
        name = basename(normpath(folder))
        if "_" in name:
            year = name.split("_")[1]
        else:
            year = name
        yield year, folder


def iterate(mapping, mode):
    """
    name of result file is used as "mode" to determine output format
    """
    if mode == "WORDIDS":
        for word, value in mapping.items():
            yield [str(x) for x in word, value]
    elif mode == "FREQUENCY":
        for year, info in mapping:
            for word, freq in info.items():
                yield [str(x) for x in word, year, freq]
    elif mode == "PPMI" or mode == "CHI":
        for year, generator in mapping:
            for word, word2, metric in generator:
                yield [str(x) for x in word, word2, year, metric]
    # similarity now calculated at runtime, using
    # read_top for 5 per year only (2 of first and last year
    # would be enough with current jeseme, plan on new API)
    elif mode == "SIMILARITY":
        for year, generator in mapping:
            for word, word2, metric in generator:
                yield [str(x) for x in word, word2, year, metric]
    elif mode == "EMBEDDING":
        for year, generator in mapping:
            for word, embeddings in generator:
                yield [str(x) for x in word, year, embeddings]
    elif mode == "EMOTION":
        for year, generator in mapping:
            for word, emotions in generator:
                yield [str(x) for x in word, year, " ".join(emotions)]
    else:
        raise Exception("Not allowed: " + mode)


def store_results(path, *mappings):
    for name, mapping in mappings:
        with open(join(path, name + ".csv"), "w") as f:
            for l in iterate(mapping, name):
                print >>f, ",".join(l)


def folder2ppmi(folder):
    return PositiveExplicit(join(folder, "pmi")).similarity_first_order


def folder2chi(folder):
    return PositiveExplicit(join(folder, "chi")).similarity_first_order


def folder2chi(folder):
    return PositiveExplicit(join(folder, "chi")).similarity_first_order


def folder2svd(folder, raw=False):
    if raw:
        return SVDEmbedding(join(folder, "svd_pmi"))
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


def _read_helper(method, word2id, word1, id1, minimum_score=0.001):
    for word2, id2 in word2id.items():
        if word1 != word2:
            metric = method(word1, word2)
            if metric > minimum_score:
                yield id1, id2, metric


def read_top(method, word2id, limit=5):
    for word1, id1 in word2id.items():
        for id1, id2, metric in sorted(_read_helper(method, word2id, word1, id1), key=lambda x: x[2], reverse=True)[:limit]:
            yield id1, id2, metric


def read_generic(method, word2id):
    for word1, id1 in word2id.items():
        for result in _read_helper(method, word2id, word1, id1):
            yield result


def emotion_induction(word2id, embeddings, emotion_lexicon):
    """
    Generator providing the emotion value for each word in the word2id
    dictionary based on a modification of the Algorithm presented in Turney 
    and Littman (2002) (numerical seed values are expected instead of 
    positive/negative paradigm words).

    Args:
    word2id             Dictionary mapping from word to id.
    embeddings          Used for similarity
    emotion_lexicon     Pandas data frame with columns Valence, Arousal, and
                        Dominance and words as index.

    Yields:
    4-Tuple comprising the id of the word and the Valence, Arousal, Dominance 
    scores.
    """
    for target, _id in word2id.items():
        # def __turney_single_word__(target, method, emotion_lexicon):
        #     vad=np.array([.0,.0,.0])
        #     normalization=.0
        #     for word in lexicon.words():
        #         vad+=lexicon.get(entry)*embeddings.similarity(entry,targetWord)
        #         normalization += embeddings.similarity(entry, targetWord)
        #     return vad/normalization
        vad = np.array([.0, .0, .0])
        denominator = .0
        for entry in emotion_lexicon.index:
            if entry in embeddings.wi:  # otherwise similarity is 0
                similarity = embeddings.similarity(entry, target)
                vad += emotion_lexicon.loc[entry] * similarity
                denominator += similarity
        vad = vad / denominator
        yield _id, [str(emotion) for emotion in vad]


# Alignment code based on
# https://github.com/williamleif/histwords/blob/master/vecanalysis/alignment.py
def intersection_align(embed1, embed2):
    """ 
        Get the intersection of two embeddings.
        Returns embeddings with common vocabulary and indices.
    """
    common_vocab = filter(set(embed1.iw).__contains__, embed2.iw)
    newvecs1 = np.empty((len(common_vocab), embed1.m.shape[1]))
    newvecs2 = np.empty((len(common_vocab), embed2.m.shape[1]))
    for i in xrange(len(common_vocab)):
        newvecs1[i] = embed1.represent(common_vocab[i])
        newvecs2[i] = embed2.represent(common_vocab[i])
    return PlainEmbedding(newvecs1, common_vocab, False), PlainEmbedding(newvecs2, common_vocab, False)


def smart_procrustes_align(base_embed, other_embed):
    in_base_embed, in_other_embed = intersection_align(base_embed, other_embed)
    base_vecs = in_base_embed.m
    other_vecs = in_other_embed.m
    m = other_vecs.T.dot(base_vecs)
    u, _, v = np.linalg.svd(m)
    ortho = u.dot(v)
    return PlainEmbedding((other_embed.m).dot(ortho), other_embed.iw, True)


class PlainEmbedding(Embedding):

    def __init__(self, m, vocab, normalize=True):
        self.m = m
        if normalize:
            self.normalize()
        self.dim = self.m.shape[1]
        self.iw = vocab
        self.wi = {w: i for i, w in enumerate(self.iw)}
# END of Alignment code

if __name__ == "__main__":
    main()
