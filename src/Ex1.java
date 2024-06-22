import java.io.IOException;

public class Ex1 {
    public static void main(String[] args) {
        try {
            // args[1] should be the input text file if specified.
            String inputFile = args.length > 0 ? args[0] : "input.txt";
            BayesQueryHandler bayesQueryHandler = new BayesQueryHandler(inputFile);

            bayesQueryHandler.handleBayesianBallQueries();
            bayesQueryHandler.handleVariableEliminationQueries();
            bayesQueryHandler.writeOutput();
        } catch (IOException ex) {
            System.out.println("An error occurred while processing the queries. " + ex.getMessage());
        }
    }
}
