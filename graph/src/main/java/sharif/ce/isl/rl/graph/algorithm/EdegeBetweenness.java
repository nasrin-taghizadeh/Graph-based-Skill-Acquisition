package sharif.ce.isl.rl.graph.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import sharif.ce.isl.rl.graph.algorithm.core.MarkovOption;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.algorithm.core.Trajectory;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;


class Cluster{
	public int clusterID;
	public List<Link> links=new ArrayList<Link>();
	public List<State> Nodes=new ArrayList<State>();
}
class Link{
	public List<State> ArrayLinkNode=new ArrayList<State>();
	public State LinkNode;
	public int connected_ClusterID;
	public String edgeLink;
	public List<String> ArrayEdgeNode=new ArrayList<String>();
}
class StateFrequency{
	State state;
	int frequency;
	StateFrequency(State s,int f) { state=s; frequency=f; }
 }

public class EdegeBetweenness extends SubgoalBasedQLearner{
		
	protected List<option> optionList;
	protected Graph <Integer, String> g;
	protected BetweennessCentrality ranker;
	public List<Cluster> clusterList;
	public int numCluster=3;
	public int NumClusters=3;
	public int[][] ClusterLink=new int[NumClusters][NumClusters];
	protected List<Trajectory> ListTrajectory=new ArrayList<Trajectory>();
		
	public EdegeBetweenness(Environment earth) {
		super(earth, 2);
	}
	
	@Override
	public String getName() {
		return "edgeBetweenness";
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
	        AddToStateTable(currentState);
	        currentAction = SelectAction(currentState, epsilone);
	        int numOfPrimitiveAdmissibleActions = environment.allActions.size();

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
	
	        	AddToStateTable(currentState);
	            numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
	            totalRewardInEpisode.add(totalEpisodeReward);	                            
	
	            //incremental betweenness
//              create_graph();
//	            calculate_betweenness();
	            
	            for (int i = 0; i < maxStateID; i++){
					   for (int j = i ; j < maxStateID; j++) {
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
	        }
	
	    }
	    
	    return returnValue;
	}	

    public void create_graph(int[][] WeightGraph)
    {  
    	g =new SparseMultigraph<Integer, String>();	
    	for (int i=0; i<maxStateID;i++)
    		for (int j=0; j<i;j++)
    		{
    			if (WeightGraph[i][j]!=0)
    			{
    	             g.addVertex(i);
    	             g.addVertex(j);
    	             g.addEdge(i+"->"+j,i, j);
    			}
    		}
    }
	
