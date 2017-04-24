package sharif.ce.isl.rl.graph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.Grids;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomState;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

public class Run {
	protected static final Logger logger = LoggerFactory.getLogger(Run.class);

	@Argument(value = "", description = "Algorithm Name ", required = true)
	protected static String alg = "sharif.ce.isl.rl.graph.algoritm.SCC";

	@Argument(description = "Environment", required = true)
	protected static String env = "sharif.ce.isl.rl.graph.environment.GridWorldEnv";

	@Argument(description = "GridWorld", required = false)
	protected static String gridWorld = "6room";

	@Argument(description = "Initial State", required = false)
	protected static String iniStat = "1,1";

	@Argument(description = "Final State", required = false)
	protected static String finStat = "20,20";

	@Argument(description = "Learning Option", required = true)
	protected static String learnOption = "Y|N";

	@Argument(description = "Number of Episodes", required = true)
	protected static String numEpisode = "100";

	private static SubgoalBasedQLearner learner;
	private static Environment environment;

	public static void main(String[] args) {

		try {
			Args.parse(Run.class, args);
		} catch (Exception e) {
			Args.usage(Run.class);
			e.printStackTrace();
			return;
		}

		try {

			Class<?> e = null;
			e = Class.forName(env);
			if (e == GridWorldEnv.class) {

				State gridInitState = null, gridFinalState = null;
				List<State> finals = null;
				try {
					gridInitState = new State(Integer.parseInt(iniStat.split(",")[0].trim()),
							Integer.parseInt(iniStat.split(",")[1].trim()));

					gridFinalState = new State(Integer.parseInt(finStat.split(",")[0].trim()),
							Integer.parseInt(finStat.split(",")[1].trim()));

					finals = new ArrayList<State>();
					finals.add(gridFinalState);

				} catch (Exception ex) {
					ex.printStackTrace();
					logger.error("Invalid format of initial or final states.");
					logger.error("Something like 1,1 is correct.");
					System.exit(-1);
				}

				environment = new GridWorldEnv(Grids.sixroom, gridInitState, finals);
				if (!((GridWorldEnv) environment).isInitialStateVlid()) {
					logger.error("Initial state of grid world is not valid.");
					System.exit(-1);
				}
			} else if (e == TaxiDriverEnv.class) {
				char passenger, destination;
				int r = new Random().nextInt(4);
				if (r == 0)
					passenger = 'Y';

				else if (r == 1)
					passenger = 'G';

				else if (r == 2)
					passenger = 'B';

				else
					passenger = 'R';

				r = new Random().nextInt(4);
				if (r == 0)
					destination = 'Y';

				else if (r == 1)
					destination = 'G';

				else if (r == 2)
					destination = 'B';

				else
					destination = 'R';

				environment = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
			} else if (e == HanoiTowerEnv.class) {
				environment = new HanoiTowerEnv();
			} else if (e == PlayRoomEnv.class) {
				PlayRoomState initialState = new PlayRoomState(0, 0, 0, false, false, false, false);
				PlayRoomState goalState = new PlayRoomState(0, 0, 0, false, false, false, true);

				environment = new PlayRoomEnv(initialState, goalState);
			}

			Class<?> clazz = null;
			clazz = Class.forName(alg);
			Constructor<?> ctor = clazz.getConstructor(Environment.class);
			learner = (SubgoalBasedQLearner) ctor.newInstance(new Object[] { environment });
			learner.walkWithOptions(100, true);
			learner.show_episode_statistics();
			learner.show_reward_statistics();

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
