__author = "Guilherme Sena"

from sklearn.neighbors import NearestNeighbors
from sklearn import tree
import settings
import create_dataframe
import cv2
import os

print "\n\n------Starting testing sets-----------"

class_col = len(settings.HEADERS) - 1
train_rows = create_dataframe.training_df.iloc[:, 0:class_col].as_matrix()
train_classes = create_dataframe.training_df.iloc[:, class_col].as_matrix()

print "Fitting kNN from training set..."
nbrs = NearestNeighbors(n_neighbors=5, algorithm='ball_tree',p=1).fit(train_rows)

print "Fitting decision tree..."
clf = tree.DecisionTreeClassifier()
clf = clf.fit(train_rows, train_classes)

print "Finished fitting!\n"

for IMAGES_FOLDER in settings.TEST_FOLDERS:
    print "processing "+str(IMAGES_FOLDER)+"..."
    for filename in os.listdir("\\".join([os.getcwd(), settings.TEST_SETS_FOLDER , IMAGES_FOLDER])):        
        
        #Only considers image files
        if not(create_dataframe.is_image(filename)):
            continue

        print "image "+str(filename)+"..."        
        
        im,imgray,thresh,contours = create_dataframe.get_images("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, filename]))
        valid_contours, image_data = create_dataframe.get_valid_contours(contours, settings.TEST_FOLDERS.index(IMAGES_FOLDER))
        
        print "Finding kNN for image "+str(filename)+"..."
        
        image_data_noclass = []
        for img in image_data:
            image_data_noclass.append(img[0:class_col])
        
        print "Calculating kNN for test contours..."
        distances, indices = nbrs.kneighbors(image_data_noclass)
        
        print "Classifying through decision tree..."
        classes = clf.predict(image_data_noclass)
        for i in range(len(valid_contours)):
            
            #classes = []
            #for w in indices[i]:
            #    classes.append(create_dataframe.training_df.iloc[w, class_col])
            
            color = settings.COLORS_RGB[int(classes[i])]
            cv2.drawContours(im, [valid_contours[i]], -1, color, -1)
        
        print "Saving contour of "+str(filename)+"..."
        cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, settings.OTSU_FOLDER, settings.CONTOURS_FOLDER+"\\contours - "+filename]), im)
        print "Done!\n"
    