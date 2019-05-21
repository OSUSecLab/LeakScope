package edu.osu.sec.vsa.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import edu.osu.sec.vsa.backwardslicing.BackwardContext;
import edu.osu.sec.vsa.backwardslicing.BackwardController;
import edu.osu.sec.vsa.base.StmtPoint;
import edu.osu.sec.vsa.forwardexec.SimulateEngine;
import edu.osu.sec.vsa.utility.Logger;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.Block;

public class ValuePoint implements IDGNode {

	DGraph dg;

	SootMethod method_location;
	Block block_location;
	Unit instruction_location;
	HashSet<Integer> target_regs = new HashSet<Integer>();
	List<BackwardContext> bcs = null;
	HashSet<BackwardContext> solvedBCs = new HashSet<BackwardContext>();

	Object appendix = "";

	ArrayList<HashMap<Integer, HashSet<String>>> result = new ArrayList<HashMap<Integer, HashSet<String>>>();

	boolean inited = false;
	boolean solved = false;

	public ValuePoint(DGraph dg, SootMethod method_location, Block block_location, Unit instruction_location, List<Integer> regIndex) {
		this.dg = dg;
		this.method_location = method_location;
		this.block_location = block_location;
		this.instruction_location = instruction_location;
		for (int i : regIndex) {
			target_regs.add(i);
		}
		dg.addNode(this);
	}

	public DGraph getDg() {
		return dg;
	}

	public List<BackwardContext> getBcs() {
		return bcs;
	}

	public SootMethod getMethod_location() {
		return method_location;
	}

	public Block getBlock_location() {
		return block_location;
	}

	public Unit getInstruction_location() {
		return instruction_location;
	}

	public Set<Integer> getTargetRgsIndexes() {
		return target_regs;
	}

	public void setAppendix(Object str) {
		appendix = str;
	}

	@Override
	public Set<IDGNode> getDependents() {
		// TODO Auto-generated method stub

		HashSet<IDGNode> dps = new HashSet<IDGNode>();
		for (BackwardContext bc : bcs) {
			for (IDGNode node : bc.getDependentHeapObjects()) {
				dps.add(node);
			}
		}
		return dps;
	}

	@Override
	public int getUnsovledDependentsCount() {
		// TODO Auto-generated method stub
		int count = 0;
		for (IDGNode node : getDependents()) {
			if (!node.hasSolved()) {
				count++;
			}
		}
		Logger.print(this.hashCode() + "[]" + count + " " + bcs.size());
		return count;
	}

	@Override
	public boolean hasSolved() {

		return solved;
	}

	@Override
	public boolean canBePartiallySolve() {
		boolean can = false;
		boolean dsolved;
		SimulateEngine tmp;
		for (BackwardContext bc : bcs) {
			if (!solvedBCs.contains(bc)) {
				dsolved = true;
				for (HeapObject ho : bc.getDependentHeapObjects()) {
					if (!ho.hasSolved()) {
						dsolved = false;
						break;
					}
				}
				if (dsolved) {
					solvedBCs.add(bc);
					can = true;
					tmp = new SimulateEngine(dg, bc);
					tmp.simulate();
					mergeResult(bc, tmp);
				}
			}
		}
		if (can) {
			solved = true;
		}

		return can;
	}

	@Override
	public void solve() {
		solved = true;
		Logger.print("[SOLVING ME]" + this.hashCode());
		SimulateEngine tmp;
		for (BackwardContext var : this.getBcs()) {
			tmp = new SimulateEngine(dg, var);
			tmp.simulate();
			mergeResult(var, tmp);
		}

	}

