package sena;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trainer extends Logger {
	private String path;
	private Parameters params;
	Map<String, Image_Trainer> trainerData;
	List<Image_Data> shuffledTrainingData;
	
	List<String> classList;
	
	public Trainer(String path, Parameters params) {
		this.path = path;
		this.params = params;
		this.trainerData = new HashMap<String, Image_Trainer>();
		this.shuffledTrainingData = null;
		this.classList = new ArrayList<String>();
				
	}

	public void train() {
		File dir = new File(path);	
		int classNumber = 0;
		for (File dr: dir.listFiles()) {
			if(!dr.isDirectory())
				continue;

			String className = dr.getName();
			String subPath = dir.getAbsolutePath()+"\\"+className;
			File classFolder = new File(subPath);
			
			Utils.resetFolder(subPath+"\\"+Config.FILTER_FOLDER);
			Utils.resetFolder(subPath+"\\"+Config.CONTOURS_FOLDER);

			
			classList.add(className);
			
			for(File imgFile: classFolder.listFiles()) {			

				if(!imgFile.isFile())
					continue;

				String imagePath = subPath+"\\"+imgFile.getName();
				Image_Trainer trainer = new Image_Trainer(imagePath, params, className);
				trainerData.put(className, trainer);

				if(params.saveImages) {
					String contoursPath = trainer.folderPath+"\\"+Config.CONTOURS_FOLDER+"\\"+imgFile.getName();
					Saver.saveTrainerContoursImage(trainer, contoursPath, Config.COLORS_RGB[classNumber]);
				}	
			}
			classNumber++;

		}
	}
	
	public void writeDataFiles() {
		log("Making CSV file "+Config.TRAIN_CSV_DATA+"...");
		String csvPath = path+"\\"+Config.TRAIN_CSV_DATA;
		if(Saver.makeCsvData(getShuffledTrainingData(), csvPath))
			log("CSV file save successful");
		else
			log("Impossible to make CSV file. Please close it!!!");
		
		log("Makign ARFF file "+Config.TRAIN_ARFF_DATA+"...");
		String arffPath = path+"\\"+Config.TRAIN_ARFF_DATA;
		if(Saver.makeArffData(getShuffledTrainingData(), arffPath, classList))
			log("ARFF file save successful");
		else
			log("Impossible to make ARFF file. Please close it!!!");
	}

	private List<Image_Data> getShuffledTrainingData() {
		if(shuffledTrainingData != null)
			return shuffledTrainingData;

		shuffledTrainingData = new ArrayList<Image_Data>();
		for(String key : trainerData.keySet()) {
			Image_Trainer trainer = trainerData.get(key);
			for(Image_Data d : trainer.getData()) {
				shuffledTrainingData.add(d);
			}	
		}
		Collections.shuffle(shuffledTrainingData);
		return shuffledTrainingData;
	}
	
	public List<String> getClasses() {
		return classList;
	}
}
