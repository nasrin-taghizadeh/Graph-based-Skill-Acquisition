package sharif.ce.isl.rl.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uci.ics.jung.graph.Graph;
import sharif.ce.isl.rl.graph.algorithm.SCC;
import sharif.ce.isl.rl.graph.algorithm.nodeBetweenness;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.GridWorldEnv;
import sharif.ce.isl.rl.graph.environment.HanoiTowerEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomEnv;
import sharif.ce.isl.rl.graph.environment.PlayRoomState;
import sharif.ce.isl.rl.graph.environment.State;
import sharif.ce.isl.rl.graph.environment.TaxiDriverEnv;

public class Main {
	static Graph <Integer, String> g ;
	 	
	public static void main(String args[])
	{
		System.out.println("hello");
				
 
//		gridWorld(nineroom,19,18,17,16);		
		
//		gridWorld(maze,38,38,36,36);
		taxi();
//		playroom();
//		hanoi();

	       
		System.out.println("end of execution");
	}

	static void gridWorld(int[][] room,int RowNumber,int ColumnNumber,int XGoal,int YGoal)
	{
		State.num_column = ColumnNumber;
		
//		Random rand = new Random();
//		int x0=8, y0=27, x1=18, y1=26;		
//		do{
//			x0 = rand.nextInt(RowNumber);
//			y0 = rand.nextInt(ColumnNumber);
//			if(room[x0][y0] == 0){
////				gridInitialState.x = x0;
////				gridInitialState.y = y0;
////				System.out.println("initial state: "+x0+" "+y0);
//				break;
//			}			
//		}
//		while(true);
//		
//		do{
//			x1 = rand.nextInt(RowNumber);
//			y1 = rand.nextInt(ColumnNumber);
//			if(room[x1][y1] == 0){
//				
////				gridFinalState.x = x0;
////				gridFinalState.y = y0;
////				System.out.println("final state: "+x1+" "+y1);
//				break;
//			}			
//		}
//		while(true);
//		
//		try {
//            FileWriter writer = new FileWriter("9rooms.txt", true);
// //           writer.append("********************************\n");
//            writer.append(x0+"\n");
//            writer.append(y0+"\n");
//            writer.append(x1+"\n");
//            writer.append(y1+"\n");	
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
		
		//**************************
//		char[] buf;
//		int n = 8* 1;
//		FileReader fr=null;
//		BufferedReader br=null;
//		
//		try{
//			fr = new FileReader("9rooms.txt");
//			br = new BufferedReader(fr);
//		}
//		catch (Exception e) {
//			
//		}
//		
//		for(int i=0; i<1; i++){
//		try {
//			x0 = Integer.parseInt(br.readLine());		
//			y0 = Integer.parseInt(br.readLine());
//			x1 = Integer.parseInt(br.readLine());	
//			y1 = Integer.parseInt(br.readLine());
//			System.out.println(x0+" "+y0+" "+x1+" "+y1);

//			fr.close(); 
//		  } catch (IOException e) {
//		      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}
		
		// ***********************************
		State gridInitialState1 = new State();
		State gridFinalState1 = new State();		
		
		gridInitialState1.x = 1;
		gridInitialState1.y = 1;
//		gridFinalState.x = x1;
//		gridFinalState.y = y1;
		
		gridFinalState1.x = XGoal;
		gridFinalState1.y = YGoal;

		List<State> gridGoalStates1 = new ArrayList();
		gridGoalStates1.add(gridFinalState1);		
		Environment myGridEarth1 = new GridWorldEnv(room, gridInitialState1, gridGoalStates1);
			
		//**********************************
		State gridInitialState2 = new State();
		State gridFinalState2 = new State();	
		
		gridInitialState2.x = 1; 
		gridInitialState2.y = 1;
		gridFinalState2.x = XGoal;
		gridFinalState2.y = YGoal;
		
		List<State> gridGoalStates2 = new ArrayList();
		gridGoalStates2.add(gridFinalState2);			
		Environment myGridEarth2 = new GridWorldEnv(room, gridInitialState2, gridGoalStates2);		
		
		//**********************************
		State gridInitialState3 = new State();
		State gridFinalState3 = new State();	
		
		gridInitialState3.x = 1; 
		gridInitialState3.y = 1;
		gridFinalState3.x = XGoal;
		gridFinalState3.y = YGoal;
		
		List<State> gridGoalStates3 = new ArrayList();
		gridGoalStates3.add(gridFinalState3);			
		Environment myGridEarth3 = new GridWorldEnv(room, gridInitialState3, gridGoalStates3);		
	
		//**********************************
		State gridInitialState4 = new State();
		State gridFinalState4 = new State();	
		
		gridInitialState4.x = 1; 
		gridInitialState4.y = 1;
		gridFinalState4.x = XGoal;
		gridFinalState4.y = YGoal;
		
		List<State> gridGoalStates4 = new ArrayList();
		gridGoalStates4.add(gridFinalState4);			
		Environment myGridEarth4 = new GridWorldEnv(room, gridInitialState4, gridGoalStates4);		
	
		//************************************************
		State gridInitialState5 = new State();
		State gridFinalState5 = new State();	
		
		gridInitialState5.x = 1; 
		gridInitialState5.y = 1;
		gridFinalState5.x = XGoal;
		gridFinalState5.y = YGoal;
		
		List<State> gridGoalStates5 = new ArrayList();
		gridGoalStates5.add(gridFinalState5);			
		Environment myGridEarth5 = new GridWorldEnv(room, gridInitialState5, gridGoalStates5);	
		
		//************************************************
		State gridInitialState6 = new State();
		State gridFinalState6 = new State();	
		
		gridInitialState6.x = 1; 
		gridInitialState6.y = 1;
		gridFinalState6.x = XGoal;
		gridFinalState6.y = YGoal;
		
		List<State> gridGoalStates6 = new ArrayList();
		gridGoalStates6.add(gridFinalState6);			
		Environment myGridEarth6 = new GridWorldEnv(room, gridInitialState6, gridGoalStates6);	
	
		//************************************************
//		
//		QLearningAgent agent = new QLearningAgent(myGridEarth1, 1);
//		agent.walk(200);
//		agent.show_episode_statistics();
//		agent.show_reward_statistics();
		

		//+++++++++++++++++++++++++++++++++++++++++++++++++
		
//		SimsekCode agent1=new SimsekCode(myGridEarth2);
//		agent1.walk2(100, true);		
//	    agent1.show_episode_statistics();
//		agent1.show_reward_statistics();
		 	
		//*************************************************
		
//		EVC learner2 = new EVC(myGridEarth3);
//		learner2.walk2(100, true);
//		learner2.show_episode_statistics();
//		learner2.show_reward_statistics();		
				
		//+++++++++++++++++++++++++++++++++++++++++++++++++
		
		SCC scclearner = new SCC(myGridEarth4);
		scclearner.walkWithOptions(200, true);
		scclearner.show_episode_statistics();
		scclearner.show_reward_statistics();
		
		//**************************************************
		
//		EdegeBetweennness inc_agent = new EdegeBetweennness(myGridEarth5);
//		inc_agent.walk2(100, true);
//		inc_agent.show_episode_statistics();
//		inc_agent.show_reward_statistics();
		
		//*****************************
//		ReformLabelPropagation ref_agent = new ReformLabelPropagation(myGridEarth6);
//		ref_agent.walk2(100, true);
//		ref_agent.show_episode_statistics();
//		ref_agent.show_reward_statistics();
	}
	
