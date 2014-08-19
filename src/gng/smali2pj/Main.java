package gng.smali2pj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	/**
	 * @param args
	 */
	
	private static String HELP_MSG = "Usage: java -jar <filename>.jar inputFile/Directory [--use-param-names] \n\t" +
			" If input is a file, it will convert the file only \n\t" +
			" If input is a directory, it will convert all *.smali files in the immediate directory only. (No Recursvie Directory)\n\t\t" + 
			" Outputs will be stored in the s2pjout folder\n\t" +
			" if --use-param-names is specified, method params (p1, p2, ..etc..) will be replaced by their specific names\n";
	
	public static void main(String[] args) {
		// Sets the Filename
		if (args.length < 1) {
			System.out.println(HELP_MSG);
			return;
		}
		
		if (args.length > 1){
			if (args[1].equals("--use-param-names")) Utils.REPLACE_VAR_NAMES = true;
			else	 Utils.REPLACE_VAR_NAMES = false;
		}
		
		String inputPath = args[0];
		File ff = new File(inputPath);
		
		if (ff.isDirectory()){ // Handle Directory
			String outpath = inputPath + "\\s2pjout\\";
			File fout = new File(outpath);
			fout.mkdir();
			File[] allFiles = ff.listFiles();
			for (int i=0; i<allFiles.length; i++){
				File curFile = allFiles[i];
				if (curFile.isFile() && curFile.getPath().endsWith(".smali")){					
					File outFile = new File(outpath + curFile.getName() + ".java");					
					System.out.print("Processing " + curFile.getName() + "... ");
					processSmaliFile(curFile, outFile);
					System.out.print("Done\n");
				}
			}
		}
		else{ // Handle single file
			String outputPath = inputPath + ".java";			
			File inFile = ff;
			File outFile = new File(outputPath);
			System.out.print("Processing " + inputPath + "... ");
			processSmaliFile(inFile, outFile);
			System.out.print("Done\n");
		}
	}

	private static void processSmaliFile(File inFile, File outFile){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
			int inPortion = 0;
			
			// Header portion
			ArrayList<String> headers = new ArrayList<String>();
			String line = reader.readLine();
			while (line != null){
				inPortion = Utils.isNextPortion(line);
				if (inPortion != -1) break;				
				headers.add(line);
				line = reader.readLine();
			}			
			SmaliClass smaliClass = new SmaliClass(headers);
			
			// Static Fields Portion
			if (inPortion == Utils.PORTION_STATIC_FIELDS){
				ArrayList<String> staticFields = new ArrayList<String>();				
				line = reader.readLine();
				while (line != null){
					inPortion = Utils.isNextPortion(line);
					if (inPortion != -1) break;					
					staticFields.add(line);
					line = reader.readLine();
				}
				smaliClass.initStaticFields(staticFields);
			}			
			
			// Instance Fields Portion
			if (inPortion == Utils.PORTION_INSTANCE_FIELDS){
				ArrayList<String> instanceFields = new ArrayList<String>();				
				line = reader.readLine();
				while (line != null){
					inPortion = Utils.isNextPortion(line);
					if (inPortion != -1) break;					
					instanceFields.add(line);
					line = reader.readLine();
				}
				smaliClass.initInstanceFields(instanceFields);
			}			
			
			// Direct Method Portion		
			if (inPortion == Utils.PORTION_DIRECT_METHODS){
				line = reader.readLine();
				while (line != null){
					inPortion = Utils.isNextPortion(line);
					if (inPortion != -1) break;
					ArrayList<String> methodChunk = new ArrayList<String>();
					if (line.startsWith(".method")){
						methodChunk.add(line);
						line = reader.readLine();
						while (line != null){
							methodChunk.add(line);
							if (line.startsWith(".end method")) break;
							line = reader.readLine();
						}
						smaliClass.addDirectMethod(methodChunk);
					}
					line = reader.readLine();
				}	
			}
			
			// Virtual Method Portion
			if (inPortion == Utils.PORTION_VIRTUAL_METHODS){
				line = reader.readLine();
				while (line != null){
					inPortion = Utils.isNextPortion(line);
					if (inPortion != -1) break;
					ArrayList<String> methodChunk = new ArrayList<String>();
					if (line.startsWith(".method")){
						methodChunk.add(line);
						line = reader.readLine();
						while (line != null){
							methodChunk.add(line);
							if (line.startsWith(".end method")) break;
							line = reader.readLine();
						}
						smaliClass.addVirtualMethod(methodChunk);
					}
					line = reader.readLine();
				}	
			}
			
			// Print the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			smaliClass.writeToFile(writer);
			
			
			// Clean up
			reader.close();
			writer.close();
		}
		catch (IOException e){e.printStackTrace();}
	}
}
