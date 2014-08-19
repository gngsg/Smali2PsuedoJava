package gng.smali2pj;

import java.io.BufferedWriter;
import java.io.IOException;

public class SmaliField {
	String SCOPE;
	String TYPE;
	String NAME;
	String INITVALUE;
	
	public SmaliField(String fieldChunk){
		if (fieldChunk.startsWith(".field")) fieldChunk = Utils.removeFirstWord(fieldChunk);
		fieldChunk = Utils.removeComments(fieldChunk);
		
		this.INITVALUE = "";
		if (fieldChunk.indexOf(" = ") > 0){
			String[] gotInitValue = fieldChunk.split(" = ");
			fieldChunk = gotInitValue[0];
			this.INITVALUE = gotInitValue[1];
		}
		
		String[] first = fieldChunk.split(" ");
		String leftovers = "";
		if (first.length > 1){
			leftovers = first[first.length-1];
			this.SCOPE = "";
			for (int i=0; i<first.length-1; i++){
				this.SCOPE += first[i];
				if (i != first.length-2) this.SCOPE += " ";
			}
		}
		else {
			this.SCOPE = "";
			leftovers = first[0];
		}
		
		String[] parts = leftovers.split(":");
		this.NAME = parts[0];
		this.TYPE = Utils.determineType(parts[1]);
	}
	
	public void writeToFile(BufferedWriter writer) throws IOException{
		if (this.TYPE == null || this.NAME == null) Utils.writeLine(writer, "FIELD NULL!!", 1);		
		else {
			String out = "";
			if (SCOPE.isEmpty() == false) out += SCOPE + " ";
			out = TYPE + " " + NAME;
			if (this.INITVALUE.isEmpty() == false) out += " = " + this.INITVALUE + ";";
			else out += ";";
			Utils.writeLine(writer, out, 1); 
		}
	}
}
