package sharif.ce.isl.rl.graph.algorithm.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

public class QLearningAgent {

	protected static Random rand = new Random();

	///////// Environment ////////////////////
	protected Environment environment;
	protected State initialState;
	protected final int maxStateID;

	//////// Algorithm ////////////////////
	protected int episodeNum = 0;
	protected double alpha = 0.1;
	protected double gamma = 0.9;
	protected double epsilone = 0.2;
	protected double[][] QTable1;

	////////// Logging //////////////////////
	protected List<Integer> numOfActionsInEpisode;
	protected List<Double> totalRewardInEpisode;

	// protected ArrayList<Integer> nodeTable = new ArrayList();
	// protected int[][] edgeTable;
	// private int numOfEdges = 0;

	//////////////////////// QLearning Constructor
	//////////////////////// //////////////////////////////////////
	public QLearningAgent(Environment earth, int flag) {

		environment = earth;
		this.initialState = earth.currentState;
		maxStateID = earth.getMaxStateID();

		// flag == 1 -----> Qlearning
		// flag == 2 -----> SubgoalBasedQLearner
		if (flag == 1) {
			int numOfPrimitiveActions = environment.ActionSet().size();
			QTable1 = new double[maxStateID][numOfPrimitiveActions];
		}
		numOfActionsInEpisode = new ArrayList<Integer>();
		totalRewardInEpisode = new ArrayList<Double>();

		// edgeTable = new int[maxStateID][maxStateID];
	}

	protected int abs(int x) {
		if (x >= 0)
			return x;
		else
			return -1 * x;
	}

	public void show_episode_statistics() {
		try {
			FileWriter writer;

			writer = new FileWriter("excel_result/" + environment.getName() + "/" + this.getName() + "_steps.txt",
					true);

			double sum_episode_stage = 0, average_episode_stage;
			int counter = 0;
			for (int episode_stage : numOfActionsInEpisode) {
				counter++;
				sum_episode_stage += episode_stage;
				if (counter % 5 == 0) {
					average_episode_stage = sum_episode_stage / 5;
					// System.out.println(average_episode_stage);
					// writer2.append(average_episode_stage+"\n");
					writer.append(average_episode_stage + "\t");
					sum_episode_stage = 0;
				}
			}

			writer.append("\n");
			// writer2.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

	}

	public void show_reward_statistics() {
		try {
			FileWriter writer;

			writer = new FileWriter("excel_result/" + environment.getName() + "/" + this.getName() + "_reward.txt",
					true);

			double sum_episode_stage = 0;
			int counter = 0;
			for (double episode_stage : totalRewardInEpisode) {
				counter++;
				sum_episode_stage += episode_stage;
				if (counter % 5 == 0) {
					writer.append(sum_episode_stage / 5 + "\t");
					sum_episode_stage = 0;
				}
			}

			writer.append("\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

	}

	public void show_nodes_statistics(int episode) {
		try {
			FileWriter writer, writer2;

			if (environment.getClass() == GridWorldEnv.class) {
				writer = new FileWriter("ant_result/nodes/9rooms_nodes.xls", true); // 9rooms
				writer2 = new FileWriter("ant_result/nodes/9rooms_edges.xls", true);
			} else if (environment.getClass() == TaxiDriverEnv.class) {
				writer = new FileWriter("ant_result/nodes/taxi_nodes.xls", true);
				writer2 = new FileWriter("ant_result/nodes/taxi_edges.xls", true);
			} else if (environment.getClass() == HanoiTowerEnv.class) {
				writer = new FileWriter("ant_result/nodes/hanoi_nodes.xls", true);
				writer2 = new FileWriter("ant_result/nodes/hanoi_edges.xls", true);
			} else // if(environment.getClass() == PlayRoomEnv.class)
			{
				writer = new FileWriter("ant_result/nodes/playroom_nodes.xls", true);
				writer2 = new FileWriter("ant_result/nodes/playroom_edges.xls", true);
			}

			// writer.append(nodeTable.size()+"\t");
			// writer2.append(numOfEdges+"\t");

			if (episode == 29) {
				writer.append("\n");
				writer2.append("\n");
			}

			writer.close();
			writer2.close();
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

	}

	public int walk(int totalLifeCycles) {

		int returnValue = -1;
		State currentState, prevState;
		Object currentAction;
		currentState = environment.currentState(); /// initial state
		prevState = currentState; // not important at the moment
		currentAction = new Integer(0);// Action.RIGHT; // not important at the
		double totalReward = 0;
		// moment

		double reward;
		int numOfActionsDoneInOneEpisode = 0;

		while (episodeNum < totalLifeCycles) {

			currentState = environment.currentState(); /// initial state sabet
														/// nist
			currentAction = SelectAction(currentState, epsilone);
			// System.out.print("action:\t"+currentAction);

			numOfActionsDoneInOneEpisode++;
			reward = environment.ApplyAction(currentAction);
			totalReward += reward;
			prevState = currentState;
			currentState = environment.currentState(); // state pas az anjam
														// action
			// System.out.println("\t"+currentState.ID());

			// logging
			// if (!nodeTable.contains(currentState.ID())) {
			// nodeTable.add(currentState.ID());
			// }
			// if(prevState.ID() != currentState.ID() &&
			// edgeTable[prevState.ID()][currentState.ID()]==0){
			// numOfEdges++;
			// edgeTable[prevState.ID()][currentState.ID()] = 1;
			// }
			// end of logging

			double qValue = QTable1[prevState.ID()][(Integer) currentAction];
			QTable1[prevState.ID()][(Integer) currentAction] = (1 - alpha) * qValue
					+ alpha * (reward + gamma * (MAXofQs(currentState)));

			if (environment.EpisodeFinished()) {

				// System.out.println("goal: x: "+currentState.x+", y:
				// "+currentState.y);
				numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
				totalRewardInEpisode.add(totalReward);
				// System.out.println(numOfActionsDoneInOneEpisode+"\t"+totalReward);

				// show_nodes_statistics(episodeNum);
				numOfActionsDoneInOneEpisode = 0;
				totalReward = 0;

				episodeNum++;
				initialState = environment.ReNatal();

				// System.out.println(stateTable.size());
				// stateTable.clear();
			}

		}
		System.out.println("----------------");
		return returnValue;
	}

	protected Object SelectAction(State state, double epsilone) {

		Object selectedAction;
		List<Object> admissibleActions;
		admissibleActions = environment.GetAdmissibleActions(state);
		if (rand.nextDouble() <= epsilone) {
			selectedAction = admissibleActions.get(rand.nextInt(admissibleActions.size()));
		} else {
			double maxValue = -(Double.MAX_VALUE);

			for (Object admissibleAction : admissibleActions) {

				double tempValue = QTable1[state.ID()][(Integer) admissibleAction];

				if (maxValue < tempValue) {
					maxValue = tempValue;
					selectedAction = admissibleAction;

				}
			}
			List<Object> actionsWithMaxValue = new ArrayList();
			for (Object admissibleAction : admissibleActions) {
				if (QTable1[state.ID()][(Integer) admissibleAction] == maxValue)
					actionsWithMaxValue.add(admissibleAction);
			}
			selectedAction = actionsWithMaxValue.get(rand.nextInt(actionsWithMaxValue.size()));
		}
		return selectedAction;

	}

	// returns the maximum Q value for the given state.
	protected double MAXofQs(State state) {
		return QTable1[state.ID()][(Integer) SelectAction(state, 0)];
	}

	public String getName() {
		return "Q-Learning";
	}
}
