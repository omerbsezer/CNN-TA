import numpy as np
import pandas as pd
import csv
from sklearn.model_selection import StratifiedKFold
from sklearn import metrics
import matplotlib.pyplot as plt
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten
from keras.layers import Conv2D, MaxPooling2D

from sklearn.metrics import classification_report, confusion_matrix
from sklearn.utils import shuffle
import math




def train_cnn(training_df, test_df, params):
    """Trains and evaluates CNN on the given train and test data, respectively."""

    print("Training is starting ...")
    train_images = training_df.ix[:, 2:].as_matrix()   # bu satır son sütun hariç her şeyi resimler dataframeine atar.
    train_labels = training_df.ix[:, 0]    # bu satır sadece son sütunu alır.
    train_prices = training_df.ix[: ,1]

    test_images = test_df.ix[:, 2:].as_matrix()   # bu satır son sütun hariç her şeyi resimler dataframeine atar.
    test_labels = test_df.ix[:, 0]   # bu satır sadece son sütunu alır.
    test_prices = test_df.ix[:, 1]




    test_labels = keras.utils.to_categorical(test_labels, params["num_classes"])
    train_labels = keras.utils.to_categorical(train_labels, params["num_classes"])


    train_images = train_images.reshape(train_images.shape[0], params["input_w"], params["input_h"], 1)
    test_images = test_images.reshape(test_images.shape[0], params["input_w"], params["input_h"], 1)



    # CNN model
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(params["input_w"], params["input_h"], 1)))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))
    model.add(Flatten())
    model.add(Dense(128, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(params["num_classes"], activation='softmax'))
    model.compile(loss=keras.losses.categorical_crossentropy,
                  optimizer=keras.optimizers.Adadelta(),
                  metrics=['accuracy', 'mae', 'mse'])

    # metrics.accuracy_score, metrics.recall_score, metrics.average_precision_score, metrics.confusion_matrix
    train_data_size = train_images.shape[0]
    test_data_size = test_images.shape[0]


    print("model will be trained with {} and be tested with {} sample".format(train_data_size,test_data_size))
    # fit the model to the training data
    print("Fitting model to the training data...")
    print("")
    model.fit(train_images, train_labels, batch_size=params["batch_size"], epochs=params["epochs"], verbose=1,validation_data=None)

    predictions = model.predict(test_images, batch_size=params["batch_size"], verbose=1)
    print(model.evaluate(test_images, test_labels, batch_size=params["batch_size"], verbose=1))

    print("Train conf matrix: ", confusion_matrix(np.array(reverse_one_hot(train_labels)),
                                                  np.array(reverse_one_hot(model.predict(train_images, batch_size=params["batch_size"], verbose=1)))))

    print("Test conf matrix: ",  confusion_matrix(np.array(reverse_one_hot(test_labels)),
                                                  np.array(reverse_one_hot(predictions))))



    # cur_pointer = train_data_size + 1
    # print("Calculating accuracy day by day...", end='\n\n')
    # for i in range(test_data_size-2):
    #     # train with 1 more image
    #     model.train_on_batch(np.reshape(data[train_data_size + 1 + i, :], (1, params["input_w"], params["input_h"], 1)),
    #                         np.reshape(labels[train_data_size + 1 + i, :], (1, params["num_classes"])))
    #
    #     # test with first untrained day which is the day after previously trained one
    #     loss_cur,acc_cur = model.test_on_batch(np.reshape(data[train_data_size + 1 + i + 1, :], (1, params["input_w"], params["input_h"], 1)),
    #                         np.reshape(labels[train_data_size + 1 + i + 1, :], (1, params["num_classes"])))
    #
    #     accuracies.append(acc_cur)
    #     losses.append(loss_cur)
    #
    #     # show values every 100 cycle
    #     if i % 100 == 0:
    #         print("{} to {} mean : ".format(i-100,i), np.mean(accuracies))

    return predictions, test_labels, test_prices





def reverse_one_hot(predictions):
    reversed_x = []
    for x in predictions:
        reversed_x.append(np.argmax(np.array(x)))
    return reversed_x




#          Resim            Label
# | a11, a12, a13, a14 |      m
# | a21, a22, a23, a24 |
# | a31, a32, a33, a34 |
# | a41, a42, a43, a44 |

# Burada a11,...a44 resmin piksellerini temsil ediyor(4x4lük bir resim bu örneğin).
# m ise bu resmin labelini ifade ediyor.

# ben bu resmi flatten bir halde saklıyorum yani şu şekilde.
# image = [a11,a12,a13.....a43,a44]
# label = m

# Datasetimde ise bunları birleştirip tek satır yapıyorum:
# row =  [a11,a12,a13.....a43,a44, m]

# Şimdi bu row vektörü hem resmi hemde onun label ını tutuyor.Bunu basitçe şöyle okuyabiliriz.

train_df = pd.read_csv("outputOfPhase2Training.csv", header=None, index_col=None, delimiter=';')
test_df = pd.read_csv("outputOfPhase2Test.csv", header=None, index_col=None, delimiter=';')

# sonundaki ; den dolayı son sütun nan geliyor.
train_df = train_df.iloc[:,:-1]
test_df = test_df.iloc[:,:-1]

# drop nan values
train_df = train_df.dropna(axis=0)
test_df = test_df.dropna(axis=0)

# drop first 15 row
train_df = train_df.iloc[15:, :]
test_df = test_df.iloc[15:,:]



l0_train = train_df.loc[train_df[0] == 0]
l1_train = train_df.loc[train_df[0] == 1]
l2_train = train_df.loc[train_df[0] == 2]
l0_size = l0_train.shape[0]
l1_size = l1_train.shape[0]
l2_size = l2_train.shape[0]
#l0_l1_ratio = int((l0_size//l1_size)/4)
#l0_l2_ratio = int((l0_size//l2_size)/4)

l0_l1_ratio = (l0_size//l1_size)
l0_l2_ratio = (l0_size//l2_size)
print("Before")
print("l0_size:",l0_size,"l1_size:", l1_size,"l2_size:",l2_size)
print("l0_l1_ratio:",l0_l1_ratio,"l0_l2_ratio:", l0_l2_ratio)

l1_new = pd.DataFrame()
l2_new = pd.DataFrame()
for idx, row in train_df.iterrows():
    if row[0] == 1:
        for i in range(l0_l1_ratio):
            l1_new = l1_new.append(row)
    if row[0] == 2:
        for i in range(l0_l2_ratio):
            l2_new = l2_new.append(row)

train_df = train_df.append(l1_new)
train_df = train_df.append(l2_new)

# shuffle
train_df = shuffle(train_df)

########################################################
l0_train = train_df.loc[train_df[0] == 0]
l1_train = train_df.loc[train_df[0] == 1]
l2_train = train_df.loc[train_df[0] == 2]
l0_size = l0_train.shape[0]
l1_size = l1_train.shape[0]
l2_size = l2_train.shape[0]
#l0_l1_ratio = int((l0_size//l1_size)/4)
#l0_l2_ratio = int((l0_size//l2_size)/4)

l0_l1_ratio = (l0_size//l1_size)
l0_l2_ratio = (l0_size//l2_size)
print("After")
print("l0_size:",l0_size,"l1_size:", l1_size,"l2_size:",l2_size)
print("l0_l1_ratio:",l0_l1_ratio,"l0_l2_ratio:", l0_l2_ratio)


######################################################


train_df.reset_index(drop=True, inplace=True)
test_df.reset_index(drop=True, inplace=True)

print("train_df size: ", train_df.shape)

# fill params dict before call train_cnn
params = {"input_w": 15, "input_h": 15, "num_classes": 3, "batch_size": 1024, "epochs": 200}
#params = {"input_w": 15, "input_h": 15, "num_classes": 3, "batch_size": 1024, "epochs": 100}
# Buraya kadar olan yöntime izleyip verini fonksiyona yollayabilirsin.
# Örneğin şu şekilde.
predictions, test_labels, test_prices = train_cnn(train_df, test_df, params)

result_df = pd.DataFrame({"prediction": np.argmax(predictions, axis=1),
                          "test_label":np.argmax(test_labels, axis=1),
                         "test_price":test_prices})
result_df.to_csv("cnn_result.csv", sep=';', index=None)
# Geçen attığım yöntemde ise dictionary içerisinde tutmuşum veriyi.

# data_dict = {"images":images, "labels":labels}

# Yukarıdaki yapı bir dictionary. Java karşılığı hashmap.

# Böyle bir yapı kullanınca verini şu şekilde de atman gayet mümkün.

# train_cnn(data_dict, params)

# ve sonra train_cnn fonksiyonu içerisinde:
#         images = data_dict["images"]
#         labels = data_dict["labels"]
# şeklinde verileri okuyabilirsin.






