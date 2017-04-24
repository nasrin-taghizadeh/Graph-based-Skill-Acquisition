package sharif.ce.isl.rl.graph.environment;
// In the name of Allah

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Environment implements Serializable{
	
    protected static Random rand = new Random();
    public State currentState;
    protected State prevState;
    public List allActions;
    
	public static int numOfColumns ;
	public static int numOfRows ;	
	
    // If 1, no stochasticity exists. deterministic.
    protected double stochastizationThreshold =.9;
    
  	public final List ActionSet() {
	  	return allActions;
   	}
  	public int getNumOfPrimitiveActions(){
  		return allActions.size();
  	}
   	public final State prevState() {
   		return prevState;
   	}
   	public final State currentState() {
   		return currentState;
   	}
   	
   	public abstract boolean isInGoalState();

   	public abstract double CalcReward(State state, State nextState, Object action);

    public abstract boolean EpisodeFinished();

    public abstract List<Object> GetAdmissibleActions(State state);

    public abstract double ApplyAction(Object action);

    public abstract State ReNatal();

    public static State RandomInitialState() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
	public abstract int getMaxStateID();
	
	public abstract int getGoalStateID();
	
    public int whatActionCauses(int tail, int head)
    {
        State tempCur = currentState;
        State tempPrev =  prevState;
        currentState = getState(tail);
        
        for (int i=0;i<allActions.size();i++)
        {
            currentState = getState(tail);
            ApplyAction(allActions.get(i));
            if (currentState.ID()==head)
            {
                currentState = tempCur;
                prevState = tempPrev;
                return i;
            }
        }
        currentState = tempCur;
        prevState = tempPrev;
        return -1;
    }
	public abstract State getState(Integer integer);
	
	public abstract String getName();
}
