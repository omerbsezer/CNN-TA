# Algorithmic Financial Trading with Deep Convolutional Neural Networks: Time Series to Image Conversion Approach (CNN-TA) 
A novel algorithmic trading model CNN-TA using a 2-D convolutional neural network based on image processing properties.

## CNN-TA
"Computational intelligence techniques for financial trading systems have always been quite popular. In the last decade, deep learning models start getting more attention, especially within the image processing community. In this study, we propose a novel algorithmic trading model CNN-TA using a 2-D convolutional neural network based on image processing properties. In order to convert financial time series into 2-D images, 15 different technical indicators each with different parameter selections are utilized. Each indicator instance generates data for a 15 day period. As a result, 15 × 15 sized 2-D images are constructed. Each image is then labeled as Buy, Sell or Hold depending on the hills and valleys of the original time series. The results indicate that when compared with the Buy & Hold Strategy and other common trading systems over a long out-of-sample period, the trained model provides better results for stocks and ETFs."

## Method: 
![phaseMethod](https://user-images.githubusercontent.com/10358317/65412453-f050d280-ddf7-11e9-8782-66a7e863d53e.jpg)

## Generated Images:
![figureBuySelHold](https://user-images.githubusercontent.com/10358317/65412464-f5ae1d00-ddf7-11e9-85b2-98651104775b.jpg)

## Sample Result:
![dow30Capital](https://user-images.githubusercontent.com/10358317/65412466-f777e080-ddf7-11e9-9d02-e17d86c12787.jpg)


## Phases in the algorithm:

- Phase0.java: Reversing files downloaded from finance.yahoo (finance.yahoo updated it, it is no longer needed)
- Phase1.java: Dataframe, Techinical Analysis (TA) is performed, and images are created. When creating an image, the buy/sell/hold labels are determined (buy: 1, sell: 2, hold: 0), and the first value of the line becomes this label value. TA parameters are normalized between -1 and 1.
- Phase2.java: Buy/sell labels are shifted by selecting the lowest value and the highest value as sell in the 11-day window. Meanwhile, labels are added with a delay of 6 days. To correct this (the buy/sell points are in place), only the label values are shifted up to 6 lines in Phase2 and the CSV file is generated. "outputOfPhase2Training" and "outputOfPhase2Test" files are being created before CNN. When you open the files, the first value on each line is the label, the second value is the price value, and the next 225 is the pixel value in the image.
- main.py: CNN implementation and DL works here (Keras, Tensorflow). Imbalance problem solved. Proof of concept results was obtained by using Le-net CNN architecture. At this stage, the file "cnn_result" is being generated. The first value of the file is a prediction, the second one is ground_truth, the third one is price.
- Phase5.java: Running the financial scenario according to the values in the file "cnn_result". The results of the scenario (buy/sell transactions) run in the "ResultNoStop" file are being written.
- Scheduler.java: The java files are executed in order, the codes are executed in a semi-auto way.

There are 1-7 folders in the Data folder. Each represents the following technical analysis sequence.
5 years train, 1-year test results were obtained. In the last 15 years, 5 years of the train, 1 year of the test is done and the latest updated data with the progress of the CNN is being trained from scratch. The details of chapter 8 of the thesis are available (pp. 97-117). Too many tests were performed with ETFs and 30 stocks (about 600 tests).

Code is written in 2018 May. It is also needed to optimize. It will be recoded with python.

**Details in the paper**

**ResearchGate:** https://www.researchgate.net/publication/324802031_Algorithmic_Financial_Trading_with_Deep_Convolutional_Neural_Networks_Time_Series_to_Image_Conversion_Approach?_sg=lP2iLAO11ryca0WKXwfb3Rr5_QUMfPsB4KHboCZHl_PqK4vRtG9Rowal9AFHJYReSyfdC1NvQserRn_qen1tVF8iRj9tALFEU2BjBx46.4b4mXN3W2n61AamqR8Hbo6tQgh7P6UyvIvYoz5KvOyCOSftny0LSS42BDeyqqEnRzLvY-TDOyzdgWJ0GcTf1RA

**Science Direct Link:** https://www.sciencedirect.com/science/article/pii/S1568494618302151

_**Cite as:**_

**Bibtex:**

```
@article{sezer2018algorithmic,
  title={Algorithmic financial trading with deep convolutional neural networks: Time series to image conversion approach},
  author={Sezer, Omer Berat and Ozbayoglu, Ahmet Murat},
  journal={Applied Soft Computing},
  volume={70},
  pages={525--538},
  year={2018},
  publisher={Elsevier}
}
```
