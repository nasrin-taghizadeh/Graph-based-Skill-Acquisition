package sharif.ce.isl.rl.graph.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.State;

class MyNode{
	
	private final int ID;
	private int label;
	private List<MyNode> neighbors = new ArrayList<MyNode>();	
	private int[][] neighbor_freq;
	private static Random rand = new Random();
	
	public MyNode(int ID, int label){
		this.ID = ID; 
		this.label = label;
	}
	public void addNeighbors(MyNode newNeighbor){
		neighbors.add(newNeighbor);
	}
	public int getID(){ 
		return ID;
	}
	public int getLabel(){ 
		return label;
	}
	public void setLabel(int newLabel){
		this.label = newLabel;
	}
	public int getDegree(){
		return neighbors.size();
	}
	
	public List<MyNode> getNeighbors(){return neighbors;}
	public void changeLabelToFrequentLabelInNeighbors(){
		
		int size = neighbors.size() + 1;
		neighbor_freq = new int[size][2];
		for(int j=0; j<size; j++){
			neighbor_freq[j][0] = -1;
			neighbor_freq[j][1] = 0;
		}
		neighbor_freq[0][0] = this.label;
		neighbor_freq[0][1] = 1;
		
		Iterator<MyNode> itr = neighbors.iterator();
		boolean flag = true;
		int counter = 1;
		int lab;
		
		while (itr.hasNext()) {
			lab = itr.next().getLabel();
//			System.out.println("neigh lab: "+lab);
			
			for(int i=0; i<counter; i++){
				if(neighbor_freq[i][0] == lab){
					neighbor_freq[i][1] ++;
					flag = false;
					break;
				}
			}
				
			if(flag){
				neighbor_freq[counter][0] = lab;
				neighbor_freq[counter][1] = 1;
				counter++;				
			}
			else
				flag = true;
			
		}
		int max = size-1;
		for(int i=0; i<size-1; i++){
			if(neighbor_freq[i][1] > neighbor_freq[max][1])
				max = i;
			if(neighbor_freq[i][1] == neighbor_freq[max][1])
				if(rand.nextDouble() > 0.5)
					max = i;
		}
		
		this.label = neighbor_freq[max][0];
//		System.out.println(",  "+this.label);
	}
}

class MyCluster{
	private List<Integer> nodes = new ArrayList<Integer>();
	private int label;
	
	public MyCluster(int label){this.label = label;}
	public void addMember(int node){ nodes.add(node);}
	public int getLabel(){ return label;}
	public List<Integer> getMembers(){ return nodes;}
	
	public boolean isInCluster(int nodeID){
		if(nodes.contains(nodeID))
			return true;
		else
			return false;
	}
	public void printMembers(){
		
		System.out.println("Cluster: "+label);
		int size = nodes.size();
		for(int i = 0; i < size; i++)
			System.out.println(nodes.get(i));
		
		System.out.println("____");
	} 
}

public class LabelPropagationLearner extends SubgoalBasedQLearner{

	private int clusterNum = 0;
	private MyCluster[] clusters = new MyCluster[100];
	private MyNode[] allNodes = new MyNode[maxStateID];
	private int graphEdgeNum;

	
	public LabelPropagationLearner(Environment earth) {
		super(earth, 2);
		
		int numOfPrimitiveActions = environment.ActionSet().size();
        int MAX_AVAIL_ACT_OR_OPTIONS = numOfPrimitiveActions ;
        QTable1 = new double[maxStateID][MAX_AVAIL_ACT_OR_OPTIONS];
	}
	
