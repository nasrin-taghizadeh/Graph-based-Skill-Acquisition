package sharif.ce.isl.rl.graph.algorithm;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import sharif.ce.isl.rl.graph.algorithm.core.MarkovOption;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.algorithm.core.Trajectory;
import sharif.ce.isl.rl.graph.environment.Action;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

class option{
	public int optionID;
	public String optionEdge;
    public List<State> initialState=new ArrayList<State>();
    public List<State> finalState=new ArrayList<State>();
    public List<Action> policy=new ArrayList<Action>();
    public int selected;
}


public class nodeBetweenness extends SubgoalBasedQLearner {
	
	protected List<option> optionList;
    
	public List<Integer> ActionInEpisodeWithThisOption; 
	public List<Integer> ActionInEpisodeWithPreOption;  
    List<MarkovOption> AddOptions = new ArrayList<MarkovOption>();

	 protected List<Trajectory> ListTrajectory = new ArrayList<Trajectory>();	 
	 protected int trajectoryNumber=0;
	
	 protected int[] localMaxima;
	 protected int[] num_visit;

	 protected Graph <Integer, String> g;
	 protected ArrayList<State> SubgoalNeighbors = new ArrayList<State>();	
	        
	public nodeBetweenness(Environment earth) {
		super(earth, 2);	  

   	    localMaxima = new int[maxStateID];
	    num_visit = new int[maxStateID];
    
   	    for(int j=0;j<maxStateID;j++)
	    {
		      localMaxima[j] = 0;
		      num_visit[j] = 0;
	    }
   	    
   	    totalRewardInEpisode = new ArrayList<Double>();
	}
	
	@Override
	public String getName() {
		return "nodeBetweenness";
	}
	
	@Override
	public int walkWithOptions(int totalLifeCycles, boolean learn) 
	{
		
		int returnValue = -1;
	    State currentState, prevState;
	    Object currentAction;
	    currentState = environment.currentState();
	    prevState = currentState; //not important at the moment
	    currentAction = new Integer(0);//Action.RIGHT; // not important at the moment
	
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
		
	    while (episodeNum<totalLifeCycles){
	        currentState = environment.currentState();
	        currentAction = SelectAction(currentState, epsilone);                       	                   
//	        int numOfPrimitiveAdmissibleActions = environment.getNumOfPrimitiveActions();
	        int numOfPrimitiveAdmissibleActions = environment.allActions.size();
	        T.add(currentState, currentState.ID());
	        
	        if (!inOption && (Integer) currentAction >= numOfPrimitiveAdmissibleActions) {
	            selectedOption = (Integer) currentAction;
	            thisMarkovOption = applicableOptions.get(
	            		currentState.ID()).get(selectedOption - numOfPrimitiveAdmissibleActions);
	            inOption = true;
	            numOfActionsDoneInOneOption = 0;
	            cumulativeReward = 0;
	            optionStartingIndex = myExperience.size();
	            optionsStartingState = currentState;              
	        }
	
	        if (inOption){
	            currentAction = thisMarkovOption.SelectAction(currentState);
//	            System.out.println("in option");
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
//	        System.out.println(episodeNum+"  "+ currentState.ID()+"  "+numOfActionsDoneInOneEpisode+"  "+numOfActionsDoneInOneOption);
	        AddToStateTable(currentState);
	        myExperience.add(new ExperienceElement(prevState, (Integer) currentAction, currentState, reward));
	        UpdateDirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
//	        UpdateUndirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
	    	
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
	                    int optionActionIndex =GetOptionIndexInQTable(thisMarkovOption, element.prevState);
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
	
	            numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
	            totalRewardInEpisode.add(totalEpisodeReward);	                            
	
	            //incremental betweenness
//              create_graph();
//	            calculate_betweenness();
	            
	            for (int i = 0; i < maxStateID; i++){
					   for (int j = i + 1; j < maxStateID; j++) {
					     	if (edgeWeights[i][j] != 0) {    //

							logger.allObservedStates.put(i, stateTable.get(i)); //hame state hay dide shode dar hame life ha
							logger.allObservedStates.put(j, stateTable.get(j));

							Object prevObservation = logger.totalNumOfObservationsForState   //tedad dafati ke i dar hame life ha dide shode
									.get(i);

							logger.totalNumOfObservationsForState.put(i,
									prevObservation != null ? edgeWeights[i][j]
											+ (Integer) prevObservation
											: edgeWeights[i][j]);
							prevObservation = logger.totalNumOfObservationsForState
									.get(j);
							logger.totalNumOfObservationsForState.put(j,
									prevObservation != null ? edgeWeights[i][j]
											+ (Integer) prevObservation
											: edgeWeights[i][j]);
						}
					}
	            }
	            
	            if (learn == true && episodeNum == discoveryEpisodeThreshold) {
	            	learnOptions();
	            }      
//	            if (episodeNum == 2*discoveryEpisodeThreshold || 
//	            		episodeNum == 3*discoveryEpisodeThreshold) {	            
//	            	ExperienceReplay();
//	            //	insertOstacle();
//	            }
	            initialState = environment.ReNatal();
	            AddToStateTable(initialState);
	            numOfActionsDoneInOneEpisode = 0; 
	            totalEpisodeReward = 0;	            
	        	episodeNum++;
	        	ListTrajectory.add(T);
	        	T=new Trajectory();
	        }
	
	    }
	    
	    return returnValue;
	}	
    
