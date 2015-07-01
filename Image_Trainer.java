import ij.*;
import ij.process.*;
import trainableSegmentation.*;
import hr.irb.fastRandomForest.*;

public class Image_Trainer {
	
	public Image_Trainer() {
		
	}
	
	public void testing() {
		ImagePlus image = IJ.openImage("C:\\Program Files\\ImageJ\\plugins\\Sena_Plugin\\training-image.tif");
		ImagePlus labels = IJ.openImage("C:\\Program Files\\ImageJ\\plugins\\Sena_Plugin\\training-image.tif");
		
		IJ.log("images loaded");
		
		WekaSegmentation seg = new WekaSegmentation(image);
		
		int nSamplesToUse = 2000;
		
		FastRandomForest rf = new FastRandomForest();
		rf.setNumTrees(100);
		rf.setNumFeatures(0);
		rf.setSeed((new java.util.Random()).nextInt());
		
		IJ.log("Random Forest configured");
		
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
		
		IJ.log("classifier trained");
		
		seg.applyClassifier(true);
		ImagePlus prob = seg.getClassifiedImage();
		prob.setTitle("Probability maps of train image");
		prob.show();
		
		image = IJ.openImage("Hu_Moments.tif");
		
		ImagePlus probd = seg.applyClassifier(image, 0, true);
		
		probd.setTitle("probability maps of test image");
		probd.show();
		
		image.show();
		
		IJ.log("---");
		
	}
}
