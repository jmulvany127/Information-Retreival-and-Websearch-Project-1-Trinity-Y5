package eval;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Eval{
    //builds command to call trec_eval and pipe output to text file
    public static void trecEval(String trecEvalPath, String qrelFilePath, String searchResultsPath, String trecEvalResultsPath) throws IOException, InterruptedException{
    
        // Build the command adn set output destination
        ProcessBuilder processBuilder = new ProcessBuilder(trecEvalPath, "-q", qrelFilePath, searchResultsPath);
        processBuilder.redirectOutput(new File(trecEvalResultsPath));

        // Start the process, wait and handle errors
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("Trec_eval encountered an error. Exit code: " + exitCode);
        }

    }
    
    //takes results of trec_eval and extracts relvant statistics for project
    public static void parseResults(String trecEvalResultsPath, String[] searcherType){
        
        //declare variables for stats
        String mapValue = null;
        String gmMapValue = null;
        String P_5Value = null;
        String num_relValue= null;       	
        String num_rel_retValue = null;       
        double recall;   	
        
        //read each line of file and assign values to stat variables
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(trecEvalResultsPath))) {
            String line;
            String[] parts;

            
            while ((line = bufferedReader.readLine()) != null) {
                parts = line.trim().split("\\s+"); // Split by whitespace
                
                // checks for all in cnetral column then for the right stat type in the first column
                if (parts[1].equals("all")) { 
                    if (parts[0].equals("map")) {
                        mapValue = parts[2]; 
                    } else if (parts[0].equals("gm_map")) {
                        gmMapValue = parts[2]; 
                    } else if (parts[0].equals("P_5")) {
                        P_5Value = parts[2]; 
                    } else if (parts[0].equals("num_rel")) {
                        num_relValue = parts[2]; 
                    } else if (parts[0].equals("num_rel_ret")) {
                        num_rel_retValue = parts[2]; 
                    }
                }
            }
            
            //calculate recall as number of relevant docs retireved over number of relvant docs in cran q rel
            recall = Double.parseDouble(num_rel_retValue)/ Double.parseDouble(num_relValue);
            String formattedRecall = String.format("%.4f", recall);
            
            //print gathered statistics
            System.out.println("Results for similarity scoring: " + searcherType[0]+ ", and analyzer: " + searcherType[1]+ ":");
            System.out.println("map: " +  mapValue);
            System.out.println("gm_map: " + gmMapValue);
            System.out.println("P_5: " + P_5Value);
            System.out.println("Recall: " + formattedRecall +"\n");

        } catch (IOException e) {
            System.err.println("Error reading the results file: " + e.getMessage());
        }
    }

}