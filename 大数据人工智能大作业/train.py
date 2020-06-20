import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.preprocessing import OneHotEncoder
import joblib

if __name__ == "__main__":
    data = pd.read_csv("./dataset/train.csv")

    X, y = data.drop("median_house_value", axis=1), data.get("median_house_value")

    rf = RandomForestRegressor()

    rf.fit(X, y)

    joblib.dump(rf, "./model/randomforest.pkl", compress=3)

    # 预测
    test_data = pd.read_csv("./dataset/test.csv")

    test_X, test_y = test_data.drop("median_house_value", axis=1), test_data.get("median_house_value")

    prediction = pd.DataFrame(rf.predict(test_X), columns=["prediction"])

    print("MSE :", mean_squared_error(test_y, prediction))
    print("r2 :", r2_score(test_y, prediction))

    pd.merge(test_y, prediction, left_index=True, right_index=True).to_csv("./output/result.csv", index=False)