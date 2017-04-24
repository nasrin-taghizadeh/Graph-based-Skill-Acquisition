package sharif.ce.isl.rl.graph.algorithm.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uci.ics.jung.graph.Graph;
import sharif.ce.isl.rl.graph.algorithm.ExperienceElement;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

public abstract class SubgoalBasedQLearner extends QLearningAgent {

	protected int discoveryEpisodeThreshold;

	protected int[][] edgeWeights;
	protected HashMap<Integer, State> stateTable = new HashMap();
	protected Graph<Integer, String> g;
	protected FinalDecision logger = new FinalDecision();
	protected List<ExperienceElement> myExperience;
	protected HashMap<Integer, List<MarkovOption>> applicableOptions;
	protected List<MarkovOption> myGeneratedOptions;
	protected Trajectory T = new Trajectory();
	protected List<Trajectory> ListTrajectory = new ArrayList<Trajectory>();

	public SubgoalBasedQLearner(Environment earth, int flag) {
		super(earth, 2);

		if (!stateTable.containsKey(initialState.ID()))
			stateTable.put(initialState.ID(), initialState);

		edgeWeights = new int[maxStateID][maxStateID];

		for (int i = 0; i < maxStateID; i++)
			for (int j = 0; j < maxStateID; j++)
				edgeWeights[i][j] = 0;

		if (flag == 2) {
			int numOfPrimitiveActions = environment.ActionSet().size();
			int MAX_AVAIL_ACT_OR_OPTIONS = numOfPrimitiveActions + 40;
			QTable1 = new double[maxStateID][MAX_AVAIL_ACT_OR_OPTIONS];
		}

		applicableOptions = new HashMap<Integer, List<MarkovOption>>();
		myExperience = new ArrayList<ExperienceElement>();
		myGeneratedOptions = new ArrayList<MarkovOption>();

		if (environment.getClass() == GridWorldEnv.class)
			discoveryEpisodeThreshold = 10;

		else if (environment.getClass() == TaxiDriverEnv.class)
			discoveryEpisodeThreshold = 7;

		else if (environment.getClass() == PlayRoomEnv.class)
			discoveryEpisodeThreshold = 15;

		if (environment.getClass() == HanoiTowerEnv.class)
			discoveryEpisodeThreshold = 7;
	}

	final protected boolean UpdateDirectedEdgeTable(int[][] edgeTable, int id1, int id2) {
		edgeTable[id1][id2] += 1;
		// allEpisodesEdgeWeighs[id1][id2] += 1;
		return true;
	}

	protected boolean UpdateUndirectedEdgeTable(int[][] edgeTable, int id1, int id2) {
		edgeTable[id1][id2] += 1;
		edgeTable[id2][id1] = edgeTable[id1][id2];
		return true;
	}

	final protected boolean AddToStateTable(State state) {
		AddToStateTable(stateTable, state);
		return true;
	}

	protected boolean AddToStateTable(HashMap sTable, State state) {
		if (!sTable.containsKey(state.ID())) {
			sTable.put(state.ID(), state);
			return true;
		}
		return false;
	}

	protected int GetOptionIndexInQTable(MarkovOption option, State state) {
		List<MarkovOption> tempList = applicableOptions.get(state.ID());
		int optionActionIndex = tempList.indexOf(option) +
				// environment.GetAdmissibleActions(state).size();
				environment.getNumOfPrimitiveActions();

		return optionActionIndex;
	}

	protected void ExperienceReplay() {

		for (ExperienceElement incident : myExperience) {
			List<MarkovOption> intersectionOptions = Intersection(applicableOptions.get(incident.prevState.ID()),
					applicableOptions.get(incident.CurrentState.ID()));

			if (intersectionOptions != null) {
				for (MarkovOption option : intersectionOptions) {
					double totalReward = PseudoReward(option, incident.CurrentState, incident.reward);
					option.UpdateQ(incident.prevState, incident.Action, incident.CurrentState, totalReward);
				}
			}
		}
	}

