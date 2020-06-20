import pandas as pd
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.model_selection import train_test_split

if __name__ == "__main__":
    data = pd.read_csv("./dataset/housing.csv")

    # desc = data.describe(include="all")

    # desc.to_csv("./output/desc.csv")

    # 离散化处理
    data["housing_median_age"] = pd.cut(data.get("housing_median_age"), bins=[0, 10, 20, 30, 40, 60],
                                        labels=['very new', 'new', 'medium', 'old', 'very old'])
    data_num, data_discrete = data.drop(["housing_median_age", "ocean_proximity"], axis=1), pd.merge(
        data.get("housing_median_age"), data.get("ocean_proximity"), left_index=True, right_index=True)

    # 独热编码
    enc = OneHotEncoder(handle_unknown='ignore')
    cat_enc_data = enc.fit_transform(data_discrete).toarray()
    df_cat_enc = pd.DataFrame(data=cat_enc_data, columns=enc.get_feature_names(data_discrete.columns))

    data = pd.merge(data_num, df_cat_enc, left_index=True, right_index=True)

    # 用众数填充空白值
    data.get('total_bedrooms').fillna(data.get('total_bedrooms').mode()[0], inplace=True)


    y = data.get("median_house_value")
    X = data.drop("median_house_value", axis=1)

    trainX, testX, trainy, testy = train_test_split(X, y, test_size=0.3, random_state=36)

    # 将两个表拼起来，列增加。
    train = pd.merge(trainX, trainy, left_index=True, right_index=True)
    test = pd.merge(testX, testy, left_index=True, right_index=True)

    # 归一化，应该只对训练集的特征做
    ss = StandardScaler()
    train["longitude"] = ss.fit_transform(train[["longitude"]])
    train["latitude"] = ss.fit_transform(train[["latitude"]])
    train["total_rooms"] = ss.fit_transform(train[["total_rooms"]])
    train["total_bedrooms"] = ss.fit_transform(train[["total_bedrooms"]])
    train["households"] = ss.fit_transform(train[["households"]])
    train["population"] = ss.fit_transform(train[["population"]])
    train["median_income"] = ss.fit_transform(train[["median_income"]])

    test["longitude"] = ss.fit_transform(test[["longitude"]])
    test["latitude"] = ss.fit_transform(test[["latitude"]])
    test["total_rooms"] = ss.fit_transform(test[["total_rooms"]])
    test["total_bedrooms"] = ss.fit_transform(test[["total_bedrooms"]])
    test["households"] = ss.fit_transform(test[["households"]])
    test["population"] = ss.fit_transform(test[["population"]])
    test["median_income"] = ss.fit_transform(test[["median_income"]])

    train.to_csv("./dataset/train.csv", index=False)
    test.to_csv("./dataset/test.csv", index=False)