	public void mergeResult(BackwardContext var, SimulateEngine tmp) {
		HashMap<Value, HashSet<String>> sval = tmp.getCurrentValues();
		HashMap<Integer, HashSet<String>> resl = new HashMap<Integer, HashSet<String>>();
		Value reg;
		for (int i : target_regs) {
			if (i == -1) {
				reg = ((AssignStmt) var.getStmtPathTail()).getRightOp();
			} else {
				reg = ((Stmt) var.getStmtPathTail()).getInvokeExpr().getArg(i);
			}

			if (sval.containsKey(reg)) {
				resl.put(i, sval.get(reg));
			} else if (reg instanceof StringConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((StringConstant) reg).value);
			} else if (reg instanceof IntConstant) {
				resl.put(i, new HashSet<String>());
				resl.get(i).add(((IntConstant) reg).value + "");
			}
		}
		result.add(resl);
	}

	@Override
	public boolean inited() {
		return inited;
	}

	@Override
	public void initIfHavenot() {
		inited = true;

		bcs = BackwardController.getInstance().doBackWard(this, dg);

	}

	@Override
	public ArrayList<HashMap<Integer, HashSet<String>>> getResult() {
		return result;
	}

	public static List<ValuePoint> find(DGraph dg, String signature, List<Integer> regIndex) {
		List<ValuePoint> vps = new ArrayList<ValuePoint>();

		List<StmtPoint> sps = StmtPoint.findCaller(signature);
		ValuePoint tmp;
		for (StmtPoint sp : sps) {
			// XXXXXXXXXXXXX
			// XXXXXXXXXXXXX
			// XXXXXXXXXXXXX
			// XXXXXXXXXXXXX
			// if
			// (sp.getMethod_location().toString().equals("<com.amazonaws.services.s3.AmazonS3Client:
			// com.amazonaws.services.s3.model.PutObjectResult
			// putObject(com.amazonaws.services.s3.model.PutObjectRequest)>")) {
			tmp = new ValuePoint(dg, sp.getMethod_location(), sp.getBlock_location(), sp.getInstruction_location(), regIndex);
			vps.add(tmp);
			// }
		}
		return vps;
	}

	public void print() {
		System.out.println("===============================================================");
		System.out.println("Class: " + method_location.getDeclaringClass().toString());
		System.out.println("Method: " + method_location.toString());
		System.out.println("Bolck: ");
		block_location.forEach(u -> {
			System.out.println("       " + u);
		});
		target_regs.forEach(u -> {
			System.out.println("              " + u);
		});

	}

	public String toString() {
		if (!inited)
			return super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("===========================");
		sb.append(this.hashCode());
		sb.append("===========================\n");
		sb.append("Class: " + method_location.getDeclaringClass().toString() + "\n");
		sb.append("Method: " + method_location.toString() + "\n");
		sb.append("Target: " + instruction_location.toString() + "\n");
		sb.append("Solved: " + hasSolved() + "\n");
		sb.append("Depend: ");
		for (IDGNode var : this.getDependents()) {
			sb.append(var.hashCode());
			sb.append(", ");
		}
		sb.append("\n");
		sb.append("BackwardContexts: \n");
		BackwardContext tmp;
		for (int i = 0; i < this.bcs.size(); i++) {
			tmp = this.bcs.get(i);
			sb.append("  " + i + "\n");
			for (Stmt stmt : tmp.getExecTrace()) {
				sb.append("    " + stmt + "\n");
			}
			// sb.append(" i:");
			// for (Value iv : tmp.getIntrestedVariable()) {
			// sb.append(" " + iv + "\n");
			// }
		}
		sb.append("ValueSet: \n");
		for (HashMap<Integer, HashSet<String>> resl : result) {
			sb.append("  ");
			for (int i : resl.keySet()) {
				sb.append(" |" + i + ":");
				for (String str : resl.get(i)) {
					sb.append(str + ",");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public JSONObject toJson() {
		JSONObject js = new JSONObject();
		JSONObject tmp;
		for (HashMap<Integer, HashSet<String>> var : this.getResult()) {
			tmp = new JSONObject();
			for (int i : var.keySet()) {
				for (String str : var.get(i)) {
					tmp.append(i + "", str);
				}
			}
			js.append("ValueSet", tmp);
		}
		if (bcs != null)
			for (BackwardContext bc : bcs) {
				js.append("BackwardContexts", bc.toJson());
			}
		js.put("hashCode", this.hashCode() + "");
		js.put("SootMethod", this.getMethod_location().toString());
		js.put("Block", this.getBlock_location().hashCode());
		js.put("Unit", this.getInstruction_location());
		js.put("UnitHash", this.getInstruction_location().hashCode());
		js.put("appendix", appendix);

		return js;
	}
}
