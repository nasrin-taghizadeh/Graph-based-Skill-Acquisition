package sharif.ce.isl.rl.graph.environment;

import java.util.ArrayList;
import java.util.List;

public class PlayRoomEnv extends Environment{
	
	//constants
	private final static boolean OFF = false;
	private final static boolean ON = true;
	
	public final static int monkey = 1;
	public final static int bell = 2;
	public final static int ball = 3;
	public final static int light_switch = 4;
	public final static int music_button = 5;
	
	//goal is to frighten monkey
	private int goalStateID;
	
	public PlayRoomEnv(PlayRoomState initialState
			, PlayRoomState finalState){
		
		this.currentState = initialState;
		this.goalStateID = finalState.ID();
		stochastizationThreshold = 0.75;
		
		allActions = new ArrayList();
		
	    allActions.add(0); 	// LookAtRandomObject
	    allActions.add(1); 	// LookAtObjectInHand
	    allActions.add(2); 	// HoldObjectIsLookingAt
	    allActions.add(3); 	// LookAtObjectMarkerIsPlacedOn
	    allActions.add(4); 	// PlaceMarkerOnObjectLookingAt	 
	    allActions.add(5); 	// FlipLightSwitch
	    allActions.add(6); 	// PressMusicButton
	    allActions.add(7); 	// HitBallTowardMarker	   	 	    
	    allActions.add(8);  // MoveObjectInHandToLocLookingAt ??????????
	    allActions.add(9);  // NoEffect
	
	}
	
//**************************	
	@Override
    public double ApplyAction(Object action){
       
        Object realDoneAction = stochastize(action);
        State nextState = GetRealApplyActionResult(realDoneAction);
        double reward = CalcReward(currentState, nextState, realDoneAction);
        prevState = currentState;
        currentState = nextState;

        return reward;
    }
    private Object stochastize(Object action) {
        if( (Integer)action == 0 || (Integer)action == 1)
        	return action;
        
        if (rand.nextDouble() <= stochastizationThreshold) {
        	return action; 
        }
        else
        	return new Integer(9); //no effect action
    }  
    
    private State GetRealApplyActionResult(Object action) {
    	PlayRoomState nextState = new PlayRoomState((PlayRoomState)currentState);
    	if(((PlayRoomState)currentState).bell_state == ON)
    		nextState.bell_state = OFF;
    	
        switch ((Integer)action) {
            case 0: //LookAtRandomObject:
            	int num = rand.nextInt(5);
            	if(num == 0)
            		nextState.look_at_object = music_button;
            	else if(num == 1)
            		nextState.look_at_object = light_switch;
            	else if(num == 2)
            		nextState.look_at_object = bell;
            	else if(num == 3)
            		nextState.look_at_object = ball;
            	else if(num == 4)
            		nextState.look_at_object = monkey;
            	
                break;
                
            case 1: //LookAtObjectAtHand: ,no mater any object is in hand or not
                nextState.look_at_object = nextState.in_hand_object;                
                break;
                
            case 2: //HoldObjectIsLookingAt:
                nextState.in_hand_object = nextState.look_at_object;                
                break;
                
            case 3: //LookAtObjectMarkerIsPlacedOn:
                nextState.look_at_object = nextState.marker_on_object;        
                break;
                
            case 4: //PlaceMarkerOnObjectLookingAt:   
                nextState.marker_on_object = nextState.look_at_object;
                break;               
                
            case 5: //FlipLightSwitch:
            	if(nextState.light_state == ON)
            		nextState.light_state = OFF;
            	else
            		nextState.light_state = ON;
                break;
                
            case 6: //PressMusicButton:
            	if(nextState.music_state == ON){
            		nextState.music_state = OFF;
            		
            		if(nextState.monkey_state == ON)
            			nextState.monkey_state = OFF;
            	}
            	else
            		nextState.music_state = ON;      
            	
            	if(nextState.bell_state == ON)
            		nextState.monkey_state = ON;
                break;                
                
            case 7: //HitBallTowardMarker:
            	if(nextState.marker_on_object == bell){
            		nextState.bell_state = ON;
            		
            		if(nextState.music_state == ON)
            			nextState.monkey_state = ON;
            	}            
            	nextState.in_hand_object = 0;
            	nextState.look_at_object = 0;            	
                break;
 
            case 8: //MoveObjectInHandToLocLookingAt
            	nextState.look_at_object = nextState.in_hand_object;
            	nextState.in_hand_object = 0;
            	break;
            	
            case 9: //NoEffect:
                break;	                
                           
        }
        return nextState;
    }
//******************************
    
	@Override
	public double CalcReward(State state, State nextState, Object action) {
			if(nextState.ID() == goalStateID){
	            return 1000;
	       }
	        return -1;
	}

	@Override
	public boolean EpisodeFinished() {
		return isInGoalState();
	}

	@Override
	public List<Object> GetAdmissibleActions(State state) {
		
//		System.out.println("AdmissibleActions: ");
		List admissibleActions = new ArrayList();
		admissibleActions.add(0);
		admissibleActions.add(1);
		admissibleActions.add(2);
		admissibleActions.add(3);
		admissibleActions.add(4);	
		admissibleActions.add(8);
		
		PlayRoomState object = (PlayRoomState)state ;
		
		if(object.look_at_object == light_switch && object.in_hand_object == light_switch){
			admissibleActions.add(5);
//			System.out.print("5\t");
		}
		if(object.look_at_object == music_button && object.in_hand_object == music_button
				&& object.light_state == ON){
			admissibleActions.add(6);
//			System.out.print("6\t");
		}
		if(object.look_at_object == ball && object.in_hand_object == ball){
			admissibleActions.add(7);
//			System.out.print("7\t");
		}	
		
		return admissibleActions;
	}

	@Override
	public State ReNatal() {
		PlayRoomState newState = new PlayRoomState(
				rand.nextInt(5), rand.nextInt(5), rand.nextInt(5),
				rand.nextBoolean(), rand.nextBoolean(), OFF, OFF);

		currentState = newState;
		return newState;
	}

	@Override
	public boolean isInGoalState() {	
		if(((PlayRoomState)currentState).ID() == goalStateID)
			return true;
		else
			return false;
//		return((PlayRoomState)currentState).isMonkeyFrightened();
	}

	@Override
	public int getMaxStateID() {
		return 1300;
	}

	@Override
	public int getGoalStateID() {
		return goalStateID;
	}

	@Override
    public State getState(Integer id)
    {
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
		return "playroom";
	}

}
