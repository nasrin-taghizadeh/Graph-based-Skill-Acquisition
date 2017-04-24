package sharif.ce.isl.rl.graph.algorithm;
import java.io.Serializable;

import sharif.ce.isl.rl.graph.environment.State;

public class ExperienceElement implements Serializable{
    public State prevState, CurrentState;
    public int Action;
    public double reward;

    public ExperienceElement(State prevState, int action, State currentState, double reward) {
        this.prevState = prevState;
        Action = action;
        CurrentState = currentState;
        this.reward = reward;
    }

}
