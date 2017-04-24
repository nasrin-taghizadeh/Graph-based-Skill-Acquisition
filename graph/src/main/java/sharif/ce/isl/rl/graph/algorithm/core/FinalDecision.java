package sharif.ce.isl.rl.graph.algorithm.core;

import java.util.HashMap;

import sharif.ce.isl.rl.graph.environment.State;

public class FinalDecision {
	public HashMap<Integer, State> allObservedStates;
	public HashMap<Integer, Integer> totalNumOfObservationsForState;
	public HashMap<Integer, Integer> totalNumOfBeingHit;

	FinalDecision() {
		allObservedStates = new HashMap<Integer, State>();
		totalNumOfObservationsForState = new HashMap<Integer, Integer>();
		totalNumOfBeingHit = new HashMap<Integer, Integer>();
	}
}
