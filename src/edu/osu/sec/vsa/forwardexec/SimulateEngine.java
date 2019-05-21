package edu.osu.sec.vsa.forwardexec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.osu.sec.vsa.base.GlobalStatistics;
import edu.osu.sec.vsa.base.ParameterTransferStmt;
import edu.osu.sec.vsa.graph.DGraph;
import edu.osu.sec.vsa.graph.HeapObject;
import edu.osu.sec.vsa.main.ApkContext;
import edu.osu.sec.vsa.utility.FunctionUtility;
import edu.osu.sec.vsa.utility.Logger;
import edu.osu.sec.vsa.utility.OtherUtility;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

public class SimulateEngine extends AbstractStmtSwitch {
	DGraph dg;
	StmtPath spath;
	HashMap<Value, HashSet<String>> currentValues = new HashMap<Value, HashSet<String>>();

	public SimulateEngine(DGraph dg, StmtPath spath) {
		this.dg = dg;
		this.spath = spath;
	}

	public StmtPath getSpath() {
		return spath;
	}

	public HashMap<Value, HashSet<String>> getCurrentValues() {
		return currentValues;
	}

	public void setInitValue(Value val, String str, boolean append) {
		HashSet<String> tmp;
		if (!this.getCurrentValues().containsKey(val)) {
			tmp = new HashSet<String>();
			this.getCurrentValues().put(val, tmp);
		} else {
			tmp = this.getCurrentValues().get(val);
		}
		if (!append) {
			tmp.clear();
		}
		tmp.add(str);
	}

	@SuppressWarnings("unchecked")
	public void transferValues(Value from, Value to) {
		HashSet<String> vs = this.getCurrentValues().get(from);
		this.getCurrentValues().remove(to);

		if (vs != null) {
			this.getCurrentValues().put(to, (HashSet<String>) vs.clone());
		}
	}

	@SuppressWarnings("unchecked")
	public void transferValuesAndAppend(Stmt stmt, Value from, Value to, Value arg, boolean apdontheold, boolean delold) {
		if (!this.getCurrentValues().containsKey(from)) {
			this.getCurrentValues().remove(to);
			return;
		}

		HashSet<String> apds = null;
		if (this.getCurrentValues().containsKey(arg)) {
			apds = this.getCurrentValues().get(arg);
		} else if (arg instanceof StringConstant) {
			apds = new HashSet<String>();
			apds.add(((StringConstant) arg).value);
		} else {
			Logger.printW(String.format("[%s] [SIMULATE][transferValuesAndAppend arg unknow]: %s (%s)", this.hashCode(), stmt, arg.getClass()));
			apds = new HashSet<String>();
			apds.add(String.format("[unknown:%s]", arg.getClass()));
			return;
		} //

		HashSet<String> vs = this.getCurrentValues().get(from);
		HashSet<String> newValues = new HashSet<String>();
		for (String apd : apds) {
			for (String str : vs) {
				newValues.add(str + apd);
			}
		}

		if (apdontheold) {
			this.getCurrentValues().put(from, newValues);
		}

		if (delold) {
			this.getCurrentValues().remove(from);
		}

		this.getCurrentValues().put(to, (HashSet<String>) newValues.clone());
	}

