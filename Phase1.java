package ml.cnn.paper;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;

import spark.examples.FinancialAnalysis.SharePrice;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.CCIIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.CMOIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.DPOIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.PPOIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MaxPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MinPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.VolumeIndicator;
import eu.verdelhan.ta4j.indicators.statistics.CovarianceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.*;
import eu.verdelhan.ta4j.indicators.volume.ChaikinMoneyFlowIndicator;

public class Phase1 {

	public final static int WINDOW_SIZE=11; //Sh 6
	//public final static int WINDOW_SIZE=21; //Sh 11
	//public final static int WINDOW_SIZE=41; //Sh 22

	public static Double previousCloseValue=0.0;
	public static Double currentCloseValue=0.0;
	public static int counterSell=0;
	public static int counterBuy=0;
	public static int counterRow=0;
	public static int minCounter,maxCounter;
	public static DecimalFormat decim = new DecimalFormat("#.##");
	public static List<Tick> ticksList=new ArrayList<Tick>();
	public static TimeSeries timeSeries;
	
	//rsi,SmoothedRSI,wr,wma,ema,sma,hma,tripleEma, bollinger,cci,cmo,ppo,roc,ravi,parsar,
	
	public static RSIIndicator [] rsiArray=new RSIIndicator [20];
	//public static SmoothedRSIIndicator [] smoothedRsiArray=new SmoothedRSIIndicator [20];
	public static WilliamsRIndicator [] wrArray=new WilliamsRIndicator [20];
	public static WMAIndicator [] wmaArray=new WMAIndicator[20];
	public static EMAIndicator [] emaArray=new EMAIndicator[20];
	public static SMAIndicator [] smaArray=new SMAIndicator[20];
	public static HMAIndicator [] hmaArray=new HMAIndicator[20];
	public static TripleEMAIndicator [] temaArray=new TripleEMAIndicator[20];
	
	public static CCIIndicator [] cciArray=new CCIIndicator[20];
	public static CMOIndicator [] cmoArray=new CMOIndicator[20];
	public static MACDIndicator [] macdArray=new MACDIndicator[20];
	public static PPOIndicator [] ppoArray=new PPOIndicator[20];
	public static ROCIndicator [] rocArray=new ROCIndicator[20];
	//public static DPOIndicator [] dpoArray=new DPOIndicator[20];
	public static ChaikinMoneyFlowIndicator [] cmfiArray=new ChaikinMoneyFlowIndicator [20];
	public static DirectionalMovementIndicator [] dmiArray=new DirectionalMovementIndicator [20];
	public static ParabolicSarIndicator[] psiArray=new ParabolicSarIndicator [20];

	
	public static EMAIndicator ema;
	public static SMAIndicator sma50,sma200;
	public static VolumeIndicator volumeIndicator;
	public static CMOIndicator cmo;
	public static CovarianceIndicator covar;
	public static MACDIndicator macd;
	public static ROCIndicator roc;
	public static WMAIndicator wma;

	public static List<Double> closeList=new ArrayList<Double>();
	public static int windowBeginIndex=0, windowEndIndex=0, windowMiddleIndex=0, minIndex=0, maxIndex=0;
	public static String result="Yok";
	public static Double label=0.0;
	public static Double min = 10000.0;
	public static Double max = 0.0;
	public static Double number = 0.0;


	public static class SharePrice implements Serializable {
		private String date;
		private Double open;
		private Double high;
		private Double low;
		private Double close;
		private Double volume;
		private Double adjClose;

		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public Double getOpen() {
			return open;
		}
		public void setOpen(Double open) {
			this.open = open;
		}
		public Double getHigh() {
			return high;
		}
		public void setHigh(Double high) {
			this.high = high;
		}
		public Double getLow() {
			return low;
		}
		public void setLow(Double low) {
			this.low = low;
		}
		public Double getClose() {
			return close;
		}
		public void setClose(Double close) {
			this.close = close;
		}
		public Double getVolume() {
			return volume;
		}
		public void setVolume(Double volume) {
			this.volume = volume;
		}
		public Double getAdjClose() {
			return adjClose;
		}
		public void setAdjClose(Double adjClose) {
			this.adjClose = adjClose;
		}
	}
	
