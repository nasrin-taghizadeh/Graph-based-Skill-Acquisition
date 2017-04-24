package sharif.ce.isl.rl.graph.algorithm;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleGraph;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import sharif.ce.isl.rl.graph.algorithm.core.MarkovOption;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

class MyCommunity implements Serializable{
	private ArrayList members = new ArrayList();
	private ArrayList centers = new ArrayList();

	private HashMap neighbour_border = new HashMap<Integer, Border>();
	int ID;
		
	public MyCommunity(int center){
		ID = center;
		this.centers.add(center);
		this.members.add(center);
	}
	public int getID(){
		return ID;
	}
	public void addMember(int id){
		members.add(id);
	}
	public void addCenter(int id){
		centers.add(id);
	}
	public void addBorder(int nodeId, int neighbourClusterID){
		
		if(!members.contains(nodeId))
			return;
		
		//there is a border with a neighbour cluster which has neighbourClusterID
		if(neighbour_border.containsKey(neighbourClusterID)){			
			((Border)neighbour_border.get(neighbourClusterID)).addNode(nodeId);
		}
		else{	
			Border b = new Border(nodeId, neighbourClusterID, this.ID);
			this.neighbour_border.put(neighbourClusterID, b);
		}		
	}
	public ArrayList getCenters(){
		return centers;
	}
	public ArrayList getMembers(){
		return members;
	}
	
	public HashMap<Integer, Border> getBorders(){
		return neighbour_border;
	}	 
	public void printMembers() {
		System.out.println("Cluster Members: "+ID);
		for(int i = 0; i < members.size(); i++)
			System.out.println(members.get(i));
		
		System.out.println("______________");		
	}
	public void printCenters(){
		System.out.println("Cluster Centers: "+ID);
		for(int i = 0; i < centers.size(); i++)
			System.out.println(centers.get(i));
		
		System.out.println("______________");
	}
}

class Border implements Serializable{
	private ArrayList nodes = new ArrayList();
	private int c1, c2;
	
	public Border(int n, int c1, int c2){
		nodes.add(n);
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public void addNode(int id){
		if(nodes.contains(id))
			return;
		nodes.add(id);
	}
	public ArrayList getBorderNodes(){
		return nodes;
	}
}


public class EVC extends SubgoalBasedQLearner {
	
	private ArrayList<MyCommunity> communities = new ArrayList<MyCommunity>();
	private ArrayList clustersCenters = new ArrayList();//centers of all clusters
	private int[][] adjMatrix  = new int[maxStateID][maxStateID];
	private double[][] eival = new double[environment.getMaxStateID()][2];
	double[][] laplacian;
	
	public EVC(Environment earth) {	
		super(earth, 2);    		
	}
	
	@Override
	public String getName() {
		return "EVC";
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
			UpdateDirectedEdgeTable(edgeWeights, prevState.ID(), currentState.ID());
				
			
			double qValue = QTable1[prevState.ID()][(Integer) currentAction];
			QTable1[prevState.ID()][(Integer) currentAction] = (1 - alpha)* qValue 
			                           + alpha* (reward + gamma * (MAXofQs(currentState)));


			if (environment.EpisodeFinished()) {			
				numOfActionsInEpisode.add(new Integer(numOfActionsDoneInOneEpisode));
//				System.out.println("# "+numOfActionsDoneInOneEpisode);
				numOfActionsDoneInOneEpisode = 0;
				episodeNum++;				
				initialState = environment.ReNatal();
                if (episodeNum == discoveryEpisodeThreshold) {
                    ExperienceReplay();
                    ExperienceReplay();
                }      
                if (episodeNum == 2*discoveryEpisodeThreshold) {
                	ExperienceReplay();
                }
			}

		}
//		System.out.println("----------------");
		return returnValue;
	}

	@Override
	public void learnOptions() {
		
//		System.out.println("learn");
//    	calculateLaplacianMatrix();  
		
		if(environment.getClass() == GridWorldEnv.class){
			//first two line comments must be removed.
			double[][] scors = new double[environment.numOfRows][environment.numOfColumns] ;
			calculateEVC(scors, edgeWeights);
//    		Display d = new Display(calculateEVC(scors, edgeWeights));
			
//			authority_hub();
		}
		else{
			calculateEVC(null, edgeWeights);
		}
//		detectCenters(eival, adjMatrix);
		clusterGraph(eival);

		if(environment.getClass() == GridWorldEnv.class){
    		Iterator<MyCommunity> itr = communities.iterator();
    		while (itr.hasNext()) {
				MyCommunity mc = (MyCommunity) itr.next();
				if(mc.getMembers().size()>= stateTable.size()/2){
					splitCluster(mc);
				}
			}
		}
		
//    	detectSubgoals();                    	
//    	calculateLaplacianMatrix();
    	
		myGeneratedOptions = CreateOptions();
		
        //learning option policy
        ExperienceReplay();
        ExperienceReplay();
		
	}
    
