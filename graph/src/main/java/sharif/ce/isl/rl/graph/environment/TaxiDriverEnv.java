package sharif.ce.isl.rl.graph.environment;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class TaxiDriverEnv extends Environment {

    private int currentAction; 
    
    /*
     * State representation: (x,y). 
     * y shows Taxi location. y = 0 to 24 for 25 squares. 0 is the square in the down left 
     * corner. 1 is the square right to this square.
     *  x=0 : passenger is not in taxi.
     *  x=1 : passenger is in taxi.
     */
    private static char passenger, destination;
    public TaxiDriverEnv(State initialState, char passenger, char destination) {
    	
    	State.num_column = 25;
    	this.numOfColumns = 25;
        currentState = initialState;
        this.passenger = passenger;
        this.destination = destination;
        
        allActions = new ArrayList();       
        allActions.add(0); //north
        allActions.add(1); // east
        allActions.add(2); // south
        allActions.add(3); // west
        allActions.add(4); // pick-up
        allActions.add(5); // put-down
        stochastizationThreshold = 0.8;
        currentAction = -1;           
        System.out.println("x= "+initialState.x+" y= "+initialState.y+" "+passenger+" "+destination);
    }
    public List<Object> GetAdmissibleActions(State state) {
    	List<Object> result = new ArrayList<Object>(allActions);
        return result;   
    }
      
    private boolean isGoalState(State state, State prevState, int action){
        //if the passenger is in the destination and he was put-down there, 
    	//then the agent is in a goal state.
    	
    	if(destination == 'Y' && state.y == 0 && state.x == 0 && action== 5 && prevState.x == 1)
    		return true;
    	
    	else if(destination == 'B' && state.y == 3 && state.x == 0 && action== 5 && prevState.x == 1)
    		return true;
    	
    	else if(destination == 'R' && state.y == 20 && state.x == 0 && action== 5 && prevState.x == 1)
    		return true;
    	
    	else if(destination == 'G' && state.y == 24 && state.x == 0 && action== 5 && prevState.x == 1)
    		return true;
    		
         
        return false;
    }
    
    
    public boolean isInGoalState() {
        return isGoalState(currentState, prevState, currentAction);
    }
    
    public double CalcReward(State state, State nextState, Object action) {       
//        The reward is -1 for each action, an additional +20 for passenger
//        delivery and an additional -10 for an unsuccessful pick-up or put-
//        down action
    	double reward = -1;
        if ((Integer) action >= 4)//pick-up or put-down
        {
            if( isGoalState(nextState, state, (Integer)action))
                reward += 20;
            
            else if(state.x == nextState.x && state.y == nextState.y)
                reward -= 10;
        }
//        System.out.println("r: "+reward);
        return reward;

    }

    public boolean EpisodeFinished() {
        return isInGoalState();
    }

    public State ReNatal() {
        currentAction = 0;
        currentState = RandomInitialState();
        return currentState;
    }
    
    
//***************************************
    
    public double ApplyAction(Object action) {
        currentAction = (Integer) action;
        if (!GetAdmissibleActions(currentState).contains(action)) {
            //throw new Exception("This action is not admissible in this state");

            System.out.println("ERROR: This action is not admissible in this state");
            return -1000;
        }
        Object realDoneAction = stochastize(action);
        State nextState = GetRealApplyActionResult(realDoneAction);     
        double reward = CalcReward(currentState, nextState, action);

        prevState = currentState;
        currentState = nextState;

        return reward;
    }

    private Object stochastize(Object action) {
        /*
         * The navigation actions succeed
			in moving the taxi in the intended direction with probability
			0.8; the action takes the taxi to the right or left of the intended
			direction with a probability of 0.2
         */
        if((Integer)action >= 4)
        	return action;
              
        if (rand.nextDouble() >= stochastizationThreshold) {
            boolean right = rand.nextBoolean();
            Integer newAction;
            if(right)
            	newAction = new Integer(((Integer)action+1)%4);
            else
            	newAction = new Integer(((Integer)action-1)%4);
            
            return newAction;
        }
        return action;
    }  
    
    private State GetRealApplyActionResult(Object action) {
        State nextState = new State(currentState.x, currentState.y);
        switch ((Integer) action) {
            case 0: //NORTH:
                North(nextState);
                break;
            case 1: //EAST:
                East(nextState);
                break;
            case 2: //SOUTH:
                South(nextState);
                break;
            case 3: //WEST:
                West(nextState);
                break;
            case 4: //pick-up
               
//////////////////////y//////////////////////////////   
 /*                 
               4  (R)  21  22  23 (G)          
               3   15  16  17  18  19
               2   10  11  12  13  14       
               1   5   6   7   8   9
               0  (Y)  1   2  (B)  4
                   0   1   2   3   4
               * */
            	
                if (nextState.y == 0 && passenger == 'Y')
                    nextState.x = 1;
                else if (nextState.y == 3 && passenger == 'B')
                    nextState.x = 1;
                else if (nextState.y == 20 && passenger == 'R')
                    nextState.x = 1;
                else if (nextState.y == 24 && passenger == 'G')
                    nextState.x = 1;
                break;
                
            case 5: //put-down
                if (nextState.y == 0 && destination == 'Y')
                    nextState.x = 0;
                else if (nextState.y == 3 && destination == 'B')
                    nextState.x = 0;
                else if (nextState.y == 20 && destination == 'R')
                    nextState.x = 0;
                else if (nextState.y == 24 && destination == 'G')
                    nextState.x = 0;
                break;
        }
        return nextState;
    }

    private void North(State nextState) {
        nextState.y = nextState.y >= 20 ? nextState.y : nextState.y + 5;
    } 
    
    private void South(State nextState) {
        nextState.y = nextState.y < 5 ? nextState.y : nextState.y - 5;
    }

    private void West(State nextState) { 
        if(nextState.y % 5 == 0)
        	return;
        
        if(nextState.y==3  || nextState.y==8 || 
        	nextState.y==17 || nextState.y==22 ||
        	nextState.y==1  || nextState.y==6)
        	
        	return;
        
        else
        	nextState.y = nextState.y-1;   
    }

    private void East(State nextState) {
        
        if(nextState.y % 5 == 4)
        	return;
        
        if( nextState.y==2  || nextState.y==7 || 
        	nextState.y==16 || nextState.y==21 ||
        	nextState.y==0  || nextState.y==5)
        	
        	return;
        
        else
        	nextState.y = nextState.y+1;   
    }
    
//**********************************
    public static State RandomInitialState(){
        int y;
        y = rand.nextInt(25);        
        State init = new State(0, y);
        
//        int r = rand.nextInt(4);
//        if(r == 0)
//        	passenger = 'Y';
//        
//        else if(r == 1)
//        	passenger = 'G';
//        
//        else if(r == 2)
//        	passenger = 'B';
//        
//        else if(r == 3)
//        	passenger = 'R';
//        else
//        	System.out.println("out of renge");
//        
//        r = rand.nextInt(4);
//        if(r == 0)
//        	destination = 'Y';
//        
//        else if(r == 1)
//        	destination = 'G';
//        
//        else if(r == 2)
//        	destination = 'B';
//        
//        else if(r == 3)
//        	destination = 'R';

//        System.out.println(passenger+"\t"+destination);
        return init;
    }

	@Override
	public int getMaxStateID() {
		return 50;		
	}
	@Override
	public int getGoalStateID() {
		int y =0;
		if(destination == 'B'){
			y = 3;
		}
		else if(destination == 'G'){
			y = 24;
		}
		else if(destination == 'R'){
			y = 20;
		}
		else if(destination == 'Y'){
			y = 0;
		}
		return 25+y;
	}
	@Override
	public State getState(Integer id) {
       if (id>25)
            return new State(1, id-25);
        else
            return new State(0, id);
	}
	@Override
	public String getName() {
		return "taxi";
	}
}
