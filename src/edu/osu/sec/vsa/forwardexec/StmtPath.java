package edu.osu.sec.vsa.forwardexec;

import java.util.List;

import soot.Unit;
import soot.jimple.Stmt;

public interface StmtPath {

	public Unit getStmtPathHeader();

	public Unit getSuccsinStmtPath(Unit u);

	public Unit getPredsinStmtPath(Unit u);

	public Unit getStmtPathTail();

	public List<Stmt> getStmtPath();
}
