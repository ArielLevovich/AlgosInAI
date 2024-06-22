import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BayesQueryHandler {
    private final BayesNet network;
    private final List<String> bayesianBallQueries;
    private final List<String> variableEliminationQueries;
    private final FileWriter fileWriter;

    public BayesQueryHandler(String inputFile) throws IOException, RuntimeException {
        this.bayesianBallQueries = new ArrayList<>();
        this.variableEliminationQueries = new ArrayList<>();
        String xmlFilePath = getXmlFilePath(inputFile);
        this.network = parseNetworkFromXML(xmlFilePath);
        this.network.initialize();
        parseInputFile(inputFile);
        fileWriter = new FileWriter("output.txt");
    }

    private String getXmlFilePath(String inputFile) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(inputFile));
        return file.readLine(); // return the first line
    }

    public BayesNet getNetwork() {
        return network;
    }

    private BayesNet parseNetworkFromXML(String xmlFilePath) throws RuntimeException {
        BayesNet network = new BayesNet();
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Parse variables
            NodeList variableList = doc.getElementsByTagName("VARIABLE");
            for (int i = 0; i < variableList.getLength(); i++) {
                Element variableElement = (Element) variableList.item(i);
                String name = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
                NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
                ArrayList<String> outcomes = new ArrayList<>();
                for (int j = 0; j < outcomeList.getLength(); j++) {
                    outcomes.add(outcomeList.item(j).getTextContent());
                }
                Variable variable = new Variable(name, outcomes);
                network.variables.add(variable);
            }

            // Parse definitions (CPTs)
            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            for (int i = 0; i < definitionList.getLength(); i++) {
                Element definitionElement = (Element) definitionList.item(i);
                String forVar = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
                List<String> givenVarNames = new ArrayList<>();
                List<Variable> givenVars = new ArrayList<>();
                for (int j = 0; j < givenList.getLength(); j++) {
                    String varName = givenList.item(j).getTextContent();
                    givenVarNames.add(varName);
                    Optional<Variable> varNameObj = network.variables.stream().filter(v -> v.name.equals(varName)).findFirst();
                    if (varNameObj.isEmpty()) {
                        throw new Exception("Variable not found: " + forVar);
                    }
                    givenVars.add(varNameObj.get());
                }
                String tableStr = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent();
                List<Double> tableValues = new ArrayList<>();
                for (String value : tableStr.split("\\s+")) {
                    tableValues.add(Double.parseDouble(value));
                }
                Factor factor = new Factor();
                factor.given = givenVarNames;
                factor.table = new HashMap<>();

                Optional<Variable> forVarObj = network.variables.stream().filter(v -> v.name.equals(forVar)).findFirst();;
                if (forVarObj.isEmpty()) {
                    throw new Exception("Variable not found: " + forVar);
                }
                if (givenVars.isEmpty()) {
                    factor.variables = new ArrayList<>();
                } else {
                    factor.variables = new ArrayList<>(givenVars);
                }
                factor.variables.addLast(forVarObj.get());

                factor.populateTable(tableValues);

                network.cpts.put(forVar, factor);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return network;
    }

    // Method to parse input.txt
    private void parseInputFile(String inputFilePath) throws RuntimeException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("|") && line.contains("P(")) {
                    this.variableEliminationQueries.add(line);
                } else if (line.contains("|") && !line.contains("P(")) {
                    this.bayesianBallQueries.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Method to handle Bayesian Ball queries
    public void handleBayesianBallQueries() throws IOException {
        BayesBall bayesBall = new BayesBall(network);

        System.out.println("Handling Bayesian Ball Queries:");
        for (String query : bayesianBallQueries) {
            // Parse the query
            String[] parts = query.split("\\|");
            String[] ab = parts[0].split("-");
            String nodeA = ab[0];
            String nodeB = ab[1];

            ArrayList<String> evidenceNames = new ArrayList<>();
            if (parts.length > 1) {
                String[] keyValuePairs = parts[1].split(",");
                for (String pair : keyValuePairs) {
                    String[] ev = pair.split("=");
                    if (ev.length == 2) { // Ensure it's a valid key-value pair
                        evidenceNames.add(ev[0]);
                    } else {
                        // Handle invalid input if necessary
                        System.err.println("Invalid key-value pair: " + pair);
                    }
                }
            }

            boolean independent = bayesBall.isBayesBall(nodeA, nodeB, evidenceNames) && bayesBall.isBayesBall(nodeB, nodeA, evidenceNames);
            System.out.println(nodeA + " and " + nodeB + " are " + (independent ? "independent" : "dependent") + " given " + evidenceNames);
            if (independent) {
                fileWriter.write("yes\n");
            } else {
                fileWriter.write("no\n");
            }
        }
    }

    public void handleVariableEliminationQueries() throws IOException {
        VariableElimination variableElimination = new VariableElimination(network);

        System.out.println("Handling Variable Elimination Queries:");
        for (String query : variableEliminationQueries) {
            String result = variableElimination.answer(query);
            System.out.println(query + " => " + result);
            fileWriter.write(result + "\n");
        }
    }

    public void writeOutput() throws IOException {
        fileWriter.close();
    }
}
