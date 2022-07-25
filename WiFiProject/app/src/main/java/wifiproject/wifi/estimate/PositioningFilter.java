package wifilocation.wifi.estimate;

import java.util.ArrayList;
import java.util.List;

public class PositioningFilter {
    List<KalmanFilter> filterList = new ArrayList<>(2);

    public EstimatedResult run(EstimatedResult estimatedResult, long timestamp) {
        if (estimatedResult == null) {
            return null;
        }

        double[] estimatedPosition = new double[] {estimatedResult.getPositionEstimatedX(), estimatedResult.getPositionEstimatedY()};
        double[] filteredPosition = new double[2];
        for (int i = 0; i < 2; i++) {
            filteredPosition[i] = filterList.get(i).run(estimatedPosition[i], timestamp);
        }

        EstimatedResult filteredResult = new EstimatedResult(estimatedResult);
        filteredResult.setPositionEstimatedX(filteredPosition[0]);
        filteredResult.setPositionEstimatedY(filteredPosition[1]);

        return filteredResult;
    }
}

class KalmanFilter {
    double A;
    double H;
    double Q;
    double R;

    double previousEstimatedData;
    double previousP;
    long previousTime;
    boolean firstAttempt;

    public KalmanFilter() {
        firstAttempt = true;
    }

    public KalmanFilter(String method) {
        this();

        A = 1;
        H = 1;
        Q = 4;
        R = 36;
    }

    public double run(double measuredData, long timestamp) {
        if (firstAttempt) {
            previousEstimatedData = measuredData;
            previousP = 0;
            previousTime = timestamp;

            firstAttempt = false;
            return previousEstimatedData;
        }

        // 1. Prediction
        double predictedData = A * previousEstimatedData;
        double predictedP = A * previousP * A + Q;

        // 2. Kalman Gain
        double K = (predictedP * H) / (H * predictedP * H + R);

        // 3. Estimation
        double estimatedData = predictedData + K * (measuredData - H * predictedData);

        // 4. Error Covariance
        double P = predictedP - K * H * predictedP;

        previousEstimatedData = estimatedData;
        previousP = P;
        previousTime = timestamp;
        return estimatedData;
    }
}