	protected double PseudoReward(MarkovOption option, State currentState, double reward) {
		double totalReward = reward;
		// kazemitabar code
		// if (option.finalStates.contains(currentState)){
		// totalReward += 100;
		// }

		// marzieh code
		for (State st : option.finalStates)
			if (st.ID() == currentState.ID()) {
				totalReward += 100;
			}
		return totalReward;
	}

	protected List<MarkovOption> Intersection(List<MarkovOption> l1, List l2) {
		List<MarkovOption> result = new ArrayList();
		if (l1 != null && l2 != null)
			for (MarkovOption ob1 : l1)
				if (l2.contains(ob1))
					result.add(ob1);
		return result;
	}

	protected void AddApplicableOptionForState(MarkovOption newOption, int stID) {
		List<MarkovOption> myApplicableOptions = applicableOptions.get(stID);
		if (myApplicableOptions == null)
			myApplicableOptions = new ArrayList();
		myApplicableOptions.add(newOption);
		applicableOptions.put(stID, myApplicableOptions);
	}

	protected Object SelectAction(State state, double epsilone) {

		Object selectedAction;
		List<Object> admissibleList;
		admissibleList = environment.GetAdmissibleActions(state);
		// int numOfAdmissibleActions = admissibleList.size();
		int numOfAdmissibleActions = environment.allActions.size();
		List<MarkovOption> admissibleOptions = this.applicableOptions.get(state.ID());

		if (admissibleOptions != null) {
			for (int i = 0; i < admissibleOptions.size(); i++)
				admissibleList.add(new Integer(numOfAdmissibleActions + i));
		}

		int totalActions = admissibleList.size();
		if (rand.nextDouble() <= epsilone) {
			selectedAction = admissibleList.get(rand.nextInt(totalActions));
		} else {
			double maxValue = -(Double.MAX_VALUE);
			for (Object admissibleAction : admissibleList) {
				double tempValue = 0;
				tempValue = QTable1[state.ID()][(Integer) admissibleAction];
				if (maxValue < tempValue) {
					maxValue = tempValue;
					selectedAction = admissibleAction;

				}
			}
			List<Object> actionsWithMaxValue = new ArrayList();
			for (Object admissibleAction : admissibleList) {
				if (QTable1[state.ID()][(Integer) admissibleAction] == maxValue)
					actionsWithMaxValue.add(admissibleAction);
			}
			selectedAction = actionsWithMaxValue.get(rand.nextInt(actionsWithMaxValue.size()));
		}
		return selectedAction;
	}

	protected double U(MarkovOption option, State state) {
		double returnValue;
		// if( option.finalStates.contains(state)){
		for (State st : option.finalStates)
			if (st.ID() == state.ID()) {
				return MAXofQs(state);
			}
		int optionActionIndex = GetOptionIndexInQTable(option, state);
		return returnValue = QTable1[state.ID()][optionActionIndex];
	}

