import pandas as pd
import numpy as np
import sys

def __parse__(string):
	return [float(x) for x in string.split(' ')]

def __combine__(df):
    rtrn=[]
    for i in range(len(df)):
        rtrn+=[' '.join([str(x) for x in df.iloc[i]])]
    return rtrn

def scale(path):
    df=pd.read_csv(path, header=None, names=['id', 'year', 'emotion'])
    df.set_index('id', inplace=True)
    new_df=pd.DataFrame(columns=df.columns)
    for year in sorted(list(set(df.year))):
        #print(year)
        emos=[__parse__(x) for x in df.loc[df.year==year, 'emotion']]
        emos=pd.DataFrame(emos)
        emos=emos.subtract(emos.mean(axis=0)).divide(emos.std(axis=0, ddof=0))
        #print(emos.mean(axis=0), emos.std(axis=0, ddof=0))
        df.loc[df.year==year, 'emotion']=__combine__(emos)
    df.to_csv(path[:-4]+'_normalized.csv')

if __name__=='__main__':
    scale(sys.argv[1])


