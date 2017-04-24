package sharif.ce.isl.rl.graph.environment;

import java.io.Serializable;

public class HanoiTowerState extends State implements Serializable{

	//first dist is the smallest
	//leftRod = 2, middleRod =1, rightRod = 0
	public int[] disk = new int[5];
	public int movableDisk = -1;
	
	public HanoiTowerState(int first, int second, int third, int fourth, int fifth){
		disk[0] = first; //smallest disk
		disk[1] = second;
		disk[2] = third;
		disk[3] = fourth;
		disk[4] = fifth; //largest disk
	}
	
	//copy constructor
	public HanoiTowerState(HanoiTowerState state){
		this.disk[0] = state.disk[0];
		this.disk[1] = state.disk[1];
		this.disk[2] = state.disk[2];
		this.disk[3] = state.disk[3];
		this.disk[4] = state.disk[4];
	}
	
	public int ID(){
		return disk[0] + 3*disk[1] + 9*disk[2] + 27*disk[3] + 81*disk[4];
	}
}
