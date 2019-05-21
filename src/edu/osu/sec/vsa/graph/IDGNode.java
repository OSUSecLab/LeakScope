package edu.osu.sec.vsa.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public interface IDGNode {

	public Set<IDGNode> getDependents();

	public int getUnsovledDependentsCount();

	public boolean hasSolved();

	public void solve();

	public boolean canBePartiallySolve();

	public void initIfHavenot();

	public boolean inited();

	public ArrayList<HashMap<Integer, HashSet<String>>> getResult();
}