	public String getPrintableValues() {
		StringBuilder sb = new StringBuilder();
		for (Value var : this.getCurrentValues().keySet()) {
			sb.append("    ");
			sb.append(var);
			sb.append('(');
			sb.append(var.hashCode());
			sb.append(')');
			sb.append(':');
			for (String content : this.getCurrentValues().get(var)) {
				sb.append(content);
				sb.append(",");
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public void simulate() {
		Unit lastUnit = getSpath().getStmtPathTail();

		for (Stmt stmt : getSpath().getStmtPath()) {
			if (stmt == lastUnit) {
				return;
			}
			String oldv = getPrintableValues();
			Logger.print("[SIMULATE]" + this.hashCode() + ": " + stmt + " " + stmt.getClass());
			if (stmt instanceof ParameterTransferStmt) {
				caseAssignStmt((AssignStmt) stmt);
			} else {
				stmt.apply(this);
			}
			String newv = getPrintableValues();
			Logger.print(oldv + "\n====>\n" + newv);
		}
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		// TODO Auto-generated method stub
		
		String msig = stmt.getInvokeExpr().getMethod().toString();
		InvokeExpr vie =  stmt.getInvokeExpr();
		if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
			transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), ((VirtualInvokeExpr) vie).getBase(), vie.getArg(0), true, false);
		}else{
			super.caseInvokeStmt(stmt);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub
		Value leftop = stmt.getLeftOp();
		Value rightop = stmt.getRightOp();
		if (leftop instanceof Local || leftop instanceof ParameterRef || leftop instanceof ArrayRef) {
			if (rightop instanceof InvokeExpr) {
				InvokeExpr vie = (InvokeExpr) rightop;
				String msig = vie.getMethod().toString();
				if (msig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
					GlobalStatistics.getInstance().countAppendString();
					transferValuesAndAppend(stmt, ((VirtualInvokeExpr) vie).getBase(), leftop, vie.getArg(0), true, false);
				} else if (msig.equals("<android.content.Context: java.lang.String getString(int)>") || msig.equals("<android.content.res.Resources: java.lang.String getString(int)>")) {
					GlobalStatistics.getInstance().countGetString();
					if (vie.getArg(0) instanceof IntConstant) {
						setInitValue(leftop, ApkContext.getInstance().findResource(((IntConstant) vie.getArg(0)).value), false);
					} else if (this.getCurrentValues().get(vie.getArg(0)).size() > 0) {
						for (String str : (HashSet<String>) this.getCurrentValues().get(vie.getArg(0)).clone()) {
							this.getCurrentValues().remove(leftop);
							if (OtherUtility.isInt(str)) {
								setInitValue(leftop, ApkContext.getInstance().findResource(Integer.parseInt(str)), true);
							} else {
								Logger.printW(String.format("[%s] [SIMULATE][arg value not int getString(VirtualInvokeExpr)]: %s (%s)", this.hashCode(), stmt, str));
							}
						}
					} else {
						Logger.printW(String.format("[%s] [SIMULATE][arg not int getString(VirtualInvokeExpr)]: %s (%s)", this.hashCode(), stmt, vie.getArg(0).getClass()));
					}
				} else if (msig.equals("<java.lang.StringBuilder: java.lang.String toString()>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<java.lang.String: java.lang.String trim()>")) {
					transferValues(((VirtualInvokeExpr) vie).getBase(), leftop);
				} else if (msig.equals("<android.content.Context: java.lang.String getPackageName()>")) {
					setInitValue(leftop, ApkContext.getInstance().getPackageName(), false);
				} else if (msig.equals("<android.content.res.Resources: int getIdentifier(java.lang.String,java.lang.String,java.lang.String)>")) {
					this.getCurrentValues().remove(leftop);
					for (String p1 : this.getContent(vie.getArg(0))) {
						for (String p2 : this.getContent(vie.getArg(1))) {
							// for (String p3 : this.getContent(vie.getArg(2)))
							// {
							setInitValue(leftop, ApkContext.getInstance().getIdentifier(p1, p2, null), true);
							// }
						}
					}

				} else if (msig.equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>")) {
					GlobalStatistics.getInstance().countFormatString();
					FunctionUtility.String_format(this, leftop, vie);
				} else {
					Logger.printW(String.format("[%s] [SIMULATE][right unknown(VirtualInvokeExpr)]: %s (%s)", this.hashCode(), stmt, rightop.getClass()));
				}

			} else if (rightop instanceof NewExpr) {
				if (rightop.getType().toString().equals("java.lang.StringBuilder")) {
					setInitValue(leftop, "", false);
				} else {
					Logger.printW(String.format("[%s] [SIMULATE][right unknown(NewExpr)]: %s (%s)", this.hashCode(), stmt, rightop.getClass()));
				}
			} else if (rightop instanceof FieldRef) {
				HeapObject ho = HeapObject.getInstance(dg, ((FieldRef) rightop).getField());
				if (ho != null) {
					if (ho.inited() && ho.hasSolved()) {
						HashSet<String> nv = new HashSet<String>();
						ArrayList<HashMap<Integer, HashSet<String>>> hoResult = ho.getResult();
						for (HashMap<Integer, HashSet<String>> var : hoResult) {
							nv.addAll(var.get(-1));
						}
						this.getCurrentValues().put(leftop, nv);
					} else {
						Logger.printW(String.format("[%s] [SIMULATE][HeapObject not inited or Solved]: %s (%s)", this.hashCode(), stmt, ho.inited()));
					}
				} else {
					Logger.printW(String.format("[%s] [SIMULATE][HeapObject not found]: %s (%s)", this.hashCode(), stmt, rightop.getClass()));
				}
			} else if (rightop instanceof Local) {
				transferValues(stmt.getRightOp(), stmt.getLeftOp());
			} else if (rightop instanceof StringConstant) {
				setInitValue(leftop, ((StringConstant) rightop).value, false);
			} else if (rightop instanceof IntConstant) {
				setInitValue(leftop, ((IntConstant) rightop).value + "", false);
			} else if (rightop instanceof NewArrayExpr) {
				setInitValue(leftop, ((NewArrayExpr) rightop).getSize() + "", false);
			} else {
				Logger.printW(String.format("[%s] [SIMULATE][right unknown]: %s (%s)", this.hashCode(), stmt, rightop.getClass()));
			}
		} else {
			Logger.printW(String.format("[%s] [SIMULATE][left unknown]: %s (%s)", this.hashCode(), stmt, leftop.getClass()));
		}
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		transferValues(stmt.getRightOp(), stmt.getLeftOp());
	}

	@Override
	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		Logger.printW(String.format("[%s] [SIMULATE][Can't Handle]: %s (%s)", this.hashCode(), obj, obj.getClass()));
	}

	public HashSet<String> getContent(Value valu) {
		HashSet<String> vs = new HashSet<String>();
		if (this.getCurrentValues().containsKey(valu)) {
			return this.getCurrentValues().get(valu);
		} else if (valu instanceof StringConstant) {
			vs.add(((StringConstant) valu).value);
		} else if (valu instanceof IntConstant) {
			vs.add(((IntConstant) valu).value + "");
		}
		return vs;
	}

}
