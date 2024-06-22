import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import org.xml.sax.SAXException;

public class BayesQueryHandler {
    private BayesNet network;
    private List<String> bayesianBallQueries;
    private List<String> variableEliminationQueries;
    private FileWriter fileWriter;

    public BayesQueryHandler(String inputFile) throws IOException {
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

    private BayesNet parseNetworkFromXML(String xmlFilePath) {
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
                network.variables.put(name, outcomes);
            }

            // Parse definitions (CPTs)
            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            for (int i = 0; i < definitionList.getLength(); i++) {
                Element definitionElement = (Element) definitionList.item(i);
                String forVar = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
                List<String> givenVars = new ArrayList<>();
                for (int j = 0; j < givenList.getLength(); j++) {
                    givenVars.add(givenList.item(j).getTextContent());
                }
                String tableStr = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent();
                List<Double> tableValues = new ArrayList<>();
                for (String value : tableStr.split("\\s+")) {
                    tableValues.add(Double.parseDouble(value));
                }
                Factor factor = new Factor();
                factor.given = givenVars;
                factor.table = new HashMap<>();
                factor.variables = new ArrayList<>(givenVars);
                factor.variables.addLast(forVar);
                factor.populateTable(tableValues);

                network.cpts.put(forVar, factor);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return network;
    }

    // Method to parse input.txt
    private void parseInputFile(String inputFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("|") && line.contains("P(")) {
                    this.variableEliminationQueries.add(line);
                } else if (line.contains("|") && !line.contains("P(")) {
                    this.bayesianBallQueries.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters for queries
    public List<String> getBayesianBallQueries() {
        return bayesianBallQueries;
    }

    public List<String> getVariableEliminationQueries() {
        return variableEliminationQueries;
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
