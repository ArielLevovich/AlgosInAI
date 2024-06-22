import java.util.*;

public class BayesBall {
    private final BayesNet network;

    public BayesBall(BayesNet network) {
        this.network = network;
    }

    public boolean isBayesBall(String startVar, String endVar, ArrayList<String> evidenceNames) {
        return isBayesBall(network.getVariable(startVar), network.getVariable(endVar), evidenceNames);
    }

    public boolean isBayesBall(Variable startVar, Variable endVar, ArrayList<String> evidenceNames) {
        ArrayList<Variable> visited=new ArrayList<>();

        // handle parents
        for(Variable v:startVar.parents) {
            for (Variable v2 : endVar.parents) {
                if (v == v2 && !evidenceNames.contains(v.name)) {
                    return false;
                }
            }
        }

        // handle children
        for(Variable v:startVar.children) {
            for (Variable v2 : endVar.children) {
                if (v == v2 && evidenceNames.contains(v.name)) {
                    return false;
                }
            }
        }

        return areIndependent(startVar, endVar, evidenceNames, false, visited);
    }

    private boolean areIndependent(Variable startNode, Variable endNode, ArrayList<String> evidenceNames, boolean cameFromChild, ArrayList<Variable> visited) {
        visited.add(startNode);

        if (startNode == endNode) {
            return false;
        }
        // if the variable is a part of the evidence, and we came from a child it means we got stuck.
        // so the variables are independent
        if (evidenceNames.contains(startNode.name) && cameFromChild) {
            return true;
        }

        // if we are a part of the evidence we search its parents.
        if (evidenceNames.contains(startNode.name)) {
            for (Variable parent : startNode.parents) {
                if (!areIndependent(parent, endNode, evidenceNames, true, visited)) {
                    return false;
                }
            }
        }

        // if we are not a part of the evidence, and we came from a child we can go either to a parent or a child
        else if (cameFromChild) {
            for (Variable child : startNode.children) {
                if (!visited.contains(child) && !areIndependent(child, endNode, evidenceNames, false, visited)) {
                    return false;
                }
            }

            for (Variable parent : startNode.parents) {
                if (!areIndependent(parent, endNode, evidenceNames, true, visited)) {
                    return false;
                }
            }
        }

        else {
            for (Variable child : startNode.children) {
                if (!visited.contains(child) && !areIndependent(child, endNode, evidenceNames, false, visited)) {
                    return false;
                }
            }
        }
        return true;
    }
}