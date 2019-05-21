package edu.osu.sec.vsa.utility;

import java.util.HashSet;
import java.util.List;

import edu.osu.sec.vsa.forwardexec.SimulateEngine;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InvokeExpr;

public class FunctionUtility {

	@SuppressWarnings("unchecked")
	public static void String_format(SimulateEngine se, Value leftop, InvokeExpr vie) {
		// TODO Auto-generated method stub
		se.getCurrentValues().remove(leftop);
		HashSet<String> sformat = se.getContent(vie.getArg(0));
		if (sformat.size() <= 0)
			return;

		if (!se.getCurrentValues().containsKey(vie.getArg(1)))
			return;

		HashSet<String> hs_len = se.getContent(vie.getArg(1));
		if (hs_len.size() <= 0)
			return;
		int len = Integer.parseInt(hs_len.toArray()[0] + "");
		ArrayRef[] argRefs = new ArrayRef[len];
		int foundCount = 0;
		for (Value val : se.getCurrentValues().keySet()) {
			if (val instanceof ArrayRef && ((ArrayRef) val).getBase().equivTo(vie.getArg(1))) {
				argRefs[Integer.parseInt(((ArrayRef) val).getIndex().toString())] = (ArrayRef) val;
				foundCount++;
			}
		}
		if (foundCount != len) {
			Logger.printW("Some Args are unknow " + vie);
			return;
		}

		for (ArrayRef argRef : argRefs) {
			se.getContent(argRef).size();
		}

		int[] maxIndex = new int[len];
		List<String>[] vs = new List[len];
		int[] indexes = new int[len];
		HashSet<String> tmp;
		for (int i = 0; i < len; i++) {
			tmp = se.getContent(argRefs[i]);
			vs[i] = ListUtility.Array2List((tmp.toArray(new String[tmp.size()])));
			maxIndex[i] = vs[i].size();

			indexes[i] = 0;
		}

		Object[] obj = new Object[len];
		for (String formt : sformat) {
			for (int i = 0; i < len; i++) {
				indexes[i] = 0;
			}
			do {
				for (int i = 0; i < len; i++) {
					obj[i] = vs[i].get(indexes[i]);
				}
				se.setInitValue(leftop, String.format(formt, obj), true);
			} while (move2Next(maxIndex, indexes));
		}
	}

	public static boolean move2Next(int[] maxIndex, int[] indexes) {
		indexes[0]++;
		for (int i = 0; i < maxIndex.length; i++) {
			if (indexes[i] >= maxIndex[i]) {
				indexes[i] = 0;
				if (i + 1 >= maxIndex.length) {
					return false;
				}
				indexes[i + 1]++;
			}
		}
		return true;
	}

	public static void main(String[] arg) {
		String a = "%s-%s";
		Object[] obj = new Object[2];
		obj[0] = "aa";
		obj[1] = "bb";
		System.out.println(String.format(a, obj));
	}

}
