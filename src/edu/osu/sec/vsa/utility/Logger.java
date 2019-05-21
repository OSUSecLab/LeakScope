package edu.osu.sec.vsa.utility;

public class Logger {
	public static String TAG = "Logger";

	public static void printI(String args) {
		System.out.println(TAG + args);
	}

	public static void printW(String args) {
		String str = TAG + "[W]" + args;
		System.out.println(str);
		FileUtility.wf("./logs/warnning.txt", str, true);
	}

	public static void print(String args) {
		System.out.println(TAG + args);
	}

	public static void printE(String args) {
		args = TAG + args;
		FileUtility.wf("./logs/error.txt", args, true);
		System.out.println(args);
	}

}
