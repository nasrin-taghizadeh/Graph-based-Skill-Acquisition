package sharif.ce.isl.rl.graph.algorithm.core;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.State;


public class MarkovOption implements Serializable{
    private static Random rand= new Random();
    Double alpha;
    Double gamma;
    List<State> finalStates;
    double QTable[][];
    Environment environment;
//    int MarkovOptionID;
    List<State> initiationSet;

    public List<State> getInitiationSet() {
        return initiationSet;
    }
    public List<State> getFinalStates() {
        return finalStates;
    }    
    public MarkovOption(List<State> initiationSet, 
    		List<State> finalStates,
    		int numOfApplicableActions,
    		int maxStateID,
    		Double alpha,
    		Double gamma,
    		Environment environment
//    		,int ID
    		){
    	
    	this.initiationSet = initiationSet;
        this.finalStates = finalStates;
        QTable = new double [maxStateID][numOfApplicableActions];
        this.alpha = alpha;
        this.environment = environment;
        this.gamma=  gamma;
//        this.MarkovOptionID=ID;
    }

    public void UpdateQ(State currentState, int action, State nextState, double reward){
    	
        int currStateID= currentState.ID();
        double prevValue =QTable[currStateID][action];
        QTable[currStateID][action]= (1 - alpha) * prevValue + alpha * (reward + gamma * (MAXofQs(nextState)));
    }

    public Object SelectAction(State state) {
        Object selectedAction;
        Integer stID= state.ID();
        List<Object> admissibleActions;
        admissibleActions = environment.GetAdmissibleActions(state);

            double maxValue = -(Double.MAX_VALUE);
            for (Object admissibleAction : admissibleActions) {
                double tempValue = QTable[stID][(Integer)admissibleAction];
                if (maxValue < tempValue) {
                    maxValue = tempValue;
                    selectedAction = admissibleAction;

                }
            }
            List<Object> actionsWithMaxValue = new ArrayList();
            for (Object admissibleAction : admissibleActions) {
                if (QTable[stID][(Integer)admissibleAction] == maxValue)
                    actionsWithMaxValue.add(admissibleAction);
            }
            int selected=rand.nextInt(actionsWithMaxValue.size());
            selectedAction = actionsWithMaxValue.get(selected);
        return selectedAction;

    }

    protected double MAXofQs(State state) {
        return QTable[state.ID()][(Integer)SelectAction(state)];
    }
    public void printQTable(){
    	System.out.println("Option: ");
    	System.out.println("inital state: ");
    	Iterator itr = initiationSet.iterator();
    	while (itr.hasNext()) {
			System.out.println( ((State) itr.next()).ID());
			
		}
    	
    	System.out.println("final state: ");
    	itr = finalStates.iterator();
    	while (itr.hasNext()) {
			System.out.println( ((State) itr.next()).ID());
			
		}
    	
    	System.out.println("Q Table: ");
    	for(int i=0; i<QTable.length; i++){
    		for(int j=0; j<QTable[0].length; j++)
    			System.out.println(i+" "+j+" "+QTable[i][j]);
    	}
    }

}
