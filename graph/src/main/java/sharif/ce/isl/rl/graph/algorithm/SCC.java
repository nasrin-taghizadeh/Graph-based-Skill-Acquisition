package sharif.ce.isl.rl.graph.algorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Subgraph;

import sharif.ce.isl.rl.graph.algorithm.core.MarkovOption;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerState;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomState;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

public class SCC extends SubgoalBasedQLearner {
    private List<Subgraph> components = null;
    private int promissingEdgeWeight;
    protected List<Integer> trajectory = new ArrayList<Integer>();
    protected TreeSet<stateIDRatioPair> finalSubgoals;
	protected ArrayList<Integer> goalNeighbors = new ArrayList<Integer>();
	protected int adjacencyBound = 2;
    
    public SCC(Environment earth) {
        super(earth, 2);                    
        finalSubgoals = new TreeSet<stateIDRatioPair>();
        
        if(environment.getClass() == GridWorldEnv.class)
        	promissingEdgeWeight = 12;  //6room --> 11-12(1 door), 22(4 door), 27 9room --> 26 
        								//9room --> 22(1door), 19 (4 door), 29(1 door), 32, 35
        								//4room --> 11, 15-16, 18-19,  22-23 
        
        else if(environment.getClass() == TaxiDriverEnv.class)
        	promissingEdgeWeight = 6; //6, 11, 13, 15
        
        else if(environment.getClass() == HanoiTowerEnv.class)
        	promissingEdgeWeight = 9;  // 16-17, 19-20
        
        else if(environment.getClass() == PlayRoomEnv.class)
        	promissingEdgeWeight = 15;
    }
    
    @Override
	public String getName() {
		return "SCC";
	}
    