	public void create_graph()
    {  	
    	g =new SparseMultigraph<Integer, String>();	
    	for (int i=0; i<maxStateID;i++)
    		for (int j=0; j<i;j++)
    		{
    			if (edgeWeights[i][j]!=0)
    			{
    	             g.addVertex(i);
    	             g.addVertex(j);
    	             g.addEdge(i+"->"+j,i, j);
    			}
    		}
    }

	void calculate_betweenness()
	{		
		BetweennessCentrality ranker = new BetweennessCentrality(g);
	    ranker.setRemoveRankScoresOnFinalize(false);
	    ranker.evaluate();
	    
	  	int f;
		for (Integer i:g.getVertices() )
		{
			num_visit[i]++;
			f=1;
			for(int j=0;j<maxStateID;j++)
			{ 
				try{
					if (edgeWeights[i][j]!=0)
						if (ranker.getVertexRankScore(i) < ranker.getVertexRankScore(j))
							f=0;
				}
				catch(Exception e){
//					e.printStackTrace();
				}
			}
			if (f==1){
				localMaxima[i]++;
				System.out.println("local maxima: "+i);
			}
		 }	 
    }	   

	public void evaluate(double threshold)
	{	
		//Incremental betweenness 
//		for(int i=0; i<num_visit.length; i++)
//			System.out.println("num visit "+i+" : "+num_visit[i]);
//			
//		for (Integer i:logger.allObservedStates.keySet() )
//		{
//			if (((double)(localMaxima[i])/num_visit[i])>threshold)
//			{
//				//System.out.print("local maxima rate for ");
//				//System.out.print(i+":"+"("+i/environment.numOfColumns+","+i%environment.numOfColumns+")");
//				//System.out.println("="+((double)(localMaxima[i])/num_visit[i]));
//				SubgoalNeighbors.add(logger.allObservedStates.get(i));
//				System.out.println("neighb ");
//			}
//		}	
		
		//raw betweenness algorithm
		for(int j=0;j<maxStateID;j++)
		{
			if(localMaxima[j] >0)
				SubgoalNeighbors.add(stateTable.get(j));
		}
	//	System.out.println("SubgoalNeighbors size: "+SubgoalNeighbors.size());
	}
	
	public void CreateOption_Simsek(int param)
	{	
	//	System.out.println("in create option "+SubgoalNeighbors.size());
		optionList=new ArrayList<option>();
		option op1;
		
		List<State> SubmitSubgoals = new ArrayList<State>();	
		for (State goalN:SubgoalNeighbors )
		{	
//			System.out.println("new option: ");
			op1=new option();
			int f=0;
			for (State SSG:SubmitSubgoals)
				if (SSG.compareTo(goalN)==0) 
					f=1;
			if (f==0)
			{
////////////////////////1 subgoal jadid darim ke baiad baraiash option besazim
				
	//			System.out.println("final: "+goalN.ID());
	        	op1.finalState.add(goalN);
	        	SubmitSubgoals.add(goalN);
				for (State gn:SubgoalNeighbors)
			    	if (gn!=null)
				        if ( environment.getClass() == GridWorldEnv.class &&
				        		(((gn.x-goalN.x)==0 && abs(gn.y-goalN.y)<=2)||((gn.y-goalN.y)==0 && abs(gn.x-goalN.x)<=2 ) ))
				        {
//				        	System.out.println("final: "+gn.ID());
				        	op1.finalState.add(gn);
				        	SubmitSubgoals.add(gn);
				        }				        				        
			    for (Trajectory T:ListTrajectory)
			    {
			    	 int flag=0,counter=0,c1=0;
			    	 for (State s:T.AllStates)
			    	 {
			    			counter++;
						    for(State subgoal:op1.finalState) 
						    {
			    		         if (s.compareTo(subgoal)==0)
			    			          flag=1;
			    		    }
			    	        if (flag==1)
			    	        	break;
			    	 }
			    	 if (flag==1)
			    	 {
			    	    	for (State s:T.AllStates)
			    	    	{
			    	    		c1++;			    	    	
			    	    		if ( c1<counter && c1>counter-param)
			    	    		{
			    	    			int f2=0;
			    	    			for (State s1:op1.initialState)
			    	    				if (s.compareTo(s1)==0)
			    	    					f2=1;
			    	    			if (f2==0){
			    	    				op1.initialState.add(s);
//			    	    				System.out.println("init: "+s.ID());
			    	    			}
			    	    		}
			    	    	}
			    	}
			        		        
			    }
			    op1.selected=0;
			    optionList.add(op1);
			    
			    MarkovOption newOption = new MarkovOption(op1.initialState, op1.finalState,
	                    environment.ActionSet().size(), maxStateID, /*alpha*/ .1, gamma, environment);
			    for (Object state : op1.initialState.toArray()) {
                    AddApplicableOptionForState(newOption, ((State) state).ID());

                }        		
        		myGeneratedOptions.add(newOption);
        		
			}			
		} 
		System.out.println("simsek: "+myGeneratedOptions.size());
	}   

	public void learnOptions() {
        create_graph();
        calculate_betweenness();
        evaluate(0.5);
        
        int param=0;
        if(environment instanceof GridWorldEnv)
        	param = 120;
        else if(environment instanceof TaxiDriverEnv)
        	param = 7;
        else if(environment instanceof HanoiTowerEnv)
        	param = 10;
        else if(environment instanceof PlayRoomEnv)
        	param = 50;
        
        CreateOption_Simsek(param);
        ExperienceReplay();
//        ExperienceReplay();
    }
	
}
