package ml.cnn.paper;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Precision;

public class Phase5 {
	
	
	
	public static void main(String[] args) throws Exception  {
		StringBuilder builder = new StringBuilder();
		String fName = "resources3/cnn_result.csv";
		String thisLine; 
		int count=0; 
		FileInputStream fis = new FileInputStream(fName);
		DataInputStream myInput = new DataInputStream(fis);
		int i=0, totalTransactionLength=0; 
		Double buyPoint,sellPoint,gain=0.0,totalGain=0.0, money=10000.0, shareNumber=0.0, moneyTemp=0.0, maximumMoney=0.0, minimumMoney=10000.0,maximumGain=0.0, maximumLost=100.0, totalPercentProfit=0.0;
		int transactionCount=0, successTransactionCount=0,failedTransactionCount=0;
		Double buyPointBAH,shareNumberBAH,moneyBAH=10000.0, maximumProfitPercent=0.0, maximumLostPercent=0.0;
		Boolean forceSell=false;

		ArrayList<Double> dailyProfitList = new ArrayList<Double>();
		double[] dailyProfit;

		String[][] data = new String[0][];//csv data line count=0 initially
		while ((thisLine = myInput.readLine()) != null) {
			++i;//increment the line count when new line found

			String[][] newdata = new String[i][2];//create new array for data

			String strar[] = thisLine.split(";");//get contents of line as an array
			newdata[i - 1] = strar;//add new line to the array

			System.arraycopy(data, 0, newdata, 0, i - 1);//copy previously read values to new array
			data = newdata;//set new array as csv data
		}

		dailyProfit=new double[data.length-1];


		/*System.out.println("------------------------------"); //debug print data array
		for (String[] strings : data) {
			for (String string : strings) {
				System.out.print("\t" + string);
			}
			System.out.println();
		}*/

		double sharpaR=0;
		double prof=0,oneDayProf=0;
		int numberOfDay=0;
		int k=0;
		System.out.println("Start Capital: \\$" + money);
		builder.append("Start Capital: \\$" + money+"\n");
		
		while(k<data.length-1){

			dailyProfit[k]=0.0;
			
			if(Double.valueOf(data[k][0])==1.0){

				buyPoint=Double.valueOf(data[k][2]);
				buyPoint=buyPoint*100;
				shareNumber=(money-1.0)/buyPoint;
				forceSell=false;
				for (int j=k; j<data.length-1; j++) {

					sellPoint=Double.valueOf(data[j][2]);
					sellPoint=sellPoint*100;
					moneyTemp=(shareNumber*sellPoint)-1.0;
					//stop loss %10
					/*if(money*0.95>moneyTemp){
						money=moneyTemp;
						forceSell=true;
					}*/
				
					if(Double.valueOf(data[j][0])==2.0 || forceSell==true){
						sellPoint=Double.valueOf(data[j][2]);
						sellPoint=sellPoint*100;
						gain=sellPoint-buyPoint;
						if(gain>0){
							successTransactionCount++;
						}
						else{
							failedTransactionCount++;
						}

						if(gain>=maximumGain){
							maximumGain=gain;
							maximumProfitPercent=Double.valueOf(maximumGain)/Double.valueOf(buyPoint)*100;		
						}
						if(gain<=maximumLost){
							maximumLost=gain;
							maximumLostPercent=Double.valueOf(maximumLost)/Double.valueOf(buyPoint)*100;		
						}
						moneyTemp=(shareNumber*sellPoint)-1.0;
						money=moneyTemp;
						if(money>maximumMoney){
							maximumMoney=money;
						}
						if(money<minimumMoney){
							minimumMoney=money;
						}
						transactionCount++;
						//System.out.println("\\\\"+transactionCount+"."+"("+(k+1)+"-"+(j+1)+") => " + Precision.round(sellPoint,2) + "-" + Precision.round(buyPoint,2)+ "= " + Precision.round(gain,2) + " Capital: \\$" + Precision.round(money,2) );
						System.out.println(transactionCount+"."+"("+(k+1)+"-"+(j+1)+") => " + Precision.round((gain*shareNumber),2) + " Capital: $" + Precision.round(money,2) );
						//System.out.println(Precision.round((gain*shareNumber),2));
						builder.append(transactionCount+"."+"("+(k+1)+"-"+(j+1)+") => " + Precision.round((gain*shareNumber),2) + " Capital: $" + Precision.round(money,2)+"\n");
						
						//////////////////////////////
						prof=Precision.round(Precision.round((gain*shareNumber),2)/(money-(gain*shareNumber)),4);
						numberOfDay=j-k;
						oneDayProf=Precision.round((prof/numberOfDay),4);
						for(int m=k+1;m<=j;m++){
							dailyProfit[m]=oneDayProf;
							//System.out.println("dailyProfit["+m+"]:"+dailyProfit[m]);
						}
						//////////////////////////////////////////
						
						totalPercentProfit=totalPercentProfit +(gain/buyPoint);

						totalTransactionLength=totalTransactionLength+(j-k);
						k=j+1;
						totalGain=totalGain+gain;
						break;
					}
				}
			}
			k++;
			
		}
		System.out.println("dailyProfit[z]");
		for(int z=0;z<dailyProfit.length;z++){
			System.out.println(z+":"+dailyProfit[z]);
		}
		sharpaR=findSharpaRatio(dailyProfit);
		
		System.out.println("Sharpa Ratio of Our System=>"+sharpaR);
		builder.append("Sharpa Ratio of Our System=>"+sharpaR+"\n");
		System.out.println("Our System => totalMoney = $" + Precision.round(money,2) );
		builder.append("Our System => totalMoney = $" + Precision.round(money,2)+"\n");
		
		buyPointBAH=Double.valueOf(data[0][2]);
		shareNumberBAH=(moneyBAH-1.0)/buyPointBAH;
		moneyBAH=(Double.valueOf(data[data.length-1][2])*shareNumberBAH)-1.0;


		System.out.println("BAH => totalMoney = $" + Precision.round(moneyBAH,2) );
		builder.append("BAH => totalMoney = $" + Precision.round(moneyBAH,2)+"\n");

		double numberOfDays=Double.valueOf(data.length-1);
		double numberOfYears=numberOfDays/365;
		//money/10000.0;

		//report the results
		/*System.out.println("Our System Annualized return % => " + Precision.round(((Math.exp(Math.log(money/10000.0)/numberOfYears)-1)*100),2) + "%" );
		System.out.println("BaH Annualized return % => " +  Precision.round(((Math.exp(Math.log(moneyBAH/10000.0)/numberOfYears)-1)*100),2) +"%");
		System.out.println("Annualized number of transaction => " + Precision.round((Double.valueOf(transactionCount)/numberOfYears),1) + "#");
		System.out.println("Percent success of transaction => " +Precision.round((Double.valueOf(successTransactionCount)/Double.valueOf(transactionCount))*100,2)+ "%");
		System.out.println("Average percent profit per transaction => " + Precision.round((totalPercentProfit/transactionCount*100),2) + "%" );
		System.out.println("Average transaction length => " + totalTransactionLength/transactionCount + "#");
		System.out.println("Maximum profit percent in transaction=> " + Precision.round(maximumProfitPercent,2) + "%");
		System.out.println("Maximum loss percent in transaction=> " + Precision.round(maximumLostPercent,2)+ "%");
		System.out.println("Maximum capital value=> " + "$"+ Precision.round(maximumMoney,2));
		System.out.println("Minimum capital value=> " + "$"+ Precision.round(minimumMoney,2));
		System.out.println("Idle Ratio %=>  " +  Precision.round((Double.valueOf(data.length-totalTransactionLength)/Double.valueOf(data.length)*100),2) + "%");

		builder.append("Our System Annualized return % => " + Precision.round(((Math.exp(Math.log(money/10000.0)/numberOfYears)-1)*100),2) + "%" +"\n");
		builder.append("BaH Annualized return % => " +  Precision.round(((Math.exp(Math.log(moneyBAH/10000.0)/numberOfYears)-1)*100),2) +"%"+"\n");
		builder.append("Annualized number of transaction => " + Precision.round((Double.valueOf(transactionCount)/numberOfYears),1) + "#"+"\n");
		builder.append("Percent success of transaction => " +Precision.round((Double.valueOf(successTransactionCount)/Double.valueOf(transactionCount))*100,2)+ "%"+"\n");
		builder.append("Average percent profit per transaction => " + Precision.round((totalPercentProfit/transactionCount*100),2) + "%" +"\n");
		builder.append("Average transaction length => " + totalTransactionLength/transactionCount + "#"+"\n");
		builder.append("Maximum profit percent in transaction=> " + Precision.round(maximumProfitPercent,2) + "%"+"\n");
		builder.append("Maximum loss percent in transaction=> " + Precision.round(maximumLostPercent,2)+ "%"+"\n");
		builder.append("Maximum capital value=> " + "$"+ Precision.round(maximumMoney,2)+"\n");
		builder.append("Minimum capital value=> " + "$"+ Precision.round(minimumMoney,2)+"\n");
		builder.append("Idle Ratio %=>  " +  Precision.round((Double.valueOf(data.length-totalTransactionLength)/Double.valueOf(data.length)*100),2) + "%"+"\n");
		*/
		System.out.println("Our System Annualized return % => " + Precision.round(((Math.pow(money/10000.0, 0.2)-1)*100),2) + "%" ); //5 years 0.2
		System.out.println("BaH Annualized return % => " +  Precision.round(((Math.exp(Math.log(moneyBAH/10000.0))-1)*100),2) +"%");
		System.out.println("Annualized number of transaction => " + Precision.round((Double.valueOf(transactionCount)),1) + "#");
		System.out.println("Percent success of transaction => " +Precision.round((Double.valueOf(successTransactionCount)/Double.valueOf(transactionCount))*100,2)+ "%");
		System.out.println("Average percent profit per transaction => " + Precision.round((totalPercentProfit/transactionCount*100),2) + "%" );
		System.out.println("Average transaction length => " + totalTransactionLength/transactionCount + "#");
		System.out.println("Maximum profit percent in transaction=> " + Precision.round(maximumProfitPercent,2) + "%");
		System.out.println("Maximum loss percent in transaction=> " + Precision.round(maximumLostPercent,2)+ "%");
		System.out.println("Maximum capital value=> " + "$"+ Precision.round(maximumMoney,2));
		System.out.println("Minimum capital value=> " + "$"+ Precision.round(minimumMoney,2));
		System.out.println("Idle Ratio %=>  " +  Precision.round((Double.valueOf(data.length-totalTransactionLength)/Double.valueOf(data.length)*100),2) + "%");

		builder.append("Our System Annualized return % => " + Precision.round(((Math.pow(money/10000.0, 0.2)-1)*100),2) + "%" +"\n"); //5 years 0.2
		//builder.append("Our System Annualized return % => " + Precision.round(((Math.exp(Math.log(money/10000.0))-1)*100),2) + "%" +"\n");
		builder.append("BaH Annualized return % => " +  Precision.round(((Math.exp(Math.log(moneyBAH/10000.0))-1)*100),2) +"%"+"\n");
		builder.append("Annualized number of transaction => " + Precision.round((Double.valueOf(transactionCount)),1) + "#"+"\n");
		builder.append("Percent success of transaction => " +Precision.round((Double.valueOf(successTransactionCount)/Double.valueOf(transactionCount))*100,2)+ "%"+"\n");
		builder.append("Average percent profit per transaction => " + Precision.round((totalPercentProfit/transactionCount*100),2) + "%" +"\n");
		builder.append("Average transaction length => " + totalTransactionLength/transactionCount + "#"+"\n");
		builder.append("Maximum profit percent in transaction=> " + Precision.round(maximumProfitPercent,2) + "%"+"\n");
		builder.append("Maximum loss percent in transaction=> " + Precision.round(maximumLostPercent,2)+ "%"+"\n");
		builder.append("Maximum capital value=> " + "$"+ Precision.round(maximumMoney,2)+"\n");
		builder.append("Minimum capital value=> " + "$"+ Precision.round(minimumMoney,2)+"\n");
		builder.append("Idle Ratio %=>  " +  Precision.round((Double.valueOf(data.length-totalTransactionLength)/Double.valueOf(data.length)*100),2) + "%"+"\n");
		
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("resources3/ResultsNoStop.txt"));
			writer.write(builder.toString());//save the string representation of the board
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}
	
	
	public static double findSharpaRatio(double[] dailyProfit)
    {
        
        double sum = 0;
        double max = 0;
        
        double sd = 0;
        for(int i=0; i<dailyProfit.length; i++)
        {
            sum = sum + dailyProfit[i];
        }
        System.out.println("sum is : " + sum);
        double average = sum / dailyProfit.length;
        System.out.println("Average value is : " + average);
       
        
        for (int i = 0; i < dailyProfit.length; i++)
        {
            sd += ((dailyProfit[i] - average)*(dailyProfit[i] - average)) / dailyProfit.length;
        }
        double standardDeviation = Math.sqrt(sd);
        System.out.println("standardDeviation is : " + standardDeviation);
        
        double sharpaRatio=average/standardDeviation;
        
        return sharpaRatio;
    }
	

}