    public void BetweennessCommunityDetection(int numClusters)
	{
		create_graph(edgeWeights);
		EdgeBetweennessClusterer clusterer=new EdgeBetweennessClusterer(numClusters-1);
		Set<Set> clusters = clusterer.transform(g);
		Object[] allClusters=clusters.toArray();
		List<String> RemovedEdges = clusterer.getEdgesRemoved();
		clusterList=new ArrayList<Cluster>();
		for (String e:RemovedEdges)
			System.out.println(e);
		int counter=0;
		for(Object collection:allClusters)
		{	 
		     //List<Integer> clusterNode=new ArrayList<Integer>();
			 List<State> clusterNode=new ArrayList<State>();
			 int f = 1,e;  
             String clusterVector = collection.toString();
       	     e = clusterVector.indexOf(",",f);
//       	     System.out.println("e : "+e+" f: "+f+" clusterVector: "+clusterVector);
             while (e!=-1)
             {
                 String Node=clusterVector.substring(f, e);
                 //in khat dorost shavad
//                 System.out.println("Integer.parseInt(Node) "+ Integer.parseInt(Node));
                 
                 if(logger.allObservedStates.get(Integer.parseInt(Node)) != null){
            //    	 System.out.println("logger is null "+ Integer.parseInt(Node));
                	 clusterNode.add(logger.allObservedStates.get(Integer.parseInt(Node)));                	 
                 }                                
                 f=e+2;
           	     e=clusterVector.indexOf(",",f);
             }
             Cluster c=new Cluster();
             c.Nodes = clusterNode;
             for(State s: c.Nodes)
            	 if(s == null)
            		 System.out.println("nulll --- 1");
             c.clusterID=counter;
             clusterList.add(c);
         //    System.out.print(counter+":");
        /*     for (State n:clusterNode)
             {
            	// System.out.print("("+(n.ID()/numOfColumns)+","+(n.ID()%numOfColumns)+") ");     
            	 System.out.print(PrintClusterID(n.ID()/numOfColumns,n.ID()%numOfColumns)+" ");
            	 /*   if (n.ID()%numOfColumns<8)
            	     System.out.print("1 ");
            	 else if (n.ID()%numOfColumns<15) 
            		 System.out.print("2 ");
            	 else 
            		 System.out.print("3 ");
            		 */
        //     } 
		
        //     System.out.println();
             counter++;            
		}
	
		
		for (String e:RemovedEdges)
		{
			int x = e.indexOf("->", 0);
			int SourceNode = Integer.parseInt(e.substring(0, x));
			int destNode = Integer.parseInt(e.substring(x+2, e.length()));
			///////eslah//////////////////
			State SourceState = logger.allObservedStates.get(SourceNode);
			State DestState = logger.allObservedStates.get(destNode);
			
//			State SourceState = stateTable.get(SourceNode);
//			State DestState = stateTable.get(destNode);
			
			int sourceClusterID = -1, destClusterID = -1;
			int SourceClusterIndex = -1, DestClusterIndex = -1;
//			System.out.println(SourceState.ID()+" "+DestState.ID());
			
			////////////////////////////////////////

			for (Cluster c:clusterList)
			{	
	            for(State s: c.Nodes)
	            	 if(s == null)
	            		 System.out.println("nulll --- 2");
	            
				if (c.Nodes.indexOf(SourceState)!=-1)
				{
					SourceClusterIndex = clusterList.indexOf(c);
					sourceClusterID = c.clusterID;
//					System.out.println(SourceState.ID()+" j "+SourceClusterIndex+" "+sourceClusterID);
				}
				
				if (c.Nodes.indexOf(DestState)!=-1)
				{
					DestClusterIndex=clusterList.indexOf(c);
					destClusterID = c.clusterID;
//					System.out.println(DestState.ID()+" jjj "+DestClusterIndex+" "+destClusterID);
				}
			}

//			System.out.println("&&&&&&&&&&&&&&&&&");
			if (sourceClusterID != destClusterID && sourceClusterID !=-1 && destClusterID!=-1)
			{
//				System.out.println("link bayad sakhte shavad");
		//		 ClusterLink[sourceClusterID][destClusterID]++;
			     //System.out.println(DestState.ID()+"("+DestState.ID()/numOfColumns+","+DestState.ID()%numOfColumns+")"+" jjj "+DestClusterIndex+" "+destClusterID);
				 Link clustLink=new Link();
				 Cluster clust=new Cluster();
				 clust.Nodes = clusterList.get(SourceClusterIndex).Nodes;	
		            for(State s: clust.Nodes)
		            	 if(s == null)
		            		 System.out.println("nulll --- 3");
				 clust.clusterID=sourceClusterID;
				 clustLink.connected_ClusterID=destClusterID;
			     clustLink.LinkNode = DestState;
			     clustLink.edgeLink = e;
			     clust.links = clusterList.get(SourceClusterIndex).links;
			     clust.links.add(clustLink);
			     clusterList.set(SourceClusterIndex, clust);
			     
				 clustLink=new Link();
				 clust=new Cluster();
				 clust.Nodes=clusterList.get(DestClusterIndex).Nodes;
				 
				 for(State s: clust.Nodes)
	            	 if(s == null)
	            		 System.out.println("nulll --- 4");
				 
				 clust.clusterID=destClusterID;
				 clustLink.connected_ClusterID=sourceClusterID;
			     clustLink.LinkNode = SourceState;
			     clustLink.edgeLink=e;
			     clust.links=clusterList.get(DestClusterIndex).links;
			     clust.links.add(clustLink);			    
			     clusterList.set(DestClusterIndex, clust);
			     
			}
	/*
			for(Cluster c:clusterList)
		    {
				for (State node1:c.Nodes)
				{
					for (State node2:c.Nodes)
					{
						if  (total_edgeWeights[node1.ID()][node2.ID()]!=0)
							ClusterLink[c.clusterID][c.clusterID]++;
					}
				}
		    }
		    */
		}
		
		System.out.println("*********");
	}
		
