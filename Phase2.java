package ml.cnn.paper;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.util.Precision;

public class Phase2 {
	public static int CREATEBOOSTEDTRAINDATA=0;
	public static int numberOfZero,numberOfOne,numberOfTwo;
	public static int lineCount, formula;
	public static String[][] secondData;
	public static String[][] firstData;
	
	public static void PhaseProcess(String inputFilePath, String outputFile) throws Exception  {

		//String fName = "resources3/outputOfDf.csv";
		String fName=inputFilePath;
		String thisLine; 
		int count=0; 
		FileInputStream fis = new FileInputStream(fName);
		DataInputStream myInput = new DataInputStream(fis);
		int i=0; 


		String[][] data = new String[0][];//csv data line count=0 initially
		while ((thisLine = myInput.readLine()) != null) {
			++i;//increment the line count when new line found

			String[][] newdata = new String[i][2];//create new array for data

			String strar[] = thisLine.split(";");//get contents of line as an array
			newdata[i - 1] = strar;//add new line to the array

			System.arraycopy(data, 0, newdata, 0, i - 1);//copy previously read values to new array
			data = newdata;//set new array as csv data
		}

		for(int j=0;j<6;j++){ //call shiftUp windowSize/2
			shiftUp(0,data);
		}

		//relabel(data);
		
		converToCsv(data,outputFile);
	
		
	}
	
	public static void main(String[] args) throws Exception  {

		String fName = "resources3/outputOfDf.csv";
		String thisLine; 
		int count=0; 
		FileInputStream fis = new FileInputStream(fName);
		DataInputStream myInput = new DataInputStream(fis);
		int i=0; 
		String outputFile="Test";


		String[][] data = new String[0][];//csv data line count=0 initially
		while ((thisLine = myInput.readLine()) != null) {
			++i;//increment the line count when new line found

			String[][] newdata = new String[i][2];//create new array for data

			String strar[] = thisLine.split(";");//get contents of line as an array
			newdata[i - 1] = strar;//add new line to the array

			System.arraycopy(data, 0, newdata, 0, i - 1);//copy previously read values to new array
			data = newdata;//set new array as csv data
		}

		for(int j=0;j<6;j++){ //call shiftUp windowSize/2
			shiftUp(0,data);
		}
		
		//relabel(data);
		
		
		converToCsv(data,outputFile);
	
		
	}
	
	//relabel data
	public static void relabel(String[][] array) {
		int m = array.length;
		int k=0;
		while(k<m-1){
			
		//for (int k=0; k<m-1; k++){
			if(array[k][0].equals("2")){
				
				//System.out.println("into 0");
				for (int i=1; i<5; i++){
					if(array[k-i][0].equals("0") && array[k-i-1][0].equals("0")){
						array[k-i][0]="2";
					}
					if(array[k+i][0].equals("0") && array[k+i+1][0].equals("0")){
						array[k+i][0]="2";
					}	
				}
				k=k+4;
			}
			if(array[k][0].equals("1")){
				for (int i=1; i<5; i++){
					if(array[k-i][0].equals("0") && array[k-i-1][0].equals("0")){
						array[k-i][0]="1";
					}
					if(array[k+i][0].equals("0") && array[k+i+1][0].equals("0")){
						array[k+i][0]="1";
					}	
				}
				k=k+4;
			}
			k++;
		}
		//array[m-1][0] = "--";
	}
		
	//shift up labelled data
	public static void shiftUp(int i, String[][] array) {
		int m = array.length;

		for (int k=0; k<m-1; k++){
			array[k][0] = array[k+1][0];
		}
		//array[m-1][0] = "--";
	}

	public static void converToCsv(String[][] board, String outputFile) {

		StringBuilder builder = new StringBuilder();
		for(int n = 0; n < board.length; n++)//for each row
		{
			for(int j = 0; j < board[0].length; j++)//for each column
			{
				builder.append(board[n][j]+"");//append to the output string
				if(j < board.length - 1)//if this is not the last row element
					builder.append(";");//then add comma (if you don't like commas you can use spaces)
			}
			builder.append("\n");//append new line at the end of the row
		}
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("resources3/outputOfPhase2"+outputFile+".csv"));
			writer.write(builder.toString());//save the string representation of the board
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