	private void filterOptions() {
		System.out.println("in filter option");
		
    	double subgoalV = -Double.MAX_VALUE;
    	int positive = 0, negative = 0;
    	double v;
    	
    	for(MarkovOption option: myGeneratedOptions){
    		System.out.println("new option");
    		subgoalV = -Double.MAX_VALUE;
    		for(State subgoal: option.getFinalStates()){
//    			System.out.println("final: "+subgoal.ID());
    			v = V(subgoal);
    			if(v>subgoalV)
    				subgoalV = v; 
    		}
//    		System.out.println("V: "+subgoalV);
    		for(State initial: option.getInitiationSet()){    			
    			v = V(initial);
//    			System.out.println("initial: "+initial.ID()+"  "+v);
    			if(v<=subgoalV)
    				positive++;
    			else
    				negative++;
    		}
//    		System.out.println("pos: "+positive+"\tneg: "+negative);
    		positive = negative = 0;
    		  			
    	}
		
	}
	
	private double V(State state) {

		List<Object> admissibleActions = environment.GetAdmissibleActions(state);
		double maxValue = -(Double.MAX_VALUE);
		
		for (Object admissibleAction : admissibleActions) {
			
			double tempValue = QTable1[state.ID()][(Integer) admissibleAction];
			
			if (maxValue < tempValue) {
				maxValue = tempValue;				
			}
		}		
		return maxValue;

	}
	
	private void splitCluster(MyCommunity mc) {
		
//    	System.out.println("in split");
    	int n = mc.getMembers().size();
		double[][] neweival = new double[2][maxStateID];
	   	g = new UndirectedSparseGraph<Integer, String>();
		
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			if(!mc.getMembers().contains(i))
				continue;
			
			for(int j=0; j<maxStateID; j++){
				if(!mc.getMembers().contains(j))
					continue;
	   			if (edgeWeights[i][j]!=0)
    			{
    			    g.addVertex(i);    			    
   	             	g.addVertex(j);   	             	   	        
	    	        g.addEdge(i+"..."+j,i, j);
    				
    			}							
			}
		}
		
		EigenvectorCentrality eigenvector = new EigenvectorCentrality(g);
		eigenvector.evaluate();
		
		double value;
		
//		System.out.println(g.getVertexCount());
		
//		Iterator<Integer> itr = g.getVertices().iterator();
//		Integer node;
//		while (itr.hasNext()) {
//			node = itr.next();
//			value = ((Double)eigenvector.getVertexScore(node)).doubleValue()*100000000;
//   			System.out.println(node.intValue()+"\t"+ value);
//		}		
		
