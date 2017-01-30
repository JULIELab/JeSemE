from docopt import docopt
import numpy as np
from collections import defaultdict
from representations.embedding import SVDEmbedding
from representations.explicit import PositiveExplicit
from os.path import join


def main():
    args = docopt("""
    Usage:
        vectors2similarity.py <target> <vocab> <ppmi> <svd> <limit>
""")
    word2id, id2freq = read_vocab(args["<vocab>"], int(args["<limit>"]))
    ppmi = read_generic(PositiveExplicit(
        args["<ppmi>"]).similarity_first_order, word2id)
    svd = read_generic(SVDEmbedding(args["<svd>"]).similarity, word2id)
    store_results(args["<target>"], ("WORDIDS", word2id), ("FREQUENCY",
                                                           id2freq), ("PPMI", ppmi), ("SIMILARITY", svd))


def iterate(mapping):
    for word, value in mapping.items():
        if isinstance(value, dict):
            for word2, innervalue in value.items():
                yield [str(x) for x in word, word2, innervalue]
        else:
            yield [str(x) for x in word, value]


def store_results(path, *mappings):
    for name, mapping in mappings:
        with open(join(path, name + ".csv"), "w") as f:
            for l in iterate(mapping):
                print >>f, ",".join(l)


def read_vocab(vocab, limit):
    word2id = {}
    id2freq = {}
    i = 0
    with open(vocab, "r") as v:
        for line in v:
            if i == limit:
                break
            word, freq = line.strip().split()
            word2id[word] = i
            id2freq[i] = freq
            i += 1
    return word2id, id2freq


def read_generic(method, word2id):
    mapping = defaultdict(dict)
    for word1, id1 in word2id.items():
        for word2, id2 in word2id.items():
            if word1 != word2:
                mapping[id1][id2] = method(word1, word2)
    return mapping


if __name__ == "__main__":
    main()
