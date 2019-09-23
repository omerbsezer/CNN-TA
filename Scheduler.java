package ml.cnn.paper;

import java.io.File;
import java.util.Scanner;


public class Scheduler {
	public static int CREATETESTFILE=0;
	public static int CALCULATE=1;
	
	public static int mode=CREATETESTFILE;
	public static String inputFilePathPhase1Training="resources3/INTC19972007.csv";
	public static String inputFilePathPhase1Training2="resources3/reverseFileTrainingFirst.csv";
	
	public static String inputFilePathPhase1Test="resources3/INTC20072012.csv";
	public static String inputFilePathPhase1Test2="resources3/reverseFileTestFirst.csv";
	//public static String inputFilePathPhase1Training="resources3/CAT20012011.csv";
	//public static String inputFilePathPhase1Test="resources3/CAT20112017.csv";
	public static String outputFileTrainingFirst="TrainingFirst";
	public static String outputFileTestFirst="TestFirst";
	public static String outputFileTraining="Training";
	public static String outputFileTest="Test";
	
	
	
	public static String filePathOutputOfPhase1Training;
	public static String filePathOutputOfPhase1TrainingCSV;
	public static String filePathOutputOfPhase1Test;
	public static String filePathOutputOfPhase1TestCSV;
	
	 public static void main(String[] args) throws Exception {
			 
			 System.out.println("Create File[1:Training-2:Test]: ");
			 Scanner sc = new Scanner(System.in);
			 int preferences = sc.nextInt();
			 if(preferences==1){
				 System.out.println("Phase0 Training");
				 Phase0.PhaseProcess(inputFilePathPhase1Training,outputFileTrainingFirst);
				 
				 System.out.println("Phase0 Training2");
				 Phase0.PhaseProcess(inputFilePathPhase1Training2,outputFileTraining);
				 
				 System.out.println("Phase1 Training");
				 Phase1.PhaseProcess(outputFileTraining);

				
				 
				 String directoryPath = "D:\\ThesisWorkspace\\SparkJava\\resources3\\outputTraining.csv";
				 File[] filesInDirectory = new File(directoryPath).listFiles();
				 for(File f : filesInDirectory){
					 filePathOutputOfPhase1Training = f.getAbsolutePath();
					 String fileExtenstion = filePathOutputOfPhase1Training.substring(filePathOutputOfPhase1Training.lastIndexOf(".") + 1,filePathOutputOfPhase1Training.length());
					 if("csv".equals(fileExtenstion)){
						 filePathOutputOfPhase1TrainingCSV=filePathOutputOfPhase1Training;
						 System.out.println("Phase2 Training");
						 Phase2.PhaseProcess(filePathOutputOfPhase1TrainingCSV,outputFileTraining);
						 // Call the method checkForCobalt(filePath);
					 }
				 }    
				 
			 }else if(preferences==2){
				 System.out.println("Phase0 Test");
				 Phase0.PhaseProcess(inputFilePathPhase1Test,outputFileTestFirst);
				 
				 System.out.println("Phase0 Test");
				 Phase0.PhaseProcess(inputFilePathPhase1Test2,outputFileTest);
				 
				 System.out.println("Phase1 Test");
				 Phase1.PhaseProcess(outputFileTest);

				 String directoryPathTest = "D:\\ThesisWorkspace\\SparkJava\\resources3\\outputTest.csv";
				 File[] filesInDirectoryTest = new File(directoryPathTest).listFiles();
				 for(File f : filesInDirectoryTest){
					 filePathOutputOfPhase1Test = f.getAbsolutePath();
					 String fileExtenstionTest = filePathOutputOfPhase1Test.substring(filePathOutputOfPhase1Test.lastIndexOf(".") + 1,filePathOutputOfPhase1Test.length());
					 if("csv".equals(fileExtenstionTest)){
						 filePathOutputOfPhase1TestCSV=filePathOutputOfPhase1Test;
						 System.out.println("Phase2 Test");
						 Phase2.PhaseProcess(filePathOutputOfPhase1TestCSV,outputFileTest);
						 // Call the method checkForCobalt(filePath);
					 }
				 } 
			 }
		 
	}

}
