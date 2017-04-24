package sharif.ce.isl.rl.graph.environment;

import java.io.Serializable;

/*
 * State class represent the state object suitable only for GridWordlEnv and TaxiDriverEnv
 * 
 */
public class State implements Serializable{

    public int x, y;

    public static int num_column; //in main will be valued
      
    public State() {
    }

    public State(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int ID() {
        int returnValue = x *num_column + y;
        return returnValue;
    }

    public int compareTo(Object o) {
        int returnValue = 0;
        if (o == null)
            returnValue = -1;
        else {
            if (o.getClass() != State.class 
            		&& o.getClass() != HanoiTowerState.class 
            		&& o.getClass() != PlayRoomState.class)
                throw new UnsupportedOperationException("This function is " +
                        "implemented only for " + State.class.toString() + " objects. No comparation" +
                        " is currently supported for " + o.getClass() + "class type");
            State compared = (State) o;
            returnValue = this.ID() == compared.ID() ? 0 : -1;
        }   
        return returnValue;

    }

}
