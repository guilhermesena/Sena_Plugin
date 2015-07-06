# INPUT: Folder containing several images of a single class
# OUTPUT: CSV file with Hu's invariant moments + a column with the feature value

__author__ = 'Guilherme Sena'

import cv2
import os
import numpy as np

# Minimum area to consider the region relevant
MIN_SIZE = 50

# Input images folder
IMAGES_FOLDER = "Corante"

# Output folder for Otsu's thresholds
OTSU_FOLDER = "Otsu"

# Output folder for the output contours
CONTOURS_FOLDER = "Contours"

#Class value
CLASS_VALUE = "Corante"

#Feature headers
HEADERS = ["Image", "I1", "I2", "I3", "I4", "I5", "I6", "Class"]


#Print headers
print ",".join(map(str,HEADERS))

for filename in os.listdir(os.getcwd()+"\\"+IMAGES_FOLDER):
	#Only considers image files
	if filename.endswith("jpg") or filename.endswith("png") or filename.endswith("tif"):
		
		#Reads image and turns it into grayscale
		im = cv2.imread(IMAGES_FOLDER+"\\"+filename)
		imgray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
		
		#Threshold using otsu method and uses it to create contours
		ret,thresh = cv2.threshold(imgray,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)
		test, contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
		
		
		#Contours that are being put into the CSV data
		valid_contours = []
		
		for cnt in contours:
		
			# Image moments and Hu's invariants
			mom = cv2.moments(cnt)
			hus = cv2.HuMoments(mom)
			
			# Since we take the log we want only non-zero values
			if 0 in hus[0:6] or mom['m00'] < MIN_SIZE:
				continue
			
			# If this is the case, it's a valid contour
			valid_contours.append(cnt)
			
			# Hu's log is the relevant value
			huslog = np.log10(np.abs(hus[0:6]))*(-1)
			output = []
			for val in huslog:
				output.append(val[0])
				
			print filename+","+','.join(map(str,output))+","+CLASS_VALUE
		
		#Saves data
		im2 = im
		cv2.imwrite(IMAGES_FOLDER+"\\"+OTSU_FOLDER+"\\otsu - "+filename, thresh)
		#cv2.drawContours(im2, valid_contours, -1, (0, 255, 0), 2)
		#cv2.imwrite(IMAGES_FOLDER+"\\"+OTSU_FOLDER+"\\"+CONTOURS_FOLDER+"\\contours - "+filename, im2)
		
		