    @Override
    public int walkWithOptions(int totalLifeCycles, boolean learn){    	
        int returnValue = -1;
        State currentState, prevState;
        Object currentAction;
        currentState = environment.currentState();
        prevState = currentState; //not important at the moment
        currentAction = new Integer(0);//Action.RIGHT; // not important at the moment

        double reward = 0;
        int numOfActionsDoneInOneEpisode = 0;
        int numOfActionsDoneInOneOption = 0;
        int selectedOption = 0;
        boolean inOption = false;
        double cumulativeReward = 0;
        MarkovOption thisMarkovOption = null;
        State optionsStartingState = null;
        long optionStartingIndex = 0;
        double totalR = 0;
        episodeNum = 0;
        
        do{        	
            currentState = environment.currentState();
            AddToTrajectory(currentState);
            currentAction = SelectAction(currentState, epsilone);
//          int numOfPrimitiveAdmissibleActions = environment.GetAdmissibleActions(currentState).size();
            int numOfPrimitiveAdmissibleActions = environment.ActionSet().size();

            if (!inOption && (Integer) currentAction >= numOfPrimitiveAdmissibleActions) {
                selectedOption = (Integer) currentAction;
                thisMarkovOption = applicableOptions.get(currentState.ID()).get(selectedOption - numOfPrimitiveAdmissibleActions);
                inOption = true;
                numOfActionsDoneInOneOption = 0;
                cumulativeReward = 0;
                optionStartingIndex = myExperience.size();
                optionsStartingState = currentState;
            }

            if (inOption)
                currentAction = thisMarkovOption.SelectAction(currentState);
            reward = environment.ApplyAction(currentAction);
            totalR += reward;
            
            if (inOption)
                cumulativeReward += reward * Math.pow(gamma, numOfActionsDoneInOneOption);

            if (inOption)
                numOfActionsDoneInOneOption++;
            numOfActionsDoneInOneEpisode++;

//            if(episodeNum == 12){
//            	System.out.println("currentAction : "+currentAction);
//            	System.out.println(prevState.ID()+"   "+currentState.ID());  
//        		System.out.print("Admissible actions: ");
//        		for(Object ac: environment.GetAdmissibleActions(prevState))			
//        			System.out.print(((Integer)ac).intValue()+" ");
//        		System.out.println("");
//        		System.out.println("-----------");
//            }
            
            prevState = currentState;
            currentState = environment.currentState();
            AddToStateTable(currentState);
            myExperience.add(new ExperienceElement(prevState, (Integer) currentAction, currentState, reward));
            UpdateDirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
            /*if (inOption) {
                thisMarkovOption.
                        UpdateQ(prevState, (Integer) currentAction, currentState, reward);
            }*/
            for (MarkovOption option : myGeneratedOptions) {
                List<MarkovOption> firstList, secondList;
                firstList = applicableOptions.get(prevState.ID());
                secondList = applicableOptions.get(currentState.ID());
                if (firstList != null && secondList != null && firstList.contains(option) && secondList.contains(option)) {
                    double totalReward = PseudoReward(option, currentState, reward);
                    option.UpdateQ(prevState, (Integer) currentAction, currentState, totalReward);
                }
            }

	        //marzieh code
            int iflag=0,fflag=0;
            if (inOption)
            {       
                for (State s:thisMarkovOption.getInitiationSet())
         	       if (s.ID()==currentState.ID())
         	       {
         		       iflag=1;
         		       break;
         	       }
                
                for (State s:thisMarkovOption.getFinalStates())
         	       if (s.ID()==currentState.ID())
         	       {
         		       fflag=1;
         		       break;
         	       }
            }
 	           
 	        boolean intraOptionLearning = true;
 	        //marzieh code
 	        if (inOption && (fflag==1 || environment.EpisodeFinished() || iflag==0)) 
             {  //end of option 	        	
                inOption = false;

                if (intraOptionLearning ) {
                    double tempCumulativeReward = 0;
                    for (int i = myExperience.size(); i > optionStartingIndex; i--) {
                        ExperienceElement element = myExperience.get(i - 1);
                        tempCumulativeReward *= gamma;
                        tempCumulativeReward += element.reward;
                        Integer prevStateID = element.prevState.ID();
                        int optionActionIndex = GetOptionIndexInQTable(thisMarkovOption, element.prevState);
                        double qValue = QTable1[prevStateID][optionActionIndex];
                        QTable1[prevStateID][optionActionIndex] =
                                (1 - alpha) * qValue + alpha * (tempCumulativeReward + Math.pow(gamma, myExperience.size() - i + 1) *
                                        (MAXofQs(currentState)));
                    }
                }
                double qValue = QTable1[optionsStartingState.ID()][selectedOption];
                QTable1[optionsStartingState.ID()][selectedOption] =
                        (1 - alpha) * qValue + alpha * (cumulativeReward + Math.pow(gamma, numOfActionsDoneInOneOption) *
                                (MAXofQs(currentState)));
            }
            if (!inOption || (inOption && intraOptionLearning)) {
                double qValue = QTable1[prevState.ID()][(Integer) currentAction];
                QTable1[prevState.ID()][(Integer) currentAction] =
                        (1 - alpha) * qValue + alpha * (reward + gamma * (MAXofQs(currentState)));
            }
            //one-step intra-option learning
            if(inOption && intraOptionLearning){
                int optionIndex=GetOptionIndexInQTable(thisMarkovOption,prevState);
                double qValue = QTable1[prevState.ID()][optionIndex];
                QTable1[prevState.ID()][optionIndex] =
                        (1 - alpha) * qValue + alpha * (reward + gamma * (U(thisMarkovOption,currentState)));
            }
           
            if (environment.EpisodeFinished()) {
            	
//	        	System.out.println(numOfActionsDoneInOneEpisode);
                episodeNum++;
                AddToTrajectory(currentState);
                numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
                totalRewardInEpisode.add(totalR);
                UpdateGoalNeighbors(trajectory);
                
                if (learn == true && episodeNum == discoveryEpisodeThreshold) {
                	learnOptions();
                }
                if(episodeNum == 3*discoveryEpisodeThreshold)
                	ExperienceReplay();
                
                numOfActionsDoneInOneEpisode = 0;
                totalR =0;
                initialState = environment.ReNatal();
                AddToStateTable(initialState);
            }
            
        }
        while (episodeNum < totalLifeCycles);
        return returnValue;
    }
    
    private int CoreSubgoalDiscoverer(DefaultDirectedWeightedGraph realGraph, HashMap<Integer, Integer> stateComponent) {
        int returnValue;
        LogEdgeViews(edgeWeights);
        
        DefaultDirectedWeightedGraph heavilyWeithedGraph = 
        	new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);

        for (Object id : stateTable.keySet()) {
//        	System.out.println("all episode state table");
            heavilyWeithedGraph.addVertex(getXYView((Integer) id));
            realGraph.addVertex(getXYView((Integer) id));
        }
        for (int i = 0; i < maxStateID; i++)
            for (int j = 0; j < maxStateID; j++) {
                if (edgeWeights[i][j] > 0)
                    realGraph.setEdgeWeight(realGraph.addEdge(getXYView(i), getXYView(j)), edgeWeights[i][j]);
                if (edgeWeights[i][j] > promissingEdgeWeight)
                    heavilyWeithedGraph.setEdgeWeight(heavilyWeithedGraph.addEdge(getXYView(i), getXYView(j)), edgeWeights[i][j]);
            }        

//        System.out.println("num edge after filtering: "+heavilyWeithedGraph.edgeSet().size());
 
