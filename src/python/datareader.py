__author__ = 'Guilherme Sena'

import pandas as pd
import matplotlib.pyplot as plot
from pandas import DataFrame

data = pd.read_csv('data_final.csv')

print data.describe()

for i in range(data.shape[0]):
	if data.iat[i, 0] > 1:
		pcolor = "blue"
	else:
		pcolor = "red"
	
	dataRow = data.iloc[i, 0:7]
	dataRow.plot(color=pcolor)

plot.xlabel("Momento")
plot.ylabel(("Valor"))
plot.savefig('parallel_coords'+str(data.shape[0])+'.png')