	static void taxi()
	{
		char passenger, destination;
		int r = new Random().nextInt(4);
		if(r == 0)
			passenger = 'Y';
      
		else if(r == 1)
      		passenger = 'G';
      
		else if(r == 2)
			passenger = 'B';
      
		else
			passenger = 'R';

      
		r = new Random().nextInt(4);
		if(r == 0)
      		destination = 'Y';
      
		else if(r == 1)
			destination = 'G';
      
		else if(r == 2)
      		destination = 'B';
      
		else
      		destination = 'R';
	      
//		Environment myTaxiEarth = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
//		QLearningAgent agent1 = new QLearningAgent(myTaxiEarth, 1);		
//		agent1.walk(200);
//		agent1.show_reward_statistics();
//		agent1.show_episode_statistics();
		
		//**************** EVC ***************
//		Environment myTaxiEarth1 = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
//		EVC learner = new EVC(myTaxiEarth1);
//		learner.walk2(200, true);
//		learner.show_reward_statistics();
//		learner.show_episode_statistics();
		
		//************** simsek code ********************		
		Environment myTaxiEarth2 = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
		nodeBetweenness agent2 = new nodeBetweenness(myTaxiEarth2);
		agent2.walkWithOptions(200, true);		
	    agent2.show_episode_statistics();
		agent2.show_reward_statistics();		
		
		
		//*********** SCC ****************
//		Environment myTaxiEarth3 = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
//		SCC agent3 = new SCC(myTaxiEarth3);
//		agent3.walk2(200, true);		
//	    agent3.show_episode_statistics();
//		agent3.show_reward_statistics();

	    //*********** ant *********************
//	    Environment myTaxiEarth4 = new TaxiDriverEnv(
//	    TaxiDriverEnv.RandomInitialState(), passenger, destination);
//      AntColony ant = new AntColony(myTaxiEarth4, 25, 0.9f, 0.99f, 10, 1.01f, 1.5f);
//      ant.walk2(200);
//      ant.show_episode_statistics();
		
		
		//********* EdgeBetweenness ***************
//		Environment myTaxiEarth5 = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
//		EdegeBetweennness inc_agent = new EdegeBetweennness(myTaxiEarth5);
//		inc_agent.walk2(200, true);
//		inc_agent.show_episode_statistics();
//		inc_agent.show_reward_statistics();
//		
		//********* ReformLabelPropagation ***************
//		Environment myTaxiEarth6 = new TaxiDriverEnv(TaxiDriverEnv.RandomInitialState(), passenger, destination);
//		ReformLabelPropagation ref_agent = new ReformLabelPropagation(myTaxiEarth6);
//		ref_agent.walk2(200, true);
//		ref_agent.show_episode_statistics();
//		ref_agent.show_reward_statistics();
//		
	}
	
