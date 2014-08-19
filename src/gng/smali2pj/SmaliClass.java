package gng.smali2pj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SmaliClass {
	private String NAME;
	private String PARENT;
	private ArrayList<String> INTERFACES;
	private ArrayList<SmaliAnnotation> ANNOTATIONS;
	
	private ArrayList<SmaliField>	INSTANCE_FIELDS;
	private ArrayList<SmaliField> 	STATIC_FIELDS;
	private ArrayList<SmaliMethod> 	DIRECT_METHODS;
	private ArrayList<SmaliMethod>	VIRTUAL_METHODS;
	
	public SmaliClass(ArrayList<String> headerChunk){
		INTERFACES = new ArrayList<String>();
		ANNOTATIONS = new ArrayList<SmaliAnnotation>();
		
		INSTANCE_FIELDS = new ArrayList<SmaliField>();
		STATIC_FIELDS = new ArrayList<SmaliField>();
		DIRECT_METHODS = new ArrayList<SmaliMethod>();
		VIRTUAL_METHODS = new ArrayList<SmaliMethod>();
		
		// headerChunk includes the .class .super .implements .annotation
		for (int i=0; i<headerChunk.size(); i++){
			String line = headerChunk.get(i);
			line = Utils.removeComments(line);
			if 	(line.startsWith(".class")){
				String[] parts = line.split(" ");
				String lastElem = parts[parts.length-1];
				this.NAME = Utils.determineType(Utils.removeFirstWord(lastElem));
			}
			else if (line.startsWith(".super")) this.PARENT = Utils.determineType(Utils.removeFirstWord(line));
			else if (line.startsWith(".implements")){
				String imp = Utils.determineType(Utils.removeFirstWord(line));
				this.INTERFACES.add(imp);
			}
			else if (line.startsWith(".annotation")){
				ArrayList<String> annoChunk = new ArrayList<String>();
				annoChunk.add(line);
				for (int j=i+1; j<headerChunk.size(); j++){
					String annoLine = headerChunk.get(j);
					annoChunk.add(annoLine);
					if (annoLine.startsWith(".end annotation")){
						i = j;
						break;
					}
				}
				this.ANNOTATIONS.add(new SmaliAnnotation(annoChunk));
			}
		}
	}
	
	public void initInstanceFields(ArrayList<String> iFieldChunk){
		// iFieldChunk includes .field
		for (int i=0; i<iFieldChunk.size(); i++){
			String line = iFieldChunk.get(i);
			if (line.startsWith(".field")){
				this.INSTANCE_FIELDS.add(new SmaliField(line));
			}
		}
	}
	
	public void initStaticFields(ArrayList<String> sFieldChunk){
		// sFieldChunk includes .field
		for (int i=0; i<sFieldChunk.size(); i++){
			String line = sFieldChunk.get(i);
			if (line.startsWith(".field")){
				this.STATIC_FIELDS.add(new SmaliField(line));
			}
		}
	}
	
	public void addDirectMethod(ArrayList<String> methodChunk){
		this.DIRECT_METHODS.add(new SmaliMethod(methodChunk));
	}
	
	public void addVirtualMethod(ArrayList<String> methodChunk){
		this.VIRTUAL_METHODS.add(new SmaliMethod(methodChunk));
	}
	
	public void writeToFile(BufferedWriter writer) throws IOException{
		String intfaces = "";
		for (int i=0; i<this.INTERFACES.size(); i++){
			String value = this.INTERFACES.get(i);
			intfaces += value;
			if (i != this.INTERFACES.size()-1) intfaces += ", ";
		}
		String out = "class " + NAME;
		if (this.PARENT != null && this.PARENT.isEmpty() == false) out += " extends " + PARENT;
		if (intfaces.isEmpty() == false) out += " implements " + intfaces;
		out += " {";
		Utils.writeLine(writer, out, 0);
		Utils.writeLine(writer, "", 0);
		
		for (int i=0; i<this.ANNOTATIONS.size(); i++){this.ANNOTATIONS.get(i).writeToFile(writer);}
		
		if (this.STATIC_FIELDS.size() > 0) Utils.writeLine(writer, "// Static Fields", 1);
		for (int i=0; i<this.STATIC_FIELDS.size(); i++)		{this.STATIC_FIELDS.get(i).writeToFile(writer);}
		Utils.writeLine(writer, "", 0);
		
		if (this.INSTANCE_FIELDS.size() > 0) Utils.writeLine(writer, "// Instance Fields", 1);
		for (int i=0; i<this.INSTANCE_FIELDS.size(); i++)	{this.INSTANCE_FIELDS.get(i).writeToFile(writer);}
		Utils.writeLine(writer, "", 0);
		
		if (this.DIRECT_METHODS.size() > 0) Utils.writeLine(writer, "// Direct Methods", 1);
		for (int i=0; i<this.DIRECT_METHODS.size(); i++)	{this.DIRECT_METHODS.get(i).writeToFile(writer);}
		Utils.writeLine(writer, "", 0);
		
		if (this.VIRTUAL_METHODS.size() > 0) Utils.writeLine(writer, "// Virtual Methods", 1);
		for (int i=0; i<this.VIRTUAL_METHODS.size(); i++)	{this.VIRTUAL_METHODS.get(i).writeToFile(writer);}
		Utils.writeLine(writer, "", 0);
		
		Utils.writeLine(writer, "}", 0);
	}
}