	public static void PhaseProcess(String outputFile) throws Exception{
		SparkConf sparkConf = new SparkConf();
		sparkConf.setMaster("local[*]");
		sparkConf.setAppName("DL4J Spark MLP");

		SparkSession spark = SparkSession
				.builder()
				.appName("Java Spark SQL basic example")
				.config(sparkConf)
				.getOrCreate();

		runPhase1(spark,outputFile);

		spark.stop();
	}

	public static void main(String[] args) throws AnalysisException {

		SparkConf sparkConf = new SparkConf();
		sparkConf.setMaster("local[*]");
		sparkConf.setAppName("DL4J Spark MLP");

		SparkSession spark = SparkSession
				.builder()
				.appName("Java Spark SQL basic example")
				.config(sparkConf)
				.getOrCreate();
		String outputFile="Test";
		runPhase1(spark,outputFile);

		spark.stop();
	}

	private static void runPhase1(SparkSession spark,String outputFile) {

		JavaRDD<SharePrice> sharePriceRDD = spark.read()
				.textFile("resources3/reverseFile"+outputFile+".csv")
				.javaRDD()
				.map(new Function<String, SharePrice>() {
					public SharePrice call(String line) throws Exception {
						String[] parts = line.split(",");
						SharePrice sharePrice=new SharePrice();
						sharePrice.setDate(parts[0]);
						Double AdjOpen=Double.parseDouble(parts[1].trim())*(Double.parseDouble(parts[5].trim())/Double.parseDouble(parts[4].trim()));
						//sharePrice.setOpen(Double.parseDouble(parts[1].trim()));
						sharePrice.setOpen(AdjOpen);

						Double AdjHigh=Double.parseDouble(parts[2].trim())*(Double.parseDouble(parts[5].trim())/Double.parseDouble(parts[4].trim()));
						//sharePrice.setHigh(Double.parseDouble(parts[2].trim()));
						sharePrice.setHigh(AdjHigh);

						Double AdjLow=Double.parseDouble(parts[3].trim())*(Double.parseDouble(parts[5].trim())/Double.parseDouble(parts[4].trim()));
						//sharePrice.setLow(Double.parseDouble(parts[3].trim()));
						sharePrice.setLow(AdjLow);

						Double AdjClose=Double.parseDouble(parts[5].trim());
						//Double AdjClose=Double.parseDouble(parts[6].trim()); es
						//sharePrice.setClose(Double.parseDouble(parts[4].trim()));
						sharePrice.setClose(AdjClose);

						//sharePrice.setVolume(Double.parseDouble(parts[5].trim()));es
						//sharePrice.setAdjClose(Double.parseDouble(parts[6].trim()));es
						sharePrice.setAdjClose(Double.parseDouble(parts[5].trim()));
						sharePrice.setVolume(Double.parseDouble(parts[6].trim()));
						

						String year=sharePrice.getDate().split("-")[0];
						String month=sharePrice.getDate().split("-")[1];
						String day=sharePrice.getDate().split("-")[2];

						DateTime dt = new DateTime(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day),12, 0, 0, 0);
						ticksList.add(new Tick(dt,Decimal.valueOf(sharePrice.getOpen()),Decimal.valueOf(sharePrice.getHigh()),Decimal.valueOf(sharePrice.getLow()),Decimal.valueOf(sharePrice.getClose()),Decimal.valueOf(sharePrice.getVolume())));
						return sharePrice;
					}
				});
		timeSeries=new TimeSeries(ticksList);
		//rsi,SmoothedRSI,wr,wma,ema,sma,hma,tripleEma, bollinger,cci,cmo,ppo,roc,ravi,parsar,
		