	   	for (int i=0; i<maxStateID;i++){
	   		if(stateTable.containsKey(i) && mc.getMembers().contains(i)){
	   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue()*100000000;
//	   			System.out.println(i+"\t"+ value);		
	   		}
	   	}
//	   	clusterGraph(neweival);
	   	
	}

    private List<MarkovOption> CreateOptions() {
    	
        List<MarkovOption> options = new ArrayList<MarkovOption>();
        
		if(communities.size() == 1){
			System.out.println("one community created");
			int bestCenter;
			if(communities.get(0).getCenters().size() == 1)
//				SubGoals.add(stateTable.get(communities.get(0).getCenters().get(0)));
				bestCenter = (Integer)communities.get(0).getCenters().get(0);
			else{
				ArrayList centers = communities.get(0).getCenters();
				Iterator itr = centers.iterator();
				
				bestCenter = (Integer)itr.next();
				
				final int goalID = environment.getGoalStateID();
				create_graph(edgeWeights);
				DijkstraDistance distancer = new DijkstraDistance<Integer, String>(g);
				int distance = distancer.getDistance(goalID, bestCenter).intValue();
				int r =0;
				int c=0;
				
				while (itr.hasNext()) {
					c = (Integer)itr.next();
					r = distancer.getDistance(goalID, c).intValue();
					if( r < distance){
						distance = r;
						bestCenter = c;
					}
				}
//				SubGoals.add(stateTable.get(bestCenter));
				System.out.println("best center: "+bestCenter);
			}
       		ArrayList initiationSet = new ArrayList();
       		Iterator<Integer> allStates = stateTable.keySet().iterator();
       		while(allStates.hasNext())
       			initiationSet.add(stateTable.get( allStates.next() ));
    		
    		ArrayList<State> subgoalState = new ArrayList();
    		subgoalState.add(stateTable.get(bestCenter));
    		
    		MarkovOption newOption = new MarkovOption(initiationSet, subgoalState,
                    environment.ActionSet().size(), maxStateID, /*alpha*/ .1, gamma, environment);
    		
    		for (Object state : initiationSet.toArray()) {
                AddApplicableOptionForState(newOption, ((State) state).ID());

            }
    		
    		options.add(newOption);
		}
		else{
			System.out.println(communities.size()+" community was created");
			Iterator itr;
			int neighbourID;
			Border b;
			
			//to bounding options
	    	double subgoalV = -Double.MAX_VALUE;
	    	int positive = 0, negative = 0;
	    	double v;
	    	
			for(MyCommunity community : communities){
	
				System.out.println("next community: ("+community.ID%environment.numOfColumns+ ", "+ community.ID/environment.numOfColumns+")");
				System.out.println("next community: "+community.ID);
				itr = community.getBorders().keySet().iterator();	
				
				while (itr.hasNext()) {		
					
					neighbourID = (Integer)itr.next();
					b = community.getBorders().get(neighbourID);			
							
					// declaring final states of options 
					ArrayList<State> subgoalState = new ArrayList();	
					Iterator<Integer> itr2 = b.getBorderNodes().iterator();
					
	        		State finl;
	        		System.out.println("goals:");
	        		
	        		while (itr2.hasNext()) {
	        			finl = stateTable.get(itr2.next());
	        			
	        			if(isTrueBorder(community, neighbourID, finl.ID())){
	        				subgoalState.add(finl);
							System.out.println(finl.ID()/environment.numOfColumns+ ", "+ finl.ID()%environment.numOfColumns);
	        				System.out.println(finl.ID());
	        			}
	        			else{
	        				
	        				System.out.println(finl.ID()+" not true border");
	        			}
					}
	        		if(subgoalState.size() != 0){
	        			
	            		subgoalV = -Double.MAX_VALUE;
	            		for(State subgoal: subgoalState){
	            			v = V(subgoal);
	            			if(v>subgoalV)
	            				subgoalV = v; 
	            		}
	            		
		        		// declaring initial states of option
		        		ArrayList<State> initiationSet = new ArrayList();
						Iterator<Integer> itr3 = community.getMembers().iterator();
		        		State init;
		        		System.out.println("initials:");
		        		while (itr3.hasNext()) {
		        			 init = stateTable.get(itr3.next());
		        			 initiationSet.add(init);
							 System.out.println(init.ID()/environment.numOfColumns+ ", "+ init.ID()%environment.numOfColumns);
		        			 System.out.println(init.ID());
						}		        	
		        		
		        		for(State initial: initiationSet){    			
		        			v = V(initial);
		        			if(v<=subgoalV)
		        				positive++;
		        			else
		        				negative++;
		        		}
		        		
		        		if(positive > negative){
			        		MarkovOption newOption = new MarkovOption(initiationSet, subgoalState,
			                        environment.ActionSet().size(), maxStateID, /*alpha*/ .1, gamma, environment);
			        		
			        		for (Object state : initiationSet.toArray()) {
			                    AddApplicableOptionForState(newOption, ((State) state).ID());
		
			                }
			        		
			        		options.add(newOption);
		        		}
		        		else
		        			System.out.println("Eliminated..........");
		        		positive = negative = 0;
		        		
	        		}//end of if
	        		
				}// end of while
				
			}// end of for
			
		}// end of else	
		
//		System.out.println("num of options: "+options.size());
        return options;
    }
    
    private boolean isTrueBorder(MyCommunity community, int neighbourID, int stateID){
    	MyCommunity neighber = null;
    	
    	Iterator<MyCommunity> itr = communities.iterator();
    	MyCommunity myCommunity;
    	while (itr.hasNext()) {
			myCommunity = (MyCommunity) itr.next();
			if(myCommunity.ID == neighbourID){
				neighber = myCommunity;
				break;
			}
		}
    	
    	for(int i=0; i<maxStateID; i++){
    		if(edgeWeights[stateID][i]>0 && stateID != i && neighber.getMembers().contains(i)){ 
    				if(! community.getBorders().get(neighbourID).getBorderNodes().contains(i)){
    	    			return true;    					
    				}
    		}
    	}

    	return false;
    }
    
	private void create_graph(int[][] WeightGraph)
	{  
    	g = new UndirectedSparseGraph<Integer, String>();
    	
    	for (int i=0; i<maxStateID;i++)
    		for (int j=0; j<maxStateID;j++) // Directed
    		{
    			if (WeightGraph[i][j]!=0)
    			{
    			    g.addVertex(i);    			    
   	             	g.addVertex(j);   	             	   	        
	    	        g.addEdge(i+"..."+j,i, j);
    				
    			}
    		}
    }
	
	private void calculateLaplacianMatrix(){
		HashMap map = new HashMap();
		int p = 0;
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			map.put(i, p);
			p++;
		}
			
		int n = stateTable.size();
