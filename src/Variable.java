import java.util.ArrayList;

// Variable class to represent a node in the Bayesian Network
public class Variable {
    String name;

    public ArrayList<Variable> getParents() {
        return parents;
    }

    ArrayList<Variable> parents = new ArrayList<>();
    ArrayList<Variable> children = new ArrayList<>();

    public Variable(String name, ArrayList<String> outcomes) {
        this.name = name;
    }
}
