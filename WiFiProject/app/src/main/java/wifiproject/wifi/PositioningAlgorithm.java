package wifilocation.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PositioningAlgorithm {
    static RecordPoint tp;
    static List<RecordPoint> rp;
    static List<WiFiItem> previousDatabase = null;

    public static double[] run(List<WiFiItem> userData, List<WiFiItem> databaseData) {
        // 데이터베이스는 한 줄에 하나의 AP 정보가 담겨있기 때문에
        // 이것을 다루기 쉽게 한 측정 지점에서 측정한 RSSI 값들을 모두 하나의 RecordPoint 객체에 담아주는 과정입니다.
        // 데이터베이스에 대한 작업은 기존에 변환한 정보가 없거나 받은 데이터베이스 정보가 변경되었을 때만 시행합니다.
        tp = transform(userData).get(0);
        if (databaseData != previousDatabase) {
            rp = transform(databaseData);
            previousDatabase = databaseData;
        }

        // 변환된 정보를 함수에 넣어서 추정값을 반환받습니다.
        return estimate(tp, rp);
    }

    static List<RecordPoint> transform(List<WiFiItem> databaseData) {
        List<RecordPoint> rp = new ArrayList<>();

        for (WiFiItem databaseRow : databaseData) {
            RecordPoint workingRP = null;

            for (RecordPoint recordPoint : rp) {
                if (databaseRow.getX() == recordPoint.getLocation()[0] && databaseRow.getY() == recordPoint.getLocation()[1]) {
                    //RecordPoint newRecordPoint = new RecordPoint(new double[]);
                    workingRP = recordPoint;

                    break;
                }
            }

            if (workingRP == null) {
                workingRP = new RecordPoint(new double[] {databaseRow.getX(), databaseRow.getY()});
                rp.add(workingRP);
            }
            workingRP.getRSSI().put(databaseRow.getBSSID(), databaseRow.getRSSI());
        }

        return rp;
    }

    static double[] estimate(RecordPoint tp, List<RecordPoint> rp) {
        List<RecordPoint> vrp = interpolation(rp, 3);
        return weightedKNN(tp, vrp, 5, 2, -0, -70);
    }

    static List<RecordPoint> interpolation(List<RecordPoint> rp, double standardRecordDistance) {
        List<RecordPoint> vrp = new ArrayList<>(rp);

        for (int i = 0; i < rp.size(); i++) {
            for (int j = i + 1; j < rp.size(); j++) {
                double distanceSquare = 0;
                for (int k = 0; k < 2; k++) {
                    distanceSquare += Math.pow(rp.get(i).getLocation()[k] - rp.get(j).getLocation()[k], 2);
                }

                if (distanceSquare > Math.pow(standardRecordDistance, 2) * 2) {
                    continue;
                }

                Set<String> intersectBSSID = new HashSet<>(rp.get(i).getRSSI().keySet());
                intersectBSSID.retainAll(rp.get(j).getRSSI().keySet());

                RecordPoint newRP = new RecordPoint();
                for (String BSSID : intersectBSSID) {
                    newRP.getRSSI().put(BSSID, (rp.get(i).getRSSI().get(BSSID) + rp.get(j).getRSSI().get(BSSID)) / 2);
                }
                for (int k = 0; k < 2; k++) {
                    newRP.getLocation()[k] = (rp.get(i).getLocation()[k] + rp.get(j).getLocation()[k]) / 2;
                }

                vrp.add(newRP);
            }
        }

        return vrp;
    }

    static double[] weightedKNN(RecordPoint tp, List<RecordPoint> rp, int K, int minAPNum, int maxDbm, int minDbm) {
        // K개의 최근접 RP를 선별하는 과정
        List<RecordPoint> candidateRP = new ArrayList<>();
        for (int i = 0; i < rp.size(); i++) {
            Set<String> intersectBSSID = new HashSet<>(rp.get(i).getRSSI().keySet());
            intersectBSSID.retainAll(tp.getRSSI().keySet());
            for (String BSSID : new HashSet<>(intersectBSSID)) {
                if (rp.get(i).getRSSI().get(BSSID) < minDbm) {
                    intersectBSSID.remove(BSSID);
                }
            }

            if (intersectBSSID.size() < minAPNum) {
                continue;
            }

            candidateRP.add(rp.get(i));
        }

        Map<RecordPoint, Double> nearDistance = getKNearDistance(tp, candidateRP, K, maxDbm, minDbm);

        // K개의 최근접 AP를 토대로 평가용 가중치를 산정하는 과정
        Map<RecordPoint, Double> evaluateDistance = getKNearDistance(tp, new ArrayList<RecordPoint>(nearDistance.keySet()), K, maxDbm, minDbm);

        Map<RecordPoint, Double> weight = new HashMap<>();
        for (Entry<RecordPoint, Double> entry : evaluateDistance.entrySet()) {
            weight.put(entry.getKey(), 1 / (entry.getValue() + 0.01));
        }

        double totalWeight = 0;
        for (double val : weight.values()) {
            totalWeight += val;
        }

        // 최종 위치를 추정하는 과정
        double[] estimatedPosition = {0, 0};
        for (RecordPoint key : evaluateDistance.keySet()) {
            for (int i = 0; i < 2; i++) {
                double fraction = (weight.get(key) / totalWeight);
                estimatedPosition[i] += fraction * key.getLocation()[i];
            }
        }

        return estimatedPosition;
    }

    static Map<RecordPoint, Double> getKNearDistance(RecordPoint tp, List<RecordPoint> rp, int K, int maxDbm, int minDbm) {
        Set<String> allBSSID = new HashSet<>(tp.getRSSI().keySet());

        for (int i = 0; i < rp.size(); i++) {
            allBSSID.addAll(rp.get(i).getRSSI().keySet());
        }

        Map<RecordPoint, Double> nearDistance = new HashMap<>();
        double maxDistance = Double.MAX_VALUE;
        for (int i = 0; i < rp.size(); i++) {
            double currentDistanceSquare = 0;

            for (String BSSID : allBSSID) {
                if (tp.getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().get(BSSID) >= minDbm) {
                    currentDistanceSquare += Math.pow(tp.getRSSI().get(BSSID) - rp.get(i).getRSSI().get(BSSID), 2);
                } else {
                    currentDistanceSquare += Math.pow(Math.abs(maxDbm - minDbm) + 10, 2);
                }

                if (nearDistance.size() >= K && currentDistanceSquare >= Math.pow(maxDistance, 2)) {
                    break;
                }
            }

            if (nearDistance.size() >= K && currentDistanceSquare >= Math.pow(maxDistance, 2)) {
                continue;
            }

            double currentDistance = Math.sqrt(currentDistanceSquare) / allBSSID.size();
            nearDistance.put(rp.get(i), currentDistance);
            if (nearDistance.size() > K) {
                nearDistance.values().remove(maxDistance);
            }

            maxDistance = Collections.max(nearDistance.values());
        }

        return nearDistance;
    }
}