	static void playroom()
	{
		//*************** Q ******************
//		PlayRoomState initialState = new PlayRoomState(0, 0, 0, false, false, false, false);
//		PlayRoomState goalState = new PlayRoomState(0, 0, 0, false, false, false, true); 		
//		PlayRoomEnv earth = new PlayRoomEnv(initialState ,goalState);
//		
//		QLearningAgent qlearner = new QLearningAgent(earth, 1);
//		qlearner.walk(200);
//		qlearner.show_episode_statistics();
//		qlearner.show_reward_statistics();
		
		//*****************  EVC  ********************
//		PlayRoomState initialState2 = new PlayRoomState(0, 0, 0, false, false, false, false);
//		PlayRoomState goalState2 = new PlayRoomState(0, 0, 0, false, false, false, true); 		
//		PlayRoomEnv earth2 = new PlayRoomEnv(initialState2 ,goalState2);
//
//		EVC learner = new EVC(earth2);
//		learner.walk2(200, true);
//		learner.show_episode_statistics();
//		learner.show_reward_statistics();
		
		
		//***************** SCC  ********************
		PlayRoomState initialState3 = new PlayRoomState(0, 0, 0, false, false, false, false);
		PlayRoomState goalState3 = new PlayRoomState(0, 0, 0, false, false, false, true); 		
		PlayRoomEnv earth3 = new PlayRoomEnv(initialState3 ,goalState3);

		SCC learner2 = new SCC(earth3);
		learner2.walkWithOptions(100, true);
		learner2.show_episode_statistics();
		learner2.show_reward_statistics();		

		//*****************  simsek  ********************
//		PlayRoomState initialState4 = new PlayRoomState(0, 0, 0, false, false, false, false);
//		PlayRoomState goalState4 = new PlayRoomState(0, 0, 0, false, false, false, true); 		
//		PlayRoomEnv earth4 = new PlayRoomEnv(initialState4 ,goalState4);
//
//		SimsekCode learner3 = new SimsekCode(earth4);
//		learner3.walk2(100, true);
//		learner3.show_episode_statistics();
//		learner3.show_reward_statistics();
		
		//*****************  Edge Bet  ********************
//		PlayRoomState initialState5 = new PlayRoomState(0, 0, 0, false, false, false, false);
//		PlayRoomState goalState5 = new PlayRoomState(0, 0, 0, false, false, false, true); 		
//		PlayRoomEnv earth5 = new PlayRoomEnv(initialState5 ,goalState5);
//
//		EdegeBetweennness learner4 = new EdegeBetweennness(earth5);
//		learner4.walk2(200, true);
//		learner4.show_episode_statistics();
//		learner4.show_reward_statistics();
		
		//*****************  Reform lbel prop  ********************
//		PlayRoomState initialState6 = new PlayRoomState(0, 0, 0, false, false, false, false);
//		PlayRoomState goalState6 = new PlayRoomState(0, 0, 0, false, false, false, true); 		
//		PlayRoomEnv earth6 = new PlayRoomEnv(initialState6 ,goalState6);
//
//		ReformLabelPropagation learner5 = new ReformLabelPropagation(earth6);
//		learner5.walk2(200, true);
//		learner5.show_episode_statistics();
//		learner5.show_reward_statistics();
	}
	
