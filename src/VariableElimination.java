import java.util.*;

public class VariableElimination {
    private BayesNet network;
    int numOfMultiplies; // we use it as a data member, to prevent passing it as a parameter from one method to another.

    public VariableElimination(BayesNet network) {
        this.network = network;
    }

    public String answer(String query) {
        int numOfAdds = 0;
        numOfMultiplies = 0;

        // Initialize necessary data structures
        ArrayList<String> givenNames = new ArrayList<>();
        ArrayList<String> givenValues = new ArrayList<>();
        ArrayList<String> varsToEliminate = new ArrayList<>();

        // Split the query to extract relevant information
        String[] queryParts = query.split("\\)\\s+|\\|");

        // Extract query variable and value
        String[] queryVariablePart = queryParts[0].substring(2).split("=");
        String queryName = queryVariablePart[0].trim();
        String queryValue = queryVariablePart[1].trim();

        // Parse the given evidence, if any
        if (queryParts.length > 1) {
            String evidencePart = queryParts[1].split("\\s+")[0];
            String[] evidences = evidencePart.split(",");
            for (String evidence : evidences) {
                String[] evidenceSplit = evidence.split("=");
                if (evidenceSplit.length == 2) {
                    givenNames.add(evidenceSplit[0].trim());
                    givenValues.add(evidenceSplit[1].trim());
                }
            }
        }

        // Parse variables to eliminate
        if (queryParts.length > 1 && queryParts[2].contains("-")) {
            String[] eliminationPart = queryParts[2].split("\\s+-\\s+");
            if (eliminationPart.length >= 1) {
                String[] toEliminate = eliminationPart[0].split("-");
                for (String var : toEliminate) {
                    varsToEliminate.add(var.trim());
                }
            }
        }

        // Copy CPTs from the network for manipulation
        ArrayList<Factor> factorVec = new ArrayList<>();
        ArrayList<Variable> relevantVars = new ArrayList<>();
        Map<String, Factor> cptMap = network.getCptMaps();

        // add the ancestors of the query and evidence variables as they are relevant.
        addRelevantVars(relevantVars, queryName);
        for (int i = 0; i < givenNames.size(); i++) {
            String givenName = givenNames.get(i);
            addRelevantVars(relevantVars, givenName);
        }

        // drop independent variables with the query variable
        dropIndependentVars(relevantVars, queryName, givenNames);

        for (Variable var : relevantVars) {
            Factor tempFactor = new Factor(cptMap.get(var.name));
            factorVec.add(tempFactor);
        }

        // go over evidences (e.g. M=T, J=T) and eliminate them from the CPTs
        ArrayList<Factor> factorsToAdd = new ArrayList<>();
        ArrayList<Factor> factorsToRemove = new ArrayList<>(); // factors to remove from the loop variable factorVec.
        for (int i = 0; i < givenNames.size(); i++) {
            String givenName = givenNames.get(i);
            String givenValue = givenValues.get(i);
            for (Factor factor : factorVec) {
                if (factor.getVariables().contains(givenName)) {
                    factorsToRemove.add(factor);
                    Factor newFactor = factor.removeEvidence(givenName, givenValue);
                    factorsToAdd.add(newFactor);
                    System.out.println("After removing evidence " + givenName + "=" + givenValue + " from the factor:\n");
                    System.out.println(newFactor + "\n");
                }
            }
        }
        factorVec.removeAll(factorsToRemove);
        factorVec.addAll(factorsToAdd);

        // Process each variable to eliminate
        while (!varsToEliminate.isEmpty()) {
            String varToEliminate = varsToEliminate.remove(0);
            // if the variable to eliminate is not in the relevant variables, skip it.
            if (factorVec.stream().noneMatch(factor -> factor.variables.contains(varToEliminate))) {
                continue;
            }

            // Collect relevant CPTs for joining
            ArrayList<Factor> factorsToProceed = new ArrayList<>();
            Iterator<Factor> iterator = factorVec.iterator();
            while (iterator.hasNext()) {
                Factor factor = iterator.next();
                // Check if the variable to eliminate is either the key variable or in the given list of the CPT
                if (factor.variables.contains(varToEliminate)) {
                    factorsToProceed.add(factor);
                    iterator.remove();
                }
            }

            sortFactors(factorsToProceed); // sort the factors from the smallest to the largest table size.
            factorVec.removeAll(factorsToProceed); // remove the factors as we don't need them any longer

            // Join the CPTs of the variable to eliminate
            Factor afterJoin = Join(factorsToProceed);

            // Eliminate the joined variable from the CPT
            Factor afterEliminate = afterJoin.Eliminate(varToEliminate);
            numOfAdds += afterEliminate.getTableSize(); // number of add operations performed during elimination.

            // Add the eliminated CPT back to the list
            factorVec.add(afterEliminate);

            factorsToProceed.clear();
        }

        // join the remaining factors
        Factor finalFactor = Join(factorVec);
        System.out.println("Before normalize: the final factor is:\n");
        System.out.println(finalFactor + "\n");
        // normalize the final factor
        numOfAdds += finalFactor.normalize();
        System.out.println("After normalize: the final factor is:\n");
        System.out.println(finalFactor + "\n");

        String roundedProb = String.format("%.5f", finalFactor.getProbability(queryName, queryValue));
        return String.format("%s,%d,%d",roundedProb, numOfAdds, numOfMultiplies);
    }

    private void dropIndependentVars(ArrayList<Variable> relevantVars, String queryName, ArrayList<String> evidenceNames) {
        BayesBall bayesBall = new BayesBall(network);
        ArrayList<Variable> varsToRemove = new ArrayList<>();
        for (Variable var : relevantVars) {
            if (bayesBall.isBayesBall(var.name, queryName, evidenceNames)) {
                varsToRemove.add(var);
            }
        }
        relevantVars.removeAll(varsToRemove);
    }

    private void addRelevantVars(ArrayList<Variable> relevantVars, String queryName) {
        if (relevantVars.stream().anyMatch(var -> var.name.equals(queryName))) {
            return;
        }
        Optional<Variable> queryVar = network.getVariables().stream().filter(var -> var.name.equals(queryName)).findFirst();
        if (queryVar.isPresent()) {
            relevantVars.add(queryVar.get());
            for (Variable parent : queryVar.get().parents) {
                addRelevantVars(relevantVars, parent.name);
            }
        }
    }

    public Factor Join(List<Factor> factors) {
        if (factors.isEmpty()) return new Factor();

        Factor newResult = new Factor(factors.getFirst());

        for (int i = 1; i < factors.size(); i++) {
            Factor factor = factors.get(i);

            System.out.println("Joining factors:\n");
            System.out.println(newResult);
            System.out.println("and\n");
            System.out.println(factor);
            newResult = newResult.JoinFactor(factor);
            numOfMultiplies += newResult.getNumOfMultiplies();
            System.out.println("The join result is:\n");
            System.out.println(newResult + "\n");
        }

        return newResult;
    }

    // function to sort factors by their table size: from the smallest to the largest
    private void sortFactors(ArrayList<Factor> factors) {
        factors.sort(new Comparator<Factor>() {
            @Override
            public int compare(Factor f1, Factor f2) {
                int result = Integer.compare(f1.getTableSize(), f2.getTableSize());
                if (result == 0) {
                    return Integer.compare(f1.getTotalVarsAsciiCodes(), f2.getTotalVarsAsciiCodes());
                } else {
                    return result;
                }
            }
        });
    }
}
