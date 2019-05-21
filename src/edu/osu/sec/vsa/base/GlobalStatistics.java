package edu.osu.sec.vsa.base;

import org.json.JSONObject;

public class GlobalStatistics {
	static GlobalStatistics gs = new GlobalStatistics();

	private GlobalStatistics() {
	}

	public static GlobalStatistics getInstance() {
		return gs;
	}

	public void countGetString() {
		getString++;
	}

	public void countAppendString() {
		appendString++;
	}

	public void countFormatString() {
		formatString++;
	}

	public void countDiveIntoMethodCall() {
		diveIntoMethodCall++;
	}

	public void countBackWard2Caller() {
		backWard2Caller++;
	}

	public void updateMaxCallStack(int i) {
		if (i > maxCallStack)
			maxCallStack = i;
	}

	int getString = 0;
	int appendString = 0;
	int formatString = 0;
	int diveIntoMethodCall = 0;
	int backWard2Caller = 0;
	int maxCallStack = 0;

	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		result.put("getString", getString);
		result.put("appendString", appendString);
		result.put("formatString", formatString);
		result.put("diveIntoMethodCall", diveIntoMethodCall);
		result.put("backWard2Caller", backWard2Caller);
		result.put("maxCallStack", maxCallStack);
		return result;
	}
}
