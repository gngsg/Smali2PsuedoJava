package gng.smali2pj;

import java.io.IOException;
import java.util.ArrayList;

public class ZRunTest{

	public static void main(String[] args) throws IOException{
		Utils.REPLACE_VAR_NAMES = false; // force it to false so var names will not be replaced
		/*
		ZSmaliMethodTester tester = new ZSmaliMethodTester();
		try{
			tester.runTests();
		}
		catch (IOException e){e.printStackTrace();}
		*/		
		
		// 00: nop
		test("01: move",				"move v1, v0",						"v1 = v0;");
		test("02: move/from16",			"move/from16 v0, v17", 				"v0 = v17;");
		test("03: move/16",				"move/16 v15, v17", 				"v15 = v17;");
		test("04: move-wide",			"move-wide v2, v0", 				"v2(,v3) = v0(,v1);");
		test("05: move-wide/from16",	"move-wide/from16 v0, v17",			"v0(,v1) = v17(,v18);");
		test("06: move-wide/16",		"move-wide/16 v15, v17", 			"v15(,v16) = v17(,v18);");
		test("07: move-object",			"move-object v1, v3", 				"v1 = v3;");
		test("08: move-object/from16",	"move-object/from16 v1, v16", 		"v1 = v16;");
		test("09: move-object/16",		"move-object/16 v17, v16", 			"v17 = v16;");
		// 0A: move-result
		// 0B: move-result-wide
		// 0C: move-result-object
		// 0D: move-exception
		test("0E: return-void",			"return-void", 						"return;");
		test("0F: return",				"return v0", 						"return v0;");
		test("10: return-wide",			"return-wide v0", 					"return v0;");
		test("11: return-object",		"return-object v1",					"return v1;");
		// test for negatives also?, Normal constants are left sign extended, high16 constants are right sign extended
		test("12: const/4",				"const/4 v13, 0x1", 					"v13 = 0x1;");
		test("13: const/16",			"const/16 v13, 0x1",					"v13 = 0x1;");
		test("14: const", 				"const v3, 0x1020011",					"v3 = 0x1020011;");
		test("15: const/high16", 		"const/high16 v4, 0x10a", 				"v4 = 0x10a00000;");
		test("16: const-wide/16", 		"const-wide/16 v3, 0x1388", 			"v3(,v4) = 0x00000000 00001388;");
		test("17: const-wide/32", 		"const-wide/32 v0, 0x493e0", 			"v0(,v1) = 0x00000000 000493e0;");
		test("18: const-wide", 			"const-wide v2, 0x3fde28c7460698c7L", 	"v2(,v3) = 0x3fde28c7 460698c7;");
		test("19: const-wide/high16",	"const-wide/high16 v9, 0x4059", 		"v9(,v10) = 0x40590000 00000000;");
		test("1A: const-string", 		"const-string v10, \"FragmentManager\"","v10 = \"FragmentManager\";");
		// 1B: const-string-jumbo
		test("1C: const-class", 		"const-class v10, Landroid/graphics/drawable/Drawable;","v10 = android/graphics/drawable/Drawable.class;");
		test("1D: monitor-enter", 		"monitor-enter v3", 									"Monitor-Enter v3");
		test("1E: monitor-exit", 		"monitor-exit v6", 										"Monitor-Exit v6");
		test("1F: check-cast", 			"check-cast v2, Landroid/support/v4/app/Fragment;",		"v2 = (android/support/v4/app/Fragment) v2;");
		test("20: instance-of",			"instance-of v3, v2, Landroid/widget/ListView;",		"if (v2 instanceOf android/widget/ListView) v3 = 1;\nelse v3 = 0;");
		test("21: array-length", 		"array-length v5, v2", 									"v5 = v2.length;");
		test("22: new-instance", 		"new-instance v0, Landroid/view/View;",					"android/view/View v0;");
		test("23: new-array", 			"new-array v2, v0, [I", 								"v2 = new int[v0];");
		// 24: filled-new-array
		// 25: filled-new-array-range
		//test("26: fill-array-data",	this.processCodeLine("", null, 0), 		"throw v7;");
		test("27: throw", 				"throw v7",			 					"throw v7;");
		test("28: goto", 				"goto :goto_1",			 				"goto :goto_1");
		test("29: goto/16", 			"goto/16 :goto_5",			 			"goto :goto_5");
		// 2A: goto/32
		// 2B: packed-switch
		// 2C: sparse-switch
		test("2D: cmpl-float", 			"cmpl-float v3, v1, v3", 	"if ( (v1 is NaN) || (v3 is NaN) ) v3 = -1;\nelse v3 = v1.compareTo(v3);");
		test("2E: cmpg-float", 			"cmpg-float v3, v1, v3", 	"if ( (v1 is NaN) || (v3 is NaN) ) v3 = 1;\nelse v3 = v1.compareTo(v3);");
		test("2F: cmpl-double", 		"cmpl-double v4, v2, v9", 	"if ( (v2 is NaN) || (v9 is NaN) ) v4 = -1;\nelse v4 = v2.compareTo(v9);");
		test("30: cmpg-double", 		"cmpg-double v4, v2, v9", 	"if ( (v2 is NaN) || (v9 is NaN) ) v4 = 1;\nelse v4 = v2.compareTo(v9);");
		test("31: cmp-long", 			"cmp-long v3, v1, v5", 		"v3 = v1.compareTo(v5);");
		test("32: if-eq", 				"if-eq v0, v4, :cond_1", 	"if (v0 == v4) goto :cond_1");
		test("33: if-ne", 				"if-ne v0, v4, :cond_1", 	"if (v0 != v4) goto :cond_1");
		test("34: if-lt", 				"if-lt v0, v4, :cond_1", 	"if (v0 < v4) goto :cond_1");
		test("35: if-ge", 				"if-ge v0, v4, :cond_1", 	"if (v0 >= v4) goto :cond_1");
		test("36: if-gt", 				"if-gt v0, v4, :cond_1", 	"if (v0 > v4) goto :cond_1");
		test("37: if-le", 				"if-le v0, v4, :cond_1", 	"if (v0 <= v4) goto :cond_1");
		test("38: if-eqz", 				"if-eqz p2, :cond_2", 		"if (p2 == 0) goto :cond_2");
		test("39: if-nez", 				"if-nez p2, :cond_2", 		"if (p2 != 0) goto :cond_2");		
		test("3A: if-ltz", 				"if-ltz p2, :cond_2", 		"if (p2 < 0) goto :cond_2");		
		test("3B: if-gez", 				"if-gez p2, :cond_2", 		"if (p2 >= 0) goto :cond_2");		
		test("3C: if-gtz", 				"if-gtz p2, :cond_2", 		"if (p2 > 0) goto :cond_2");
		test("3D: if-lez", 				"if-lez p2, :cond_2", 		"if (p2 <= 0) goto :cond_2");
		// 3E : unused
		// 3F : unused
		// 40 : unused
		// 41 : unused
		// 42 : unused
		// 43 : unused
		test("44: aget", 				"aget v9, v2, v8", 			"v9 = v2[v8];");
		test("45: aget-wide", 			"aget-wide v6, v1, v0",		"v6(,v7) = v1[v0];");
		test("46: aget-object",			"aget-object v6, v1, v0", 	"v6 = v1[v0];");
		test("47: aget-boolean",		"aget-boolean v6, v1, v0", 	"v6 = v1[v0];");
		test("48: aget-byte",			"aget-byte v1, v0, v2",		"v1 = v0[v2];");
		test("49: aget-char",			"aget-char v1, v0, v2",		"v1 = v0[v2];");
		test("4A: aget-short",			"aget-short v1, v0, v2", 	"v1 = v0[v2];");
		test("4B: aput", 				"aput v2, v0, v1", 			"v0[v1] = v2;");
		test("4C: aput-wide",			"aput-wide v2, v0, v1",		"v0[v1] = v2(,v3);");
		test("4D: aput-object",			"aput-object v2, v0, v1", 	"v0[v1] = v2;");
		test("4E: aput-boolean", 		"aput-boolean v2, v0, v1",	"v0[v1] = v2;");
		test("4F: aput-byte", 			"aput-byte v2, v0, v1", 	"v0[v1] = v2;");
		test("50: aput-char", 			"aput-char v2, v0, v1", 	"v0[v1] = v2;");
		test("51: aput-short", 			"aput-short v2, v0, v1", 	"v0[v1] = v2;");
		test("52: iget", 				"iget v0, p1, Lcom/haha/hoho;->what:I",					"v0 = p1.what;");
		test("53: iget-wide", 			"iget-wide v4, p2, Lcom/haha/hoho;->when:J", 			"v4(,v5) = p2.when;");
		test("54: iget-object", 		"iget-object v6, p0, Lcom/haha/hoho;->mOps:[I", 		"v6 = p0.mOps;");
		test("55: iget-boolean", 		"iget-boolean v1, p0, Lcom/haha/hoho;->mAttached:Z", 	"v1 = p0.mAttached;");
		test("56: iget-byte", 			"iget-byte v1, p0, Lcom/haha/hoho$lala;->memInit:B",	"v1 = p0.memInit;");
		test("57: iget-char", 			"iget-char v2, p0, Lcom/haha/hoho$lala;->lastChar:C",	"v2 = p0.lastChar;");
		test("58: iget-short", 			"iget-short v2, p0, Lcom/haha/hoho$lala;->lastShort:S",	"v2 = p0.lastShort;");
		test("59: iput", 				"iput v0, p0, Lcom/haha/hoho;->mState:I", 				"p0.mState = v0;");
		test("5A: iput-wide", 			"iput-wide v0, p0, Lcom/haha/hoho;->fakeTime:J",		"p0.fakeTime = v0(,v1);");
		test("5B: iput-object", 		"iput-object p1, v0, Lcom/haha/hoho;->vibrate:[J",		"v0.vibrate = p1;");
		test("5C: iput-boolean",		"iput-boolean v13, v1, Lcom/haha/hoho;->isStacked:Z",	"v1.isStacked = v13;");
		test("5D: iput-byte",			"iput-byte v6, p0, Lcom/haha/hoho$lala;->memInit:B",	"p0.memInit = v6;");
		test("5E: iput-char", 			"iput-char v3, p0, Lcom/haha/hoho$lala;->lastChar:C",	"p0.lastChar = v3;");
		test("5F: iput-short", 			"iput-short v3, p0, Lcom/haha/hoho$lala;->lastShort:S",	"p0.lastShort = v3;");
		test("60: sget", 				"sget v0, Landroid/os/Build$VERSION;->SDK_INT:I",		"v0 = android/os/Build$VERSION.SDK_INT;");
		test("61: sget-wide",			"sget-wide v2, Lcom/gogo/haha/haroro;->curTime:J",		"v2(,v3) = com/gogo/haha/haroro.curTime;");
		test("62: sget-object",			"sget-object v9, Lcom/haha/hoho;->TYPE:Lcom/haha/la;",	"v9 = com/haha/hoho.TYPE;");
		test("63: sget-boolean",		"sget-boolean v0, Lcom/haha/hoho;->DEBUG:Z",			"v0 = com/haha/hoho.DEBUG;");
		test("64: sget-byte", 			"sget-byte v0, Landroid/os/Build$VERSION;->SDK_BYTE:B",	"v0 = android/os/Build$VERSION.SDK_BYTE;");
		test("65: sget-char", 			"sget-char v0, Landroid/os/Build$VERSION;->SDK_CHAR:C",	"v0 = android/os/Build$VERSION.SDK_CHAR;");
		test("66: sget-short", 			"sget-short v0, Landroid/os/Build$VERSION;->SDK_SS:S",	"v0 = android/os/Build$VERSION.SDK_SS;");
		test("67: sput", 				"sput v4, Lcom/haha/hoho;->mNextId:I",					"com/haha/hoho.mNextId = v4;");
		test("68: sput-wide", 			"sput-wide v0, Lcom/gogo/haha/haroro;->curTime:J",		"com/gogo/haha/haroro.curTime = v0(,v1);");
		test("69: sput-object", 		"sput-object v0, Lcom/haha/hoho;->Fragment:[I",			"com/haha/hoho.Fragment = v0;");
		test("6A: sput-boolean", 		"sput-boolean p0, Lcom/haha/hoho;->DEBUG:Z",			"com/haha/hoho.DEBUG = p0;");
		test("6B: sput-byte", 			"sput-byte p0, Lcom/haha/hoho;->DEBUG_BYTE:B",			"com/haha/hoho.DEBUG_BYTE = p0;");
		test("6C: sput-char", 			"sput-char p0, Lcom/haha/hoho;->DEBUG_CHAR:C",			"com/haha/hoho.DEBUG_CHAR = p0;");
		test("6D: sput-short", 			"sput-short p0, Lcom/haha/hoho;->DEBUG_SHORT:S",		"com/haha/hoho.DEBUG_SHORT = p0;");
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
		test("7B: neg-int", 			"neg-int v5, v0",			"v5 = -v0;");		
		test("7C: not-int", 			"not-int v5, v0",			"v5 = !v0;");
		test("7D: neg-long", 			"neg-long v2, v6",			"v2(,v3) = -v6(,v7);");
		test("7E: not-long", 			"not-long v2, v6",			"v2(,v3) = !v6(,v7);");
		test("7F: neg-float", 			"neg-float v1, v5",			"v1 = -v5;");
		test("80: neg-double", 			"neg-double v2, v6",		"v2(,v3) = -v6(,v7);");
		test("81: int-to-long", 		"int-to-long v2, v6",		"v2(,v3) = (long) v6;");
		test("82: int-to-float", 		"int-to-float v0, v11",		"v0 = (float) v11;");
		test("83: int-to-double", 		"int-to-double v10, v6",	"v10(,v11) = (double) v6;");
		test("84: long-to-int", 		"long-to-int v3, v5",		"v3 = (int) v5(,v6);");
		test("85: long-to-float", 		"long-to-float v6, v2",		"v6 = (float) v2(,v3);");
		test("86: long-to-double", 		"long-to-double v8, v0",	"v8(,v9) = (double) v0(,v1);");
		test("87: float-to-int", 		"float-to-int v3, v5",		"v3 = (int) v5;");
		test("88: float-to-long", 		"float-to-long v3, v5",		"v3(,v4) = (long) v5;");
		test("89: float-to-double",		"float-to-double v3, v5",	"v3(,v4) = (double) v5;");
		test("8A: double-to-int", 		"double-to-int v0, v5",		"v0 = (int) v5(,v6);");
		test("8B: double-to-long", 		"double-to-long v0, v5",	"v0(,v1) = (long) v5(,v6);");
		test("8C: double-to-float", 	"double-to-float v0, v5",	"v0 = (float) v5(,v6);");
		test("8D: int-to-byte", 		"int-to-byte v1, p1",		"v1 = (byte) p1;");
		test("8E: int-to-char", 		"int-to-char v1, p1",		"v1 = (char) p1;");
		test("8F: int-to-short", 		"int-to-short v1, p1",		"v1 = (short) p1;");
		test("90: add-int", 			"add-int v0, p2, p3",		"v0 = p2 + p3;");
		test("91: sub-int", 			"sub-int v0, p2, p3",		"v0 = p2 - p3;");
		test("92: mul-int", 			"mul-int v0, p2, p3",		"v0 = p2 * p3;");
		test("93: div-int", 			"div-int v0, p2, p3",		"v0 = p2 / p3;");
		test("94: rem-int", 			"rem-int v0, p2, p3",		"v0 = p2 % p3;");
		test("95: and-int", 			"and-int v0, p2, p3",		"v0 = p2 & p3;");
		test("96: or-int", 				"or-int v0, p2, p3",		"v0 = p2 | p3;");
		test("97: xor-int",				"xor-int v0, p2, p3",		"v0 = p2 ^ p3;");
		test("98: shl-int",				"shl-int v0, p2, p3",		"v0 = p2 << p3;");
		test("99: shr-int",				"shr-int v0, p2, p3",		"v0 = p2 >> p3;");
		test("9A: ushr-int",			"ushr-int v0, p2, p3",		"v0 = p2 >>> p3;");
		test("9B: add-long", 			"add-long v3, v5, v7",		"v3(,v4) = v5(,v6) + v7(,v8);");
		test("9C: sub-long", 			"sub-long v3, v5, v7",		"v3(,v4) = v5(,v6) - v7(,v8);");
		test("9D: mul-long", 			"mul-long v3, v5, v7",		"v3(,v4) = v5(,v6) * v7(,v8);");
		test("9E: div-long", 			"div-long v3, v5, v7",		"v3(,v4) = v5(,v6) / v7(,v8);");
		test("9F: rem-long", 			"rem-long v3, v5, v7",		"v3(,v4) = v5(,v6) % v7(,v8);");
		test("A0: and-long", 			"and-long v3, v5, v7",		"v3(,v4) = v5(,v6) & v7(,v8);");
		test("A1: or-long", 			"or-long v3, v5, v7",		"v3(,v4) = v5(,v6) | v7(,v8);");
		test("A2: xor-long", 			"xor-long v3, v5, v7",		"v3(,v4) = v5(,v6) ^ v7(,v8);");
		test("A3: shl-long", 			"shl-long v3, v5, v7",		"v3(,v4) = v5(,v6) << v7(,v8);");
		test("A4: shr-long", 			"shr-long v3, v5, v7",		"v3(,v4) = v5(,v6) >> v7(,v8);");
		test("A5: ushr-long", 			"ushr-long v3, v5, v7",		"v3(,v4) = v5(,v6) >>> v7(,v8);");		
		test("A6: add-float", 			"add-float v6, v4, v9",		"v6 = v4 + v9;");
		test("A7: sub-float", 			"sub-float v6, v4, v9",		"v6 = v4 - v9;");
		test("A8: mul-float", 			"mul-float v6, v4, v9",		"v6 = v4 * v9;");
		test("A9: div-float", 			"div-float v6, v4, v9",		"v6 = v4 / v9;");
		test("AA: rem-float", 			"rem-float v6, v4, v9",		"v6 = v4 % v9;");
		test("AB: add-double", 			"add-double v3, v5, v7",	"v3(,v4) = v5(,v6) + v7(,v8);");
		test("AC: sub-double", 			"sub-double v3, v5, v7",	"v3(,v4) = v5(,v6) - v7(,v8);");
		test("AD: mul-double", 			"mul-double v3, v5, v7",	"v3(,v4) = v5(,v6) * v7(,v8);");
		test("AE: div-double", 			"div-double v3, v5, v7",	"v3(,v4) = v5(,v6) / v7(,v8);");
		test("AF: rem-double", 			"rem-double v3, v5, v7",	"v3(,v4) = v5(,v6) % v7(,v8);");
		test("B0: add-int/2addr", 		"add-int/2addr v1, v2",		"v1 = v1 + v2;");
		test("B1: sub-int/2addr", 		"sub-int/2addr v1, v2",		"v1 = v1 - v2;");
		test("B2: mul-int/2addr", 		"mul-int/2addr v1, v2",		"v1 = v1 * v2;");
		test("B3: div-int/2addr", 		"div-int/2addr v1, v2",		"v1 = v1 / v2;");
		test("B4: rem-int/2addr", 		"rem-int/2addr v1, v2",		"v1 = v1 % v2;");
		test("B5: and-int/2addr", 		"and-int/2addr v1, v2",		"v1 = v1 & v2;");
		test("B6: or-int/2addr", 		"or-int/2addr v1, v2",		"v1 = v1 | v2;");
		test("B7: xor-int/2addr", 		"xor-int/2addr v1, v2",		"v1 = v1 ^ v2;");
		test("B8: shl-int/2addr", 		"shl-int/2addr v1, v2",		"v1 = v1 << v2;");
		test("B9: shr-int/2addr", 		"shr-int/2addr v1, v2",		"v1 = v1 >> v2;");
		test("BA: ushr-int/2addr", 		"ushr-int/2addr v1, v2",	"v1 = v1 >>> v2;");		
		test("BB: add-long/2addr", 		"add-long/2addr v2, v4",	"v2(,v3) = v2(,v3) + v4(,v5);");
		test("BC: sub-long/2addr", 		"sub-long/2addr v2, v4",	"v2(,v3) = v2(,v3) - v4(,v5);");
		test("BD: mul-long/2addr", 		"mul-long/2addr v2, v4",	"v2(,v3) = v2(,v3) * v4(,v5);");
		test("BE: div-long/2addr", 		"div-long/2addr v2, v4",	"v2(,v3) = v2(,v3) / v4(,v5);");
		test("BF: rem-long/2addr", 		"rem-long/2addr v2, v4",	"v2(,v3) = v2(,v3) % v4(,v5);");
		test("C0: and-long/2addr", 		"and-long/2addr v2, v4",	"v2(,v3) = v2(,v3) & v4(,v5);");
		test("C1: or-long/2addr", 		"or-long/2addr v2, v4",		"v2(,v3) = v2(,v3) | v4(,v5);");
		test("C2: xor-long/2addr", 		"xor-long/2addr v2, v4",	"v2(,v3) = v2(,v3) ^ v4(,v5);");
		test("C3: shl-long/2addr", 		"shl-long/2addr v2, v4",	"v2(,v3) = v2(,v3) << v4(,v5);");
		test("C4: shr-long/2addr", 		"shr-long/2addr v2, v4",	"v2(,v3) = v2(,v3) >> v4(,v5);");
		test("C5: ushr-long/2addr",		"ushr-long/2addr v2, v4",	"v2(,v3) = v2(,v3) >>> v4(,v5);");
		test("C6: add-float/2addr", 	"add-float/2addr v2, v5",	"v2 = v2 + v5;");
		test("C7: sub-float/2addr", 	"sub-float/2addr v2, v5",	"v2 = v2 - v5;");
		test("C8: mul-float/2addr", 	"mul-float/2addr v2, v5",	"v2 = v2 * v5;");
		test("C9: div-float/2addr", 	"div-float/2addr v2, v5",	"v2 = v2 / v5;");
		test("CA: rem-float/2addr", 	"rem-float/2addr v2, v5",	"v2 = v2 % v5;");		
		test("CB: add-double/2addr",	"add-double/2addr v6, v8",	"v6(,v7) = v6(,v7) + v8(,v9);");
		test("CC: sub-double/2addr",	"sub-double/2addr v6, v8",	"v6(,v7) = v6(,v7) - v8(,v9);");
		test("CD: mul-double/2addr",	"mul-double/2addr v6, v8",	"v6(,v7) = v6(,v7) * v8(,v9);");
		test("CE: div-double/2addr",	"div-double/2addr v6, v8",	"v6(,v7) = v6(,v7) / v8(,v9);");
		test("CF: rem-double/2addr",	"rem-double/2addr v6, v8",	"v6(,v7) = v6(,v7) % v8(,v9);");
		test("D0: add-int/lit16", 		"add-int/lit16 v0, v1, 0x30b",	"v0 = v1 + 0x30b;");
		test("D1: sub-int/lit16", 		"sub-int/lit16 v0, v1, 0x30b",	"v0 = v1 - 0x30b;");
		test("D2: mul-int/lit16", 		"mul-int/lit16 v0, v1, 0x30b",	"v0 = v1 * 0x30b;");
		test("D3: div-int/lit16", 		"div-int/lit16 v0, v1, 0x30b",	"v0 = v1 / 0x30b;");
		test("D4: rem-int/lit16", 		"rem-int/lit16 v0, v1, 0x30b",	"v0 = v1 % 0x30b;");
		test("D5: and-int/lit16", 		"and-int/lit16 v0, v1, 0x30b",	"v0 = v1 & 0x30b;");
		test("D6: or-int/lit16", 		"or-int/lit16 v0, v1, 0x30b",	"v0 = v1 | 0x30b;");
		test("D7: xor-int/lit16", 		"xor-int/lit16 v0, v1, 0x30b",	"v0 = v1 ^ 0x30b;");
		test("D8: add-int/lit8", 		"add-int/lit8 v2, v4, -0x1",	"v2 = v4 + -0x1;");
		test("D9: sub-int/lit8", 		"sub-int/lit8 v2, v4, -0x1",	"v2 = v4 - -0x1;");
		test("DA: mul-int/lit8", 		"mul-int/lit8 v2, v4, -0x1",	"v2 = v4 * -0x1;");
		test("DB: div-int/lit8", 		"div-int/lit8 v2, v4, -0x1",	"v2 = v4 / -0x1;");
		test("DC: rem-int/lit8", 		"rem-int/lit8 v2, v4, -0x1",	"v2 = v4 % -0x1;");
		test("DD: and-int/lit8", 		"and-int/lit8 v2, v4, -0x1",	"v2 = v4 & -0x1;");
		test("DE: or-int/lit8", 		"or-int/lit8 v2, v4, -0x1",		"v2 = v4 | -0x1;");
		test("DF: xor-int/lit8", 		"xor-int/lit8 v2, v4, -0x1",	"v2 = v4 ^ -0x1;");
		test("E0: shl-int/lit8", 		"shl-int/lit8 v2, v4, -0x1",	"v2 = v4 << -0x1;");
		test("E1: shr-int/lit8", 		"shr-int/lit8 v2, v4, -0x1",	"v2 = v4 >> -0x1;");
		test("E2: ushr-int/lit8", 		"ushr-int/lit8 v2, v4, -0x1",	"v2 = v4 >>> -0x1;");
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
	
	private static void test(String title, String inputcode, String expected) throws IOException{
		String methodheader = ".method protected onCreate(Landroid/os/Bundle;)V";
		inputcode = methodheader + "\n" + inputcode;
		ArrayList<String> input = new ArrayList<String>();
		String[] lines = inputcode.split("\n");
		for (int i=0; i<lines.length; i++){
			input.add(lines[i]);
		}
		
		ZSmaliMethodTester tester = new ZSmaliMethodTester(input);
		String output = tester.processCode();
				
		if (output.equals(expected)) System.out.println(title + ": Good");
		else{
			System.out.println(title + ": Fail");
			System.out.println("\t[Expected: " + expected + "]");
			System.out.println("\t[Output: " + output + "]");
		}
	}

}
