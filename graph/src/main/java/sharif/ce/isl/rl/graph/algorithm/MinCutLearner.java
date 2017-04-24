package sharif.ce.isl.rl.graph.algorithm;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.State;

class Edge {
	int source, destination;

}

public class MinCutLearner extends SubgoalBasedQLearner {

	public MinCutLearner(Environment earth) {
		super(earth, 2);
		g = new DirectedSparseGraph<Integer, String>();
	}

	@Override
	public String getName() {
		return "MinCut";
	}

	public int walk(int totalLifeCycles) {

		int returnValue = -1;
		State currentState, prevState;
		Object currentAction;
		currentState = environment.currentState(); /// initial state
		prevState = currentState; // not important at the moment
		currentAction = new Integer(0);// Action.RIGHT; // not important at the
		// moment

		double reward;
		int numOfActionsDoneInOneEpisode = 0;

		while (episodeNum < totalLifeCycles) {

			currentState = environment.currentState(); /// initial state sabet
														/// nist
			currentAction = SelectAction(currentState, epsilone);
			// System.out.println("selectedAction: "+(Integer)currentAction);

			numOfActionsDoneInOneEpisode++;
			reward = environment.ApplyAction(currentAction);
			prevState = currentState;
			currentState = environment.currentState(); // state pas az anjam
														// action

			double qValue = QTable1[prevState.ID()][(Integer) currentAction];
			QTable1[prevState.ID()][(Integer) currentAction] = (1 - alpha) * qValue
					+ alpha * (reward + gamma * (MAXofQs(currentState)));

			if (environment.EpisodeFinished()) {

				// System.out.println("goal: x: "+currentState.x+", y:
				// "+currentState.y);
				numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));

				System.out.println(numOfActionsDoneInOneEpisode);
				numOfActionsDoneInOneEpisode = 0;

				episodeNum++;
				initialState = environment.ReNatal();

				if (episodeNum == discoveryEpisodeThreshold) {

				}
			}

		}
		System.out.println("----------------");
		return returnValue;
	}

	@Override
	public void learnOptions() {
		// TODO Auto-generated method stub

	}

}
