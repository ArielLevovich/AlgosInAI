import java.util.ArrayList;

// Variable class to represent a node in the Bayesian Network
public class Variable {
    String name;
    int numberOfOutcomes;

    ArrayList<Variable> parents;
    ArrayList<Variable> children;
    ArrayList<String> outcomes;

    public Variable(String name, ArrayList<String> outcomes) {
        this.name = name;
        this.outcomes = new ArrayList<>(outcomes);
        parents = new ArrayList<>();
        children = new ArrayList<>();
        numberOfOutcomes = outcomes.size();
    }
}
