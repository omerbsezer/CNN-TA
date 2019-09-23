package ml.cnn.paper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.input.ReversedLinesFileReader;

public class Phase0 {
	
	public static String outputFilePath;
	
	

	public static void PhaseProcess(String inputFilePath, String outputFile) throws Exception{
		//String strpath="resources2/APPL19972007.csv";
		//String strpath="resources2/APPL20072017.csv";
		String strpath=inputFilePath;
		
		ReversedLinesFileReader fr = new ReversedLinesFileReader(new File(strpath));
		String ch;
		int time=0;
		String Conversion="";

	
		PrintWriter writer = new PrintWriter("resources3/reverseFile"+outputFile+".csv", "UTF-8");

		do {
			ch = fr.readLine();
			System.out.println(ch);
			if(ch != null)
				writer.println(ch);

		} while (ch != null);
		writer.close();
		fr.close();
		
	}

	public static void main(String[] args) throws Exception  {
		String strpath="resources3/AXP19972007.csv";
		//String strpath="resources3/CAT20072017.csv";

		ReversedLinesFileReader fr = new ReversedLinesFileReader(new File(strpath));
		String ch;
		int time=0;
		String Conversion="";

		PrintWriter writer = new PrintWriter("resources3/reverseFile.csv", "UTF-8");

		do {
			ch = fr.readLine();
			System.out.println(ch);
			if(ch != null)
				writer.println(ch);

		} while (ch != null);
		writer.close();
		fr.close();
	}
}
