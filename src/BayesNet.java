import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BayesNet {
    public Map<String, ArrayList<String>> variables = new HashMap<>();
    public Map<String, Factor> cpts = new HashMap<>();
    private Map<String, Variable> variableMap = new HashMap<>();

    // Method to build graph from CPTs
    public Map<String, ArrayList<String>> buildGraph() {
        Map<String, ArrayList<String>> graph = new HashMap<>();
        for (Map.Entry<String, Factor> entry : cpts.entrySet()) {
            for (String given : entry.getValue().given) {
                graph.computeIfAbsent(given, k -> new ArrayList<>()).add(entry.getKey());
            }
        }
        return graph;
    }

    // Method to build parents from CPTs
    public Map<String, ArrayList<String>> buildParents() {
        Map<String, ArrayList<String>> parents = new HashMap<>();
        for (Map.Entry<String, Factor> entry : cpts.entrySet()) {
            for (String given : entry.getValue().given) {
                parents.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(given);
            }
        }
        return parents;
    }

    public void initialize() {
        Map<String, ArrayList<String>> graph = buildGraph();
        Map<String, ArrayList<String>> parents = buildParents();
        buildVariableMap(graph, parents);
    }

    private void buildVariableMap(Map<String, ArrayList<String>> graph, Map<String, ArrayList<String>> parents) {
        // Create Variable objects and populate variableMap with them
        for (String var : variables.keySet()) {
            Variable variable = new Variable(var, variables.get(var));
            variableMap.put(var, variable);
        }

        // Establish parent-child relationships based on graph and parents mappings
        for (String vertex : graph.keySet()) {
            Variable vertexVar = variableMap.get(vertex);
            for (String child : graph.get(vertex)) {
                Variable childVar = variableMap.get(child);
                vertexVar.children.add(childVar);   // childVar has parentVar as parent
                childVar.parents.add(vertexVar);  // parentVar has childVar as child
            }
        }
    }

    public Variable getVariable(String name) {
        return variableMap.get(name);
    }

    public Collection<Variable> getVariables() {
        return variableMap.values();
    }

    public Map<String, Factor> getCptMaps() {
        return this.cpts;
    }
}


