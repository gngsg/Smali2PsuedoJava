package gng.smali2pj;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class SmaliAnnotation {
	String SYSTEM;
	String TYPE;
	HashMap<String, String> CONTENTS;
	
	public SmaliAnnotation(ArrayList<String> annoChunk){
		CONTENTS = new HashMap<String, String>();
		
		for (int i=0; i<annoChunk.size(); i++){
			String line = annoChunk.get(i);
			if (line.startsWith(".annotation")){
				line = Utils.removeFirstWord(line);
				String[] parts = line.split(" ");
				this.SYSTEM = parts[0];
				this.TYPE = Utils.determineType(parts[1]);
			}
			else if (line.startsWith(".end annotation")){} // do nothing
			else {				
				line = line.trim();
				String key = line.substring(0,line.indexOf(" = "));
				String value = line.substring(line.indexOf(" = ") + 3);
				if (value.startsWith("{")){
					for (int j=i+1; j<annoChunk.size(); j++){
						String nLine = annoChunk.get(j).trim();
						value += nLine;
						if (nLine.startsWith("}")){
							i = j;
							break;
						}
					}
				}
				CONTENTS.put(key, value);
			}
		}
	}
	
	public void writeToFile(BufferedWriter writer){
		
	}
}
