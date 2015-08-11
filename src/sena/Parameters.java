package sena;

public class Parameters {
	public double sigma;
	public String thresholdMethod;
	public boolean saveImages = true;
	public Parameters(double sigma, String thresholdMethod, boolean saveImages) {
		this.sigma = sigma;
		this.thresholdMethod = thresholdMethod;
		this.saveImages = saveImages;
	}
}