	public void createOption_coummunity()
	{
		 optionList = new ArrayList<option>();
		 int counter=0;
		 
//		 System.out.println("num clusters: "+clusterList.size());
		 
		 
	     for(Cluster clust:clusterList)
	     {
	          for (Link l:clust.links)
	          {
//	             if (IsTrueFinalState(l.LinkNode,clust.Nodes))
//	               {
//	            	    System.out.println("yes"+"("+l.LinkNode.x+","+l.LinkNode.y+"):"+"("+clust.Nodes.get(0).x+","+clust.Nodes.get(0).y+")");
	                    option o=new option();
	                    o.finalState.add(l.LinkNode);

	                    o.optionID=counter;
	                    o.optionEdge=l.edgeLink;
//	                    System.out.println("new option: "+o.optionID);
//	                    for(State s: o.finalState)
//	                    	System.out.println("final state: " + s.ID());
	                    
//	                    System.out.println(clust.Nodes.size());
	                    for(State s:clust.Nodes){
	            	        o.initialState.add(s);
//	            	        System.out.println("initial state: " + s.ID());
	                    }
//	                    System.out.println(o.initialState.size());
	                    //	System.out.println("ini: "+s.ID());	                 
	                    
	                    o.selected=0;
	                    optionList.add(o);	
	                    counter++;
//	               }
	          }
	     }
	     System.out.println("num options="+counter);
	}

	private boolean IsTrueFinalState(State SubGoal,List<State> ClusterSet)
	{
		
		int c=0;
//		List<State> PreviousStates = InitiationSet(SubGoal,30);  //taxi
		List<State> PreviousStates = InitiationSet(SubGoal,1000);  //playroom
		for (State state1:PreviousStates)
			for (State state2:ClusterSet)
			//if (ClusterSet.contains(state)) 
				if (state1.ID()==state2.ID())
				  c++;
		System.out.println(PreviousStates.size()+" NNNNNNNN  c: "+c);
		if (c>(PreviousStates.size()/2)) 
		{
			return true; 
		}
		else 
			return false;
	}
    
