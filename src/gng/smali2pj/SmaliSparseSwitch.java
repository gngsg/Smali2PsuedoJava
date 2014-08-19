package gng.smali2pj;

import java.util.ArrayList;
import java.util.HashMap;

public class SmaliSparseSwitch {
	String NAME;
	ArrayList<String> KEYS;
	HashMap<Long, String> SWITCHES;

	public SmaliSparseSwitch(ArrayList<String> swChunk){
		SWITCHES = new HashMap<Long, String>();
		KEYS = new ArrayList<String>();
		
		for (int i=0; i<swChunk.size(); i++){
			String line = swChunk.get(i).trim();
			if (i == 0){ // first line must be :sswitch_data_n
				if (line.startsWith(":sswitch_data_")){
					this.NAME = "SSWITCH_" + line.substring(14);
				}
				else return;
			}
			else if (i == 1){ // second line must be .sparse-switch
				// Do nothing
			}
			else{
				if (line.startsWith(".end sparse-switch")) break;
				String[] parts = line.split(" -> ");
				String keystr = parts[0].trim();
				String value = parts[1].trim();				
				long key = Long.parseLong(keystr.replace("0x", ""), 16);
				this.KEYS.add(""+key);
				this.SWITCHES.put(key, value);
			}
		}
	}
}