//		System.out.println("n: "+n);
		laplacian = new double[n][n];	
//		double[][] R = new double[n][n]; // R = right stochastic matrix
		int degree;
		int m = 0;
		p = 0;
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			degree = 0;
			for(int j=0; j<maxStateID; j++)
				if(edgeWeights[i][j] >= 1 && i!=j){
					laplacian[p][(Integer)map.get(j)] = -1;
					degree ++;	
					
//					R[p][(Integer)map.get(j)] = edgeWeights[i][j];
				}
			laplacian[p][p] = degree;
			m += degree;
			p++;
		}

//		for(int i=0; i<n; i++){			
//			for(int j=0; j<n; j++)
//				if(i != j)
//					R[i][j] = Math.abs((laplacian[i][j]) / laplacian[i][i]);
//		}
		
//		for(int i=0; i<n; i++){
//			for(int j=0; j<n; j++)
//				System.out.print(R[i][j]+" ");
//			System.out.println();
//		}
//		Matrix matrix = new Matrix(R);
		Matrix matrix2 = new Matrix(laplacian);
		
//		EigenvalueDecomposition decompose = new EigenvalueDecomposition(matrix);
		EigenvalueDecomposition decompose2 = new EigenvalueDecomposition(matrix2);
		
//		double[][] ReigenVector = decompose.getV().getArray();
//		double[][] ReigenValue = decompose.getD().getArray();
		
		double[][] eigenVector = decompose2.getV().getArray();
		double[][] eigenValue = decompose2.getD().getArray();
		        
//		System.out.println("eigen values:");
//		for(int i=0; i<n; i++)
//			System.out.println(ReigenValue[i][i]);
		
//		int landa2 = 1;
//		while(fielderValue[landa2][landa2] <= 0){
//			landa2 ++;
//		}
//		System.out.println("landa2: "+fielderValue[landa2][landa2]);

		for(int i=0; i<n; i++){
//			System.out.println("("+eigenVector[i][1]*40+", "+eigenVector[i][2]*40+") circle (1pt)");
			System.out.println(eigenVector[i][1]*40+"\t"+eigenVector[i][2]*40+"\t"+eigenVector[i][3]*40);
		}
		
//		for(int i=0; i<n; i++){
//			System.out.println(i+"\t"+ReigenVector[i][n-1]+"\t"+ReigenVector[i][n-2]
//			                    +"\t"+ReigenVector[i][1]);
//		}
		
//		int k = 3;
//		KMeans(fielderVector, k);
		
//		calculateCommuteDistance(laplacian, eigenVector, eigenValue, m);
	}
	
	private double[][] calculateCommuteDistance(final double[][] laplacian, final double[][] U, final double[][] lambda, int m){
		int n = U.length;
		double[][] pseudoInverse = new double[n][n];
		
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++){
				for(int k=2; k<n; k++){
					pseudoInverse[i][j] += (U[i][k] * U[j][k] / lambda[k][k]); 
				}
//				System.out.println(i+" "+j+" "+pseudoInverse[i][j]);
			}
		