        StrongConnectivityInspector scc = new StrongConnectivityInspector(heavilyWeithedGraph);
        components = scc.stronglyConnectedSubgraphs();
        
        ListComparator lComp = new ListComparator();
        Collections.sort(components, lComp);
        
//        System.out.println("num SCCs: "+components.size());
        
        for (int i = 0; i < components.size(); i++){
//        	System.out.println(i+" "+components.get(i).vertexSet().size());
//       	if(components.get(i).vertexSet().size() == 1)
//        		continue;
            for (Object state : components.get(i).vertexSet()) { 
                stateComponent.put((getIDView((String) state)), i);
            }
        }

        FilterGoalNeighborSubgoals(finalSubgoals);
        returnValue = finalSubgoals.size() > 0 ? finalSubgoals.last().stID : -1;
        
//        System.out.println("final subgoals num: "+returnValue);
        
        return returnValue;
    }
 
    private void printGridResults() {
        if (episodeNum >= discoveryEpisodeThreshold) {
            System.out.println("FINAL RESULTS");
            System.out.print("\t");
            for (int i = 0; i < environment.numOfColumns; i++)
                System.out.print(i + "\t");
            System.out.println();
            System.out.print("\t");
            for (int i = 0; i < environment.numOfRows; i++) {
                System.out.println();
                System.out.print(i + "\t");
                for (int j = 0; j < environment.numOfColumns; j++) {                  //
                    boolean found = false;
                    for (int k = 0; k < components.size() && !found; k++) {
                        if (components.get(k).containsVertex(getXYView(i * environment.numOfColumns + j))) {
                            System.out.print(k + "\t");
                            found = true;
                            break;
                        }

                    }
                    if (!found)
                        System.out.print("#\t");
                }
                System.out.println();
            }
            System.out.println();

        }
    }
    
    private void printTaxiResults() {
        if (episodeNum >= discoveryEpisodeThreshold) {
            System.out.println("FINAL RESULTS");
            System.out.print("\t");
            for (int i = 0; i < 25; i++)
                System.out.print(i + "\t");
            System.out.println();
            System.out.print("\t");
            for (int i = 0; i < 2; i++) {
                System.out.println();
                System.out.print(i + "\t");
                for (int j = 0; j < 25; j++) {                  //
                    boolean found = false;
                    for (int k = 0; k < components.size() && !found; k++) {
                        if (components.get(k).containsVertex(getXYView(i * 25 + j))) {
                            System.out.print(k + "\t");
                            found = true;
                        }

                    }
                    if (!found)
                        System.out.print("#\t");
                }
                System.out.println();
            }
            System.out.println();

        }
    }
    
    private void printHanoiResults(){
        for (int j = 0; j < 242; j++) {
            for (int k = 0; k < components.size(); k++) {
                if (components.get(k).containsVertex(getXYView(j))) {
                    System.out.println(j + "\t" + k);
                    break;
                }

            }
        }

    }

    protected void UpdateGoalNeighbors(List<Integer> trajectory) {
        int bound = adjacencyBound;
        int size = trajectory.size();
        while (bound >= 0) {
            if (bound < size) {

                if (!goalNeighbors.contains(trajectory.get(size - bound - 1)))
                    goalNeighbors.add(trajectory.get(size - bound - 1));
            }
            bound--;
        }
    }

    protected void FilterGoalNeighborSubgoals(TreeSet<stateIDRatioPair> finalSubgoals) {
        while (finalSubgoals.size() > 0 && AroundGoal(finalSubgoals.last().stID)) {
            finalSubgoals.remove(finalSubgoals.last());
        }
    }

    protected boolean AroundGoal(int id) {
        for (Integer stID : goalNeighbors)
            if (stID == id)
                return true;
        return false;

    }    
    
    private void LogEdgeViews(int[][] allEpisodesEdgeWeighs) {

//    	int shreshold = 0;
        HashMap<Integer, Integer> edgeWeightFrequency = new HashMap<Integer, Integer>();
        for (int i = 0; i < maxStateID; i++)
            for (int j = 0; j < maxStateID; j++) {
                int value = allEpisodesEdgeWeighs[i][j];
                if (value > 0) {
                    if (edgeWeightFrequency.get(value) != null)
                        edgeWeightFrequency.put(value, edgeWeightFrequency.get(value) + 1);
                    else
                        edgeWeightFrequency.put(value, 1);
                }
            }
        try {
            FileWriter writer;
			
			if(environment.getClass() == GridWorldEnv.class){
				writer = new FileWriter("room_Histogram.xls");			
			}
			else if(environment.getClass() == TaxiDriverEnv.class){
				writer = new FileWriter("taxi_Histogram.xls");

			}
			else if(environment.getClass() == HanoiTowerEnv.class){
				writer = new FileWriter("hanoi_Histogram.xls");
			}
			else //if(environment.getClass() == PlayRoomEnv.class)
			{
				writer = new FileWriter("playroom_Histogram.xls");
			}
			
            writer.write("In the name of Allah\n");
            writer.write("Weight\t#ofEdges\n");
            ArrayList entries = new ArrayList(edgeWeightFrequency.entrySet());
            Comparator keyComparator = new KeyComparator();
            Collections.sort(entries, keyComparator);
            for (Object o : entries.toArray()) {
                Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) o;
                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        System.out.println("FINAL RESULTS");
//        for (int i = 0; i < maxStateID; i++) {
//            for (int j = 0; j < maxStateID; j++){
//            	if(allEpisodesEdgeWeighs[i][j] > 0)
//            		System.out.print("(" +i + ", " + j + ") --> " + allEpisodesEdgeWeighs[i][j] + "\t");
//            		
//            }
//            System.out.println();
//        }
//        System.out.println();

    }


    private void AddToTrajectory(State currentState) {
        trajectory.add(currentState.ID());

    }
    
    private List<ComponentState> GetSubgoals(HashMap<Integer, Integer> stateComponent, DefaultDirectedWeightedGraph realGraph) {
    	
        List<ComponentState> result = new ArrayList<ComponentState>();
        DirectedNeighborIndex index = new DirectedNeighborIndex(realGraph);
        List<Subgraph> potentialComponents = GetPotentialComponents();
        
        for (Subgraph component : potentialComponents){
            for (Object vertex : component.vertexSet())
                for (Object neighbour : index.successorListOf(vertex)/*Graphs.neighborListOf(realGraph,vertex)*/) {
                    Object compNum = stateComponent.get(getIDView((String) neighbour));
                    if (compNum != null && (Integer) compNum != stateComponent.get(getIDView((String) vertex))) {
                        State newMember = getStateView((String) neighbour);
                        if (!result.contains(new ComponentState(newMember, stateComponent.get(getIDView((String) vertex)))))
                            result.add(new ComponentState(newMember, stateComponent.get(getIDView((String) vertex))));
                    }

                }
            
        }

//        System.out.println("size: "+result.size());
        return result;

    }

    private List<Subgraph> GetPotentialComponents() {
        List<Subgraph> potentialComponents = new ArrayList<Subgraph>();
        double sizes[] = new double[components.size()];
        for (int i = 0; i < components.size(); i++)
            sizes[i] = components.get(i).vertexSet().size();
        StandardDeviation standardDev = new StandardDeviation();
        double stDev = standardDev.evaluate(sizes);
        double mean = StatUtils.mean(sizes);

        boolean goOn = true;
        int tempCounter = 0;
        while (goOn && tempCounter <= components.size() - 1) {
            Subgraph subgraph = components.get(tempCounter);
            // in top 0.05 of population in a normal distribution
            if (subgraph.vertexSet().size() > mean + stDev)
                potentialComponents.add(subgraph);
            else
                goOn = false;
            tempCounter++;
        }
        return potentialComponents;
    }

    private String getXYView(int id) {
    	String result = "";
    
    	if(environment.getClass() == GridWorldEnv.class || 
    			environment.getClass() == TaxiDriverEnv.class)
    	{
    		result = "(" + String.valueOf(id / environment.numOfColumns);
    		result += ",";
    		result += id % environment.numOfColumns;
    		result += ")";
    	}
    	else if(environment.getClass()==HanoiTowerEnv.class){    		
    		result = "(" + id%3 + "," + (id/3)%3 + "," + (id/9)%3 
    		                          + "," + (id/27)%3 + "," + (id/81)%3+")";     		
    	}
    	else if(environment.getClass() == PlayRoomEnv.class){    	
    		result = "("+ id + ")";
    	}
        return result;
    }

    private int getIDView(String xyView) {
        return getStateView(xyView).ID();
    }

    private State getStateView(String xyView) {
        StringTokenizer tokens = new StringTokenizer(xyView, "(,)");
        
        if(environment.getClass() == GridWorldEnv.class || 
        		environment.getClass() == TaxiDriverEnv.class){
        	return new State(Integer.valueOf(tokens.nextToken()),
                Integer.valueOf(tokens.nextToken()));
        }
        else if(environment.getClass() == HanoiTowerEnv.class){
        	return new HanoiTowerState(Integer.valueOf(tokens.nextToken()), 
        			Integer.valueOf(tokens.nextToken()), 
        			Integer.valueOf(tokens.nextToken()), 
        			Integer.valueOf(tokens.nextToken()), 
        			Integer.valueOf(tokens.nextToken()));
        }
        else{//play room state
        	int id = Integer.valueOf(tokens.nextToken());
        	return new PlayRoomState((PlayRoomState)(stateTable.get(id)));
        }       
    }
    
    private List<MarkovOption> CreateOptions(List<ComponentState> subgoals) {
    	
        List<MarkovOption> options = new ArrayList<MarkovOption>();       
        for (ComponentState subgoal : subgoals) {
//        	System.out.println("new option");
            ArrayList initiationSet = new ArrayList(components.get(subgoal.neighborCompID).vertexSet());
            for (int i = 0; i < initiationSet.size(); i++) {
                initiationSet.add(i, getStateView((String) initiationSet.get(i)));
                initiationSet.remove(i + 1);
            }
            //String goal = getXYView(subgoal.state.ID());
            if (!initiationSet.contains(subgoal.state))
                initiationSet.add(subgoal.state);// an option can start in its final state
            ArrayList<State> subgoalState = new ArrayList();
            subgoalState.add(subgoal.state);
//            System.out.println("subgoal: "+subgoal.state.ID());           
            
            MarkovOption newOption = new MarkovOption(initiationSet, subgoalState,
                    environment.ActionSet().size(), maxStateID, /*alpha*/ .1, gamma, environment);
            for (Object state : initiationSet.toArray()) {
//            	System.out.println(((State) state).ID());
                AddApplicableOptionForState(newOption, ((State) state).ID());

            }
            options.add(newOption);
        }
//        System.out.println("tedade option ha: "+options.size());
        return options;
    }

	@Override
	public void learnOptions() {		
//    	System.out.println("start---------");
        DefaultDirectedWeightedGraph realGraph = new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);        
        HashMap<Integer, Integer> stateComponent = new HashMap<Integer, Integer>();
        
        CoreSubgoalDiscoverer(realGraph, stateComponent);       
        List<ComponentState> subgoals = GetSubgoals(stateComponent, realGraph);
        myGeneratedOptions = CreateOptions(subgoals);
        System.out.println("SCC num of options: "+myGeneratedOptions.size());
        ExperienceReplay();
        ExperienceReplay();
        if (environment instanceof GridWorldEnv)
            printGridResults();
        else if(environment instanceof TaxiDriverEnv)
        	printTaxiResults();
        else if(environment instanceof HanoiTowerEnv)
        	printHanoiResults();
	}

}

