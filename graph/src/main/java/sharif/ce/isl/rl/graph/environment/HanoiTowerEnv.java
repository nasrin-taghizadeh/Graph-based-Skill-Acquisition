package sharif.ce.isl.rl.graph.environment;

import java.util.ArrayList;
import java.util.List;

public class HanoiTowerEnv extends Environment {

	//initial state: all disks are in left rod
	//goal: all disks be in right rod	
	//leftRod = 2, middleRod =1, rightRod = 0;
	
	public HanoiTowerEnv(){	
        currentState = new HanoiTowerState(2,2,2,2,2);
        allActions = new ArrayList();       
        allActions.add(0); //R --> M
        allActions.add(1); //R --> L
        allActions.add(2); //M --> R
        allActions.add(3); //M --> L
        allActions.add(4); //L --> R
        allActions.add(5); //L --> M           
        
        stochastizationThreshold = 1;
	}
	@Override
	public double ApplyAction(Object action) {
		HanoiTowerState nextState = new HanoiTowerState((HanoiTowerState)currentState);
		int disk = -1;
		if((Integer)action == ((HanoiTowerState)currentState).disk[0]*2 ||
				(Integer)action == ((HanoiTowerState)currentState).disk[0]*2 + 1)
			disk = 0;
		else
			disk = ((HanoiTowerState)currentState).movableDisk;
		
//		System.out.println("selected action"+(Integer)action+" disk: "+disk);
		
        switch ((Integer)action) {
            case 0: //R --> M
                nextState.disk[disk] = 1;
                break;
            case 1: //R --> L 
                nextState.disk[disk] = 2;
                break;
            case 2: //M --> R
                nextState.disk[disk] = 0;
                break;
            case 3: //M --> L
                nextState.disk[disk] = 2;
                break;
            case 4: //L --> R
                nextState.disk[disk] = 0;
                break;
            case 5: //L --> M
                nextState.disk[disk] = 1;
                break;	                
        }
        double reward = CalcReward(currentState, nextState, action);
        prevState = currentState;
        currentState = nextState;

        return reward;
	}

	@Override
	public double CalcReward(State state, State nextState, Object action) {
        if (isInGoalState((HanoiTowerState)nextState))
            return 1000;             
        return -1; 
	}
	private boolean isInGoalState(HanoiTowerState state) {
		if(state.ID() == 0)
			return true;
		
        return false;
    }
	
	@Override
	public boolean EpisodeFinished() {
		return isInGoalState();
	}

	@Override
	public List<Object> GetAdmissibleActions(State state) {
		
		/*
		 * in every state at most 3 actions are available. 
		 * 2 actions are moving smallest disk into other pods.
		 * third action must be found ;)
		 */
		HanoiTowerState stst = (HanoiTowerState)state;
//		System.out.println("ncurrent state: "+stst.disk[0]+" "+stst.disk[1]+" "+stst.disk[2]+" "+
//				stst.disk[3]+" "+stst.disk[4]+" ");
		
		ArrayList admissibleActions = new ArrayList();
		admissibleActions.add(stst.disk[0]*2);
		admissibleActions.add(stst.disk[0]*2+1);
		
		/*
		 * disk #i can move to another rod, if all smaller disks are in same rod
		 * and this rod is differnet from rod that disk #i is into it. 
		 */
		
		int movableDisk = -1;
		int invalidRod = stst.disk[0];
		
		if(stst.disk[1] != stst.disk[0])
			movableDisk = 1;		
		else if(stst.disk[2] != stst.disk[1]) // disk[1] = disk[0]
			movableDisk = 2;	
		else if(stst.disk[3] != stst.disk[2]) // disk[2] = disk[1] = disk[0]
			movableDisk = 3;		
		else if(stst.disk[4] != stst.disk[3]) // disk[3] = disk[2] = disk[1] = disk[0]
			movableDisk = 4;
		else								  // disk[4] = disk[3] = disk[2] = disk[1] = disk[0], 
			movableDisk = 0;				  // only smallest disk can move.
		
		stst.movableDisk = movableDisk;
		
		if(stst.disk[movableDisk]==0 && invalidRod == 2)
			admissibleActions.add(0);
		
		else if(stst.disk[movableDisk]==0 && invalidRod == 1)
			admissibleActions.add(1);
		
		else if(stst.disk[movableDisk]==1 && invalidRod == 2)
			admissibleActions.add(2);
		
		else if(stst.disk[movableDisk]==1 && invalidRod == 0)
			admissibleActions.add(3);
		
		else if(stst.disk[movableDisk]==2 && invalidRod == 1)
			admissibleActions.add(4);
		
		else if(stst.disk[movableDisk]==2 && invalidRod == 0)
			admissibleActions.add(5);
		
//		System.out.print("Admissible actions: ");
//		for(Object ac: admissibleActions)			
//			System.out.print(((Integer)ac).intValue()+" ");
//		
//		System.out.println("");
		
		return admissibleActions;
	}
	
	@Override
	public State ReNatal() {
		currentState = new HanoiTowerState(2,2,2,2,2);
        return currentState;
	}
	
	public int getMaxStateID() {
		return 243;	//3*3*3*3*3
	}
	public boolean isInGoalState() {
		return isInGoalState((HanoiTowerState)currentState);
	}
	
	public int getGoalStateID() {
		//initial state: all disks are in left rod
		//goal: all disks be in right rod	
		//leftRod = 2, middleRod =1, rightRod = 0;
		return 0;
	}
	@Override
	public State getState(Integer id) {
		int[] disks= new int[5];
        
        for (int i=0;i<5;i++)
        {
            disks[i] = id%3;
            id/=3;
        }
        return new HanoiTowerState(disks[0],disks[1],disks[2],disks[3],disks[4]);
	}
	@Override
	public String getName() {
		return "hanoi";
	}

}