	@Override
	public String getName() {
		return "labelPro";
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
//			System.out.println(ID+ " added");
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
    				
    				graphEdgeNum ++;
    				allNodes[i].addNeighbors(allNodes[j]);
    				allNodes[j].addNeighbors(allNodes[i]);

    			}
	}
	
	public void propagateLabel(){
		createGraph();
		
		for(int i = 0; i < 10; i++){
			for(int j=0; j<maxStateID; j++){
				if(allNodes[j] != null)
					allNodes[j].changeLabelToFrequentLabelInNeighbors();	
			}
		}
			
		boolean flag = true;
		MyCluster c;
		
		for(int k=0; k<maxStateID; k++){
			
			if(allNodes[k] != null){
//				System.out.println(allNodes[k].getID()+", "+allNodes[k].getLabel());
				for(int n=0; n<clusterNum; n++){
					c = clusters[n];
					if( c.getLabel() == allNodes[k].getLabel()){ 
						 c.addMember(allNodes[k].getID());
						 flag = false;
						 break;
					}
					
				}
				if(flag){
					MyCluster newCluster = new MyCluster(allNodes[k].getLabel());
					newCluster.addMember(allNodes[k].getID());
					clusters[clusterNum] = newCluster;
					clusterNum++;
				}
				else
					flag = true;
			}
		}
		System.out.println("clusters: "+clusterNum);
		
		for(int m = 0; m < clusterNum; m++){
			clusters[m].printMembers();	
//			getIntraClusterEdges(clusters[m]);
//			getTotalDegree(clusters[m]);
		}
//		System.out.println("M: "+computeModularity(clusters));
		
		optimumModularity();
	}

	public void optimumModularity(){
		System.out.println("optimum");
		double M1;
		double M2;
		MyCluster mergedCluster;
		
		for(int m = 0; m < clusterNum - 1; m++){
			if(isConnected(clusters[m], clusters[m+1])){
				M1 = getModularityofOneCluster(clusters[m]) + 
						getModularityofOneCluster(clusters[m+1]);
				
				mergedCluster = new MyCluster(maxStateID + 10);
				Iterator<Integer> itr = clusters[m].getMembers().iterator();
				while (itr.hasNext())
					mergedCluster.addMember(itr.next());
					
				itr = clusters[m+1].getMembers().iterator();
				while (itr.hasNext())
					mergedCluster.addMember(itr.next());							
				
				M2 = getModularityofOneCluster(mergedCluster);
				
				if(M2 > M1)
					System.out.println(clusters[m].getLabel()+", "+clusters[m+1].getLabel()
							+" merge"+(M2-M1));
			}
		}
	}
	
	public double computeModularity(MyCluster[] clusters){
		double modularity = 0;
		
		for(int i = 0; i <clusterNum; i++){
			if(clusters[i] != null){
				modularity += getModularityofOneCluster(clusters[i]);
			}			
		}
		return modularity;
	}
	
	private double getModularityofOneCluster(MyCluster c){
		double k = 0;
		if(c != null){
			k = (getIntraClusterEdges(c)/(double)graphEdgeNum) -
			Math.pow(getTotalDegree(c)/((double)graphEdgeNum*2.0), 2);
//			System.out.println(clusters[i].getLabel()+": "+k);
		}	
		return k;
	}
	
	private double getIntraClusterEdges(MyCluster c){
		//computes number of edges within the cluster
		int intraClusterEdges = 0;
		Iterator<Integer> itr = c.getMembers().iterator();
		Iterator<MyNode> itrOnNeighbors;
		int j;
		
		while (itr.hasNext()) {
			j = itr.next().intValue();
			
			itrOnNeighbors = allNodes[j].getNeighbors().iterator();
			
			while (itrOnNeighbors.hasNext()) {
				if(c.isInCluster(itrOnNeighbors.next().getID()))
					intraClusterEdges ++;			
			}						
		}
//		System.out.println("intra : "+(intraClusterEdges / 2));
		return (intraClusterEdges / 2);
	}
	
	private double getTotalDegree(MyCluster c){
		
		int totalDegree = 0;
		Iterator<Integer> itr = c.getMembers().iterator();
		int j;
		
		while (itr.hasNext()) {
			j = itr.next().intValue();
			totalDegree += allNodes[j].getDegree();					
		}
//		System.out.println("total : "+ totalDegree);
		return totalDegree;
	}

	private boolean isConnected(MyCluster c1, MyCluster c2){
//		System.out.println("in connected");
		
		Iterator<Integer> itr = c1.getMembers().iterator();
		Iterator<Integer> itr2;
		int i;
		
		while (itr.hasNext()){
			i = itr.next().intValue();
			itr2 = c2.getMembers().iterator();
			
			while(itr2.hasNext())
				if(edgeWeights[i][itr2.next().intValue()] != 0)
					return true;
		}
		return false;
	}

	@Override
	public void learnOptions() {
		propagateLabel();		
	}
}
