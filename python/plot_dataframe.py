__author__ = 'Guilherme Sena'

import matplotlib.pyplot as plot
import settings
import create_dataframe
import numpy as np

def create_graphs(df):
    class_index = len(settings.HEADERS) - 1
    
    #List with distinct classes
    distinct_classes = list(set(df.iloc[:, class_index]))
    
    print "Creating parallel coordinates plot..."
    for i in range(df.shape[0]):
       pcolor = settings.COLORS[distinct_classes.index(df.iloc[i, class_index])]
       dataRow = df.iloc[i, 0:class_index]
       dataRow.plot(color=pcolor)
    
    plot.xlabel("Atributos")
    plot.ylabel(("Valor"))
    plot.savefig("\\".join([settings.TRAINING_SETS_FOLDER, settings.PLOTS_FOLDER, 'parallel_coords.png']))

create_graphs(create_dataframe.training_df)