	public int walkWithOptions(int totalLifeCycles, boolean learn) {
		int returnValue = -1;
		State currentState, prevState;
		Object currentAction = new Integer(0);
		;
		double reward = 0;
		double totalEpisodeReward = 0;
		int numOfActionsDoneInOneEpisode = 0;
		int numOfActionsDoneInOneOption = 0;
		int selectedOption = 0;
		boolean inOption = false;
		double cumulativeReward = 0;
		MarkovOption thisMarkovOption = null;
		State optionsStartingState = null;
		long optionStartingIndex = 0;

		currentState = environment.currentState();
		prevState = currentState;
		episodeNum = 0;

		do {
			currentState = environment.currentState();
			currentAction = SelectAction(currentState, epsilone);
			if (currentState == null)
				System.out.println("in subgoal null");
			else
				T.add(currentState, currentState.ID());

			int numOfPrimitiveAdmissibleActions = environment.ActionSet().size();

			if (!inOption && (Integer) currentAction >= numOfPrimitiveAdmissibleActions) {
				selectedOption = (Integer) currentAction;
				thisMarkovOption = applicableOptions.get(currentState.ID())
						.get(selectedOption - numOfPrimitiveAdmissibleActions);
				inOption = true;
				numOfActionsDoneInOneOption = 0;
				cumulativeReward = 0;
				optionStartingIndex = myExperience.size();
				optionsStartingState = currentState;
			}

			if (inOption) {
				currentAction = thisMarkovOption.SelectAction(currentState);
				// System.out.println("in option ");
			}
			reward = environment.ApplyAction(currentAction);
			totalEpisodeReward += reward;

			if (inOption)
				cumulativeReward += reward * Math.pow(gamma, numOfActionsDoneInOneOption);

			if (inOption)
				numOfActionsDoneInOneOption++;
			numOfActionsDoneInOneEpisode++;

			prevState = currentState;
			currentState = environment.currentState();

			AddToStateTable(currentState);
			myExperience.add(new ExperienceElement(prevState, (Integer) currentAction, currentState, reward));
			UpdateDirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
			// UpdateUndirectedEdgeTable(edgeWeights, prevState.ID(),
			// currentState.ID());

			/*
			 * if (inOption) { thisMarkovOption. UpdateQ(prevState, (Integer)
			 * currentAction, currentState, reward); }
			 */
			for (MarkovOption option : myGeneratedOptions) {

				List<MarkovOption> firstList, secondList;
				firstList = applicableOptions.get(prevState.ID());
				secondList = applicableOptions.get(currentState.ID());

				if (firstList != null && secondList != null && firstList.contains(option)
						&& secondList.contains(option)) {
					double totalReward = PseudoReward(option, currentState, reward);
					option.UpdateQ(prevState, (Integer) currentAction, currentState, totalReward);
				}
			}

			// marzieh code
			int iflag = 0, fflag = 0;
			if (inOption) {
				for (State s : thisMarkovOption.initiationSet)
					if (s.ID() == currentState.ID()) {
						iflag = 1;
						break;
					}

				for (State s : thisMarkovOption.finalStates)
					if (s.ID() == currentState.ID()) {
						fflag = 1;
						break;
					}
			}

			boolean intraOptionLearning = true;
			// marzieh code
			if (inOption && (fflag == 1 || environment.EpisodeFinished() || iflag == 0)) { // end
																							// of
																							// option

				// kazemitabar code
				// if (inOption &&
				// (thisMarkovOption.finalStates.contains(currentState) ||
				// environment.EpisodeFinished() ||
				// !thisMarkovOption.initiationSet.contains(currentState))) {
				inOption = false;

				if (intraOptionLearning) {
					double tempCumulativeReward = 0;
					for (int i = myExperience.size(); i > optionStartingIndex; i--) {
						ExperienceElement element = myExperience.get(i - 1);
						tempCumulativeReward *= gamma;
						tempCumulativeReward += element.reward;
						Integer prevStateID = element.prevState.ID();
						int optionActionIndex = GetOptionIndexInQTable(thisMarkovOption, element.prevState);
						double qValue = QTable1[prevStateID][optionActionIndex];
						QTable1[prevStateID][optionActionIndex] = (1 - alpha) * qValue + alpha * (tempCumulativeReward
								+ Math.pow(gamma, myExperience.size() - i + 1) * (MAXofQs(currentState)));
					}
				}
				double qValue = QTable1[optionsStartingState.ID()][selectedOption];
				QTable1[optionsStartingState.ID()][selectedOption] = (1 - alpha) * qValue + alpha
						* (cumulativeReward + Math.pow(gamma, numOfActionsDoneInOneOption) * (MAXofQs(currentState)));

			} // end of option

			// ----------------- primitive action ----------------
			if (!inOption || (inOption && intraOptionLearning)) {

				double qValue = QTable1[prevState.ID()][(Integer) currentAction];
				QTable1[prevState.ID()][(Integer) currentAction] = (1 - alpha) * qValue
						+ alpha * (reward + gamma * (MAXofQs(currentState)));
			}
			// one-step intra-option learning
			if (inOption && intraOptionLearning) {
				int optionIndex = GetOptionIndexInQTable(thisMarkovOption, prevState);
				double qValue = QTable1[prevState.ID()][optionIndex];
				QTable1[prevState.ID()][optionIndex] = (1 - alpha) * qValue
						+ alpha * (reward + gamma * (U(thisMarkovOption, currentState)));
			}

			if (environment.EpisodeFinished()) {

				episodeNum++;
				numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
				totalRewardInEpisode.add(totalEpisodeReward);
				// System.out.println(numOfActionsDoneInOneEpisode);

				if (learn == true && episodeNum == discoveryEpisodeThreshold) {
					learnOptions();
				}
				// if (episodeNum == 2*discoveryEpisodeThreshold) {
				// // ExperienceReplay();
				// insertOstacle();
				// }
				initialState = environment.ReNatal();
				AddToStateTable(initialState);
				numOfActionsDoneInOneEpisode = 0;
				totalEpisodeReward = 0;
				ListTrajectory.add(T);
				T = new Trajectory();

			}

		} while (episodeNum < totalLifeCycles);

		return returnValue;
	}

