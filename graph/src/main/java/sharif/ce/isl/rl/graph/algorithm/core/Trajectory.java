package sharif.ce.isl.rl.graph.algorithm.core;

import java.util.ArrayList;
import java.util.List;

import sharif.ce.isl.rl.graph.environment.State;

public class Trajectory {
	public List<State> AllStates=new ArrayList<State>();
	public List<Integer> AllStatesID=new ArrayList<Integer>();
	public void add(State s,Integer id) { AllStates.add(s); AllStatesID.add(id);}
	
}
