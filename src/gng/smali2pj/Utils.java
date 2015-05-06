package gng.smali2pj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {
	public static int PORTION_HEADER = 0;
	public static int PORTION_INSTANCE_FIELDS = 1;
	public static int PORTION_STATIC_FIELDS = 2;
	public static int PORTION_DIRECT_METHODS = 3;
	public static int PORTION_VIRTUAL_METHODS = 4;
	static String MARKER_INSTANCE_FIELDS = "# instance fields";
	static String MARKER_STATIC_FIELDS = "# static fields";
	static String MARKER_DIRECT_METHODS = "# direct methods";
	static String MARKER_VIRTUAL_METHODS = "# virtual methods";
	
	public static boolean SHOW_DEST_TYPE = false;
	public static boolean REPLACE_VAR_NAMES = false;
	
	public static ArrayList<String> processParamTypes(String line){
		ArrayList<String> params = new ArrayList<String>();
		String curChar = "";
		String curElem = "";
		String arr = "";
		for (int i=0; i<line.length(); i++){
			curChar = line.substring(i, i+1);
			if(isArrayType(curChar)) {
				arr += "[";
				continue;
			}
			
			if (curElem.isEmpty()){
				if (isBasicType(curChar)) {
					params.add(arr + curChar);
					arr = "";
				}
				else curElem += curChar;				
			}
			else{
				if (curChar.equals(";")){
					params.add(arr + curElem);
					curElem = "";
					arr = "";
				}
				else curElem += curChar;
			}
		}
		
		for (int i=0; i<params.size(); i++){
			params.set(i, determineType(params.get(i)));
		}
		
		return params;
	}
	
	private static boolean isBasicType(String curChar){
		if (curChar.equals("V")) return true;
		else if (curChar.equals("Z")) return true;
		else if (curChar.equals("B")) return true;
		else if (curChar.equals("S")) return true;
		else if (curChar.equals("C")) return true;
		else if (curChar.equals("I")) return true;
		else if (curChar.equals("F")) return true;
		else if (curChar.equals("J")) return true;
		else if (curChar.equals("D")) return true;
		else return false;
	}
	
	private static boolean isArrayType(String curChar){
		if (curChar.equals("[")) return true;
		else return false;
	}
	
	public static String determineType(String type){
		type = type.trim();
		if (type.isEmpty()) return "";
		
		String arrAppend = "";
		for (int i=0; i<type.length(); i++){
			if (type.substring(i, i+1).equals("[")){
				arrAppend += "[]";
				type = type.substring(i+1);
			}
			else break;
		}
		
		if (type.equals("V")) return "void";
		else if (type.equals("Z")) return "boolean"+arrAppend;
		else if (type.equals("B")) return "byte"+arrAppend;
		else if (type.equals("S")) return "short"+arrAppend;
		else if (type.equals("C")) return "char"+arrAppend;
		else if (type.equals("I")) return "int"+arrAppend;
		else if (type.equals("F")) return "float"+arrAppend;
		else if (type.equals("J")) return "long"+arrAppend;
		else if (type.equals("D")) return "double"+arrAppend;
		else {
			// Objects			
			if (type.startsWith("L")){
				// Remove last char if it is ;
				if (type.substring(type.length()-1).equals(";")) return type.substring(1, type.length()-1) + arrAppend;
				else return type.substring(1) + arrAppend;
			}
			else return null;
		}
	}
	
	public static String removeFirstWord(String line){
		return line.substring(line.indexOf(" ")+1);
	}
	
	public static void writeLine(BufferedWriter writer, String line, int numTabs) throws IOException{				
		write(writer, line + "\n", numTabs);
	}
	
	public static void write(BufferedWriter writer, String line, int numTabs) throws IOException{
		String output = line;
		for (int i=0; i<numTabs; i++) output = "\t" + output;
		writer.write(output);
	}
	
	public static int isNextPortion(String line){
		if 		(line.startsWith(MARKER_INSTANCE_FIELDS)) 	return PORTION_INSTANCE_FIELDS;
		else if (line.startsWith(MARKER_STATIC_FIELDS)) 	return PORTION_STATIC_FIELDS;
		else if	(line.startsWith(MARKER_DIRECT_METHODS))	return PORTION_DIRECT_METHODS;
		else if (line.startsWith(MARKER_VIRTUAL_METHODS))	return PORTION_VIRTUAL_METHODS;
		else return -1;
	}
	
	public static String removeComments(String line){
		if (line.indexOf("#") >= 0){
			line = line.substring(0, line.indexOf("#"));
		}
		return line;
	}
	
	/*
	private String s2pj_IfLessThan(String line){
		String[] parts = line.split(", ");
		String left = getVarName(parts[0]);
		String right = getVarName(parts[1]);
		String go = parts[2].trim();
		return "if (" + left + " < " + right + ") goto " + go;
	}
	
	private String s2pj_IfGreaterThan(String line){
		String[] parts = line.split(", ");
		String left = getVarName(parts[0]);
		String right = getVarName(parts[1]);
		String go = parts[2].trim();
		return "if (" + left + " > " + right + ") goto " + go;
	}
	
	private String s2pj_IfLesserEqual(String line){
		String[] parts = line.split(", ");
		String left = getVarName(parts[0]);
		String right = getVarName(parts[1]);
		String go = parts[2].trim();
		return "if (" + left + " <= " + right +") goto " + go;
	}
	
	private String s2pj_IfGreaterEqual(String line){
		String[] parts = line.split(", ");
		String left = getVarName(parts[0]);
		String right = getVarName(parts[1]);
		String go = parts[2].trim();
		return "if (" + left + " >= " + right +") goto " + go;
	}
	*/
	
	/*
	 private String s2pj_DivIntLit8(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String src = getVarName(parts[1]);
		String val = parts[2];
		return dest + " = " + src + "/" + val + ";"; 
	}
	
	private String s2pj_AddIntLit8(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String src = getVarName(parts[1]);
		String val = parts[2].trim();
		return dest + " = " + src + " + " + val + ";";
	}
	*/
}
