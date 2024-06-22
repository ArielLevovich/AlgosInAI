import java.util.*;

public class Factor {
    public List<String> given = new ArrayList<>();
    public List<String> variables; // List of variable names in the factor
    public Map<LinkedHashMap<String, String>, Double> table; // Table mapping variable assignments to probabilities
    private int numOfAdds;
    private int numOfMultiplies;

    public Factor(Factor factor) {
        this.given = new ArrayList<>(factor.given);
        this.variables = new ArrayList<>(factor.variables);
        this.table = new HashMap<>(factor.table);
        numOfMultiplies = 0;
    }

    public Factor(List<String> variables) {
        this.given = new ArrayList<>();
        this.variables = new ArrayList<>(variables);
        this.table = new HashMap<>();
        numOfMultiplies = 0;
    }

    public Factor() {
        numOfMultiplies = 0;
    }

    void setProbability(LinkedHashMap<String, String> assignment, double probability) {
        table.put(new LinkedHashMap<>(assignment), probability);
    }

    double getProbability(LinkedHashMap<String, String> assignment) {
        return table.getOrDefault(assignment, 0.0);
    }

    public List<String> getVariables() {
        return variables;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Factor factor = (Factor) obj;
        return Objects.equals(table, factor.table) &&
                Objects.equals(given, factor.given) &&
                Objects.equals(variables, factor.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, given, variables);
    }