//		System.out.println("_____________________");
		double[][] distance = new double[n][n];
		
		try {
			FileWriter writer = new FileWriter("Distance.xls");
			for(int i=0; i<n; i++)
				for(int j=0; j<n; j++){
//					distance[i][j] = (pseudoInverse[i][i] - 2*pseudoInverse[i][j] + pseudoInverse[j][j]);
					distance[i][j] = 
						(pseudoInverse[i][i] - 2*pseudoInverse[i][j] + pseudoInverse[j][j])
						-(1.0/laplacian[i][i] + 1.0/laplacian[j][j])
						+ 2.0/laplacian[i][i]*laplacian[j][j];
					
					writer.write(i+"\t"+j+"\t"+distance[i][j]+ "\n");
				}
			writer.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return distance;
	}	
	
	public double[][] calculateEVC(double[][] scors, int[][] matrix){		
				
//		for(int i=0; i<maxStateID; i++){
//			if(!stateTable.containsKey(i))
//				continue;
//			for(int j=0; j<maxStateID; j++)
//				if(matrix[i][j] >= 1 && i!=j){					
//					adjMatrix[i][j] = 1;
//				}			
//		}
		
//		for(int i=0; i<maxStateID; i++){
//			if(!stateTable.containsKey(i))
//				continue;
//			for(int j=0; j<maxStateID; j++){
//				if(!stateTable.containsKey(j))
//					continue;
//								
//				System.out.print(adjMatrix[i][j]+" ");						
//			}
//			System.out.println("");
//		}

//		EigenvalueDecomposition eig = eigen(adjMatrix);
//		double[][] V = eig.getV();
//		double[][] D = eig.getD();
		  
//		Matrix matrix1 = new Matrix(adjMatrix);		
//		EigenvalueDecomposition decompose = new EigenvalueDecomposition(matrix1);
//		
//		double[][] vectors = decompose.getV().getArray();
//		double[][] values = decompose.getD().getArray();
//
//		//eigen values arranged in diameter of values matrix, from smallest to largest
//		int column = decompose.getV().getColumnDimension();
//		int row = decompose.getV().getRowDimension();	
//		
//		int landaIndex = column -1;
//		double landa = values[row-1][column-1];
//		System.out.println("landa: "+landa);
//
//		for(int i=0; i<column; i++)
//			System.out.println(values[i][i]);
				
		create_graph(matrix);
		EigenvectorCentrality eigenvector = new EigenvectorCentrality(g);
		eigenvector.evaluate(); 
		
//		if(environment.getClass() == TaxiDriverEnv.class)
//	   		showGraph();
				
		double m = g.getEdgeCount();
//		System.out.println("num of edges: "+m+" "+g.getVertexCount());
//		BetweennessCentrality betweenness = new BetweennessCentrality(g);
//		betweenness.setRemoveRankScoresOnFinalize(false);
//		betweenness.evaluate();
		
		double value, value2;
		
		if(environment.getClass() == GridWorldEnv.class){
			int ii = 0, jj = 0;
			int col = scors[0].length;
			
		   	for (int i=0; i<maxStateID;i++){
		   		if(stateTable.containsKey(i)){
		   			
//		   			value = Math.abs(vectors[i][landaIndex]*10000); //largest eigenvalue 		   			
		   			value = ((Double)eigenvector.getVertexScore(i))*1000000000;		   			
//					value = betweenness.getVertexRankScore(i);
		   			System.out.println(i+"\t"+ value);
		   			scors[ii][jj] = value;
		   			
		   			eival[i][0] = value;
		   			eival[i][1] = -1; // cluster
//					System.out.println(i+ "\t"+ value);
//							+"\t"+fielderVector[i][landa2]*1000000000*1000000000);
					
		   			ii = i / col;
		   			jj = i % col;					
		   			scors[ii][jj] = value;
					
		   		}
		   	}

		   	
		}		
		else if(environment.getClass() == TaxiDriverEnv.class){// other environment						
		   	for (int i=0; i<maxStateID;i++)
		   		if(stateTable.containsKey(i)){
//		   			value = Math.round(vectors[i][landaIndex]*10000000); //largest eigenvalue 
//		   			value = Math.abs(vectors[i][landaIndex]*10000000); //largest eigenvalue
		   			
		   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue();
		   			eival[i][0] = value;
		   			eival[i][1] = -1; // cluster
//		   			System.out.println(i+ "\t"+ value);
		   				   			
	//	   			value = betweenness.getVertexRankScore(i);
	//	   			System.out.println(i+"\t"+ value);
		   		}		 

		   	
		}
		else if(environment.getClass() == HanoiTowerEnv.class){// other environment						
		   	for (int i=0; i<maxStateID;i++)
		   		if(stateTable.containsKey(i)){
//		   			value = Math.round(vectors[i][landaIndex]*10000000); //largest eigenvalue 
//		   			value = Math.abs(vectors[i][landaIndex]*10000000); //largest eigenvalue
		   			
		   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue()*100000;
		   			eival[i][0] = (int) value;
		   			eival[i][1] = -1; // cluster
//		   			System.out.println(i+ "\t"+ eival[i][0]);
		   				   			
	//	   			value = betweenness.getVertexRankScore(i);
	//	   			System.out.println(i+"\t"+ value);
		   		}		 

		   	
		}
		else if(environment.getClass() == PlayRoomEnv.class){						
		   	for (int i=0; i<maxStateID;i++)
		   		if(stateTable.containsKey(i)){
//		   			value = Math.round(vectors[i][landaIndex]*10000000); //largest eigenvalue 
//		   			value = Math.abs(vectors[i][landaIndex]*10000000); //largest eigenvalue
		   			
		   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue()*100000;
		   			eival[i][0] = value;
		   			eival[i][1] = -1; // cluster
//		   			System.out.println(i+ "\t"+ value);
		   				   			
	//	   			value = betweenness.getVertexRankScore(i);
	//	   			System.out.println(i+"\t"+ value);
		   		}		 

		   	
		}
	   	return scors;
	}	
	
	private void detectCenters(final double[][] eival, final int[][] adjMatrix){
		
		boolean localMaxima;
		
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			localMaxima = true;
			for(int j=0; j<maxStateID; j++)
				if(adjMatrix[i][j]==1 && eival[j][0]> eival[i][0]){
					localMaxima = false;
					break;
				}
			if(localMaxima == true)
				clustersCenters.add(i);
		}
		Iterator itr = clustersCenters.iterator();
