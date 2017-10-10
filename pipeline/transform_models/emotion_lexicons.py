import pandas as pd

path_english=''
path_german=''

def load_english():
	'''
	Warriner et al. 2013
	'''
	if path_english=='':
		raise ValueError('Path to the lexicon file is empty!'+\
			'Please make sure to insert the correct file path')
	warriner13 = pd.read_csv(path_english, sep=',')
	warriner13=warriner13[['Word','V.Mean.Sum', 'A.Mean.Sum', 'D.Mean.Sum']]
	warriner13.columns=heads_vad
	warriner13.set_index('Word',inplace=True)
	# print(warriner13.head())
	# print(warriner13.shape)
	return warriner13


def load_german():
	'''
	Schmidtke et al., 2014
	'''
	if path_german=='':
		raise ValueError('Path to the lexicon file is empty!'+\
			'Please make sure to insert the correct file path.')
	schmidtke14=pd.read_csv(path_german, sep='\t')
	schmidtke14=schmidtke14[['Word','Valence','Arousal','Dominance']]
	schmidtke14.columns=heads_vad
	schmidtke14['Word']=schmidtke14['Word'].str.lower()
	schmidtke14.set_index('Word', inplace=True)
	## rescaling valence

	schmidtke14.Valence = [scaleInRange(x = x, oldmin = -3.,
									   oldmax = 3., newmin = 1., newmax=9.) 
						   for x in schmidtke14.Valence]

	### setting word column to lower case for compatiblity with briesemeister11
	# print(schmidtke14.head())
	# print(schmidtke14.shape)
	return schmidtke14