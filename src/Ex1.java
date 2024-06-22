import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Ex1 {
    public static void main(String[] args) {
        try {
            BayesQueryHandler bayesQueryHandler = new BayesQueryHandler("alarm_net.xml", "input.txt", "output.txt");

            bayesQueryHandler.handleBayesianBallQueries();
            bayesQueryHandler.handleVariableEliminationQueries();
            bayesQueryHandler.writeOutput();
        } catch (IOException ex) {
            System.out.println("An error occurred while processing the queries. " + ex.getMessage());
        }
    }
}