//		System.out.println("Centers:");
//		while (itr.hasNext()) {
//			System.out.println( itr.next());
//			
//		}
//		System.out.println("________________");
	}
		
	private void clusterGraph(double[][] eival){
		
		ArrayList<Integer> centers = new ArrayList<Integer>();		
		int max = 0;
		
		while(true){
			for(int j=0; j<maxStateID; j++){
				if(eival[j][1] == -1 && eival[j][0] > 0){
					max = j;
					break;
				}
			}
			for (int j=max+1; j<maxStateID; j++){
				if(eival[j][0] > eival[max][0] && eival[j][1] == -1)
					max = j;
			}	
			if(centers.contains(max))
				break; //all nodes have been clustered
    		centers.add(max);
    		eival[max][1] = max; // cluster id
			MyCommunity newCommunity = new MyCommunity(max);
			communities.add(newCommunity);
			
			extendClusterRule1(max, newCommunity, eival);
//			newCommunity.printMembers();
		}
	}
	
	private void extendClusterRule1(int member, MyCommunity cls, double[][] eival){
		
		//Rule1: every node in the cluster introduces its neighbors with less EVC.
		for(int i=0; i<maxStateID; i++)
			
			//if i is a neighbor of member && has less eigenvalue then we add it to our cluster:
			if(edgeWeights[member][i]>0 && (eival[i][0] <= eival[member][0] )){
				//|| eival[i][0]-eival[member][0]<0.01
			                                                             			
							
				if(eival[i][1] == -1){				
					if(eival[i][0] == eival[cls.ID][0])
						cls.addCenter(i);
				
					eival[i][1] = cls.getID();
					cls.addMember(i);
					extendClusterRule1(i, cls, eival);
				}
				//if i is in another cluster then it is a border node
				else if(eival[i][1]!=-1 && eival[i][1]!= cls.getID()){
					
					cls.addMember(i);
					cls.addBorder(i, (int)eival[i][1]);
					eival[i][1] = cls.getID();
					
					Iterator<MyCommunity> coms = communities.iterator();
					MyCommunity comm;
					while (coms.hasNext()) {
						comm = (MyCommunity) coms.next();
						if(comm.getID() != cls.ID){
							comm.addBorder(i, cls.ID);
						}
						
					}					
					extendClusterRule1(i, cls, eival);
				}								
				
			}
	}	

	private void authority_hub(){
		System.out.println("in hub");
//		create_graph(edgeWeights);	
//	   	showGraph();
		
	   	int frequent = 0;

		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;		
			
			frequent = 0;
			for(int j=0; j<maxStateID; j++){
		   		if(edgeWeights[j][i] >0 && i != j)
		   			frequent += edgeWeights[j][i];
		   	}		   	
			System.out.println("State "+i+" fre: "+frequent);		
		   	if(frequent < 100)
		   		continue;
		   	
		   	for(int j=0; j<maxStateID; j++){
		   		if(edgeWeights[i][j] > 30 && i!=j)
					adjMatrix[i][j] = 1;
					
				if(edgeWeights[j][i] > 30 && i!=j)
					adjMatrix[j][i] = 1;					
		   	}
			
//			for(int j=i+1; j<maxStateID; j++){
//				
////				if(edgeWeights[i][j] < 18)
////					continue;
//				if(edgeWeights[i][j] > edgeWeights[j][i] && i!=j){
//					adjMatrix[i][j] = 1;
//					System.out.println(i+"  "+j+"  ("+edgeWeights[i][j]+", "+edgeWeights[j][i]+")");
//				}
//				else if(edgeWeights[i][j] < edgeWeights[j][i] && i!=j){
//					adjMatrix[j][i] = 1;		
//					System.out.println(j+"  "+i+"  ("+edgeWeights[i][j]+", "+edgeWeights[j][i]+")");
//				}
//				else if(edgeWeights[i][j] == edgeWeights[j][i] && edgeWeights[i][j]!= 0 && i!=j){
//					adjMatrix[i][j] = 1;
//					adjMatrix[j][i] = 1;
//					System.out.println(i+"  "+j+"  , "+j+"  "+i+"  ("+edgeWeights[i][j]+", "+edgeWeights[j][i]+")");
//				}
//			}
		}
		
		
