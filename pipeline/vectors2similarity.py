from docopt import docopt
import numpy as np
from collections import defaultdict
from representations.embedding import SVDEmbedding
from representations.explicit import PositiveExplicit
from os.path import join, basename, normpath

def main():
    args = docopt("""
    Usage:
        vectors2similarity.py <target> <limit> <folders>...
""")
    id2freq, ppmi, svd_similarity = [], [], []
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
        svd_similarity.append((year, read_generic(folder2svd(folder), word2id, True)))

    store_results(args["<target>"], ("WORDIDS", iterate(word2id, True)), ("FREQUENCY", iterate(
        id2freq)), ("PPMI", iterate(ppmi)), ("SIMILARITY", iterate(svd_similarity)))


def iterate(mapping, is_word2id=False):
    if is_word2id:
        for word, value in mapping.items():
            yield [str(x) for x in word, value]
    else:
        for (year, info) in mapping:
            for word, value in info.items():
                if isinstance(value, dict):
                    for word2, innervalue in value.items():
                        yield [str(x) for x in word, word2, year, innervalue]
                else:
                    yield [str(x) for x in word, year, value]

def store_results(path, *mappings):
    for name, mapping in mappings:
        with open(join(path, name + ".csv"), "w") as f:
            for l in mapping:
                print >>f, ",".join(l)

def folder2ppmi(folder):
    return PositiveExplicit(join(folder, "pmi")).similarity_first_order
def folder2svd(folder):
    return SVDEmbedding(join(folder, "svd_pmi")).similarity
def folder2vocab(folder):
    return join(folder, join("shared", "counts.words.vocab"))

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
    return {y:x for x, y in enumerate(intersect(words))}

def read_freq(folder, word2id):
    id2freq = {}
    with open(folder2vocab(folder), "r") as v:
        for line in v:
            word, freq = line.strip().split()
            if word in word2id:
                id2freq[word2id[word]] = freq
    return id2freq


def read_generic(method, word2id, remove_duplicates=False):
    mapping = defaultdict(dict)
    for word1, id1 in word2id.items():
        for word2, id2 in word2id.items():
            if word1 != word2:
                if remove_duplicates:
                    if id1 < id2:
                        mapping[id1][id2] = method(word1, word2)
                else:
                    mapping[id1][id2] = method(word1, word2)
    return mapping


if __name__ == "__main__":
    main()
