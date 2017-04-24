package sharif.ce.isl.rl.graph.environment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridWorldEnv extends Environment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Logger logger = LoggerFactory.getLogger(GridWorldEnv.class);

	public int[][] worldModel;
	protected List<State> finalStates;

	public GridWorldEnv(int[][] worldModel, State initialState, List<State> finalStates) {

		numOfColumns = worldModel[0].length;
		numOfRows = worldModel.length;

		this.worldModel = worldModel;
		this.currentState = initialState;
		this.finalStates = finalStates;

		allActions = new ArrayList<Integer>();
		allActions.add(0); // up
		allActions.add(1); // right
		allActions.add(2); // down
		allActions.add(3); // left
		
		logger.info("GridWorld environment is initialized: {}, {}", worldModel.length, worldModel[0].length);
	}

	public boolean isInitialStateVlid() {
		if (worldModel == null) {
			logger.error("Grid is Null");
			return false;
		}
		if (this.currentState == null || finalStates == null) {
			logger.error("Initial or final state is Null");
			return false;
		}
		if (this.currentState.x >= worldModel.length || this.currentState.x < 0) {
			logger.error("Initial State X is {}, while the world has length {}", this.currentState.x,
					worldModel.length);
			return false;
		}
		if (this.currentState.y >= worldModel[0].length || this.currentState.y < 0) {
			logger.error("Initial State Y is {}, while the world has wide {}", this.currentState.y,
					worldModel[0].length);
			return false;
		}
		if (worldModel[this.currentState.x][this.currentState.y] == 1) {
			logger.error("Initial state is on wall");
			return false;
		}

		for(State s: finalStates){
			if (worldModel[s.x][s.y] == 1) {
				logger.info("Final state is on wall");
				return false;
			}	
		}
		return true;
	}

	public boolean isInGoalState() {
		return isInGoalState(currentState);
	}

	private boolean isInGoalState(State state) {
		for (State st : finalStates)
			if (st.compareTo(state) == 0)
				return true;
		return false;
	}

	public double CalcReward(State state, State nextState, Object action) {
		if (isInGoalState(nextState))
			return 1000;
		return -1;
	}

	public boolean EpisodeFinished() {
		return isInGoalState();
	}

	public List<Object> GetAdmissibleActions(State state) {
		List<Object> result = new ArrayList<Object>(allActions);
		return result;
	}

	// ****************************

	public double ApplyAction(Object action) /* throws Exception */ {
		if (!GetAdmissibleActions(currentState).contains(action))
			// throw new Exception("This action is not admissible in this
			// state");
			System.out.println("This action is not admissible in this state");

		Object realDoneAction = stochastize(action);
		State nextState = GetRealApplyActionResult(realDoneAction);
		double reward = CalcReward(currentState, nextState, realDoneAction);
		prevState = currentState;
		currentState = nextState;

		return reward;
	}

	private Object stochastize(Object action) {
		Object selectedAction = action;
		if (rand.nextDouble() >= stochastizationThreshold) {
			while (selectedAction == action) {
				selectedAction = this.ActionSet().get(rand.nextInt(this.ActionSet().size()));
			}
		}
		return selectedAction;
	}

	private State GetRealApplyActionResult(Object action) {

		State nextState = new State(currentState.x, currentState.y);
		// 0 1 2 3 4 ...
		// ------------------------------> y Column=10
		// 0 |0 1 2 3 4 5 6 7 8 9
		// 1 |10 11 .....
		// row 2 |
		// 3 |
		// . v
		// . x
		switch ((Integer) action) {
		case 0: // UP:
			nextState.x--;
			break;
		case 1: // RIGHT:
			nextState.y++;
			break;
		case 2: // DOWN:
			nextState.x++;
			break;
		case 3: // LEFT:
			nextState.y--;
			break;
		}
		try {
			if (worldModel[nextState.x][nextState.y] == 1)

				nextState = new State(currentState.x, currentState.y);
		} catch (Exception e) {
			System.out.println(nextState.x + " " + nextState.y);
		}
		return nextState;
	}
	// ******************************

	public State ReNatal() {

		currentState = RandomInitialState();
		return currentState;

	}

	public State ReNatal(int NR, int NC) {

		currentState = RandomInitialState(NR, NC);
		return currentState;

	}

	public static State RandomInitialState() {

		State init = new State(1 + rand.nextInt(3), 1 + rand.nextInt(3));
		return init;
	}

	public static State RandomInitialState(int NR, int NC) {
		State init = new State(1 + rand.nextInt(NR), 1 + rand.nextInt(NC));
		return init;
	}

	public final List<State> GetGoalStates() {
		return finalStates;
	}

	public int getMaxStateID() {
		return numOfColumns * numOfRows;
	}

	public int getGoalStateID() {
		return finalStates.get(0).ID();
	}

	public State ReFirstRoom() {

		currentState = RandomInitialState(3, 3);
		return currentState;
	}

	@Override
	public State getState(Integer id) {
		return new State(id / numOfColumns, id % numOfColumns);
	}

	@Override
	public String getName() {
		return "grid";
	}
}