		for (int i=0;i<20;i++){
			rsiArray[i]= new RSIIndicator(new ClosePriceIndicator(timeSeries), i+1);
			wrArray[i]= new WilliamsRIndicator(new ClosePriceIndicator(timeSeries), i+1, new MaxPriceIndicator(timeSeries),
					new MinPriceIndicator(timeSeries));
			wmaArray[i]= new WMAIndicator(new ClosePriceIndicator(timeSeries), i+1);
			emaArray[i]= new EMAIndicator(new ClosePriceIndicator(timeSeries), i+1);
			smaArray[i]= new SMAIndicator(new ClosePriceIndicator(timeSeries), i+1);
			hmaArray[i]= new HMAIndicator(new ClosePriceIndicator(timeSeries), i+1);
			temaArray[i]= new TripleEMAIndicator(new ClosePriceIndicator(timeSeries), i+1);
			cciArray[i]= new CCIIndicator(timeSeries, i+1);
			cmoArray[i]= new CMOIndicator(new ClosePriceIndicator(timeSeries), i+1);
			macdArray[i]= new MACDIndicator(new ClosePriceIndicator(timeSeries), i+1,(i+1)*2);
			ppoArray[i]= new PPOIndicator(new ClosePriceIndicator(timeSeries), i+1,(i+1)*2);
			rocArray[i]= new ROCIndicator(new ClosePriceIndicator(timeSeries), i+1);
			//dpoArray[i]= new DPOIndicator(new ClosePriceIndicator(timeSeries), i+1);
			cmfiArray[i]= new ChaikinMoneyFlowIndicator(timeSeries, i+1);
			dmiArray[i]= new DirectionalMovementIndicator(timeSeries, i+1);
			psiArray[i]= new ParabolicSarIndicator(timeSeries, i+1);
		}
		/*for (int i=0;i<20;i++){
			wrArray[i]= new WilliamsRIndicator(new ClosePriceIndicator(timeSeries), i+1, new MaxPriceIndicator(timeSeries),
					new MinPriceIndicator(timeSeries));
		}*/
		
		//rsi,SmoothedRSI,wr,wma,ema,sma,hma,tripleEma, bollinger,cci,cmo,ppo,roc,ravi,parsar,
		
		sma50= new SMAIndicator(new ClosePriceIndicator(timeSeries), 50);
		sma200= new SMAIndicator(new ClosePriceIndicator(timeSeries), 200);
		
		/*wr = new WilliamsRIndicator(new ClosePriceIndicator(timeSeries), 10, new MaxPriceIndicator(timeSeries),
				new MinPriceIndicator(timeSeries));
		ema = new EMAIndicator(new ClosePriceIndicator(timeSeries), 10);
		volumeIndicator = new VolumeIndicator(timeSeries, 5);
		cmo = new CMOIndicator(new ClosePriceIndicator(timeSeries), 9);
		covar = new CovarianceIndicator(new ClosePriceIndicator(timeSeries), new VolumeIndicator(timeSeries, 5), 5);
		roc = new ROCIndicator(new ClosePriceIndicator(timeSeries), 12);
		macd = new MACDIndicator(new ClosePriceIndicator(timeSeries), 12,28);
		wma = new WMAIndicator(new ClosePriceIndicator(timeSeries), 9);*/
		// Apply a schema to an RDD of JavaBeans to get a DataFrame
		Dataset<Row> sharePriceDF = spark.createDataFrame(sharePriceRDD, SharePrice.class);

		// Register the DataFrame as a temporary view
		sharePriceDF.createOrReplaceTempView("sharePrice");
		//sharePriceDF.show();

		// SQL statements can be run by using the sql methods provided by spark
		Dataset<Row> sharePriceCloseDF = spark.sql("SELECT date,close FROM sharePrice");

		// The columns of a row in the result can be accessed by field index
		Encoder<String> stringEncoder = Encoders.STRING();