//		for(int i=0; i<maxStateID; i++){
//			if(!stateTable.containsKey(i))
//				continue;
//			for(int j=0; j<maxStateID; j++)
//				if(edgeWeights[i][j] >= 1 && i!=j){
//					adjMatrix[i][j] = 1;				
//				}			
//		}
		
		double[][] WWT = new double[maxStateID][maxStateID];
		double[][] WTW = new double[maxStateID][maxStateID];
		
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			for(int j=0; j<maxStateID; j++)
				for(int k=0; k<maxStateID; k++){
					WWT[i][j] += adjMatrix[i][k] * adjMatrix[j][k];
					WTW[i][j] += adjMatrix[k][i] * adjMatrix[k][j];	
				}
							
		}
		
//		for(int i=0; i<maxStateID; i++){
//			if(!stateTable.containsKey(i))
//				continue;
//			for(int j=0; j<maxStateID; j++)
//				if(WWT[i][j] !=0)
//					System.out.println(i+" "+j+" "+WWT[i][j]);
//			
//		}
		
		Matrix matrix1 = new Matrix(WWT);		
		Matrix matrix2 = new Matrix(WTW);
		
		EigenvalueDecomposition decompose = new EigenvalueDecomposition(matrix1);
		EigenvalueDecomposition decompose2 = new EigenvalueDecomposition(matrix2);
		
		double[][] vectors = decompose.getV().getArray();
		double[][] values = decompose.getD().getArray();
		
		double[][] vectors2 = decompose2.getV().getArray();
		double[][] values2 = decompose2.getD().getArray();

		//eigen values arranged in diameter of values matrix, from smallest to largest
		int column = decompose.getV().getColumnDimension();
		int row = decompose.getV().getRowDimension();	
		
		int landaIndex = column -1;