class KeyComparator implements Comparator {
    public KeyComparator() {

    }

    public int compare(Object o1, Object o2) {
        Map.Entry<Integer, Integer> e1 = (Map.Entry<Integer, Integer>) o1;
        Map.Entry<Integer, Integer> e2 = (Map.Entry<Integer, Integer>) o2;
        return e1.getKey().compareTo(e2.getKey());
    }
}

/* sorts in descending order
* */
class ListComparator implements Comparator {
    public ListComparator() {
    }

    public int compare(Object o1, Object o2) {
        Integer i1 = ((Subgraph) o1).vertexSet().size();
        Integer i2 = ((Subgraph) o2).vertexSet().size();
        return -i1.compareTo(i2);

    }
}

class ComponentState {
    public State getState() {
        return state;
    }

    public Integer getNeighborCompID() {
        return neighborCompID;
    }

    State state;
    Integer neighborCompID;

    ComponentState(State st, Integer i) {
        this.state = st;
        neighborCompID = i;
    }
}

 //stateIDRatioPair class is defined for filtering Subgoals
class stateIDRatioPair implements Comparable {
	stateIDRatioPair(int id, double pair) {
		stID = id;
		this.pair = pair;
	}

	public int compareTo(Object o) {
		if (pair > ((stateIDRatioPair) o).pair)
			return 1;
		return pair < ((stateIDRatioPair) o).pair ? -1 : 0;
	}

	int stID;
	double pair;
}

