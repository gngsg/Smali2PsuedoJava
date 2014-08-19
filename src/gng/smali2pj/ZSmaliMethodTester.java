package gng.smali2pj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ZSmaliMethodTester extends SmaliMethod{
	
	public ZSmaliMethodTester(){
		super(new ArrayList<String>());
		CODE = new ArrayList<String>();
	}
	
	public ZSmaliMethodTester(ArrayList<String> methodChunk){
		super(methodChunk);
	}
	
	public String processCode() throws IOException{
		BufferedWriter writer = null;
		String result = "";
		for (int i=0; i<CODE.size(); i++){
			String line = CODE.get(i).trim();
						
			if (line.isEmpty()) continue;
			if (line.startsWith("nop")) continue;
			// Main Processing here, process each line of the code
			 result += processCodeLine(line, writer, i);
		}
		return result;
	}
	
	public void runTests() throws IOException{
		System.out.println("Starting SmaliMethod Tests");
		
		// 00: nop
		test("01: move",				this.processCodeLine("move v1, v0", null, 0), 						"v1 = v0;");
		test("02: move/from16",			this.processCodeLine("move/from16 v0, v17", null, 0), 				"v0 = v17;");
		test("03: move/16",				this.processCodeLine("move/16 v15, v17", null, 0), 					"v15 = v17;");
		test("04: move-wide",			this.processCodeLine("move-wide v2, v0", null, 0), 					"v2(,v3) = v0(,v1);");
		test("05: move-wide/from16",	this.processCodeLine("move-wide/from16 v0, v17", null, 0), 			"v0(,v1) = v17(,v18);");
		test("06: move-wide/16",		this.processCodeLine("move-wide/16 v15, v17", null, 0), 			"v15(,v16) = v17(,v18);");
		test("07: move-object",			this.processCodeLine("move-object v1, v3", null, 0), 				"v1 = v3;");
		test("08: move-object/from16",	this.processCodeLine("move-object/from16 v1, v16", null, 0), 		"v1 = v16;");
		test("09: move-object/16",		this.processCodeLine("move-object/16 v17, v16", null, 0), 			"v17 = v16;");
		// 0A: move-result
		// 0B: move-result-wide
		// 0C: move-result-object
		// 0D: move-exception
		test("0E: return-void",			this.processCodeLine("return-void", null, 0), 						"return;");
		test("0F: return",				this.processCodeLine("return v0", null, 0), 						"return v0;");
		test("10: return-wide",			this.processCodeLine("return-wide v0", null, 0), 					"return v0;");
		test("11: return-object",		this.processCodeLine("return-object v1", null, 0), 					"return v1;");
		
		// test for negatives also?
		// Normal constants are left sign extended
		// high16 constants are right sign extended
		test("12: const/4",				this.processCodeLine("const/4 v13, 0x1", null, 0), 						"v13 = 0x1;");
		test("13: const/16",			this.processCodeLine("const/16 v13, 0x1", null, 0),						"v13 = 0x1;");
		test("14: const", 				this.processCodeLine("const v3, 0x1020011", null, 0),					"v3 = 0x1020011;");
		test("15: const/high16", 		this.processCodeLine("const/high16 v4, 0x10a", null, 0), 				"v4 = 0x10a00000;");
		test("16: const-wide/16", 		this.processCodeLine("const-wide/16 v3, 0x1388", null, 0), 				"v3(,v4) = 0x00000000 00001388;");
		test("17: const-wide/32", 		this.processCodeLine("const-wide/32 v0, 0x493e0", null, 0), 			"v0(,v1) = 0x00000000 000493e0;");
		test("18: const-wide", 			this.processCodeLine("const-wide v2, 0x3fde28c7460698c7L", null, 0), 	"v2(,v3) = 0x3fde28c7 460698c7;");
		test("19: const-wide/high16",	this.processCodeLine("const-wide/high16 v9, 0x4059", null, 0), 			"v9(,v10) = 0x40590000 00000000;");
		test("1A: const-string", 		this.processCodeLine("const-string v10, \"FragmentManager\"", null, 0), "v10 = \"FragmentManager\";");
		// 1B: const-string-jumbo
		test("1C: const-class", 		
				this.processCodeLine("const-class v10, Landroid/graphics/drawable/Drawable;", null, 0), 
				"v10 = android/graphics/drawable/Drawable.class;");
		test("1D: monitor-enter", 		this.processCodeLine("monitor-enter v3", null, 0), "Monitor-Enter v3");
		test("1E: monitor-exit", 		this.processCodeLine("monitor-exit v6", null, 0), "Monitor-Exit v6");
		test("1F: check-cast", 			
				this.processCodeLine("check-cast v2, Landroid/support/v4/app/Fragment;", null, 0),
				"v2 = (android/support/v4/app/Fragment) v2;");
		test("20: instance-of",
				this.processCodeLine("instance-of v3, v2, Landroid/widget/ListView;", null, 0),
				"if (v2 instanceOf android/widget/ListView) v3 = 1;\nelse v3 = 0;");
		test("21: array-length", 		this.processCodeLine("array-length v5, v2", null, 0), 					"v5 = v2.length;");
		test("22: new-instance", 		this.processCodeLine("new-instance v0, Landroid/view/View;", null, 0), 	"android/view/View v0;");
		test("23: new-array", 			this.processCodeLine("new-array v2, v0, [I", null, 0), 					"v2 = new int[v0];");
		// 24: filled-new-array
		// 25: filled-new-array-range
		test("26: fill-array-data",		this.processCodeLine("", null, 0), 					"throw v7;");
		test("27: throw", 			this.processCodeLine("throw v7", null, 0), 					"throw v7;");
		test("28: goto", 			this.processCodeLine("goto :goto_1", null, 0), 				"goto :goto_1");
		test("29: goto/16", 		this.processCodeLine("goto/16 :goto_5", null, 0), 			"goto :goto_5");
		// 2A: goto/32
		// 2B: packed-switch
		// 2C: sparse-switch
		
		test("2D: cmpl-float", 		this.processCodeLine("cmpl-float v3, v1, v3", null, 0), 	"if ( (v1 is NaN) || (v3 is NaN) ) v3 = -1;\nelse v3 = v1.compareTo(v3);");
		test("2E: cmpg-float", 		this.processCodeLine("cmpg-float v3, v1, v3", null, 0), 	"if ( (v1 is NaN) || (v3 is NaN) ) v3 = 1;\nelse v3 = v1.compareTo(v3);");
		test("2F: cmpl-double", 	this.processCodeLine("cmpl-double v4, v2, v9", null, 0), 	"if ( (v2 is NaN) || (v9 is NaN) ) v4 = -1;\nelse v4 = v2.compareTo(v9);");
		test("30: cmpg-double", 	this.processCodeLine("cmpg-double v4, v2, v9", null, 0), 	"if ( (v2 is NaN) || (v9 is NaN) ) v4 = 1;\nelse v4 = v2.compareTo(v9);");
		test("31: cmp-long", 		this.processCodeLine("cmp-long v3, v1, v5", null, 0), 		"v3 = v1.compareTo(v5);");
		
		test("32: if-eq", 			this.processCodeLine("if-eq v0, v4, :cond_1", null, 0), 	"if (v0 == v4) goto :cond_1");
		test("33: if-ne", 			this.processCodeLine("if-ne v0, v4, :cond_1", null, 0), 	"if (v0 != v4) goto :cond_1");
		test("34: if-lt", 			this.processCodeLine("if-lt v0, v4, :cond_1", null, 0), 	"if (v0 < v4) goto :cond_1");
		test("35: if-ge", 			this.processCodeLine("if-ge v0, v4, :cond_1", null, 0), 	"if (v0 >= v4) goto :cond_1");
		test("36: if-gt", 			this.processCodeLine("if-gt v0, v4, :cond_1", null, 0), 	"if (v0 > v4) goto :cond_1");
		test("37: if-le", 			this.processCodeLine("if-le v0, v4, :cond_1", null, 0), 	"if (v0 <= v4) goto :cond_1");
		
		test("38: if-eqz", 			this.processCodeLine("if-eqz p2, :cond_2", null, 0), 		"if (p2 == 0) goto :cond_2");
		test("39: if-nez", 			this.processCodeLine("if-nez p2, :cond_2", null, 0), 		"if (p2 != 0) goto :cond_2");		
		test("3A: if-ltz", 			this.processCodeLine("if-ltz p2, :cond_2", null, 0), 		"if (p2 < 0) goto :cond_2");		
		test("3B: if-gez", 			this.processCodeLine("if-gez p2, :cond_2", null, 0), 		"if (p2 >= 0) goto :cond_2");		
		test("3C: if-gtz", 			this.processCodeLine("if-gtz p2, :cond_2", null, 0), 		"if (p2 > 0) goto :cond_2");
		test("3D: if-lez", 			this.processCodeLine("if-lez p2, :cond_2", null, 0), 		"if (p2 <= 0) goto :cond_2");
		
		// 3E : unused
		// 3F : unused
		// 40 : unused
		// 41 : unused
		// 42 : unused
		// 43 : unused
		
		test("44: aget", 			this.processCodeLine("aget v9, v2, v8", null, 0), 			"v9 = v2[v8];");
		test("45: aget-wide", 		this.processCodeLine("aget-wide v6, v1, v0", null, 0), 		"v6(,v7) = v1[v0];");
		test("46: aget-object",		this.processCodeLine("aget-object v6, v1, v0", null, 0), 	"v6 = v1[v0];");
		test("47: aget-boolean",	this.processCodeLine("aget-boolean v6, v1, v0", null, 0), 	"v6 = v1[v0];");
		test("48: aget-byte",		this.processCodeLine("aget-byte v1, v0, v2", null, 0), 		"v1 = v0[v2];");
		test("49: aget-char",		this.processCodeLine("aget-char v1, v0, v2", null, 0), 		"v1 = v0[v2];");
		test("4A: aget-short",		this.processCodeLine("aget-short v1, v0, v2", null, 0), 	"v1 = v0[v2];");
		test("4B: aput", 			this.processCodeLine("aput v2, v0, v1", null, 0), 			"v0[v1] = v2;");
		test("4C: aput-wide",		this.processCodeLine("aput-wide v2, v0, v1", null, 0), 		"v0[v1] = v2(,v3);");
		test("4D: aput-object",		this.processCodeLine("aput-object v2, v0, v1", null, 0), 	"v0[v1] = v2;");
		test("4E: aput-boolean", 	this.processCodeLine("aput-boolean v2, v0, v1", null, 0),	"v0[v1] = v2;");
		test("4F: aput-byte", 		this.processCodeLine("aput-byte v2, v0, v1", null, 0), 		"v0[v1] = v2;");
		test("50: aput-char", 		this.processCodeLine("aput-char v2, v0, v1", null, 0), 		"v0[v1] = v2;");
		test("51: aput-short", 		this.processCodeLine("aput-short v2, v0, v1", null, 0), 	"v0[v1] = v2;");
		
		test("52: iget", 			this.processCodeLine("iget v0, p1, Landroid/os/Message;->what:I", null, 0), 									"v0 = p1.what;");
		test("53: iget-wide", 		this.processCodeLine("iget-wide v4, p2, Landroid/app/Notification;->when:J", null, 0), 							"v4(,v5) = p2.when;");
		test("54: iget-object", 	this.processCodeLine("iget-object v6, p0, Landroid/support/v4/app/BackStackState;->mOps:[I", null, 0), 			"v6 = p0.mOps;");
		test("55: iget-boolean", 	this.processCodeLine("iget-boolean v1, p0, Landroid/support/v4/app/FragmentTabHost;->mAttached:Z", null, 0), 	"v1 = p0.mAttached;");
		test("56: iget-byte", 		
				this.processCodeLine("iget-byte v1, p0, Lcom/google/analytics/containertag/proto/Debug$DataLayerEventEvaluationInfo;->memoizedIsInitialized:B", null, 0),
				"v1 = p0.memoizedIsInitialized;");
		test("57: iget-char", 		
				this.processCodeLine("iget-char v2, p0, Landroid/support/v4/text/BidiFormatter$DirectionalityEstimator;->lastChar:C", null, 0),
				"v2 = p0.lastChar;");
		test("58: iget-short", 	
				this.processCodeLine("iget-char v2, p0, Landroid/support/v4/text/BidiFormatter$DirectionalityEstimator;->lastShort:S", null, 0),
				"v2 = p0.lastShort;");
		test("59: iput", 			this.processCodeLine("iput v0, p0, Landroid/support/v4/app/Fragment;->mState:I", null, 0), 		"p0.mState = v0;");
		test("5A: iput-wide", 		
				this.processCodeLine("iput-wide v0, p0, Landroid/support/v4/view/ViewPager;->mFakeDragBeginTime:J", null, 0),
				"p0.mFakeDragBeginTime = v0(,v1);");
		test("5B: iput-object", 	this.processCodeLine("iput-object p1, v0, Landroid/app/Notification;->vibrate:[J", null, 0), 	"v0.vibrate = p1;");
		test("5C: iput-boolean",	
				this.processCodeLine("iput-boolean v13, v1, Landroid/support/v4/app/BackStackRecord;->mAddToBackStack:Z", null, 0),
				"v1.mAddToBackStack = v13;");
		test("5D: iput-byte",		
				this.processCodeLine("iput-byte v6, p0, Lcom/google/analytics/containertag/proto/Debug$DebugEvents;->memoizedIsInitialized:B", null, 0),
				"p0.memoizedIsInitialized = v6;");
		test("5E: iput-char", 			
				this.processCodeLine("iput-char v3, p0, Landroid/support/v4/text/BidiFormatter$DirectionalityEstimator;->lastChar:C", null, 0),
				"p0.lastChar = v3;");
		test("5F: iput-short", 			
				this.processCodeLine("iput-short v3, p0, Landroid/support/v4/text/BidiFormatter$DirectionalityEstimator;->lastShort:S", null, 0),
				"p0.lastShort = v3;");
		
		test("60: sget", 		this.processCodeLine("sget v0, Landroid/os/Build$VERSION;->SDK_INT:I", null, 0),		"v0 = android/os/Build$VERSION.SDK_INT;");
		test("61: sget-wide",		
				this.processCodeLine("sget-wide v2, Lcom/gogo/haha/haroro;->curTime:J", null, 0),
				"v2(,v3) = com/gogo/haha/haroro.curTime;");
		test("62: sget-object",		
				this.processCodeLine("sget-object v10, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;", null, 0),
				"v10 = java/lang/Integer.TYPE;");
		test("63: sget-boolean",	
				this.processCodeLine("sget-boolean v0, Landroid/support/v4/app/FragmentManagerImpl;->DEBUG:Z", null, 0),
				"v0 = android/support/v4/app/FragmentManagerImpl.DEBUG;");
		test("64: sget-byte", 	this.processCodeLine("sget-byte v0, Landroid/os/Build$VERSION;->SDK_BYTE:B", null, 0),	"v0 = android/os/Build$VERSION.SDK_BYTE;");
		test("65: sget-char", 	this.processCodeLine("sget-char v0, Landroid/os/Build$VERSION;->SDK_CHAR:C", null, 0),	"v0 = android/os/Build$VERSION.SDK_CHAR;");
		test("66: sget-short", 	this.processCodeLine("sget-short v0, Landroid/os/Build$VERSION;->SDK_SHORT:S", null, 0),"v0 = android/os/Build$VERSION.SDK_SHORT;");
		test("67: sput", 		
				this.processCodeLine("sput v4, Landroid/support/v4/content/WakefulBroadcastReceiver;->mNextId:I", null, 0),
				"android/support/v4/content/WakefulBroadcastReceiver.mNextId = v4;");
		test("68: sput-wide", 		
				this.processCodeLine("sput-wide v0, Lcom/gogo/haha/haroro;->curTime:J", null, 0),
				"com/gogo/haha/haroro.curTime = v0(,v1);");
		test("69: sput-object", 		
				this.processCodeLine("sput-object v0, Landroid/support/v4/app/FragmentActivity$FragmentTag;->Fragment:[I", null, 0),
				"android/support/v4/app/FragmentActivity$FragmentTag.Fragment = v0;");
		test("6A: sput-boolean", 		
				this.processCodeLine("sput-boolean p0, Landroid/support/v4/app/LoaderManagerImpl;->DEBUG:Z", null, 0),
				"android/support/v4/app/LoaderManagerImpl.DEBUG = p0;");
		test("6B: sput-byte", 		
				this.processCodeLine("sput-byte p0, Landroid/support/v4/app/LoaderManagerImpl;->DEBUG_BYTE:B", null, 0),
				"android/support/v4/app/LoaderManagerImpl.DEBUG_BYTE = p0;");
		test("6C: sput-char", 		
				this.processCodeLine("sput-char p0, Landroid/support/v4/app/LoaderManagerImpl;->DEBUG_CHAR:C", null, 0),
				"android/support/v4/app/LoaderManagerImpl.DEBUG_CHAR = p0;");
		test("6D: sput-short", 		
				this.processCodeLine("sput-short p0, Landroid/support/v4/app/LoaderManagerImpl;->DEBUG_SHORT:S", null, 0),
				"android/support/v4/app/LoaderManagerImpl.DEBUG_SHORT = p0;");
		
		// 6E: invoke-virtual
		// 6F: invoke-super
		// 70: invoke-direct
		// 71: invoke-static
		// 72: invoke-interface
		// 73: unused
		// 74: invoke-virtual/range
		// 75: invoke-super/range
		// 76: invoke-direct/range
		// 77: invoke-static/range
		// 78: invoke-interface/range
		// 79: unused
		// 7A: unused
		
		test("7B: neg-int", 		this.processCodeLine("neg-int v5, v0", null, 0),		"v5 = -v0;");		
		test("7C: not-int", 		this.processCodeLine("not-int v5, v0", null, 0),		"v5 = !v0;");
		test("7D: neg-long", 		this.processCodeLine("neg-long v2, v6", null, 0),		"v2(,v3) = -v6(,v7);");
		test("7E: not-long", 		this.processCodeLine("not-long v2, v6", null, 0),		"v2(,v3) = !v6(,v7);");
		test("7F: neg-float", 		this.processCodeLine("neg-float v1, v5", null, 0),		"v1 = -v5;");
		test("80: neg-double", 		this.processCodeLine("neg-double v2, v6", null, 0),		"v2(,v3) = !v6(,v7);");
		test("81: int-to-long", 	this.processCodeLine("int-to-long v2, v6", null, 0),	"v2(,v3) = (long) v6;");
		test("82: int-to-float", 	this.processCodeLine("int-to-float v0, v11", null, 0),	"v0 = (float) v11;");
		test("83: int-to-double", 	this.processCodeLine("int-to-double v10, v6", null, 0),	"v10(,v11) = (double) v6;");
		test("84: long-to-int", 	this.processCodeLine("long-to-int v3, v5", null, 0),	"v3 = (int) v5(,v6);");
		test("85: long-to-float", 	this.processCodeLine("long-to-float v6, v2", null, 0),	"v6 = (float) v2(,v3);");
		test("86: long-to-double", 	this.processCodeLine("long-to-double v8, v0", null, 0),	"v8(,v9) = (double) v0(,v1);");
		test("87: float-to-int", 	this.processCodeLine("float-to-int v3, v5", null, 0),	"v3 = (int) v5;");
		test("88: float-to-long", 	this.processCodeLine("float-to-long v3, v5", null, 0),	"v3(,v4) = (long) v5;");
		test("89: float-to-double",	this.processCodeLine("float-to-double v3, v5", null, 0),"v3(,v4) = (double) v5;");
		test("8A: double-to-int", 	this.processCodeLine("double-to-int v0, v5", null, 0),	"v0 = (int) v5(,v6);");
		test("8B: double-to-long", 	this.processCodeLine("double-to-long v0, v5", null, 0),	"v0(,v1) = (long) v5(,v6);");
		test("8C: double-to-float", this.processCodeLine("double-to-float v0, v5", null, 0),"v0 = (float) v5(,v6);");
		test("8D: int-to-byte", 	this.processCodeLine("int-to-byte v1, p1", null, 0),	"v1 = (byte) p1;");
		test("8E: int-to-char", 	this.processCodeLine("int-to-char v1, p1", null, 0),	"v1 = (char) p1;");
		test("8F: int-to-short", 	this.processCodeLine("int-to-short v1, p1", null, 0),	"v1 = (short) p1;");
		
		test("90: add-int", 		this.processCodeLine("add-int v0, p2, p3", null, 0),	"v0 = p2 + p3;");
		test("91: sub-int", 		this.processCodeLine("sub-int v0, p2, p3", null, 0),	"v0 = p2 - p3;");
		test("92: mul-int", 		this.processCodeLine("mul-int v0, p2, p3", null, 0),	"v0 = p2 * p3;");
		test("93: div-int", 		this.processCodeLine("div-int v0, p2, p3", null, 0),	"v0 = p2 / p3;");
		test("94: rem-int", 		this.processCodeLine("rem-int v0, p2, p3", null, 0),	"v0 = p2 % p3;");
		test("95: and-int", 		this.processCodeLine("and-int v0, p2, p3", null, 0),	"v0 = p2 & p3;");
		test("96: or-int", 			this.processCodeLine("or-int v0, p2, p3", null, 0),		"v0 = p2 | p3;");
		test("97: xor-int",			this.processCodeLine("xor-int v0, p2, p3", null, 0),	"v0 = p2 ^ p3;");
		test("98: shl-int",			this.processCodeLine("shl-int v0, p2, p3", null, 0),	"v0 = p2 << p3;");
		test("99: shr-int",			this.processCodeLine("shr-int v0, p2, p3", null, 0),	"v0 = p2 >> p3;");
		test("9A: ushr-int",		this.processCodeLine("ushr-int v0, p2, p3", null, 0),	"v0 = p2 >>> p3;");
		
		test("9B: add-long", 		this.processCodeLine("add-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) + v7(,v9);");
		test("9C: sub-long", 		this.processCodeLine("sub-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) - v7(,v9);");
		test("9D: mul-long", 		this.processCodeLine("mul-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) * v7(,v9);");
		test("9E: div-long", 		this.processCodeLine("div-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) / v7(,v9);");
		test("9F: rem-long", 		this.processCodeLine("rem-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) % v7(,v9);");
		test("A0: and-long", 		this.processCodeLine("and-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) & v7(,v9);");
		test("A1: or-long", 		this.processCodeLine("or-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) | v7(,v9);");
		test("A2: xor-long", 		this.processCodeLine("xor-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) ^ v7(,v9);");
		test("A3: shl-long", 		this.processCodeLine("shl-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) << v7(,v9);");
		test("A4: shr-long", 		this.processCodeLine("shr-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) >> v7(,v9);");
		test("A5: ushr-long", 		this.processCodeLine("ushr-long v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) >>> v7(,v9);");
		
		test("A6: add-float", 		this.processCodeLine("add-float v6, v4, v9", null, 0),	"v6 = v4 + v9;");
		test("A7: sub-float", 		this.processCodeLine("sub-float v6, v4, v9", null, 0),	"v6 = v4 - v9;");
		test("A8: mul-float", 		this.processCodeLine("mul-float v6, v4, v9", null, 0),	"v6 = v4 * v9;");
		test("A9: div-float", 		this.processCodeLine("div-float v6, v4, v9", null, 0),	"v6 = v4 / v9;");
		test("AA: rem-float", 		this.processCodeLine("rem-float v6, v4, v9", null, 0),	"v6 = v4 % v9;");
		
		test("AB: add-double", 		this.processCodeLine("add-double v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) + v7(,v9);");
		test("AC: sub-double", 		this.processCodeLine("sub-double v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) - v7(,v9);");
		test("AD: mul-double", 		this.processCodeLine("mul-double v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) * v7(,v9);");
		test("AE: div-double", 		this.processCodeLine("div-double v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) / v7(,v9);");
		test("AF: rem-double", 		this.processCodeLine("rem-double v3, v5, v7", null, 0),	"v3(,v4) = v5(,v6) % v7(,v9);");
		
		
		test("B0: add-int/2addr", 	this.processCodeLine("add-int/2addr v1, v2", null, 0),	"v1 = v1 + v2;");
		test("B1: sub-int/2addr", 	this.processCodeLine("sub-int/2addr v1, v2", null, 0),	"v1 = v1 - v2;");
		test("B2: mul-int/2addr", 	this.processCodeLine("mul-int/2addr v1, v2", null, 0),	"v1 = v1 * v2;");
		test("B3: div-int/2addr", 	this.processCodeLine("div-int/2addr v1, v2", null, 0),	"v1 = v1 / v2;");
		test("B4: rem-int/2addr", 	this.processCodeLine("rem-int/2addr v1, v2", null, 0),	"v1 = v1 % v2;");
		test("B5: and-int/2addr", 	this.processCodeLine("and-int/2addr v1, v2", null, 0),	"v1 = v1 & v2;");
		test("B6: or-int/2addr", 	this.processCodeLine("or-int/2addr v1, v2", null, 0),	"v1 = v1 | v2;");
		test("B7: xor-int/2addr", 	this.processCodeLine("xor-int/2addr v1, v2", null, 0),	"v1 = v1 ^ v2;");
		test("B8: shl-int/2addr", 	this.processCodeLine("shl-int/2addr v1, v2", null, 0),	"v1 = v1 << v2;");
		test("B9: shr-int/2addr", 	this.processCodeLine("shr-int/2addr v1, v2", null, 0),	"v1 = v1 >> v2;");
		test("BA: ushr-int/2addr", 	this.processCodeLine("ushr-int/2addr v1, v2", null, 0),	"v1 = v1 >>> v2;");		
		test("BB: add-long/2addr", 	this.processCodeLine("add-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) + v4(,v5);");
		test("BC: sub-long/2addr", 	this.processCodeLine("sub-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) - v4(,v5);");
		test("BD: mul-long/2addr", 	this.processCodeLine("mul-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) * v4(,v5);");
		test("BE: div-long/2addr", 	this.processCodeLine("div-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) / v4(,v5);");
		test("BF: rem-long/2addr", 	this.processCodeLine("rem-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) % v4(,v5);");
		test("C0: and-long/2addr", 	this.processCodeLine("and-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) & v4(,v5);");
		test("C1: or-long/2addr", 	this.processCodeLine("or-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) | v4(,v5);");
		test("C2: xor-long/2addr", 	this.processCodeLine("xor-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) ^ v4(,v5);");
		test("C3: shl-long/2addr", 	this.processCodeLine("shl-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) << v4(,v5);");
		test("C4: shr-long/2addr", 	this.processCodeLine("shr-long/2addr v2, v4", null, 0),	"v2(,v3) = v2(,v3) >> v4(,v5);");
		test("C5: ushr-long/2addr",	this.processCodeLine("ushr-long/2addr v2, v4", null, 0),"v2(,v3) = v2(,v3) >>> v4(,v5);");
		test("C6: add-float/2addr", this.processCodeLine("add-float/2addr v2, v5", null, 0),"v2 = v2 + v5;");
		test("C7: sub-float/2addr", this.processCodeLine("sub-float/2addr v2, v5", null, 0),"v2 = v2 - v5;");
		test("C8: mul-float/2addr", this.processCodeLine("mul-float/2addr v2, v5", null, 0),"v2 = v2 * v5;");
		test("C9: div-float/2addr", this.processCodeLine("div-float/2addr v2, v5", null, 0),"v2 = v2 / v5;");
		test("CA: rem-float/2addr", this.processCodeLine("rem-float/2addr v2, v5", null, 0),"v2 = v2 % v5;");		
		test("CB: add-double/2addr",	this.processCodeLine("add-double/2addr v6, v8", null, 0),	"v6(,v7) = v6(,v7) + v8(,v9);");
		test("CC: sub-double/2addr",	this.processCodeLine("sub-double/2addr v6, v8", null, 0),	"v6(,v7) = v6(,v7) - v8(,v9);");
		test("CD: mul-double/2addr",	this.processCodeLine("mul-double/2addr v6, v8", null, 0),	"v6(,v7) = v6(,v7) * v8(,v9);");
		test("CE: div-double/2addr",	this.processCodeLine("div-double/2addr v6, v8", null, 0),	"v6(,v7) = v6(,v7) / v8(,v9);");
		test("CF: rem-double/2addr",	this.processCodeLine("rem-double/2addr v6, v8", null, 0),	"v6(,v7) = v6(,v7) % v8(,v9);");
		
		test("D0: add-int/lit16", 	this.processCodeLine("add-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 + 0x30b;");
		test("D1: sub-int/lit16", 	this.processCodeLine("sub-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 - 0x30b;");
		test("D2: mul-int/lit16", 	this.processCodeLine("mul-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 * 0x30b;");
		test("D3: div-int/lit16", 	this.processCodeLine("div-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 / 0x30b;");
		test("D4: rem-int/lit16", 	this.processCodeLine("rem-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 % 0x30b;");
		test("D5: and-int/lit16", 	this.processCodeLine("and-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 & 0x30b;");
		test("D6: or-int/lit16", 	this.processCodeLine("or-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 | 0x30b;");
		test("D7: xor-int/lit16", 	this.processCodeLine("xor-int/lit16 v0, v1, 0x30b", null, 0),	"v0 = v1 ^ 0x30b;");
		
		test("D8: add-int/lit8", 	this.processCodeLine("add-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 + -0x1;");
		test("D9: sub-int/lit8", 	this.processCodeLine("sub-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 - -0x1;");
		test("DA: mul-int/lit8", 	this.processCodeLine("mul-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 * -0x1;");
		test("DB: div-int/lit8", 	this.processCodeLine("div-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 / -0x1;");
		test("DC: rem-int/lit8", 	this.processCodeLine("rem-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 % -0x1;");
		test("DD: and-int/lit8", 	this.processCodeLine("and-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 & -0x1;");
		test("DE: or-int/lit8", 	this.processCodeLine("or-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 | -0x1;");
		test("DF: xor-int/lit8", 	this.processCodeLine("xor-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 ^ -0x1;");
		test("E0: shl-int/lit8", 	this.processCodeLine("shl-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 << -0x1;");
		test("E1: shr-int/lit8", 	this.processCodeLine("shr-int/lit8 v2, v4, -0x1", null, 0),		"v2 = v4 >> -0x1;");
		test("E2: ushr-int/lit8", 	this.processCodeLine("ushr-int/lit8 v2, v4, -0x1", null, 0),	"v2 = v4 >>> -0x1;");
		
		// E3: unused
		// E4: unused
		// E5: unused
		// E6: unused
		// E7: unused
		// E8: unused
		// E9: unused
		// EA: unused
		// EB: unused
		// EC: unused
		// ED: unused
		// EE: unused (execute-inline, unsafe, only for ODEX)
		// EF: unused
		// F0: unused (invoke-direct-empty, unsafe, only for ODEX)
		// F1: unused
		// F2: unused (iget-quick, unsafe, only for ODEX)
		// F3: unused (iget-wide-quick, unsafe, only for ODEX)
		// F4: unused (iget-object-quick, unsafe, only for ODEX)
		// F5: unused (iput-quick, unsafe, only for ODEX)
		// F6: unused (iput-wide-quick, unsafe, only for ODEX)
		// F7: unused (iput-object-quick, unsafe, only for ODEX)
		// F8: unused (invoke-virtual-quick, unsafe, only for ODEX)
		// F9: unused (invoke-virtual-quick/range, unsafe, only for ODEX)
		// FA: unused (invoke-super-quick, unsafe, only for ODEX)
		// FB: unused (invoke-super-quick/range, unsafe, only for ODEX)
		// FC: unused
		// FD: unused
		// FE: unused
		// FF: unused
	}
	
	private void test(String title, String output, String expected){
		if (output.equals(expected)) System.out.println(title + ": Good");
		else{
			System.out.println(title + ": Fail");
			System.out.println("\t[Expected: " + expected + "]");
			System.out.println("\t[Output: " + output + "]");
		}
	}
}