    @Override
    public String toString() {
        if (table.isEmpty()) {
            return "Factor is empty.";
        }
        // print in the first line the variables separated by 5 whitespaces and the word Probability
        // each following line should contain the combination of boolean values (true or false) separated by 5 whitespaces and the corresponding double value.
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("    ", variables)).append("    Probability\n");
        for (Map.Entry<LinkedHashMap<String, String>, Double> entry : table.entrySet()) {
            LinkedHashMap<String, String> assignment = entry.getKey();

            String assignmentStr = assignment.entrySet().stream()
                    .map(e -> e.getValue().equals("true") ? "T    " : "F    ")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            String roundedProb = String.format("%.5f", entry.getValue());
            sb.append(String.join("", assignmentStr)).append(roundedProb).append("\n");
        }
        return sb.toString();
    }

    // Normalize the factor
    public double getTotalProbability(Factor factor) {
        double total = 0.0;

        for (double probability : factor.table.values()) {
            total += probability;
            this.numOfAdds++;
        }

        return total;
    }

    // Join operation between two factors
    public Factor JoinFactor(Factor f2) {
        Factor f1 = this;

        // Identify common and all variables
        List<String> commonVars = new ArrayList<>(f1.variables);
        commonVars.retainAll(f2.variables);

        List<String> allVars = new ArrayList<>(f1.variables);
        for (String var : f2.variables) {
            if (!allVars.contains(var)) {
                allVars.add(var);
            }
        }

        // handle a special case when one of the factor has only one value, e.g. B=T
        if (f1.table.size() == 1) {
            allVars.remove(f1.variables.getFirst());
        } else if (f2.table.size() == 1) {
            allVars.remove(f2.variables.getFirst());
        }
        // Create the resulting factor
        Factor result = new Factor(allVars);
        result.table = new HashMap<>();
        int numOfMultiplies = 0;

        // Perform the join
        for (LinkedHashMap<String, String> assignment1 : f1.table.keySet()) {
            for (LinkedHashMap<String, String> assignment2 : f2.table.keySet()) {
                if (consistent(assignment1, assignment2, commonVars)) {
                    // If one of the factor has only one value, we take the assignment from the other factor
                    LinkedHashMap<String, String> combinedAssignment;
                    if (f1.table.size() == 1) {
                        combinedAssignment = new LinkedHashMap<>(assignment2);
                    } else if (f2.table.size() == 1) {
                        combinedAssignment = new LinkedHashMap<>(assignment1);
                    } else {
                        // Combine the assignments
                        combinedAssignment = new LinkedHashMap<>(assignment1);
                        combinedAssignment.putAll(assignment2);
                    }

                    double prob1 = f1.getProbability(assignment1);
                    double prob2 = f2.getProbability(assignment2);
                    double combinedProbability = prob1 * prob2;
                    numOfMultiplies++;
                    result.setProbability(combinedAssignment, combinedProbability);
                }
            }
        }
        result.setNumOfMultiplies(numOfMultiplies);
        return result;
    }

    // Check if assignments are consistent over common variables
    private boolean consistent(LinkedHashMap<String, String> a1, LinkedHashMap<String, String> a2, List<String> commonVars) {
        for (String var : commonVars) {
            if (a1.containsKey(var) && a2.containsKey(var) && !a1.get(var).equals(a2.get(var))) {
                return false;
            }
        }
        return true;
    }

    // Eliminate a variable by summing out
    public Factor Eliminate(String variable) {
        // Check if cpt or cpt.variables is null
        if (this.variables == null) {
            throw new IllegalArgumentException("The CPT or its variables cannot be null.");
        }

        System.out.println("Eliminate variable " + variable + " from the factor:\n");
        System.out.println(this);
        // Determine the variables of the new factor
        ArrayList<String> newVariables = new ArrayList<>(this.variables);
        newVariables.remove(variable);

        // Create the resulting factor
        Factor result = new Factor(newVariables);

        // Create a map to accumulate sums for each reduced assignment
        Map<LinkedHashMap<String, String>, Double> sumMap = new HashMap<>();

        // Perform the summing out
        for (LinkedHashMap<String, String> assignment : this.table.keySet()) {
            LinkedHashMap<String, String> reducedAssignment = new LinkedHashMap<>(assignment);
            reducedAssignment.remove(variable);

            double currentProbability = sumMap.getOrDefault(reducedAssignment, 0.0);
            sumMap.put(reducedAssignment, currentProbability + this.getProbability(assignment));
        }

        // Set probabilities for the resulting factor
        for (Map.Entry<LinkedHashMap<String, String>, Double> entry : sumMap.entrySet()) {
            result.setProbability(entry.getKey(), entry.getValue());
        }

        System.out.println("The elimination result is:\n");
        System.out.println(result + "\n");
        return result;
    }

    public Factor removeEvidence(String givenName, String givenValue) {
        // Create the resulting factor
        Factor resultFactor = new Factor(this.variables);
        // remove the column "givenName" from the list of variables, if there are more than one variable.
        if (this.variables.size() > 1) {
            resultFactor.variables.remove(givenName);
        }

        // create a key of the map "table" to remove
        LinkedHashMap<String, String> queryAssignment = new LinkedHashMap<>();
        String queryVar = givenValue.equals("T") ? "true" : "false";
        queryAssignment.put(givenName, queryVar);

        for (LinkedHashMap<String, String> assignment : this.table.keySet()) {
            if (assignment.entrySet().containsAll(queryAssignment.entrySet())) {
                LinkedHashMap<String, String> newKey = new LinkedHashMap<>();
                for (String key : assignment.keySet()) {
                    if (!key.equals(givenName) || this.variables.size() == 1) {
                        newKey.put(key, assignment.get(key));
                    }
                }
                resultFactor.table.put(newKey, this.table.get(assignment));
            }
        }

        return resultFactor;
    }

    public int getTableSize(){
        return table.size();
    }

    public int normalize() { // return the number of add operations.
        this.numOfAdds = 0;
        double total = getTotalProbability(this);

        for (Map.Entry<LinkedHashMap<String, String>, Double> entry : table.entrySet()) {
            entry.setValue(entry.getValue() / total);
        }
        return this.numOfAdds-1;
    }

    void populateTable(List<Double> tableValues) {
        generateCombinations(tableValues, new ArrayList<>(), 0);
    }

    void generateCombinations(List<Double> tableValues, List<String> current, int index) {
        if (index == this.variables.size()) {
            // We have a complete combination
            LinkedHashMap<String, String> assignment = new LinkedHashMap<>();
            for (int i = 0; i < this.variables.size(); i++) {
                assignment.put(this.variables.get(i), current.get(i));
            }
            this.setProbability(assignment, tableValues.get(this.table.size()));
        } else {
            // Recursively generate combinations for the remaining variables
            current.add("true");
            generateCombinations(tableValues, current, index + 1);
            current.set(index, "false");
            generateCombinations(tableValues, current, index + 1);
            current.remove(index);
        }
    }

    public double getProbability(String givenName, String givenValue) {
        LinkedHashMap<String, String> assignment = new LinkedHashMap<>();
        String queryVar = givenValue.equals("T") ? "true" : "false";
        assignment.put(givenName, queryVar);

        return getProbability(assignment);
    }

    public int getTotalVarsAsciiCodes() {
        // calculate the ascii code value of all the variables in the factor and return.
        int total = 0;
        for (String var : this.variables) {
            for (char c : var.toCharArray()) {
                total += c;
            }
        }
        return total;
    }

    public int getNumOfMultiplies() {
        return numOfMultiplies;
    }

    public void setNumOfMultiplies(int numOfMultiplies) {
        this.numOfMultiplies = numOfMultiplies;
    }
}
