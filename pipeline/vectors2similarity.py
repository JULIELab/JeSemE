from docopt import docopt
import numpy as np
from collections import defaultdict
from representations.embedding import SVDEmbedding
from representations.explicit import PositiveExplicit
from os.path import join


def main():
    args = docopt("""
    Usage:
        vectors2similarity.py <target> <year> <vocab> <ppmi> <svd> <limit>
""")
    word2id = read_vocab(int(args["<limit>"]), args["<vocab>"])
    id2freq = read_freq(args["<vocab>"], word2id)
    ppmi = read_generic(PositiveExplicit(
        args["<ppmi>"]).similarity_first_order, word2id)
    svd = read_generic(SVDEmbedding(args["<svd>"]).similarity, word2id)
    year = args["<year>"]
    store_results(args["<target>"], ("WORDIDS", iterate(word2id)), ("FREQUENCY", iterate(
        id2freq, year)), ("PPMI", iterate(ppmi, year)), ("SIMILARITY", iterate(svd, year)))


def iterate(mapping, year=False):
    for word, value in mapping.items():
        if isinstance(value, dict):
            for word2, innervalue in value.items():
                yield [str(x) for x in word, word2, year, innervalue]
        elif year != False:
            yield [str(x) for x in word, year, value]
        else:
            yield [str(x) for x in word, value]


def store_results(path, *mappings):
    for name, mapping in mappings:
        with open(join(path, name + ".csv"), "w") as f:
            for l in mapping:
                print >>f, ",".join(l)


def intersect(*words):
    if len(words) == 0:
        return set()
    if len(words) == 1:
        return words[0]:
    intersection = words[0]
    for w in words[1:]:
        intersection = intersection.intersection(w)
    return intersection


def read_vocab(limit, *vocabs):
    words = []
    for vocab in vocabs:
        w = set()
        i = 0
        with open(vocab, "r") as v:
            for line in v:
                if i == limit:
                    break
                i += 1
                word, freq = line.strip().split()
                w.add(word)
        words.add(w)
    return {x: y for x, y in enumerate(intersection(words))}


def read_freq(vocab, word2id):
    id2freq = {}
    with open(vocab, "r") as v:
        for line in v:
            word, freq = line.strip().split()
            if word in word2id:
                id2freq[word2id[word]] = freq
    return id2freq


def read_generic(method, word2id):
    mapping = defaultdict(dict)
    for word1, id1 in word2id.items():
        for word2, id2 in word2id.items():
            if word1 != word2:
                mapping[id1][id2] = method(word1, word2)
    return mapping


if __name__ == "__main__":
    main()