	private List<State> InitiationSet(State subgoal,double Trajectorylength)
    {
    	//for (MarkovOption op1:AddOptions)
    	List<StateFrequency> PreviousState;
  //  	for (option op1:optionList)
    //	{
    //		 int OptionIndex=optionList.indexOf(op1);
    		 PreviousState=new ArrayList<StateFrequency>();
	         for (Trajectory T:ListTrajectory)
	         {
	    	      int flag=0,counter=0,c1=0;
	    	//    for (State s:ArrayTrajectory[i])
	    	      for (State s:T.AllStates)
	    	      {
	    			    counter++;
				   //   for(State subgoal:op1.finalState) 
				    //  {
	    		      //    if (s.compareTo(subgoal)==0)
				    	if (s.ID()==subgoal.ID())
	    			          flag=1;
	    		   //   }
	    	            if (flag==1)
	    	        	  break;
	    	      }
	///////////////    
	    	      if (flag==1)
	    	      {
	    	    	   for (State s:T.AllStates)
	    	    	   { 
	    	    		    c1++; 	   
	    	    		    if ( c1<counter && c1>counter-Trajectorylength)    ///inja fekr konam
	    	    		    {
		    	    		//	System.out.print("("+s.x+","+s.y+") ");
	    	    			    int f2=0;
	    	    			 //   for (State s1:op1.initialState)
	    	    			 //   if (!PreviousState.isEmpty())
	    	    			    for (StateFrequency preState:PreviousState)
	    	    			    //	if (preState!=null)
	    	    				  //  if (s.compareTo(preState.state)==0)
	    	    			    	if (s.ID()==preState.state.ID())
	    	    				    {
	    	    				    	int IndexPreviousState=PreviousState.indexOf(preState);
	    	    					    f2=1;
	    	    					    int fr=preState.frequency;
	    	    					    StateFrequency StateFr=new StateFrequency(preState.state,fr+1);
	    	    					    PreviousState.set(IndexPreviousState, StateFr);
	    	    					   // PreviousState.remove(preState);
	    	    					 //   PreviousState.add(StateFr);
	    	    				    }
	    	    			    if (f2==0)
	    	    			    {
	    	    			   //   op1.initialState.add(s);	
	    	    			    	StateFrequency preState=new StateFrequency(s,1);
	    	    			    	PreviousState.add(preState);
	    	    			    }
	    	    		    }
	    	    	   }
	    	      }
	///////////////
	    	 //   System.out.print("\n");
	    	}
	        for (int i=0;i<PreviousState.size();i++)
	        	 for (int j=0;j<PreviousState.size()-i-1;j++)
	        	 {
	        		 if (PreviousState.get(j).frequency<PreviousState.get(j+1).frequency)
	        		 {
	        			 StateFrequency StateFr=PreviousState.get(j);
	        			 PreviousState.set(j, PreviousState.get(j+1));
	        			 PreviousState.set(j+1,StateFr);
	        		 }
	        	 }
	       /*
	        int [][] StatePreviousFrequency=new int[numOfRows][numOfColumns];
	        for (int i=0;i<numOfRows;i++)
	            for (int j=0;j<numOfColumns;j++)
	            	StatePreviousFrequency[i][j]=0;
	        */
	        List<State> InitialState=new ArrayList<State> ();
	        for (int i=0;i<PreviousState.size();i++)
	        {
	        //	System.out.println("("+PreviousState.get(i).state.ID()/numOfColumns+","+
	        //			           PreviousState.get(i).state.ID()%numOfColumns+") :"+PreviousState.get(i).frequency);
	       // 	StatePreviousFrequency[PreviousState.get(i).state.ID()/numOfColumns]
	       // 	                      [PreviousState.get(i).state.ID()%numOfColumns]=PreviousState.get(i).frequency;
	        		
	        //	op1. .add(PreviousState.get(i).state);
	        	InitialState.add(PreviousState.get(i).state);
	        }
	  ///////////////////// 
	        /*
	    	Writer output = null;
	    	try 
	    	{
	    		output = new BufferedWriter(new FileWriter("D:\\PreviousSet1.txt",true));
	    		output.append("Hello\n");
	    	//	System.out.println("Hello\n");
	    	//	System.out.println();
                for (int j=0;j<numOfColumns;j++)
                	output.append(j+"\t");
                output.append("\n");
	            for (int i=0;i<numOfRows;i++)
	            {
	                 for (int j=0;j<numOfColumns;j++)
	                	 output.append(StatePreviousFrequency[i][j]+"\t");
	            	//     System.out.print(StatePreviousFrequency[i][j]+"\t");
	                // System.out.println();
	                 output.append("\n");
	            }
	            output.close();
	        }
	        catch (Exception e) 
	        {
	              System.out.println("There is a problem in files ");
	              e.printStackTrace();
	        }
	        */
	  /////////////
	   //     optionList.set(OptionIndex,op1);
	   // }
	      return InitialState;
    }
	
