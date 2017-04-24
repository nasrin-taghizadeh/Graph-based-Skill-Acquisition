package sharif.ce.isl.rl.graph.algorithm;

import java.util.Iterator;

import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.State;


public class KShellDecomposition extends SubgoalBasedQLearner {

	private MyNode[] allNodes = new MyNode[maxStateID];
	
	public KShellDecomposition(Environment earth) {
		super(earth, 2);
		
		int numOfPrimitiveActions = environment.ActionSet().size();
        int MAX_AVAIL_ACT_OR_OPTIONS = numOfPrimitiveActions ;
        QTable1 = new double[maxStateID][MAX_AVAIL_ACT_OR_OPTIONS];
	}
	@Override
	public String getName() {
		return "Kshell";
	}
	public int walk(int totalLifeCycles) {
		
		int returnValue = -1;
		State currentState, prevState;
		Object currentAction;
		currentState = environment.currentState();  ///initial state
		prevState = currentState; 
		currentAction = new Integer(0);
		
		//add node to graph
		AddToStateTable(stateTable,prevState);
		AddToStateTable(stateTable, currentState);
		
		double reward;
		int numOfActionsDoneInOneEpisode = 0;
	
		while (episodeNum<totalLifeCycles){

			currentState = environment.currentState();     ///initial state sabet nist	
			currentAction = SelectAction(currentState, epsilone);
			
			numOfActionsDoneInOneEpisode++;
			reward = environment.ApplyAction(currentAction);
			prevState = currentState;
			currentState = environment.currentState(); //state pas az anjam action
	
			//add node & edge to graph								
			AddToStateTable(stateTable, currentState);
			UpdateUndirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
				
			
			double qValue = QTable1[prevState.ID()][(Integer) currentAction];
			QTable1[prevState.ID()][(Integer) currentAction] = (1 - alpha)* qValue 
			                           + alpha* (reward + gamma * (MAXofQs(currentState)));


			if (environment.EpisodeFinished()) {			

				numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
//				System.out.println(numOfActionsDoneInOneEpisode);
				numOfActionsDoneInOneEpisode = 0;

				episodeNum++;
				initialState = environment.ReNatal();

                if (episodeNum == discoveryEpisodeThreshold) {
                	
                }
			}

		}
//		System.out.println("----------------");
		return returnValue;
	}
	
	public void createGraph(){
		
//		System.out.println("nodes: "+stateTable.size());
		Iterator<Integer> itr = stateTable.keySet().iterator();
		int ID;
		while (itr.hasNext()) {
			ID = itr.next().intValue();
			allNodes[ID] = new MyNode(ID, ID);
			System.out.println(ID+ " added");
		}
		
		//notice: graph is undirected 
	   	for (int i=0; i<maxStateID;i++)			
    		for (int j=i+1; j<maxStateID;j++)
    			if (edgeWeights[i][j]!=0)
    			{
    				if(allNodes[j] == null)
    					System.err.println("j "+j);
    				
    				if(allNodes[i] == null)
    					System.err.println("i "+i);
    				
    				allNodes[i].addNeighbors(allNodes[j]);
    				allNodes[j].addNeighbors(allNodes[i]);

    			}
	}
	
	public void KShellDecompose(){
		for(int i = 0; i< maxStateID; i++){
	//		if(allNodes.)
		}
	}
	@Override
	public void learnOptions() {
		createGraph();
		KShellDecompose();
	}
}
