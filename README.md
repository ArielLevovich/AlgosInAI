# Bayesian Network

## Files Overview

- **Ex1.java**: Main class calling the methods of the class BayesQueryHandler.

- **BayesQueryHandler**: class for processing Bayesian network queries getting the input xml file, input and output text files.

- **BayesBall.java**: Implements Bayesian Ball algorithm to test variable independence in Bayesian networks.

- **BayesNet.java**: Defines structure for capturing variable definitions and conditional probability tables in Bayesian networks.

- **Variables.java**: Defines the Variable class for Bayesian network nodes
  
- **Factor.java**: Manages factors in probability graph models, supporting operations like factor join, variable removal and more.
  
- **VariableElimination.java**: Implements variable elimination algorithm for Bayesian network inference.

- **input.txt**: Input file containing queries and evidence configurations.
  
- **output.txt**: Output file where results of Bayesian network queries are written.
  
- **alarm_net.xml**: Example XML representation of a Bayesian network structure and probabilities.

## Usage

**Setup and Execution:**

1. Ensure Java is installed on your system.
2. Compile the code:
   ```bash
   javac Ex1.java BayesBall.java BayesNet.java BayesQueryHandler.java Factor.java Variable.java VariableElimination.java 
   ```
3. Run the program:
   ```bash
   java Ex1
   ```
Note: alarm_net.xml, input.txt and output.txt files should be placed in the same directory as the java class files.

**Input Format:**

Modify `input.txt` to include queries:
- For Variable Elimination: `P(variables | evidence)`
- For Bayesian Ball: `B-variable1-variable2 | evidence`
- Example for input file:
  ```bash
  alarm_net.xml
  B-E|
  B-E|J=T
  P(B=T|J=T,M=T) A-E
  P(B=T|J=T,M=T) E-A
  P(J=T|B=T) A-E-M
  P(J=T|B=T) M-E-A
  ```

**Output:**

Results are written to `output.txt`, with each line corresponding to `yes` or `no` for each query.
- Example for output file:
  ```bash
  yes
  no
  0.28417,7,16
  0.28417,7,16
  0.84902,7,12
  0.84902,5,8
  ```

  **XML FILE:**:
  The main will take the details(Variables,Probabilitis and Connection between variables) from the XML file.
  - Example for XML file:
  ```bash
  <NETWORK>
  	<VARIABLE>
  		<NAME>E</NAME>
  		<OUTCOME>T</OUTCOME>
  		<OUTCOME>F</OUTCOME>
  	</VARIABLE>
  
  	<VARIABLE>
  		<NAME>B</NAME>
  		<OUTCOME>T</OUTCOME>
  		<OUTCOME>F</OUTCOME>
  	</VARIABLE>
  
  	<VARIABLE>
  		<NAME>A</NAME>
  		<OUTCOME>T</OUTCOME>
  		<OUTCOME>F</OUTCOME>
  	</VARIABLE>
  
  	<VARIABLE>
  		<NAME>J</NAME>
  		<OUTCOME>T</OUTCOME>
  		<OUTCOME>F</OUTCOME>
  	</VARIABLE>
  
  	<VARIABLE>
  		<NAME>M</NAME>
  		<OUTCOME>T</OUTCOME>
  		<OUTCOME>F</OUTCOME>
  	</VARIABLE>
  
  	<DEFINITION>
  		<FOR>E</FOR>
  		<TABLE>0.002 0.998</TABLE>
  	</DEFINITION>
  
  	<DEFINITION>
  		<FOR>B</FOR>
  		<TABLE>0.001 0.999</TABLE>
  	</DEFINITION>
  
  	<DEFINITION>
  		<FOR>A</FOR>
  		<GIVEN>E</GIVEN>
  		<GIVEN>B</GIVEN>
  		<TABLE>0.95 0.05 0.29 0.71 0.94 0.06 0.001 0.999</TABLE>
  	</DEFINITION>
  
  	<DEFINITION>
  		<FOR>J</FOR>
  		<GIVEN>A</GIVEN>
  		<TABLE>0.9 0.1 0.05 0.95</TABLE>
  	</DEFINITION>
  
  	<DEFINITION>
  		<FOR>M</FOR>
  		<GIVEN>A</GIVEN>
  		<TABLE>0.7 0.3 0.01 0.99</TABLE>
  	</DEFINITION>
  </NETWORK>
  ```

## Example Queries

**Variable Elimination:**

```plaintext
P(B=T | A=F)
```
Computes probability of B=true given evidence A=false.

**Bayesian Ball:**

```plaintext
B-E | J=T,K=F
```
Checks independence between B and E given evidence J=true and K=false.

## Dependencies

Standard Java libraries (`java.io.*`, `java.util.*`, `javax.xml.parsers.*`, `org.w3c.dom.*`) are used for file operations, data structures, and XML parsing.

## Notes

- Ensure `input.txt` and `output.txt` follow specified query formats.
- Bayesian network structure and probabilities are read from XML format (`alarm_net.xml`).

## Contributors

- Developed by Ariel Levovich.
- Contact: levoariel@gmail.com.
