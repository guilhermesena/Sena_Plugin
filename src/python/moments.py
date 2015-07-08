# INPUT: Folder containing several images of a single class
# OUTPUT: CSV file with contours + a column with the feature value

__author__ = 'Guilherme Sena'

import cv2
import os
import numpy as np
import pandas as pd

#Output CSV file name
FILE_NAME = 'data_final.csv'

# Minimum area to consider the region relevant
MIN_SIZE = 50

# Input images folder
IMAGES_FOLDER = "Giovanna"

# Output folder for Otsu's thresholds
OTSU_FOLDER = "Otsu"

# Output folder for the output contours
CONTOURS_FOLDER = "Contours"

#Class value
CLASS_VALUE = 1

#Feature headers
HEADERS = ["Area", "I1", "I2", "I3", "I4", "I5", "I6", "Class"]

data = []

for filename in os.listdir(os.getcwd()+"\\"+IMAGES_FOLDER):
	#Only considers image files
	if filename.endswith("jpg") or filename.endswith("png") or filename.endswith("tif"):
		
		#Reads image and turns it into grayscale
		im = cv2.imread(IMAGES_FOLDER+"\\"+filename)
		im2 = im
		imgray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
		
		#Threshold using otsu method and uses it to create contours
		ret,thresh = cv2.threshold(imgray,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)
		test, contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
		
		#TODO: Clean using filters
		
		#Contours that are being put into the CSV data
		valid_contours = []
		
		for cnt in contours:
		
			# Image moments and Hu's invariants
			mom = cv2.moments(cnt)
			hus = cv2.HuMoments(mom)
			
			# Since we take the log we want only non-zero values
			if 0 in hus[0:6]:
				continue
			
			# If this is the case, it's a valid contour
			valid_contours.append(cnt)
			
			# Hu's log is the relevant value
			huslog = np.log10(np.abs(hus[0:6]))*(-1)
			output = []
			for val in huslog:
				output.append(val[0])
			
			#Append appropriate data here
			data.append ([
				mom['m00'],
				output[0],
				output[1],
				output[2],
				output[3],
				output[4],
				output[5],
				CLASS_VALUE
			])
			
			if mom['m00'] > 400:
				cv2.drawContours(im2, [cnt], -1, (255,0,255), -1)
			else:
				cv2.drawContours(im2, [cnt], -1, (255, 0, ), -1)
			
		#Normalizes data
		df = pd.DataFrame(data = data, columns = HEADERS)
		summary = df.describe()
		ncols = len(df.columns)
		
		for i in range(ncols):
			mean = summary.iloc[1,i]
			sd = summary.iloc[2,i]
			
			df.iloc[:, i:(i+1)] = (df.iloc[:,i:(i+1)] - mean)/sd
			
		#Saves data
		df.to_csv(FILE_NAME, sep=',')
		
		cv2.imwrite(IMAGES_FOLDER+"\\"+OTSU_FOLDER+"\\otsu - "+filename, thresh)
		cv2.imwrite(IMAGES_FOLDER+"\\"+OTSU_FOLDER+"\\"+CONTOURS_FOLDER+"\\contours - "+filename, im2)
		
		