	static void hanoi()
	{
//		HanoiTowerEnv myEarth = new HanoiTowerEnv();	
//		QLearningAgent agent1 = new QLearningAgent(myEarth, 1);		
//		agent1.walk(200);
//		agent1.show_reward_statistics();
//		agent1.show_episode_statistics();
		

		//*********** EVC ********************
//		HanoiTowerEnv myEarth2 = new HanoiTowerEnv();
//		EVC agent2 = new EVC(myEarth2);
//		agent2.walk2(200, true);
//		agent2.show_episode_statistics();
//		agent2.show_reward_statistics();

		
		
	    //*********** ant *********************
//	    Environment myEarth3 = new HanoiTowerEnv();
//        AntColony ant = new AntColony(myEarth3, 25, 0.9f, 0.99f, 10, 1.01f, 1.5f);
//        ant.walk2(200);
//        ant.show_episode_statistics();
        
        
        //************ betweenness *************
//		HanoiTowerEnv myEarth4 = new HanoiTowerEnv();
//        SimsekCode betw_agent = new SimsekCode(myEarth4); 
//        betw_agent.walk2(200, true);
//        betw_agent.show_episode_statistics();
//        betw_agent.show_reward_statistics();
        
        
        //************* SCC **************
		for(int i =1; i<30; i++){
        HanoiTowerEnv myEarth5 = new HanoiTowerEnv();
        SCC scc_agent = new SCC(myEarth5); 
        scc_agent.walkWithOptions(200, true);
        scc_agent.show_episode_statistics();
        scc_agent.show_reward_statistics();
		}
        
        //********** Edge Betweennness ************
//        HanoiTowerEnv myEarth6 = new HanoiTowerEnv();
//        EdegeBetweennness inc_agent = new EdegeBetweennness(myEarth6);
//        inc_agent.walk2(200, true);
//        inc_agent.show_episode_statistics();
//        inc_agent.show_reward_statistics();       
        
        //********** Reform Label ************
//        HanoiTowerEnv myEarth7 = new HanoiTowerEnv();
//        ReformLabelPropagation ref_agent = new ReformLabelPropagation(myEarth7);
//        ref_agent.walk2(200, true);
//        ref_agent.show_episode_statistics();
//        ref_agent.show_reward_statistics();
	}
	
}
