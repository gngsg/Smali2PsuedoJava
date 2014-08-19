package gng.smali2pj;

import java.util.ArrayList;

public class SmaliArrayData {
	String NAME;
	int ELEMENT_LENGTH;
	ArrayList<String> DATA;
	
	public SmaliArrayData(ArrayList<String> dataChunk){
		DATA = new ArrayList<String>();
		
		for (int i=0; i<dataChunk.size(); i++){
			String line = dataChunk.get(i).trim();
			if (i == 0){ // first line must be :array_n
				if (line.startsWith(":array_")){
					this.NAME = line.substring(7);
				}
				else return;
			}
			else if (i == 1){ // second line must be .array-data 0xX
				if (line.startsWith(".array-data")){
					line = Utils.removeFirstWord(line);
					line = line.replace("0x", "");
					this.ELEMENT_LENGTH = Integer.parseInt(line, 16);
				}
				else return;
			}
			else{
				if (line.startsWith(".end array-data")) break;
				DATA.add(line);
			}
		}
	}
}
