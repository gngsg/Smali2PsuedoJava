package gng.smali2pj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SmaliMethod {
	String SCOPE;
	String NAME;
	String RETURNTYPE;
	ArrayList<SmaliAnnotation> ANNOTATIONS;
	ArrayList<String> CODE;
	
	ArrayList<String> PARAM_NAMES;
	ArrayList<String> PARAM_TYPES;
	
	HashMap<String, String> VAR_NAMES;
	HashMap<String, SmaliArrayData> ARRAY_DATA;
	HashMap<String, SmaliPackedSwitch> PSWITCHES;
	HashMap<String, SmaliSparseSwitch> SSWITCHES;
	
	boolean ISSTATIC = false;
	
	public SmaliMethod(ArrayList<String> methodChunk){
		ANNOTATIONS = new ArrayList<SmaliAnnotation>();
		CODE = new ArrayList<String>();
		PARAM_NAMES = new ArrayList<String>();
		PARAM_TYPES = new ArrayList<String>();
		
		VAR_NAMES = new HashMap<String, String>();
		ARRAY_DATA = new HashMap<String, SmaliArrayData>();
		PSWITCHES = new HashMap<String, SmaliPackedSwitch>();
		SSWITCHES = new HashMap<String, SmaliSparseSwitch>();
		
		for(int i=0; i<methodChunk.size(); i++){
			String line = methodChunk.get(i).trim();
			// Remove useless comments (only the below operations require information stored in comments
			if ( (line.startsWith(".restart local") || line.startsWith(".end local")) == false ) line = Utils.removeComments(line);
			
			if (i==0){
				if (line.startsWith(".method")) line = Utils.removeFirstWord(line);				
				String[] parts = line.split(" ");
				String last = parts[parts.length-1];
								
				this.SCOPE = "";
				for (int a=0; a<parts.length-1; a++){
					if (parts[a].equals("static")) this.ISSTATIC = true;
					this.SCOPE += parts[a];
					if (a != parts.length-2) this.SCOPE += " ";
				}
				
				this.NAME = last.substring(0, last.indexOf("("));
				this.RETURNTYPE = last.substring(last.indexOf(")")+1);
				this.RETURNTYPE = Utils.determineType(this.RETURNTYPE);
				
				String paramStr = last.substring(last.indexOf("(")+1, last.indexOf(")"));
				this.PARAM_TYPES = Utils.processParamTypes(paramStr);
			}			
			else{
				line = line.trim();
				
				// Handle Parameter
				if (line.startsWith(".parameter")){
					line = Utils.removeFirstWord(line);
					if (line.startsWith(".parameter")){line = "noname" + (this.PARAM_NAMES.size()+1);}
					else{
						if (line.startsWith("\"")) line = line.substring(1, line.length()-1);
					}					
					this.PARAM_NAMES.add(line);
				}
				else if (line.startsWith(".param")){
					line = Utils.removeFirstWord(line);
					if (line.startsWith(".param")){line = "noname" + (this.PARAM_NAMES.size()+1);}
					else{
						int startpos = line.indexOf("\"");
						int endpos = line.indexOf("\"", startpos+1);
						line = line.substring(startpos+1, endpos);
					}
					this.PARAM_NAMES.add(line);
				}
				
				else if (line.startsWith(".prologue")){} // Do Nothing
				else if (line.startsWith(".line")){} // Do Nothing
				
				// Handle Annotations
				else if (line.startsWith(".annotation")){
					ArrayList<String> annoChunk = new ArrayList<String>();
					annoChunk.add(line);					
					for (int j=i+1; j<methodChunk.size(); j++){
						String nLine = methodChunk.get(j).trim();
						annoChunk.add(nLine);
						if (nLine.startsWith(".end annotation")) {
							i = j;
							break;
						}
					}
					this.ANNOTATIONS.add(new SmaliAnnotation(annoChunk));
				}
				
				// Handle Array Data
				else if (line.startsWith(":array")){					
					String key = "ARR_" + line.substring(7);
					ArrayList<String> dataChunk = new ArrayList<String>();
					dataChunk.add(line);
					for (int j=i+1; j<methodChunk.size(); j++){
						String nLine = methodChunk.get(j).trim();
						nLine = Utils.removeComments(nLine);
						dataChunk.add(nLine);
						if (nLine.startsWith(".end array-data")){
							i = j;
							break;
						}
					}
					this.ARRAY_DATA.put(key, new SmaliArrayData(dataChunk));
				}
				
				// Handle Packed-Switches
				else if (line.startsWith(":pswitch_data")){
					String key = "PSWITCH_" + line.substring(14);
					ArrayList<String> switchChunk = new ArrayList<String>();
					switchChunk.add(line);
					for (int j=i+1; j<methodChunk.size(); j++){
						String nLine = methodChunk.get(j).trim();
						nLine = Utils.removeComments(nLine);
						switchChunk.add(nLine);
						if (nLine.startsWith(".end packed-switch")){
							i = j;
							break;
						}
					}
					this.PSWITCHES.put(key, new SmaliPackedSwitch(switchChunk));
				}
				
				// Handle Sparse-Switches
				else if (line.startsWith(":sswitch_data")){
					String key = "SSWITCH_" + line.substring(14);
					ArrayList<String> switchChunk = new ArrayList<String>();
					switchChunk.add(line);
					for (int j=i+1; j<methodChunk.size(); j++){
						String nLine = methodChunk.get(j).trim();
						nLine = Utils.removeComments(nLine);
						switchChunk.add(nLine);
						if (nLine.startsWith(".end sparse-switch")){
							i = j;
							break;
						}
					}
					this.SSWITCHES.put(key, new SmaliSparseSwitch(switchChunk));
				}
				
				// Handle normal code
				else{
					this.CODE.add(line);
				}
			}
		}
		
		if (this.PARAM_NAMES.size() < this.PARAM_TYPES.size()){
			for (int i=this.PARAM_NAMES.size(); i<this.PARAM_TYPES.size(); i++){
				this.PARAM_NAMES.add("p"+(i+1));
			}
		}
		
		// p0 is this if method is NOT STATIC, else p0 is the first parameter
		if (this.ISSTATIC){
			for (int i=0; i<this.PARAM_TYPES.size(); i++){
				VAR_NAMES.put("p"+(i), this.PARAM_NAMES.get(i));
			}
		}
		else{
			VAR_NAMES.put("p0", "this");
			for (int i=0; i<this.PARAM_TYPES.size(); i++){
				VAR_NAMES.put("p"+(i+1), this.PARAM_NAMES.get(i));
			}
		}		
	}
	
	public void writeToFile(BufferedWriter writer) throws IOException{
		if (this.NAME == null || this.SCOPE == null || this.RETURNTYPE == null) Utils.writeLine(writer, "METHOD FORMAT WRONG!!", 1);
		else{
			// HEADER Portion
			String header = this.SCOPE + " " + this.RETURNTYPE + " " + this.NAME;
			
			String paramStr = "";
			
			if (Utils.REPLACE_VAR_NAMES){
				// Uses Param Names from .parameter
				for (int i=0; i<this.PARAM_TYPES.size(); i++){
					paramStr += this.PARAM_TYPES.get(i) + " " + this.PARAM_NAMES.get(i);
					if (i != this.PARAM_TYPES.size()-1) paramStr += ", ";
				}
			}			
			else{
				// Just Use p1.....px for param names
				for (int i=0; i<this.PARAM_TYPES.size(); i++){
					paramStr += this.PARAM_TYPES.get(i) + " p" + (i+1);
					if (i != this.PARAM_TYPES.size()-1) paramStr += ", ";
				}
			}
			
			paramStr = "(" + paramStr + ")";			
			header += paramStr;
			
			// Check Annotations for Throw
			for (int i=0; i<this.ANNOTATIONS.size(); i++){
				SmaliAnnotation anno = this.ANNOTATIONS.get(i);
				if (anno.TYPE.equals("dalvik/annotation/Throws")){
					String value = anno.CONTENTS.get("value");
					if (value.startsWith("{")) value = value.substring(1, value.length()-1);
					String[] types = value.split(";,");
					String throwStr = "";
					for (int j=0; j<types.length; j++){
						throwStr += Utils.determineType(types[j]);
						if (j != types.length-1) throwStr += ", ";
					}
					header += " throws " + throwStr;
				}
			}
			
			header += " {";
			Utils.writeLine(writer, header, 1);
			
			// CODE Portion
			for (int i=0; i<CODE.size(); i++){
				String line = CODE.get(i).trim();
				
				if (line.isEmpty()) continue;
				if (line.startsWith("nop")) continue;
				// Main Processing here, process each line of the code
				String result = processCodeLine(line, writer, i);
				
				if (result.isEmpty() == false){
					// Possible to replace params p0.... with their respective param name
					if (result.contains("\n")){
						String[] allres = result.split("\n");
						for (int r=0; r<allres.length; r++){
							String res = allres[r];
							Utils.writeLine(writer, res, 3);
						}
					}
					else Utils.writeLine(writer, result, 3);
				}
			}
		}
	}
	
	protected String processCodeLine(String line, BufferedWriter writer, int index) throws IOException{
		// Check for Local variables and naming
		if (line.startsWith(".locals")){
			line = Utils.removeFirstWord(line).trim();
			int numVars = Integer.parseInt(line);					
			for (int num=0; num<numVars; num++){
				String key = "v" + num;
				this.VAR_NAMES.put(key, key);
			}
			return "";
		}
		else if (line.startsWith(".local")){
			line = Utils.removeFirstWord(line);
			String[] parts = line.split(", ");
			String key = parts[0];
			String[] secondparts = parts[1].split(":");
			String name = secondparts[0];
			//String type = Utils.determineType(secondparts[1]);
			//this.VAR_NAMES.put(key, name);
			if (writer != null) Utils.writeLine(writer, "//(Assigned name of " + name + " to " + key + ")", 4);
			return "";
		}
		else if (line.startsWith(".restart local")){
			line = Utils.removeFirstWord(line);
			line = Utils.removeFirstWord(line);
			String key = line.substring(0, line.indexOf(" "));
			String name = line.substring(line.indexOf("#")+1, line.indexOf(":"));
			//this.VAR_NAMES.put(key, name);
			if (writer != null) Utils.writeLine(writer, "//(Re-assigned name of " + name + " to " + key + ")", 4);
			return "";
		}
		else if (line.startsWith(".end local")){
			line = Utils.removeFirstWord(line);
			line = Utils.removeFirstWord(line); //remove the first 2 words					
			if (line.indexOf(" ") < 0){
				String key = line;
				//this.VAR_NAMES.put(key, key);
				if (writer != null) Utils.writeLine(writer, "//(Unassigned name of variable " + key + ")", 4);
			}
			else{
				String key = line.substring(0, line.indexOf(" "));
				String name = line.substring(line.indexOf("#")+1, line.indexOf(":"));
				//this.VAR_NAMES.put(key, key);
				if (writer != null) Utils.writeLine(writer, "//(Unassigned name of " + name + " from " + key + ")", 4);
			}
			return "";
		}
		
		// Check for Method Calls (Invoke)
		else if (line.startsWith("invoke-direct") || line.startsWith("invoke-virtual") ||
				line.startsWith("invoke-static") || line.startsWith("invoke-super") || line.startsWith("invoke-interface")){
			boolean isStaticInvoke = false;
			boolean isSuperInvoke = false;
			if (line.startsWith("invoke-static")) isStaticInvoke = true;					
			if (line.startsWith("invoke-super")) isSuperInvoke = true;
			
			line = Utils.removeFirstWord(line);
			String methodCall = s2pj_InvokeMethod(line, isStaticInvoke, isSuperInvoke);
			// Check next 2 lines for a move-result
			String nextLine = CODE.get(index+1);
			if (nextLine != null){
				if (nextLine.trim().isEmpty()) nextLine = CODE.get(index+2);
			}
			if (nextLine != null){
				nextLine = nextLine.trim();
				if (nextLine.startsWith("move-result")){
					String front = s2pj_MoveResult(Utils.removeFirstWord(nextLine));
					if (writer != null) Utils.writeLine(writer, front + methodCall, 3);
				}
				else{
					if (writer != null) Utils.writeLine(writer, methodCall, 3);
				}
			}
			return "";
		}
		else if (line.startsWith("move-result")) 	{return "";} //do nothing
		
		// Check for other normal operations
		else if (line.startsWith(".end method"))	{
			if (writer != null) {
				Utils.writeLine(writer, s2pj_MethodClosing(), 1);
				Utils.writeLine(writer, "", 1);
			}			 
			return "";
		}
		
		else if	(line.startsWith("const")) 			return s2pj_Const(line);
		
		else if	(line.startsWith("add-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "+");
		else if	(line.startsWith("sub-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "-");
		else if	(line.startsWith("mul-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "*");
		else if	(line.startsWith("div-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "/");
		else if	(line.startsWith("rem-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "%");
		else if	(line.startsWith("and-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "&");
		else if	(line.startsWith("or-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "|");
		else if	(line.startsWith("xor-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "^");
		else if	(line.startsWith("shl-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), "<<");
		else if	(line.startsWith("shr-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), ">>");
		else if	(line.startsWith("ushr-int/lit")) 	return s2pj_ArithmeticLiteral(Utils.removeFirstWord(line), ">>>");
		
		else if	(line.startsWith("add-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "+", false);
		else if	(line.startsWith("sub-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "-", false);
		else if	(line.startsWith("mul-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "*", false);
		else if	(line.startsWith("div-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "/", false);
		else if	(line.startsWith("rem-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "%", false);
		else if	(line.startsWith("and-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "&", false);
		else if	(line.startsWith("or-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "|", false);
		else if	(line.startsWith("xor-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "^", false);
		else if	(line.startsWith("shl-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "<<", false);
		else if	(line.startsWith("shr-int/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), ">>", false);
		else if	(line.startsWith("ushr-int/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), ">>>", false);
		else if	(line.startsWith("add-long/2addr"))	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "+", true);
		else if	(line.startsWith("sub-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "-", true);
		else if	(line.startsWith("mul-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "*", true);
		else if	(line.startsWith("div-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "/", true);
		else if	(line.startsWith("rem-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "%", true);
		else if	(line.startsWith("and-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "&", true);
		else if	(line.startsWith("or-long/2addr")) 	return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "|", true);
		else if	(line.startsWith("xor-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "^", true);
		else if	(line.startsWith("shl-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "<<", true);
		else if	(line.startsWith("shr-long/2addr")) return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), ">>", true);
		else if	(line.startsWith("ushr-long/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), ">>>", true);
		else if	(line.startsWith("add-float/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "+", false);
		else if	(line.startsWith("sub-float/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "-", false);
		else if	(line.startsWith("mul-float/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "*", false);
		else if	(line.startsWith("div-float/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "/", false);
		else if	(line.startsWith("rem-float/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "%", false);
		else if	(line.startsWith("add-double/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "+", true);
		else if	(line.startsWith("sub-double/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "-", true);
		else if	(line.startsWith("mul-double/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "*", true);
		else if	(line.startsWith("div-double/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "/", true);
		else if	(line.startsWith("rem-double/2addr"))return s2pj_Arithmetic2Address(Utils.removeFirstWord(line), "%", true);
		
		else if	(line.startsWith("add-double")) 	return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "+", true);
		else if	(line.startsWith("add-long")) 		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "+", true);
		else if	(line.startsWith("add-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "+", false);
		else if	(line.startsWith("sub-double"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "-", true);
		else if	(line.startsWith("sub-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "-", true);
		else if	(line.startsWith("sub-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "-", false);
		else if	(line.startsWith("mul-double"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "*", true);
		else if	(line.startsWith("mul-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "*", true);
		else if	(line.startsWith("mul-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "*", false);
		else if	(line.startsWith("div-double"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "/", true);
		else if	(line.startsWith("div-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "/", true);
		else if	(line.startsWith("div-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "/", false);
		else if	(line.startsWith("rem-double")) 	return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "%", true);
		else if	(line.startsWith("rem-long")) 		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "%", true);
		else if	(line.startsWith("rem-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "%", false);
		else if	(line.startsWith("and-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "&", true);
		else if	(line.startsWith("and-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "&", false);
		else if	(line.startsWith("or-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "|", true);
		else if	(line.startsWith("or-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "|", false);
		else if	(line.startsWith("xor-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "^", true);
		else if	(line.startsWith("xor-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "^", false);
		else if	(line.startsWith("shl-long"))		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "<<", true);
		else if	(line.startsWith("shl-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), "<<", false);
		else if	(line.startsWith("shr-long")) 		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), ">>", true);
		else if	(line.startsWith("shr-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), ">>", false);
		else if	(line.startsWith("ushr-long")) 		return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), ">>>", true);
		else if	(line.startsWith("ushr-")) 			return s2pj_ArithmeticNormal(Utils.removeFirstWord(line), ">>>", false);
		else if	(line.startsWith("neg-double"))		return s2pj_Negate(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("neg-long"))		return s2pj_Negate(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("neg"))			return s2pj_Negate(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("not-long"))		return s2pj_Not(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("not"))			return s2pj_Not(Utils.removeFirstWord(line), false);
		
		else if	(line.startsWith("if-eqz")) 		return s2pj_IfZero(Utils.removeFirstWord(line), "==");
		else if	(line.startsWith("if-nez")) 		return s2pj_IfZero(Utils.removeFirstWord(line), "!=");
		else if	(line.startsWith("if-ltz")) 		return s2pj_IfZero(Utils.removeFirstWord(line), "<");
		else if	(line.startsWith("if-gtz")) 		return s2pj_IfZero(Utils.removeFirstWord(line), ">");
		else if	(line.startsWith("if-lez")) 		return s2pj_IfZero(Utils.removeFirstWord(line), "<=");
		else if	(line.startsWith("if-gez")) 		return s2pj_IfZero(Utils.removeFirstWord(line), ">=");
		else if	(line.startsWith("if-eq")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), "==");
		else if	(line.startsWith("if-ne")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), "!=");
		else if	(line.startsWith("if-lt")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), "<");
		else if	(line.startsWith("if-gt")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), ">");
		else if	(line.startsWith("if-le")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), "<=");
		else if	(line.startsWith("if-ge")) 			return s2pj_If2Operands(Utils.removeFirstWord(line), ">=");
		
		else if	(line.startsWith("int-to-long")) 	return s2pj_ToType(Utils.removeFirstWord(line), "long", false, true);
		else if	(line.startsWith("int-to-float")) 	return s2pj_ToType(Utils.removeFirstWord(line), "float", false, false);
		else if	(line.startsWith("int-to-double")) 	return s2pj_ToType(Utils.removeFirstWord(line), "double", false, true);
		else if	(line.startsWith("long-to-int")) 	return s2pj_ToType(Utils.removeFirstWord(line), "int", true, false);
		else if	(line.startsWith("long-to-float")) 	return s2pj_ToType(Utils.removeFirstWord(line), "float", true, false);
		else if	(line.startsWith("long-to-double")) return s2pj_ToType(Utils.removeFirstWord(line), "double", true, true);
		else if	(line.startsWith("float-to-int"))	return s2pj_ToType(Utils.removeFirstWord(line), "int", false, false);
		else if	(line.startsWith("float-to-long"))	return s2pj_ToType(Utils.removeFirstWord(line), "long", false, true);
		else if	(line.startsWith("float-to-double"))return s2pj_ToType(Utils.removeFirstWord(line), "double", false, true);
		else if	(line.startsWith("double-to-int"))	return s2pj_ToType(Utils.removeFirstWord(line), "int", true, false);
		else if	(line.startsWith("double-to-long"))	return s2pj_ToType(Utils.removeFirstWord(line), "long", true, true);
		else if	(line.startsWith("double-to-float"))return s2pj_ToType(Utils.removeFirstWord(line), "float", true, false);
		else if	(line.startsWith("int-to-byte")) 	return s2pj_ToType(Utils.removeFirstWord(line), "byte", false, false);
		else if	(line.startsWith("int-to-char")) 	return s2pj_ToType(Utils.removeFirstWord(line), "char", false, false);
		else if	(line.startsWith("int-to-short")) 	return s2pj_ToType(Utils.removeFirstWord(line), "short", false, false);
		
		else if	(line.startsWith("sget-wide"))		return s2pj_StaticGet(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("sget"))		 	return s2pj_StaticGet(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("sput-wide"))		return s2pj_StaticPut(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("sput"))		 	return s2pj_StaticPut(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("iput-wide")) 		return s2pj_InstancePut(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("iput")) 			return s2pj_InstancePut(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("iget-wide")) 		return s2pj_InstanceGet(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("iget")) 			return s2pj_InstanceGet(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("aget-wide"))		return s2pj_ArrayGet(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("aget")) 			return s2pj_ArrayGet(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("aput-wide"))		return s2pj_ArrayPut(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("aput")) 			return s2pj_ArrayPut(Utils.removeFirstWord(line), false);
						
		else if	(line.startsWith("cmpl")) 			return s2pj_Compare(Utils.removeFirstWord(line), "l");
		else if	(line.startsWith("cmpg")) 			return s2pj_Compare(Utils.removeFirstWord(line), "g");
		else if	(line.startsWith("cmp")) 			return s2pj_Compare(Utils.removeFirstWord(line), "");
		
		else if	(line.startsWith("return-void")) 	return s2pj_ReturnVoid();
		else if	(line.startsWith("return")) 		return s2pj_ReturnObject(Utils.removeFirstWord(line));
		
		else if	(line.startsWith("packed-switch")) 	return s2pj_PackedSwitch(Utils.removeFirstWord(line));
		else if	(line.startsWith("sparse-switch")) 	return s2pj_SparseSwitch(Utils.removeFirstWord(line));
		
		else if	(line.startsWith("new-array")) 		return s2pj_NewArray(Utils.removeFirstWord(line));
		else if	(line.startsWith("new-instance")) 	return s2pj_NewInstance(Utils.removeFirstWord(line));
		else if	(line.startsWith("array-length")) 	return s2pj_ArrayLength(Utils.removeFirstWord(line));				
		else if	(line.startsWith("instance-of"))	return s2pj_InstanceOf(Utils.removeFirstWord(line));
		else if	(line.startsWith("check-cast")) 	return s2pj_CheckCast(Utils.removeFirstWord(line));
		else if	(line.startsWith("move-exception"))	return s2pj_MoveException(Utils.removeFirstWord(line));
		else if	(line.startsWith("move-wide"))		return s2pj_Move(Utils.removeFirstWord(line), true);
		else if	(line.startsWith("move"))			return s2pj_Move(Utils.removeFirstWord(line), false);
		else if	(line.startsWith("throw")) 			return s2pj_Throw(Utils.removeFirstWord(line));
		else if	(line.startsWith("goto")) 			return s2pj_Goto(Utils.removeFirstWord(line));
		else if	(line.startsWith("monitor-enter"))	return s2pj_MonitorEnter(Utils.removeFirstWord(line));
		else if	(line.startsWith("monitor-exit"))	return s2pj_MonitorExit(Utils.removeFirstWord(line));
		else if	(line.startsWith("fill-array-data"))return s2pj_FillArrayData(Utils.removeFirstWord(line));
		else if	(line.startsWith(":try_start"))		{if (writer != null) Utils.writeLine(writer, s2pj_TryStart(line), 2); return "";}
		else if	(line.startsWith(":try_end")) 		{if (writer != null) Utils.writeLine(writer, s2pj_TryEnd(line), 2); return "";}
		else if	(line.startsWith(".catchall")) 		{if (writer != null) Utils.writeLine(writer, s2pj_CatchAll(Utils.removeFirstWord(line)), 2); return "";}
		else if	(line.startsWith(".catch")) 		{if (writer != null) Utils.writeLine(writer, s2pj_Catch(Utils.removeFirstWord(line)), 2); return "";}
		else if	(line.startsWith(":")) 				{if (writer != null) Utils.writeLine(writer, s2pj_GotoTag(line), 1); return "";}
		else {
			return "OMG UNKNOWN CODE!!!! (" + line + ")";
		}
	}
	
	private String getVarName(String name){
		if (Utils.REPLACE_VAR_NAMES) return this.VAR_NAMES.get(name);
		else return name;
	}
	
	
	// **************************************************************************************
	// **************************************************************************************	
	// **************************************************************************************
	// **************************************************************************************
	
	// Methods to convert Smali to PsuedoJava
	private String s2pj_InvokeMethod(String line, boolean isStatic, boolean isSuper){
		line = line.trim();
		String[] parts = line.split("}, ");
		String argumentStr = parts[0].substring(1).trim(); // removes the first {
		String commandStr = parts[1].trim();
		
		ArrayList<String> args = new ArrayList<String>();
		ArrayList<String> argsTypes = new ArrayList<String>();		
		String outArguments = "";
		
		// Handle Arguments
		if (argumentStr.isEmpty()){} // Do nothing
		else if (argumentStr.indexOf(" .. ") >= 0){ 
			// arguments represented in range form
			String[] range = argumentStr.split(" .. ");
			String start = range[0];
			String end = range[1];
			String prefix = start.substring(0,1); // gets the prefix (v or p)
			int startNum = Integer.parseInt(start.substring(1));
			int endNum = Integer.parseInt(end.substring(1));
			
			ArrayList<Integer> tempNums = new ArrayList<Integer>();
			for (int n = startNum; n <= endNum; n++){
				tempNums.add(n);
			}
			for (int i=0; i<tempNums.size(); i++){
				args.add(prefix + tempNums.get(i));
			}
		}
		else{
			// arguments represented in normal form
			String[] argarr = argumentStr.split(", ");
			for (int i=0; i<argarr.length; i++){
				args.add(argarr[i]);
			}
		}
		
		// Handle Command
		String objType = commandStr.substring(0, commandStr.indexOf("->"));
		objType = Utils.determineType(objType);
		String methodName = commandStr.substring(commandStr.indexOf("->") + 2, commandStr.indexOf("("));
		//String returnType = Utils.determineType(commandStr.substring(commandStr.indexOf(")")+1));
		
		String argTypeStr = commandStr.substring(commandStr.indexOf("(")+1, commandStr.indexOf(")"));
		if (argTypeStr.isEmpty() == false){
			argsTypes = Utils.processParamTypes(argTypeStr);
		}
		
		// Determine Caller
		String caller = "";
		if (isStatic == false){
			caller = args.get(0);
			caller = getVarName(caller);
			args.remove(0);			
		}
		else if (isSuper){
			caller = "super";
		}
		
		// Check sizes of arguments and argument types, if correct build the outarg string
		int expectedArgSize = argsTypes.size();
		for (int a=0; a<argsTypes.size(); a++){
			String curType = argsTypes.get(a);
			if (curType.equals("long") || curType.equals("double")) expectedArgSize++;
		}
		if (args.size() != expectedArgSize) outArguments = "OMG!! Num of arguments and argument types does not match!!!";
		else{
			for (int i=0; i<argsTypes.size(); i++){
				String type = argsTypes.get(i);
				String arg = args.get(i);
				if (type.equals("long") || type.equals("double")){
					//arg = "[" + arg + "-" + args.get(i+1) + "]";
					arg += args.get(i+1);
					args.remove(i+1);
				}
				//outArguments += type + " " + arg;
				outArguments += getVarName(arg);				
				if (i != argsTypes.size()-1) outArguments += ", ";
			}
		}
		outArguments = "(" + outArguments + ")";
		
		String concat = ".";
		if (methodName.equals("<init>")){
			methodName =  "new " + objType;
			concat = " = ";
		}
		
		if (isStatic) 	return objType + concat + methodName + outArguments;
		else 			return caller + concat + methodName + outArguments;
	}
	
	private String s2pj_MoveResult(String line){return line + " = ";}
	
	private String s2pj_Const(String line){
		if (line.startsWith("const-class")){
			line = Utils.removeFirstWord(line);
			String name = line.substring(0, line.indexOf(","));
			String value = line.substring(line.indexOf(", ") + 2);
			value = Utils.determineType(value) + ".class";
			return name + " = "  + value + ";";
		}
		else if (line.startsWith("const-wide")){
			//const-wide v2, 0x3fde28c7460698c7L
			String[] words = line.split(" ");
			String[] modeparts = words[0].split("/");
			if (modeparts.length > 1){
				String mode = modeparts[1];
				int paddingmode = -1;
				if (mode.compareTo("16") == 0 || mode.compareTo("32") == 0) paddingmode = Utils.PADDING_INFRONT; 
				else if (mode.compareTo("high16") == 0) paddingmode = Utils.PADDING_BEHIND;
				else return "[ERROR] const-wide Unknown Mode!!!";
				
				line = Utils.removeFirstWord(line);
				String name = line.substring(0, line.indexOf(","));
				String regtype = name.substring(0,1);
				String reg = name.substring(1);
				int adjreg = Integer.parseInt(reg) + 1;				
				String adjname = regtype + adjreg;
				name = getVarName(name);
				String value = line.substring(line.indexOf(", ") + 2);
				value = Utils.padToWide(value, paddingmode);
				value = Utils.splitWideValue(value);
				return name + "(," + adjname + ") = "  + value + ";";
			}
			else{
				line = Utils.removeFirstWord(line);
				String name = line.substring(0, line.indexOf(","));
				String regtype = name.substring(0,1);
				String reg = name.substring(1);
				int adjreg = Integer.parseInt(reg) + 1;				
				String adjname = regtype + adjreg;
				name = getVarName(name);
				String value = line.substring(line.indexOf(", ") + 2);
				value = Utils.splitWideValue(value);
				value = value.substring(0, value.length()-1);
				return name + "(," + adjname + ") = "  + value + ";";
				//return name + " = "  + value + ";";	
			}
		}
		
		// Else
		String[] words = line.split(" ");
		String[] modeparts = words[0].split("/");
		int paddingmode = -1;
		if (modeparts.length > 1){
			String mode = modeparts[1];
			if (mode.compareTo("4") == 0 || mode.compareTo("16") == 0) {} // Do nothing
			else if (mode.compareTo("high16") == 0) paddingmode = Utils.PADDING_BEHIND;
			else return "[ERROR] const-wide Unknown Mode!!!";
		}
		line = Utils.removeFirstWord(line);
		String name = line.substring(0, line.indexOf(","));
		name = getVarName(name);		
		String value = line.substring(line.indexOf(", ") + 2);
		if (paddingmode > 0) value = Utils.padToNormal(value, paddingmode);
		return name + " = "  + value + ";";	
	}
	
	private String s2pj_ArithmeticLiteral(String line, String arith){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String src = getVarName(parts[1]);
		String litValue = parts[2].trim();
		return dest + " = " + src + " " + arith + " " + litValue + ";";
	}	
	
	private String s2pj_Arithmetic2Address(String line, String arith, boolean wide){
		String[] parts = line.split(", ");
		
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		
		String operand = getVarName(parts[1].trim());
		if (wide){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			operand = operand + "(," + adjname + ")";
		}
		return dest + " = " + dest + " " + arith + " " + operand + ";";
	}
	
	private String s2pj_ArithmeticNormal(String line, String arith, boolean wide){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		String src1 = getVarName(parts[1]);
		if (wide){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			src1 = src1 + "(," + adjname + ")";
		}
		String src2 = getVarName(parts[2].trim());
		if (wide){
			String regtype = parts[2].substring(0,1);
			String reg = parts[2].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			src2 = src2 + "(," + adjname + ")";
		}
		return dest + " = " + src1 + " " + arith + " " + src2 + ";";
	}
	
	private String s2pj_Negate(String line, boolean wide){
		String[] parts = line.split(", ");
		
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		
		String operand = getVarName(parts[1]);
		if (wide){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			operand = operand + "(," + adjname + ")";
		}
		return dest + " = -" + operand + ";";
	}
	
	private String s2pj_Not(String line, boolean wide){
		String[] parts = line.split(", ");
		
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		
		String operand = getVarName(parts[1]);
		if (wide){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			operand = operand + "(," + adjname + ")";
		}
		return dest + " = !" + operand + ";";
	}
	
	private String s2pj_IfZero(String line, String cmp){
		String[] parts = line.split(", ");
		String name = getVarName(parts[0]);
		String go = parts[1].trim();
		return "if (" + name + " " + cmp + " 0) goto " + go;
	}
	
	private String s2pj_If2Operands(String line, String cmp){
		String[] parts = line.split(", ");
		String left = getVarName(parts[0]);
		String right = getVarName(parts[1]);
		String go = parts[2].trim();
		return "if (" + left + " " + cmp + " " + right + ") goto " + go;
	}
	
	private String s2pj_ToType(String line, String type, boolean widesrc, boolean widedst){
		String[] parts = line.split(", ");
		
		String dest = getVarName(parts[0]);
		if (widedst){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		
		String src = getVarName(parts[1].trim());
		if (widesrc){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			src = src + "(," + adjname + ")";
		}
		
		return dest + " = (" + type + ") " + src + ";";
	}
	
	private String s2pj_StaticGet(String line, boolean wide){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		String field = parts[1].trim();
		String obj = field.substring(0, field.indexOf("->"));
		obj = Utils.determineType(obj);
		String fieldname = field.substring(field.indexOf("->")+2, field.indexOf(":"));
		String type = field.substring(field.indexOf(":")+1);
		type = Utils.determineType(type);
		
		String out = dest + " = " + obj + "." + fieldname + ";"; 
		if (Utils.SHOW_DEST_TYPE) out = type + " " + out;
		return out;
	}
	
	private String s2pj_StaticPut(String line, boolean wide){
		String[] parts = line.split(", ");
		String value = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			value = value + "(," + adjname + ")";
		}
		String dest = parts[1].trim();
		String obj = dest.substring(0, dest.indexOf("->"));
		obj = Utils.determineType(obj);
		String fieldname = dest.substring(dest.indexOf("->")+2, dest.indexOf(":"));
		String type = dest.substring(dest.indexOf(":")+1);
		type = Utils.determineType(type);
		
		String out = obj + "." + fieldname + " = " + value + ";"; 
		if (Utils.SHOW_DEST_TYPE) out = type + " " + out;
		return out;
	}
	
	private String s2pj_InstancePut(String line, boolean wide){
		String[] parts = line.split(", ");
		String value = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			value = value + "(," + adjname + ")";
		}
		String destObject = getVarName(parts[1]);		
		String destField = parts[2];
		String type = destField.substring(destField.indexOf(":")+1);
		type = Utils.determineType(type);
		destField = destField.substring(destField.indexOf("->")+2, destField.indexOf(":"));
		
		String out = destObject + "." + destField + " = " + value + ";";
		if (Utils.SHOW_DEST_TYPE) out = type + " " + out;		
		return out;
	}
	
	private String s2pj_InstanceGet(String line, boolean wide){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		String srcObject = getVarName(parts[1]);		
		String srcField = parts[2];
		String type = srcField.substring(srcField.indexOf(":")+1);
		type = Utils.determineType(type);
		srcField = srcField.substring(srcField.indexOf("->")+2, srcField.indexOf(":"));
		
		String out = dest + " = " + srcObject + "." + srcField +";";
		if (Utils.SHOW_DEST_TYPE) out = type + " " + out;		
		return out;
	}
	
	private String s2pj_ArrayGet(String line, boolean wide){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		String arrayObj = getVarName(parts[1]);		
		String arrayIndex = getVarName(parts[2].trim());		
		return dest + " = " + arrayObj + "[" + arrayIndex + "];";
	}
	
	private String s2pj_ArrayPut(String line, boolean wide){
		String[] parts = line.split(", ");
		String val = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			val = val + "(," + adjname + ")";
		}
		String arrayObj = getVarName(parts[1]);		
		String arrayIndex = getVarName(parts[2].trim());		
		return arrayObj + "[" + arrayIndex + "] = " + val + ";";
	}
	
	private String s2pj_Compare(String line, String bias){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String src1 = getVarName(parts[1]);
		String src2 = getVarName(parts[2].trim());
		
		String out = "";
		if (bias.equals("g")) 		out += "if ( (" + src1 + " is NaN) || (" + src2 + " is NaN) ) " + dest + " = 1;\nelse ";
		else if (bias.equals("l"))	out += "if ( (" + src1 + " is NaN) || (" + src2 + " is NaN) ) " + dest + " = -1;\nelse ";
		out += dest + " = " + src1 + ".compareTo(" + src2 + ");";
		return out;
	}
	
	private String s2pj_NewArray(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String len = getVarName(parts[1]);
		String type = parts[2].trim();
		type = Utils.determineType(type);
		if (type.endsWith("[]")) type = type.substring(0, type.length()-2);
		return dest + " = new " + type + "[" + len + "];"; 
	}	
	
	private String s2pj_NewInstance(String line){
		String[] parts = line.split(", ");
		String name = getVarName(parts[0]);
		String type = Utils.determineType(parts[1]);
		return type + " " + name + ";";
	}
	
	private String s2pj_ArrayLength(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String array = getVarName(parts[1]);
		return dest + " = " + array + ".length;";		
	}
	
	private String s2pj_InstanceOf(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String obj = getVarName(parts[1]);
		String isType = parts[2].trim();
		isType = Utils.determineType(isType);
		
		String line1 = "if (" + obj + " instanceOf " + isType + ") " + dest + " = 1;\n";
		String line2 =  "else " + dest + " = 0;";
		return line1 + line2;
	}
	
	private String s2pj_CheckCast(String line){
		String[] parts = line.split(", ");
		String name = getVarName(parts[0]);
		String type = parts[1];
		type = Utils.determineType(type);
		
		return name + " = (" + type + ") " + name + ";";
	}
	
	private String s2pj_Move(String line, boolean wide){
		String[] parts = line.split(", ");
		
		String dest = getVarName(parts[0]);
		if (wide){
			String regtype = parts[0].substring(0,1);
			String reg = parts[0].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			dest = dest + "(," + adjname + ")";
		}
		
		String src = getVarName(parts[1]);
		if (wide){
			String regtype = parts[1].substring(0,1);
			String reg = parts[1].substring(1);
			int adjreg = Integer.parseInt(reg) + 1;
			String adjname = regtype + adjreg;
			src = src + "(," + adjname + ")";
		}
		
		return dest + " = " + src + ";";
	}
	
	private String s2pj_TryStart(String line){
		String name = line.substring(line.indexOf("start_")+6);		
		return "try TRY_" + name + "{";
	}
	
	private String s2pj_TryEnd(String line){
		String name = line.substring(line.indexOf("end_")+4);
		return "}TRY_" + name;
	}
	
	private String s2pj_Catch(String line){
		line = line.trim();
		String exceptType = line.substring(0, line.indexOf(" "));
		exceptType = Utils.determineType(exceptType);
		
		line = line.substring(line.indexOf(" "));
		String block = line.substring(line.indexOf("{")+1, line.indexOf("}"));
		String name = block.substring(block.indexOf("start_")+6, block.indexOf(" "));
		name = "TRY_" + name;
		
		line = line.substring(line.indexOf("}")+1).trim();
		String gotoPlace = line;
		
		return "catch " + name + " (" + exceptType + ") goto " + gotoPlace + ";";
	}
	
	private String s2pj_CatchAll(String line){
		line = line.trim();
		String block = line.substring(line.indexOf("{")+1, line.indexOf("}"));
		String name = block.substring(block.indexOf("start_")+6, block.indexOf(" "));
		name = "TRY_" + name;
		
		line = line.substring(line.indexOf("}")+1).trim();
		String gotoPlace = line;
		
		return "catch all " + name + ", goto " + gotoPlace + ";";
	}
	
	private String s2pj_MoveException(String line){return line + " = " + "Thrown Exception";}
	
	private String s2pj_MethodClosing(){return "}";}
	
	private String s2pj_ReturnVoid(){return "return;";}
	
	private String s2pj_ReturnObject(String line){return "return " + getVarName(line.trim()) + ";";}
	
	private String s2pj_GotoTag(String line){return "" + line;}
	
	private String s2pj_Goto(String line){return "goto " + line;}
	
	private String s2pj_Throw(String line){return "throw " + getVarName(line.trim()) + ";";}
	
	private String s2pj_MonitorEnter(String line){return "Monitor-Enter " + getVarName(line.trim());}
	
	private String s2pj_MonitorExit(String line){return "Monitor-Exit " + getVarName(line.trim());}
	
	private String s2pj_FillArrayData(String line){
		String[] parts = line.split(", ");
		String dest = getVarName(parts[0]);
		String arrName = "ARR_" + parts[1].trim().substring(7);
		SmaliArrayData arrData = this.ARRAY_DATA.get(arrName);
		
		String out = dest + " = {";
		for (int i=0; i<arrData.DATA.size(); i++){
			String data = arrData.DATA.get(i).trim();
			String dataOut = "";
			String[] bytes = data.split("t");
			for (int a=bytes.length-1; a>=0; a--){ // Change the endian format
				String cur = bytes[a].trim();
				cur = cur.replace("0x", "");
				if (cur.length() == 1) cur = "0"+cur;
				dataOut +=  cur;
			}			
			out += "0x" + dataOut;
			if (i != arrData.DATA.size()-1) out += ", ";
		}
		out += "};";		
		return out;
	}
	
	private String s2pj_PackedSwitch(String line){
		String[] parts = line.split(", ");
		String src = getVarName(parts[0]);
		String swName = "PSWITCH_" + parts[1].substring(14);
		SmaliPackedSwitch pSwitch = this.PSWITCHES.get(swName);
		
		String out = "switch (" + src + "){\n";
		long startval = pSwitch.STARTVAL;
		for (int i=0; i<pSwitch.SWITCHES.size(); i++){
			String sw = pSwitch.SWITCHES.get(i);
			out += "\tcase " + (startval+i) + " : goto " + sw + "\n";
		}
		out += "}";		
		return out;
	}
	
	private String s2pj_SparseSwitch(String line){
		String[] parts = line.split(", ");
		String src = getVarName(parts[0]);
		String swName = "SSWITCH_" + parts[1].substring(14);
		SmaliSparseSwitch sSwitch = this.SSWITCHES.get(swName);
		
		String out = "switch (" + src + "){\n";
		for (int i=0; i<sSwitch.KEYS.size(); i++){
			String key = sSwitch.KEYS.get(i);
			String value = sSwitch.SWITCHES.get(Long.parseLong(key));
			out += "\tcase " + key + " : goto " + value + "\n";
		}
		out += "}";
		return out;
	}
}