//		double landa = values[row-1][column-1];
		double value, value2;
		
	   	for (int i=0; i<maxStateID;i++){
	   		if(!stateTable.containsKey(i))
				continue;
	   		value = Math.abs(vectors[i][landaIndex]*10000); //largest eigenvalue 
 			value2 = Math.abs(vectors2[i][landaIndex]*10000); //largest eigenvalue
			System.out.println(i+ "\t"+value+ "\t"+ value2);
	   		
	   	}
	}
	
	private void pageRank(){
		create_graph(edgeWeights);
		
		PageRank pagerank = new PageRank(g, 0);
		pagerank.evaluate(); 
		
		double value;
	   	for (int i=0; i<maxStateID;i++){
	   		if(stateTable.containsKey(i)){
	   			
	   			value = ((Double)pagerank.getVertexScore(i)).doubleValue()*100000;	   	
	   			System.out.println(i+"\t"+ value);				
	   		}
	   	}
	}
	
	private void showGraph(){
		
		// Layout<V, E>, BasicVisualizationServer<V,E>
		Layout<Integer, String> layout = new ISOMLayout(g);
//		Layout<Integer, String> layout = new CircleLayout(g);
		
		layout.setSize(new Dimension(800,700));
		
		BasicVisualizationServer<Integer,String> vv =
		new BasicVisualizationServer<Integer,String>(layout);
		vv.setPreferredSize(new Dimension(800,700));
		
		// Setup up a new vertex to paint transformer...
		Transformer<Integer,Paint> vertexPaint = new Transformer<Integer,Paint>() {
			public Paint transform(Integer i) {
				
				//for play room environment
				if(i >= 0 && i <= 215)
					return Color.yellow;
				
				else if(i >= 216 && i <= 431)
					return Color.pink;
				
				else if(i >= 648 && i <= 863)
					return Color.red;
				
				else if(i >= 432 && i <= 647)
					return Color.orange;
					
				else if(i >= 864 && i <= 1080)
					return Color.gray;
				
				else if(i >= 1080 && i <= 1295)
					return Color.magenta;
				//
				return Color.GREEN;
			}
		};
		// Set up a new stroke Transformer for the edges
		float dash[] = {10.0f};
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				return edgeStroke;
			}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		JFrame frame = new JFrame("Simple Graph View 2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
       
	private double[][] ditanceEigenvalue(int[][] WeightGraph, double[][] scors){
		
		SimpleGraph<Integer, String> g = new SimpleGraph<Integer, String>( new EdgeFactory() 
				{
					
					public Object createEdge(Object i, Object j) {
						return ((Integer)i).intValue() +" " + ((Integer)j).intValue();
					}
				});		
	   	
		for (int i=0; i<maxStateID;i++)
//    		for (int j=0; j<maxStateID;j++) // Directed
    		for (int j=0; j<i; j++) 		//UnDirected
    		{
    			if (WeightGraph[i][j]!=0)
    			{
    			    g.addVertex(i);    			    
   	             	g.addVertex(j);   	                 	         
   	             	g.addEdge(i, j);
    				
    			}
    		}		
		
		FloydWarshallShortestPaths<Integer, String> algorithm 
				= new FloydWarshallShortestPaths<Integer, String>(g);
		System.out.println("diameter: "+algorithm.getDiameter());
		
		double[][] distanceMatrix = new double[maxStateID][maxStateID];
		for(int i=0; i<maxStateID; i++){
			if(!stateTable.containsKey(i))
				continue;
			for(int j=0; j<maxStateID; j++){
				if(!stateTable.containsKey(j))
					continue;
				distanceMatrix[i][j] = algorithm.shortestDistance(new Integer(i), new Integer(j));
			}
							
		}
//		for(int i=0; i<maxStateID; i++){
//			if(!stateTable.containsKey(i))
//				continue;
//			for(int j=0; j<maxStateID; j++)
//				System.out.print(distanceMatrix[i][j]+" ");
//			
//			System.out.println();
//		}
		
		Matrix matrix = new Matrix(distanceMatrix);		
		EigenvalueDecomposition decompose = new EigenvalueDecomposition(matrix);
		
		double[][] vectors = decompose.getV().getArray();
		double[][] values = decompose.getD().getArray();

		//eigen values arranged in diameter of values matrix, from smallest to largest
		int column = decompose.getV().getColumnDimension();
		int row = decompose.getV().getRowDimension();	
		
		int landaIndex = column -1;
		double landa = values[row-1][column-1];
//		System.out.println("landa: "+landa);			
		
		double value;
		
		if(environment.getClass() == GridWorldEnv.class){
			int ii = 0, jj = 0;
			int col = scors[0].length;
			
		   	for (int i=0; i<maxStateID;i++){
		   		if(stateTable.containsKey(i)){
		   			value = vectors[i][landaIndex]*10000; //largest eigenvalue 
		   			
//		   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue()*1000;		   			
		   			eival[i][0] = value;
		   			eival[i][1] = -1; // cluster
					System.out.println(i+ "\t"+ value);
//							+"\t"+fielderVector[i][landa2]*1000000000*1000000000);
					
		   			ii = i / col;
		   			jj = i % col;					
		   			scors[ii][jj] = value;					
		   		}
		   	}

		}		
//		else{// other environment						
//		   	for (int i=0; i<maxStateID;i++)
//		   		if(stateTable.containsKey(i)){
//		   			value = Math.round(vectors[i][landaIndex]*10000000); //largest eigenvalue 
////		   			value = Math.abs(vectors[i][landaIndex]*10000000); //largest eigenvalue
//		   			
////		   			value = ((Double)eigenvector.getVertexScore(i)).doubleValue();
//		   			eival[i][0] = value;
//		   			eival[i][1] = -1; // cluster
////		   			System.out.println(i+ "\t"+ value);
//		   				   			
//	//	   			value = betweenness.getVertexRankScore(i);
//	//	   			System.out.println("\t"+ value);
//		   		}		 
//
//		   	
//		}	 
	   	return scors;
	}
	
	
}