	private void insertOstacle() {
		if (environment instanceof GridWorldEnv) {
			GridWorldEnv grid = (GridWorldEnv) environment;
			// grid.worldModel[2][3] = 1;
			// grid.worldModel[3][4] = 1;
			// grid.worldModel[4][1] = 1;
			// grid.worldModel[9][5] = 1;
			// grid.worldModel[9][9] = 1;
			// grid.worldModel[3][12] = 1;
			// grid.worldModel[4][12] = 1;
			// grid.worldModel[8][13] = 1;
			// grid.worldModel[11][15] = 1;
			// grid.worldModel[11][18] = 1;

			// *************
			// grid.worldModel[10][18] = 1;
			// grid.worldModel[8][19] = 1;
			// grid.worldModel[7][17] = 1;
			// grid.worldModel[11][11] = 1;
			// grid.worldModel[7][11] = 1;
			// grid.worldModel[10][9] = 1;
			// grid.worldModel[10][1] = 1;
			// grid.worldModel[9][7] = 1;
			// grid.worldModel[2][8] = 1;
			// grid.worldModel[1][5] = 1;

			// door
			grid.worldModel[4][10] = 1;
			grid.worldModel[3][10] = 0;
		}

	}

	public abstract void learnOptions();

	public void show_episode_statistics() {
		try {

			FileWriter writer;
			String file_name = "Result/" + environment.getName() + "/" + this.getName() + "_steps.txt";
			File file = new File(file_name);
			if (file.exists())
				file.delete();

			writer = new FileWriter(file_name, true);
			writer.write("#episode\tnumber of steps\n");

			double sum_episode_stage = 0, average_episode_stage;
			int counter = 0;
			for (int episode_stage : numOfActionsInEpisode) {
				counter++;
				sum_episode_stage += episode_stage;
				if (counter % 5 == 0) {
					average_episode_stage = sum_episode_stage / 5;
					// System.out.println(average_episode_stage);
					// writer2.append(average_episode_stage+"\n");
					writer.append(counter + "\t" + average_episode_stage + "\n");
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
			String file_name = "Result/" + environment.getName() + "/" + this.getName() + "_reward.txt";
			File file = new File(file_name);
			if (file.exists())
				file.delete();

			writer = new FileWriter(file_name, true);
			writer.write("#episode\taverage of reward\n");

			double sum_episode_stage = 0;
			int counter = 0;
			for (double episode_stage : totalRewardInEpisode) {
				counter++;
				sum_episode_stage += episode_stage;
				if (counter % 5 == 0) {
					writer.append(counter+"\t"+sum_episode_stage / 5 + "\n");
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
}
