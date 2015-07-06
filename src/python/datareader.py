__author__ = 'Guilherme Sena'

import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plot

data = pd.read_csv('dataread.csv')
for i in range(500):
	if data.iat[i, 6] == 'Candida':
		pcolor = "red"
	else:
		pcolor = "blue"
	
	dataRow = data.iloc[i, 0:6]
	dataRow.plot(color=pcolor)

plot.xlabel("Attribute index")
plot.ylabel(("Attribute values"))
plot.savefig('parallel_coords.png')