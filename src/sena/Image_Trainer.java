package sena;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import java.util.ArrayList;

import trainableSegmentation.WekaSegmentation;

public class Image_Trainer extends Logger {
	private static final String PROCEDURE_NAME = "Image Trainer";
	
	private WekaSegmentation seg;
	private final int nSamplesToUse = 2000;
	public Image_Trainer() {
		super(PROCEDURE_NAME);
		
	}
	
	private void configure() {
		
	}
	
	public void train(ArrayList<Roi> rois) {
		
	}
	
	public void testing() {
		log("loading images...");
		ImagePlus image = IJ.openImage("D:\\9 - Polytechnique\\Academico\\3 Annee\\Julio\\training-image.tif");
		ImagePlus labels = IJ.openImage("D:\\9 - Polytechnique\\Academico\\3 Annee\\Julio\\training-image.tif");
		
		log("images loaded!");
		
		seg = new WekaSegmentation(image);
		
		FastRandomForest rf = new FastRandomForest();
		rf.setNumTrees(100);
		rf.setNumFeatures(0);
		rf.setSeed((new java.util.Random()).nextInt());
		
		log("Random Forest configured");
		
		seg.setClassifier(rf);
		
		//???
		seg.setMembranePatchSize(11);
		
		//???
		seg.setMaximumSigma(16.0f);
		
		// Selected attributes
		boolean[] enableFeatures = new boolean[]{
		            true,   /* Gaussian_blur */
		            true,   /* Sobel_filter */
		            true,   /* Hessian */
		            true,   /* Difference_of_gaussians */
		            true,   /* Membrane_projections */
		            false,  /* Variance */
		            false,  /* Mean */
		            false,  /* Minimum */
		            false,  /* Maximum */
		            false,  /* Median */
		            false,  /* Anisotropic_diffusion */
		            false,  /* Bilateral */
		            false,  /* Lipschitz */
		            false,  /* Kuwahara */
		            false,  /* Gabor */
		            false,  /* Derivatives */
		            false,  /* Laplacian */
		            false,  /* Structure */
		            false,  /* Entropy */
		            false   /* Neighbors */
		};
		
		seg.setEnabledFeatures(enableFeatures);
		seg.addRandomBalancedBinaryData(image, labels, "class 2", "class 1", nSamplesToUse);
		seg.trainClassifier();
		
		log("classifier trained");
		
		seg.applyClassifier(true);
		ImagePlus prob = seg.getClassifiedImage();
		prob.setTitle("Probability maps of train image");
		prob.show();
		
		image = IJ.openImage("D:\\9 - Polytechnique\\Academico\\3 Annee\\Julio\\Hu_Moments.tif");
		
		ImagePlus probd = seg.applyClassifier(image, 0, true);
		
		probd.setTitle("probability maps of test image");
		probd.show();
		
		image.show();
		
		log("---");
		
	}
}
