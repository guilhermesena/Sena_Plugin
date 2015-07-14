__author = "Guilherme Sena"

from sklearn.neighbors import NearestNeighbors
from sklearn import tree,linear_model, svm
from sklearn.externals.six import StringIO
import pydot
import settings
import create_dataframe
import cv2
import os

def get_class_svm(i, svm_classes):
    return int(svm_classes[i])

def get_class_logreg(i, logreg_classes):
    return int(logreg_classes[i])

def get_class_knn(image_data, indices):
    classes = []
    for w in indices[i]:
       classes.append(create_dataframe.training_df.iloc[w, class_col])
    return int(max(set(classes), key=classes.count))

def get_class_decisiontree(i, dt_classes):
    return int(dt_classes[i])

print "\n\n------Starting testing sets-----------"
classifiers = ["Logistic_Regression", "kNN", "Decision_Tree", "SVM"]

class_col = len(settings.HEADERS) - 1
train_rows = create_dataframe.training_df.iloc[:, 0:class_col].as_matrix()
train_classes = create_dataframe.training_df.iloc[:, class_col].as_matrix()

print "Fitting Support Vector Machines..."
supvec = svm.SVC().fit(train_rows, train_classes)

print "Fitting logistic regression..."
logreg = linear_model.LogisticRegression().fit(train_rows, train_classes)

print "Fitting kNN from training set..."
nbrs = NearestNeighbors(n_neighbors=5, algorithm='ball_tree',p=2).fit(train_rows)

print "Fitting decision tree..."
clf = tree.DecisionTreeClassifier().fit(train_rows, train_classes)
print "Finished fitting!"

print "Exporting decision tree to PNG file..."
clf_filename = "decision_tree.png"
dot_data = StringIO()
tree.export_graphviz(clf, out_file=dot_data)
graph = pydot.graph_from_dot_data(dot_data.getvalue())
graph.write_png("\\".join([settings.TRAINING_SETS_FOLDER, settings.PLOTS_FOLDER, clf_filename]))
print "File "+str(clf_filename)+" saved!\n"

# CSV output
test_results = open("\\".join([settings.TEST_SETS_FOLDER, settings.TEST_RESULTS_FILE]), 'w')

for IMAGES_FOLDER in settings.TEST_FOLDERS:
    print "processing "+str(IMAGES_FOLDER)+"..."
    for filename in os.listdir("\\".join([os.getcwd(), settings.TEST_SETS_FOLDER , IMAGES_FOLDER])):        
        
        #Only considers image files
        if not(create_dataframe.is_image(filename)):
            continue

        print "image "+str(filename)+"..."        
        
        #Thresholds and finds contours as in regular data
        im,imgray,thresh,contours = create_dataframe.get_images("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, filename]))
        valid_contours, image_data = create_dataframe.get_valid_contours(imgray, contours, settings.TEST_FOLDERS.index(IMAGES_FOLDER))
        
        #ttt = 0
        #for cnt in valid_contours:
        #    ttt += 1
        #    x,y,w,h = cv2.boundingRect(cnt)
        #    cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, settings.OTSU_FOLDER, settings.CONTOURS_FOLDER+"\\"+str(ttt)+".png"]), im[y:(y+h),x:(x+w)])
       

        # Append data without class col        
        image_data_noclass = []
        for img in image_data:
            image_data_noclass.append(img[0:class_col])
    
        print "Classifying through SVM"
        svm_classes = supvec.predict(image_data_noclass)
        im_svm = im.copy()
        
        print "Classifying through logistic regression..."
        logreg_classes= logreg.predict(image_data_noclass)
        im_logreg = im.copy()
        
        print "Classifying through kNN..."
        knn_distances, knn_indices = nbrs.kneighbors(image_data_noclass)
        im_knn = im.copy()
        
        print "Classifying through decision tree..."
        dt_classes = clf.predict(image_data_noclass)
        im_dt = im.copy()
        
        for i in range(len(valid_contours)):
            print "Painting with SVM..."
            color_svm = settings.COLORS_RGB[get_class_svm(i, svm_classes)] 
            cv2.drawContours(im_svm, [valid_contours[i]], -1, color_svm, -1)
            
            print "Painting image with Logistic Regression..."
            color_logreg = settings.COLORS_RGB[get_class_logreg(i, logreg_classes)]
            cv2.drawContours(im_logreg, [valid_contours[i]], -1, color_logreg, -1)

            print "Painting image with Decision Tree..."
            color_knn = settings.COLORS_RGB[get_class_decisiontree(i, dt_classes)]
            cv2.drawContours(im_knn, [valid_contours[i]], -1, color_knn, -1)

            print "Painting image with kNN classification..."
            color_dt = settings.COLORS_RGB[get_class_knn(image_data_noclass[i], knn_indices)]
            cv2.drawContours(im_dt, [valid_contours[i]], -1, color_dt, -1)

        
        print "Saving contour of "+str(filename)+"..."
        cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, "Logistic_Regression", "contours - "+filename]), im_logreg)
        cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, "kNN", "contours - "+filename]), im_knn)
        cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, "Decision_Tree", "contours - "+filename]), im_dt)
        cv2.imwrite("\\".join([settings.TEST_SETS_FOLDER, IMAGES_FOLDER, "SVM", "contours - "+filename]), im_dt)

        print "Done!\n"

print "Analysis complete!"