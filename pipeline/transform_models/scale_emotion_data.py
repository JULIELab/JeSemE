import pandas as pd
import numpy as np
import sys

if __name__=='__main':
	scale(sys.argv[0])

def __parse__(str):
	return [float(x) for x in str.split(' ')]

def __combine__(df):
	return [' '.join(str(df.iloc[i])) for i in len(df)]


def scale(path):
	df=pd.from_csv(path, header=None, names=['id', 'year', 'emotion'])
	new_df=pd.DataFrame(columns=df.columns)
	for year in sorted(list(set(df.year))):
		sub_df=df.loc[df.year==year]
		emos=pd.DataFrame([__parse__(x) for x in sub_df.emotion])
		emos=emos-emos.mean(axis=0)/emos.std(axis=0, ddof=0)
		sub_df.emotion=__combine__(emos)
		new_df=pd.concatenate([new_df, sub_df], axis=0)
	new_df.to_csv(path[:-4]+'_normalized-csv')

