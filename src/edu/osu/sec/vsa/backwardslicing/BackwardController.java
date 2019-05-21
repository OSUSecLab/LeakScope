package edu.osu.sec.vsa.backwardslicing;

import java.util.ArrayList;
import java.util.List;

import edu.osu.sec.vsa.graph.DGraph;
import edu.osu.sec.vsa.graph.ValuePoint;

public class BackwardController {
	static BackwardController sc = new BackwardController();

	public static BackwardController getInstance() {
		return sc;
	}

	private BackwardController() {

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public List<BackwardContext> doBackWard(ValuePoint vp, DGraph dg) {
		List<BackwardContext> bcs = new ArrayList<BackwardContext>();
		bcs.add(new BackwardContext(vp, dg));

		BackwardContext bc;
		while (true) {
			bc = null;
			for (BackwardContext tmp : bcs) {
				if (!tmp.backWardHasFinished()) {
					bc = tmp;
					break;
				}
			}
			if (bc == null) {
				break;
			}
			bcs.addAll(bc.oneStepBackWard());
			//bc.oneStepBackWard();

		}

		bcs.forEach(var -> {
			var.printExceTrace();
		});

		return bcs;

	}

}

/*
 * if (bcs.get(0).getIntrestedVariable().size() > 0) {
 * System.out.println("---------");
 * 
 * HashSet<Value> tt = (HashSet<Value>)
 * bcs.get(0).getIntrestedVariable().clone(); System.out.println(tt.size() + " "
 * + bcs.get(0).getIntrestedVariable().size());
 * System.out.println(tt.toArray()[0]); System.out.println(tt.toArray()[0] ==
 * bcs.get(0).getIntrestedVariable().toArray()[0]); tt.clear();
 * System.out.println(tt.size() + " " +
 * bcs.get(0).getIntrestedVariable().size());
 * 
 * System.out.println("-");
 * 
 * List<Stmt> lls = (List<Stmt>) ((ArrayList)
 * bcs.get(0).getExecTrace()).clone(); System.out.println(lls.size() + " " +
 * bcs.get(0).getExecTrace().size()); System.out.println(lls.toArray()[0]);
 * System.out.println(lls.toArray()[0] ==
 * bcs.get(0).getExecTrace().toArray()[0]); lls.clear();
 * System.out.println(lls.size() + " " + bcs.get(0).getExecTrace().size());
 * 
 * System.out.println("---------"); }
 */