	private void ShowClusters()
	{
		for(Cluster clust:clusterList)
	    {
			System.out.print(clust.clusterID+" ,");
			System.out.print("Nodes :");
			for (State st:clust.Nodes)
				System.out.print(st.ID()+", ");
			System.out.println();
			for (Link l:clust.links)
				System.out.println(":::"+l.edgeLink+" "+l.LinkNode.ID()+" "+l.connected_ClusterID);
			System.out.println();			
	    }		
	}

	
    private int PrintClusterID(int r,int c) 
    {
    	int ClusterID=0;
    	if (r>=1 && r<=10 && c>=1 && c<=8)
    		ClusterID=1;
    	else if (r>=1 && r<=12 && c>=10 && c<=18)
    		ClusterID=2;
    	else if (r>=1 && r<=9 && c>=20 && c<=30)
    		ClusterID=3;
    	else if (r>=12 && r<=23 && c>=1 && c<=8)
    		ClusterID=4;
    	else if (r>=14 && r<=23 && c>=10 && c<=18)
    		ClusterID=5;
    	else if (r>=11 && r<=23 && c>=20 && c<=30)
    		ClusterID=6;
    	else if (r==5 && c==9)
    		ClusterID=10;
    	else if (r==6 && c==19)
    		ClusterID=12;
    	else if (r==11 && c==4)
    		ClusterID=14;
    	else if (r==13 && c==15)
    		ClusterID=25;
    	else if (r==17 && c==9)
    		ClusterID=45;
    	else if (r==18 && c==19)
    		ClusterID=56;
    	else if (r==10 && c==25)
    		ClusterID=36;
    	return ClusterID;		
    }

	
//	public void create_graph(List<State> l)
//	{
//		int[] x=new int[maxStateID];
//		for (int i=0; i<maxStateID;i++) x[i]=0;
//		for (State s:l)
//		x[s.ID()]=1;
//		g =new SparseMultigraph<Integer, String>();	
//		for (int i=0; i<maxStateID;i++)
//		for (int j=0; j<i;j++)
//		{
//			if (edgeWeights[i][j]!=0 && x[i]!=0 && x[j]!=0)
//			{
//		          g.addVertex(i);
//		          g.addVertex(j);
//		          g.addEdge(i+"->"+j,i, j);
//			}
//		}
//	}

	public int maxBetweenness()
	{
		ranker = new BetweennessCentrality(g);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
		int max_rank=0;
		int max_rank_id=0;
	//		 ranker.printRankings(true,true);
		for(int j:g.getVertices())
		{ 	 
			 if (ranker.getVertexRankScore(j)>max_rank)
			 {
				 max_rank=(int) ranker.getVertexRankScore(j);
				 max_rank_id=j;
			 }
		}
		return max_rank_id;
	}


	public void RemoveEdge(String edge)
	{
	     int x = edge.indexOf("-");
	     String firstNode = edge.substring(0, x);
	     String secondNode = edge.substring(x+2, edge.length());
	     int SourceNode = Integer.parseInt(firstNode);
	     int DestNode = Integer.parseInt(secondNode);
//	     System.out.println("("+SourceNode/numOfColumns+","+SourceNode%numOfColumns+")");
//	     System.out.println("("+DestNode/numOfColumns+","+DestNode%numOfColumns+")");
	     edgeWeights[SourceNode][DestNode]=0;	    
	}

    public List<MarkovOption> CreateOptions()
    {    	  
	      List<MarkovOption> options = new ArrayList<MarkovOption>();
//          int ID=0;
	      for (option op1:optionList)
	      {
	            MarkovOption newOption = new MarkovOption(op1.initialState, op1.finalState,
	                environment.ActionSet().size(), maxStateID, /*alpha*/ .1, gamma, environment);  
	            
	            options.add(newOption); 
	        
	            for (Object state : op1.initialState)
	            {
//	            	 OptionLength newOptionLength = new OptionLength((State)state,MaxOptionLength);
//	            	 newOption.OptionDistanceToSubGoal.add(newOptionLength);
	                 AddApplicableOptionForState(newOption, ((State) state).ID());
	            }
//	            ID++;
	      }

	      myGeneratedOptions=options;
	      System.out.println("num of options: "+options.size());
	      return myGeneratedOptions;
    }

		@Override
		public void learnOptions() {			
			 
			 //parameters
			int num_communities = 1;
			
			if(environment.getClass() == GridWorldEnv.class) // 6room
				num_communities = 11;
			else if(environment.getClass() == TaxiDriverEnv.class)
				num_communities = 3;
			else if(environment.getClass() == HanoiTowerEnv.class)
				num_communities = 4;
			else if(environment.getClass() == PlayRoomEnv.class)
				num_communities = 25; //7 --> all edge between cluster 2,3
									  //13 --> all edges (2,3), some edged(3,4)
									  //19 --> all edges (2,3), all (1,2) some edged(3,4) 

			      
	    BetweennessCommunityDetection(num_communities);
	    createOption_coummunity();
	    CreateOptions();
	    ExperienceReplay();
	    
//		LearningOptions(10,-1);
//		BetweennessCommunityDetection(3);
//		createOption_coummunity();
//		CreateOptions();
//		ExperienceReplay();		  
//		LearningOptions(175);
	      
//		show_episode_statistics();			
			
		}

}
