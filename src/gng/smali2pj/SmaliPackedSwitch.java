package gng.smali2pj;

import java.util.ArrayList;

public class SmaliPackedSwitch {
	String NAME;
	long STARTVAL;
	ArrayList<String> SWITCHES;
		
	public SmaliPackedSwitch(ArrayList<String> swChunk){
		SWITCHES = new ArrayList<String>();
		
		for (int i=0; i<swChunk.size(); i++){
			String line = swChunk.get(i).trim();
			if (i == 0){ // first line must be :pswitch_data_n
				if (line.startsWith(":pswitch_data_")){
					this.NAME = "PSWITCH_" + line.substring(14);
				}
				else return;
			}
			else if (i == 1){ // second line must be .packed-switch 0xXX
				if (line.startsWith(".packed-switch")){
					line = Utils.removeFirstWord(line).trim();
					line = line.replace("0x", "");
					STARTVAL = Long.parseLong(line, 16);
				}
				else return;
			}
			else{
				if (line.startsWith(".end packed-switch")) break;
				SWITCHES.add(line);
			}
		}
	}
}
