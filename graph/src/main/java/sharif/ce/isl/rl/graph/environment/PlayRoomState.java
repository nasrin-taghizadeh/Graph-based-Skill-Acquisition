package sharif.ce.isl.rl.graph.environment;

public class PlayRoomState extends State {
	
	//state variables
	public int in_hand_object;
	public int marker_on_object;
	public int look_at_object;
	
	public boolean light_state;
	public boolean music_state;
	public boolean bell_state;
	public boolean monkey_state;

		
	//constructor
	public PlayRoomState(
			int object_in_hand, int object_marker_placed_on, int object_looking_at,
			boolean light_state, boolean music_state, boolean bell_state, boolean monkey_state){
		
		this.in_hand_object = object_in_hand;
		this.marker_on_object = object_marker_placed_on;
		this.look_at_object = object_looking_at;
		
		this.light_state = light_state;
		this.music_state = music_state;
		this.bell_state = bell_state;
		this.monkey_state = monkey_state;
	}
	
	//copy constructor
	public PlayRoomState(PlayRoomState prs){
		
		this.in_hand_object = prs.in_hand_object;
		this.marker_on_object = prs.marker_on_object;				
		this.look_at_object = prs.look_at_object;
		
		this.light_state 	= prs.light_state;
		this.music_state 	= prs.music_state;
		this.bell_state 	= prs.bell_state;
		this.monkey_state 	= prs.monkey_state;
	}
	
    public int ID() {
    	int a=0;
    	
    	if(light_state)  
    		a += 1;
    	if(music_state)  
    		a += 2;
    	if(monkey_state)  
    		a += 4;
    	
    	if(a > 5) //6 -> 4, 7 -> 5
    		a -= 2;

    	int retVal = (in_hand_object + look_at_object * 6 + marker_on_object * 36) + a*216;;
    	if(bell_state == true){
    		int b = 0;
    	   	if(light_state)  
        		b += 1;
        	if(music_state)  
        		b += 2;
        	if(monkey_state)  
        		b += 4;
        	
        	if(b > 1) //6 -> 2, 7 -> 3
        		b -= 4;
    		retVal = 1296 + b; 
    	}
    	
//    	System.out.println("ID "+retVal);
    	return retVal;
    }	
    
    public boolean isMonkeyFrightened(){
    	return monkey_state;
    }   
}