		closeList.add(currentCloseValue);
		Dataset<String> sharePriceCloseByIndexDF = sharePriceCloseDF.map(new MapFunction<Row, String>() {
			public String call(Row row) throws Exception {
				currentCloseValue=row.<Double>getAs("close");
				closeList.add(currentCloseValue);
				counterRow++;
				result="--";

				System.out.println("counterRow:" + counterRow);
				if(counterRow>WINDOW_SIZE){

					windowBeginIndex=counterRow-WINDOW_SIZE;
					windowEndIndex=windowBeginIndex+WINDOW_SIZE-1;
					windowMiddleIndex=(windowBeginIndex+windowEndIndex)/2;
				
					for(int i = windowBeginIndex; i <= windowEndIndex; i++) {
						number = closeList.get(i);
						if(number < min) {
							min = number;
							minIndex = closeList.indexOf(min);
						}
						if(number > max) {
							max = number;
							maxIndex = closeList.indexOf(max);
						}
					}
					//Label data with "Sell","Buy","Hold"
					if(maxIndex==windowMiddleIndex){
						result="Sell";
						label=2.0;
						System.out.println("max:" + max + " maxIndex:" + maxIndex + " windowMiddleIndex:" + windowMiddleIndex + " result:" + result);
						//maxCounter=4;
					}
					else if(minIndex==windowMiddleIndex){
						result="Buy";
						label=1.0;
						System.out.println("min:" + min + " minIndex:" + minIndex + " windowMiddleIndex:" + windowMiddleIndex + " result:" + result);
						//minCounter=4;
					}
					//no hold
					else{
						/*maxCounter--;
						minCounter--;
						if(maxCounter>0){
							result="Sell";
							label=2.0;
						}else if(minCounter>0){
							result="Buy";
							label=1.0;
						}else{
							result="Hold";
							label=0.0;
						}*/
						result="Hold";
						label=0.0;
							
					}
					//Forget when out of window 
					max=0.0;
					min=10000.0;
				}
			



				//System.out.println("Label | " + " Close | "  +  " RSI | " +  " WilliamR | " +  " EMA | " +  " CMO " +  " COVAR ");
				//return   decim.format(label) + ";"  +   Precision.round((row.<Double>getAs("close")/100),4) + ";"  + Precision.round((rsi.getValue(counterRow-1).toDouble()/100),4) + ";" + Precision.round((wr.getValue(counterRow-1).toDouble()/100),4)+ ";" + Precision.round((macd.getValue(counterRow-1).toDouble()),4);
				//rsi,SmoothedRSI,wr,wma,ema,sma,hma,tripleEma, bollinger,cci,cmo,ppo,roc,ravi,parsar,
				
				String resultLabel,resultRSI,resultWR,resultWMA,resultEMA,resultSMA,resultHMA,resultTripleEMA,resultCCI,resultCMO,resultMACD,resultPPO,resultROC,resultDPO,resultCMFI,resultDMI,resultPSI;
				
				resultLabel= decim.format(label) + ";"  + Precision.round((row.<Double>getAs("close")),2) + ";";
				resultRSI= Precision.round(((rsiArray[5].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((rsiArray[6].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((rsiArray[7].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((rsiArray[8].getValue(counterRow-1).toDouble()/50)-1),2) + ";" 
				+ Precision.round(((rsiArray[9].getValue(counterRow-1).toDouble()/50)-1),2) + ";"  + Precision.round(((rsiArray[10].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((rsiArray[11].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[12].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[13].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[14].getValue(counterRow-1).toDouble()/50)-1),2) + ";"
				+ Precision.round(((rsiArray[15].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((rsiArray[16].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[17].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[18].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((rsiArray[19].getValue(counterRow-1).toDouble()/50)-1),2)+";";
				
				resultWR=Precision.round(((wrArray[5].getValue(counterRow-1).toDouble()/50)+1),2) + ";" + Precision.round(((wrArray[6].getValue(counterRow-1).toDouble()/50)+1),2) + ";" + Precision.round(((wrArray[7].getValue(counterRow-1).toDouble()/50)+1),2) + ";" + Precision.round(((wrArray[8].getValue(counterRow-1).toDouble()/50)+1),2) + ";" 
						+ Precision.round(((wrArray[9].getValue(counterRow-1).toDouble()/50)+1),2) + ";"  + Precision.round(((wrArray[10].getValue(counterRow-1).toDouble()/50)+1),2) + ";" + Precision.round(((wrArray[11].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[12].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[13].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[14].getValue(counterRow-1).toDouble()/50)+1),2) + ";"
						+ Precision.round(((wrArray[15].getValue(counterRow-1).toDouble()/50)+1),2) + ";" + Precision.round(((wrArray[16].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[17].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[18].getValue(counterRow-1).toDouble()/50)+1),2) + ";"+ Precision.round(((wrArray[19].getValue(counterRow-1).toDouble()/50)+1),2)+";";
				
				resultWMA=Precision.round(((currentCloseValue-wmaArray[5].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-wmaArray[6].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-wmaArray[7].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-wmaArray[8].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" 
						+ Precision.round(((currentCloseValue-wmaArray[9].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"  + Precision.round(((currentCloseValue-wmaArray[10].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-wmaArray[11].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[12].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[13].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[14].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"
						+ Precision.round(((currentCloseValue-wmaArray[15].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-wmaArray[16].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[17].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[18].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-wmaArray[19].getValue(counterRow-1).toDouble())*10/currentCloseValue),2)+";";
				
				resultEMA=Precision.round(((currentCloseValue-emaArray[5].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-emaArray[6].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-emaArray[7].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-emaArray[8].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" 
						+ Precision.round(((currentCloseValue-emaArray[9].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"  + Precision.round(((currentCloseValue-emaArray[10].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-emaArray[11].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[12].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[13].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[14].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"
						+ Precision.round(((currentCloseValue-emaArray[15].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-emaArray[16].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[17].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[18].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-emaArray[19].getValue(counterRow-1).toDouble())*10/currentCloseValue),2)+";";
				
				resultSMA=Precision.round(((currentCloseValue-smaArray[5].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-smaArray[6].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-smaArray[7].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-smaArray[8].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" 
						+ Precision.round(((currentCloseValue-smaArray[9].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"  + Precision.round(((currentCloseValue-smaArray[10].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-smaArray[11].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[12].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[13].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[14].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"
						+ Precision.round(((currentCloseValue-smaArray[15].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-smaArray[16].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[17].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[18].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-smaArray[19].getValue(counterRow-1).toDouble())*10/currentCloseValue),2)+";";
				
				resultHMA=Precision.round(((currentCloseValue-hmaArray[5].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-hmaArray[6].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-hmaArray[7].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-hmaArray[8].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" 
						+ Precision.round(((currentCloseValue-hmaArray[9].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"  + Precision.round(((currentCloseValue-hmaArray[10].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-hmaArray[11].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[12].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[13].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[14].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"
						+ Precision.round(((currentCloseValue-hmaArray[15].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-hmaArray[16].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[17].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[18].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-hmaArray[19].getValue(counterRow-1).toDouble())*10/currentCloseValue),2)+";";
				
				resultTripleEMA=Precision.round(((currentCloseValue-temaArray[5].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-temaArray[6].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-temaArray[7].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-temaArray[8].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" 
						+ Precision.round(((currentCloseValue-temaArray[9].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"  + Precision.round(((currentCloseValue-temaArray[10].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-temaArray[11].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[12].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[13].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[14].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"
						+ Precision.round(((currentCloseValue-temaArray[15].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";" + Precision.round(((currentCloseValue-temaArray[16].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[17].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[18].getValue(counterRow-1).toDouble())*10/currentCloseValue),2) + ";"+ Precision.round(((currentCloseValue-temaArray[19].getValue(counterRow-1).toDouble())*10/currentCloseValue),2)+";";
				
				resultCCI=Precision.round((cciArray[5].getValue(counterRow-1).toDouble()/300),2) + ";" + Precision.round((cciArray[6].getValue(counterRow-1).toDouble()/300),2) + ";" + Precision.round((cciArray[7].getValue(counterRow-1).toDouble()/300),2) + ";" + Precision.round((cciArray[8].getValue(counterRow-1).toDouble()/300),2) + ";" 
						+ Precision.round((cciArray[9].getValue(counterRow-1).toDouble()/300),2) + ";"  + Precision.round((cciArray[10].getValue(counterRow-1).toDouble()/300),2) + ";" + Precision.round((cciArray[11].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[12].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[13].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[14].getValue(counterRow-1).toDouble()/300),2) + ";"
						+ Precision.round((cciArray[15].getValue(counterRow-1).toDouble()/300),2) + ";" + Precision.round((cciArray[16].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[17].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[18].getValue(counterRow-1).toDouble()/300),2) + ";"+ Precision.round((cciArray[19].getValue(counterRow-1).toDouble()/300),2)+";";
				
				resultCMO=Precision.round((cmoArray[5].getValue(counterRow-1).toDouble()/100),2) + ";" + Precision.round((cmoArray[6].getValue(counterRow-1).toDouble()/100),2) + ";" + Precision.round((cmoArray[7].getValue(counterRow-1).toDouble()/100),2) + ";" + Precision.round((cmoArray[8].getValue(counterRow-1).toDouble()/100),2) + ";" 
						+ Precision.round((cmoArray[9].getValue(counterRow-1).toDouble()/100),2) + ";"  + Precision.round((cmoArray[10].getValue(counterRow-1).toDouble()/100),2) + ";" + Precision.round((cmoArray[11].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[12].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[13].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[14].getValue(counterRow-1).toDouble()/100),2) + ";"
						+ Precision.round((cmoArray[15].getValue(counterRow-1).toDouble()/100),2) + ";" + Precision.round((cmoArray[16].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[17].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[18].getValue(counterRow-1).toDouble()/100),2) + ";"+ Precision.round((cmoArray[19].getValue(counterRow-1).toDouble()/100),2)+";";
				
				resultMACD=Precision.round((macdArray[5].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((macdArray[6].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((macdArray[7].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((macdArray[8].getValue(counterRow-1).toDouble()),2) + ";" 
						+ Precision.round((macdArray[9].getValue(counterRow-1).toDouble()),2) + ";"  + Precision.round((macdArray[10].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((macdArray[11].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[12].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[13].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[14].getValue(counterRow-1).toDouble()),2) + ";"
						+ Precision.round((macdArray[15].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((macdArray[16].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[17].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[18].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((macdArray[19].getValue(counterRow-1).toDouble()),2)+";";
				
				resultPPO=Precision.round((ppoArray[5].getValue(counterRow-1).toDouble()/10),2) + ";" + Precision.round((ppoArray[6].getValue(counterRow-1).toDouble()/10),2) + ";" + Precision.round((ppoArray[7].getValue(counterRow-1).toDouble()/10),2) + ";" + Precision.round((ppoArray[8].getValue(counterRow-1).toDouble()/10),2) + ";" 
						+ Precision.round((ppoArray[9].getValue(counterRow-1).toDouble()/10),2) + ";"  + Precision.round((ppoArray[10].getValue(counterRow-1).toDouble()/10),2) + ";" + Precision.round((ppoArray[11].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[12].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[13].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[14].getValue(counterRow-1).toDouble()/10),2) + ";"
						+ Precision.round((ppoArray[15].getValue(counterRow-1).toDouble()/10),2) + ";" + Precision.round((ppoArray[16].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[17].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[18].getValue(counterRow-1).toDouble()/10),2) + ";"+ Precision.round((ppoArray[19].getValue(counterRow-1).toDouble()/10),2)+";";
				
				resultROC=Precision.round((rocArray[5].getValue(counterRow-1).toDouble()/20),2) + ";" + Precision.round((rocArray[6].getValue(counterRow-1).toDouble()/20),2) + ";" + Precision.round((rocArray[7].getValue(counterRow-1).toDouble()/20),2) + ";" + Precision.round((rocArray[8].getValue(counterRow-1).toDouble()/20),2) + ";" 
						+ Precision.round((rocArray[9].getValue(counterRow-1).toDouble()/20),2) + ";"  + Precision.round((rocArray[10].getValue(counterRow-1).toDouble()/20),2) + ";" + Precision.round((rocArray[11].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[12].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[13].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[14].getValue(counterRow-1).toDouble()/20),2) + ";"
						+ Precision.round((rocArray[15].getValue(counterRow-1).toDouble()/20),2) + ";" + Precision.round((rocArray[16].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[17].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[18].getValue(counterRow-1).toDouble()/20),2) + ";"+ Precision.round((rocArray[19].getValue(counterRow-1).toDouble()/20),2)+";";
				
				/*resultDPO=Precision.round((dpoArray[4].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[5].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[6].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[7].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[8].getValue(counterRow-1).toDouble()),0) + ";" 
						+ Precision.round((dpoArray[9].getValue(counterRow-1).toDouble()),0) + ";"  + Precision.round((dpoArray[10].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[11].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[12].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[13].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[14].getValue(counterRow-1).toDouble()),0) + ";"
						+ Precision.round((dpoArray[15].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((dpoArray[16].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[17].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[18].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((dpoArray[19].getValue(counterRow-1).toDouble()),0)+";";
				*/
				resultCMFI=Precision.round((cmfiArray[5].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((cmfiArray[6].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((cmfiArray[7].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((cmfiArray[8].getValue(counterRow-1).toDouble()),2) + ";" 
						+ Precision.round((cmfiArray[9].getValue(counterRow-1).toDouble()),2) + ";"  + Precision.round((cmfiArray[10].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((cmfiArray[11].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[12].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[13].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[14].getValue(counterRow-1).toDouble()),2) + ";"
						+ Precision.round((cmfiArray[15].getValue(counterRow-1).toDouble()),2) + ";" + Precision.round((cmfiArray[16].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[17].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[18].getValue(counterRow-1).toDouble()),2) + ";"+ Precision.round((cmfiArray[19].getValue(counterRow-1).toDouble()),2)+";";
				
				resultDMI=Precision.round(((dmiArray[5].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((dmiArray[6].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((dmiArray[7].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((dmiArray[8].getValue(counterRow-1).toDouble()/50)-1),2) + ";" 
						+ Precision.round(((dmiArray[9].getValue(counterRow-1).toDouble()/50)-1),2) + ";"  + Precision.round(((dmiArray[10].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((dmiArray[11].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[12].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[13].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[14].getValue(counterRow-1).toDouble()/50)-1),2) + ";"
						+ Precision.round(((dmiArray[15].getValue(counterRow-1).toDouble()/50)-1),2) + ";" + Precision.round(((dmiArray[16].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[17].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[18].getValue(counterRow-1).toDouble()/50)-1),2) + ";"+ Precision.round(((dmiArray[19].getValue(counterRow-1).toDouble()/50)-1),2)+";";
				
				resultPSI=Precision.round((psiArray[5].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" + Precision.round((psiArray[6].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" + Precision.round((psiArray[7].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" + Precision.round((psiArray[8].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" 
						+ Precision.round((psiArray[9].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"  + Precision.round((psiArray[10].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" + Precision.round((psiArray[11].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[12].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[13].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[14].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"
						+ Precision.round((psiArray[15].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";" + Precision.round((psiArray[16].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[17].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[18].getValue(counterRow-1).toDouble()/currentCloseValue),2) + ";"+ Precision.round((psiArray[19].getValue(counterRow-1).toDouble()/currentCloseValue),2)+";";
				
				
				/*resultRSI=Precision.round((row.<Double>getAs("close")),4) + ";"  + Precision.round((rsiArray[0].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[1].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[2].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[3].getValue(counterRow-1).toDouble()),0) + ";" 
						+ Precision.round((rsiArray[4].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[5].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[6].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[7].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[8].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[9].getValue(counterRow-1).toDouble()),0) + ";"  
						+ Precision.round((rsiArray[10].getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsiArray[11].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[12].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[13].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[14].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[15].getValue(counterRow-1).toDouble()),0) + ";"
						+ Precision.round((rsiArray[16].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[17].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[18].getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsiArray[19].getValue(counterRow-1).toDouble()),0)+";";*/
				
				/*resultRSI=Precision.round((row.<Double>getAs("close")),4) + ";"  + Precision.round((rsi1.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi2.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi3.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi4.getValue(counterRow-1).toDouble()),0) + ";" 
						+ Precision.round((rsi5.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi6.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi7.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi8.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi9.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi10.getValue(counterRow-1).toDouble()),0) + ";"  
						+ Precision.round((rsi11.getValue(counterRow-1).toDouble()),0) + ";" + Precision.round((rsi12.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi13.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi14.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi15.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi16.getValue(counterRow-1).toDouble()),0) + ";"
						+ Precision.round((rsi17.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi18.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi19.getValue(counterRow-1).toDouble()),0) + ";"+ Precision.round((rsi20.getValue(counterRow-1).toDouble()),0)+";";*/
						
				//resultSMA= Precision.round((sma50.getValue(counterRow-1).toDouble()),2)+ ";"+ Precision.round((sma200.getValue(counterRow-1).toDouble()),2);
				
				
				//return resultRSI+" | "+resultWR+" | "+resultWMA+" | "+resultEMA+" | "+resultSMA+" | "+resultHMA+" | "+resultTripleEMA+" | "+resultCCI+" | "+resultCMO+" | "+resultMACD+" | "+resultPPO+" | "+resultROC+" | "+resultCMFI+" | "+resultDMI+" | "+resultPSI;
				
				return resultLabel+resultRSI+resultWR+resultWMA+resultEMA+resultSMA+resultHMA+resultTripleEMA+resultCCI+resultCMO+resultMACD+resultPPO+resultROC+resultCMFI+resultDMI+resultRSI; //1.
				//return resultLabel+resultWMA+resultEMA+resultSMA+resultHMA+resultTripleEMA+resultCCI+resultCMO+resultMACD+resultRSI+resultWR+resultDMI+resultRSI+resultCMFI+resultPPO+resultROC; //2.
				//return resultLabel+resultPPO+resultROC+resultRSI+resultWR+resultCCI+resultCMO+resultMACD+resultWMA+resultEMA+resultSMA+resultHMA+resultTripleEMA+resultCMFI+resultDMI+resultRSI; //3.
				//return resultLabel+resultCCI+resultCMO+resultMACD+resultRSI+resultWR+resultCMFI+resultDMI+resultRSI+resultPPO+resultROC+resultWMA+resultEMA+resultSMA+resultHMA+resultTripleEMA; //4.
				//return resultLabel+resultCMFI+resultDMI+resultRSI+resultPPO+resultROC+resultWMA+resultEMA+resultSMA+resultHMA+resultTripleEMA+resultRSI+resultWR+resultCCI+resultCMO+resultMACD; //5.
				//return resultLabel+resultWR+resultRSI+resultHMA+resultTripleEMA+resultSMA+resultWMA+resultEMA+resultMACD+resultCMO+resultCCI+resultROC+resultPPO+resultRSI+resultDMI+resultCMFI; //6.
				//return resultLabel+resultSMA+resultEMA+resultTripleEMA+resultHMA+resultWMA+resultCCI+resultMACD+resultCMO+resultWR+resultRSI+resultDMI+resultCMFI+resultRSI+resultROC+resultPPO; //7.
			}
		}, stringEncoder);


		//sharePriceCloseByIndexDF.show();

		sharePriceCloseByIndexDF.write().csv("resources3/output"+outputFile+".csv");

	}
}
