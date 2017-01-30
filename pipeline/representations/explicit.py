import heapq

from scipy.sparse import dok_matrix, csr_matrix
import numpy as np

from representations.matrix_serializer import load_vocabulary, load_matrix


class Explicit:
    """
    Base class for explicit representations. Assumes that the serialized input is e^PMI.
    """
    
    def __init__(self, path, normalize=True, sparse=True):
        self.wi, self.iw = load_vocabulary(path + '.words.vocab')
        self.ci, self.ic = load_vocabulary(path + '.contexts.vocab')
        if sparse:
            self.m = load_matrix(path)
            self.m.data = np.log(self.m.data)
        else:
            self.m = np.load(path)
            np.log(self.m, self.m)
        self.normal = normalize
        if normalize:
            self.normalize(sparse)
    
    def normalize(self, sparse):
        m2 = self.m.copy()
        if sparse:
             m2.data **= 2
        else:
             m2 **= 2
        norm = np.reciprocal(np.sqrt(np.array(m2.sum(axis=1))[:, 0]))
        normalizer = dok_matrix((len(norm), len(norm)))
        normalizer.setdiag(norm)
        self.m = normalizer.tocsr().dot(self.m)
    
    def represent(self, w):
        if w in self.wi:
            return self.m[self.wi[w], :]
        else:
            return csr_matrix((1, len(self.ic)))
    
    def similarity_first_order(self, w, c):
        return self.m[self.wi[w], self.ci[c]]
    
    def similarity(self, w1, w2):
        """
        Assumes the vectors have been normalized.
        """
        return self.represent(w1).dot(self.represent(w2).T)[0, 0]
    
    def closest_contexts(self, w, n=10):
        """
        Assumes the vectors have been normalized.
        """
        scores = self.represent(w)
        return heapq.nlargest(n, zip(scores.data, [self.ic[i] for i in scores.indices]))
    
    def closest(self, w, n=10):
        """
        Assumes the vectors have been normalized.
        """
        scores = self.m.dot(self.represent(w).T).T.tocsr()
        return heapq.nlargest(n, zip(scores.data, [self.iw[i] for i in scores.indices]))


class PositiveExplicit(Explicit):
    """
    Positive PMI (PPMI) with negative sampling (neg).
    Negative samples shift the PMI matrix before truncation.
    """
    
    def __init__(self, path, normalize=True, neg=1, sparse=True):
        Explicit.__init__(self, path, False, sparse)
        if sparse:
            self.m.data -= np.log(neg)
            self.m.data[self.m.data < 0] = 0
            self.m.eliminate_zeros()
        else:
            self.m -= np.log(neg)
            self.m[self.m < 0] = 0 
        if normalize:
            self.normalize(sparse)
