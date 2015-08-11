package sena;
import java.util.ArrayList;
import java.util.List;

import org.opencv.imgproc.Moments;

import weka.core.Attribute;
import weka.core.Instances;

public class Image_Data {
	private static final String CSV_SEPARATOR = ",";
	public static final String[] headers = {"Area", "I1","I2","I3","I4","I5","I6","I7","Class"};
	public static final String CLASS_HEADER = "Class";
	public double Area,I1,I2,I3,I4,I5,I6,I7;
	public String className;
	public static String csvHeaders() {
		return Utils.join(headers, CSV_SEPARATOR)+"\n";
	}
	
	public String toCsvRow() {
		//TODO: Proper string join
		String [] values = {
				Double.toString(Area),
				Double.toString(I1),
				Double.toString(I2),
				Double.toString(I3),
				Double.toString(I4),
				Double.toString(I5),
				Double.toString(I6),
				Double.toString(I7),
				className
				
		};
		return Utils.join(values, CSV_SEPARATOR)+"\n";
				
	}
	
	public void fillHuMoments(double[] moments) {
		if(moments.length != 7)
			throw new IllegalArgumentException("Number of moments different than 7");
		
		I1 = moments[0];
		I2 = moments[1];
		I3 = moments[2];
		I4 = moments[3];
		I5 = moments[4];
		I6 = moments[5];
		I7 = moments[6];
	}
	
	public void fillDataFromMoments(Moments mom) {
		Area = 5*Math.log10(mom.get_m00());
		if(Double.isNaN(Area))
			Area = 0;
	}
	
	public Instances makeInstances(List<String> classes) {
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		for(String className : headers) {
			if(className.equals(CLASS_HEADER))
				continue;
			
			fvWekaAttributes.add(new Attribute(className));
		}
		
		//Add class attribute
		fvWekaAttributes.add(new Attribute(CLASS_HEADER, classes));
		
		Instances ans = new Instances("BiofilmTests", fvWekaAttributes, 0);
		ans.setClassIndex(ans.numAttributes() - 1);        
		
		return ans;
	}
}
