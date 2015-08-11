# INPUT: Folder containing several images of a single class
# OUTPUT: CSV file with contours + a column with the feature value

__author__ = 'Guilherme Sena'

import cv2
import os
import numpy as np
import pandas as pd
import settings
from scipy.ndimage import label

def is_image(filename):
    return (filename.endswith("jpg") or filename.endswith("png") or filename.endswith("tif"))

def sub_image(im, cnt):
    x,y,w,h = cv2.boundingRect(cnt)
    return im[y:(y+h),x:(x+w)]

def segment_on_dt(a, img):
    border = cv2.dilate(img, None, iterations=5)
    border = border - cv2.erode(border, None)

    dt = cv2.distanceTransform(img, 2, 3)
    dt = ((dt - dt.min()) / (dt.max() - dt.min()) * 255).astype(np.uint8)
    _, dt = cv2.threshold(dt, 0, 255, cv2.THRESH_BINARY+cv2.THRESH_TRIANGLE)
    lbl, ncc = label(dt)
    lbl = lbl * (255/ncc)
    # Completing the markers now. 
    lbl[border == 255] = 255

    lbl = lbl.astype(np.int32)
    cv2.watershed(a, lbl)

    lbl[lbl == -1] = 0
    lbl = lbl.astype(np.uint8)
    return 255 - lbl


def add_filters(thresh):
    print "Adding denoise filter..."
    thresh = cv2.fastNlMeansDenoising(thresh)
    
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
    ret,thresh = cv2.threshold(blur,0,255,cv2.THRESH_BINARY+cv2.THRESH_TRIANGLE)    
    
    print "Adding filters..."     
    add_filters(thresh)
    thresh2 = thresh.copy()
    imgret, contours, hierarchy = cv2.findContours(thresh2,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)    
    
    print "Done processing, proceeding to contour calculations..."
    return im, imgray, thresh,contours

def get_valid_contours(imgray, contours, contour_class):
    
    print "calculating Hu's moments..."
    valid_contours = []    
    ans = []
    for cnt in contours:
		
        # Image moments and Hu's invariants
        mom = cv2.moments(cnt)
        hus = cv2.HuMoments(mom)
			
        # Since we take the log we want only non-zero values
        if mom['m00'] < settings.MIN_SIZE:
            continue
			
        # If this is the case, it's a valid contour
        valid_contours.append(cnt)
        
        output = []
        for hu in hus:
            v = np.sign(hu[0])*np.log10(np.abs(hu[0]))
            if pd.isnull(v) or v == 0:
                output.append(0)
            else:
                output.append(1/v)
        
        area = np.log10(np.abs(mom['m00']))
        if pd.isnull(area):
            area = 0
        #Append appropriate data here
        ans.append ([
              area,
              output[0],
              output[1],
              output[2],
              output[3],
              output[4],
              output[5],
              output[6],
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
            valid_contours, image_data = get_valid_contours(imgray, contours, folder_list.index(IMAGES_FOLDER))
            
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
        df = make_dataframe(data, headers, False)
        df.to_csv(settings.TRAINING_SET_FILE, sep=',')
        
    return df
    
training_df = create_dataframe(settings.HEADERS, settings.TRAINING_SETS_FOLDER, settings.TRAINING_FOLDERS, True)