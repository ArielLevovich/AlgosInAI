import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BayesNet {
    public ArrayList<Variable> variables;
    public Map<String, Factor> cpts;
    private final Map<String, Variable> variableMap;

    public BayesNet() {
        variables = new ArrayList<>();
        cpts = new HashMap<>();
        variableMap = new HashMap<>();
    }

    // Method to build graph from CPTs
    public Map<String, ArrayList<String>> buildGraph() {
        Map<String, ArrayList<String>> graph = new HashMap<>();
        for (Map.Entry<String, Factor> entry : cpts.entrySet()) {
            for (String given : entry.getValue().given) {
                graph.computeIfAbsent(given, _ -> new ArrayList<>()).add(entry.getKey());
            }
        }
        return graph;
    }

    public void initialize() {
        Map<String, ArrayList<String>> graph = buildGraph();
        buildVariableMap(graph);
    }

    private void buildVariableMap(Map<String, ArrayList<String>> graph) {
        // Create Variable objects and populate variableMap with them
        for (Variable var : variables) {
            // TODO: should we deep clone "var" ?
            variableMap.put(var.name, var);
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


