# INPUT: Folder containing several images of a single class
# OUTPUT: CSV file with contours + a column with the feature value

__author__ = 'Guilherme Sena'

import cv2
import os
import numpy as np
import pandas as pd
import settings

def is_image(filename):
    return (filename.endswith("jpg") or filename.endswith("png") or filename.endswith("tif"))
    
def get_images(path):
    #Reads image and turns it into grayscale
    print "Processing image in "+str(path)+"..."
    im = cv2.imread(path)
    
    print "Grayscaling..."
    imgray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)

    #Threshold using otsu method and uses it to create contours
    print "Adding Gaussian Blur..."
    blur = cv2.GaussianBlur(imgray,(5,5),0)

    print "Thresholding..."    
    ret,thresh = cv2.threshold(blur,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)    
        
    print "Denoising..."    
    openThresh = cv2.fastNlMeansDenoising(thresh)
    thresh2 = openThresh.copy()
    imgret, contours, hierarchy = cv2.findContours(thresh2,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    
    print "Done processing, proceeding to contour calculations..."
    return im, imgray, openThresh,contours

def get_valid_contours(contours, contour_class):
    
    print "calculating Hu's moments..."
    valid_contours = []    
    ans = []
    for cnt in contours:
		
        # Image moments and Hu's invariants
        mom = cv2.moments(cnt)
        hus = cv2.HuMoments(mom)
			
        # Since we take the log we want only non-zero values
        if 0 in hus[0:6] or mom['m00'] < settings.MIN_SIZE:
            continue
			
        # If this is the case, it's a valid contour
        valid_contours.append(cnt)
			
        # Hu's log is the relevant value
        huslog = np.log10(np.abs(hus[0:6]))*(-1)
        output = []
        for val in huslog:
            output.append(val[0])
			
        #Append appropriate data here
        ans.append ([
				np.log10(mom['m00']),
				output[0],
				output[1],
				output[2],
				output[3],
				output[4],
				output[5],
				contour_class
        ])
    
    return valid_contours, ans

def make_dataframe(d, cols, normalize=False):
    print "making dataframe..."    
    
    #Normalizes data
    df = pd.DataFrame(data = d, columns = cols)
    
    if normalize == True:
        summary = df.describe()
        ncols = len(df.columns)
    		
        for i in range(ncols):
            mean = summary.iloc[1,i]
            sd = summary.iloc[2,i]
            df.iloc[:, i:(i+1)] = (df.iloc[:,i:(i+1)] - mean)/sd
    
    return df
    
def shuffle_dataframe(df):     
    print "Shuffling dataframe..."
    return df.iloc[np.random.permutation(len(df))]
    
def create_dataframe(headers, root_folder, folder_list, save_to_csv):
    print "Starting to create training set dataframe..."

    #Array that will be used to populate dataframe
    data = []
    for IMAGES_FOLDER in folder_list:
        print "processing folder "+str(IMAGES_FOLDER)+"..."
        for filename in os.listdir("\\".join([os.getcwd(), root_folder, IMAGES_FOLDER])):        
            
            #Only considers image files
            if not(is_image(filename)):
                continue
            
            print "Image "+str(filename)+"..."   
            
            im,imgray,thresh,contours = get_images("\\".join([root_folder, IMAGES_FOLDER, filename]))
            valid_contours, image_data = get_valid_contours(contours, folder_list.index(IMAGES_FOLDER))
            
            for img in image_data:
                data.append(img)
                
            #Saves data
            print "Saving threshold of "+str(filename)+"..."
            cv2.imwrite("\\".join([settings.TRAINING_SETS_FOLDER, IMAGES_FOLDER, settings.OTSU_FOLDER+"\\otsu - "+filename]), thresh)        
            cv2.drawContours(im, valid_contours, -1, settings.COLORS_RGB[folder_list.index(IMAGES_FOLDER)], -1)
            
            print "Saving contours of "+str(filename)+"..."            
            cv2.imwrite("\\".join([settings.TRAINING_SETS_FOLDER, IMAGES_FOLDER, settings.OTSU_FOLDER, settings.CONTOURS_FOLDER+"\\contours - "+filename]), im)
            
            print "Done! \n"
        
        print "Done with folder "+str(IMAGES_FOLDER)+"!"
        
    if save_to_csv:        
        print "Saving training set at: "+settings.TRAINING_SET_FILE+"..."
        df = shuffle_dataframe(make_dataframe(data, headers, False))
        df.to_csv(settings.TRAINING_SET_FILE, sep=',')
        
    return df


training_df = create_dataframe(settings.HEADERS, settings.TRAINING_SETS_FOLDER, settings.TRAINING_FOLDERS, True)