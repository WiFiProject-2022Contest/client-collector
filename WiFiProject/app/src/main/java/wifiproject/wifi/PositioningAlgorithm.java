package wifilocation.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PositioningAlgorithm {
    public static double[] run(List<WiFiItem> databaseData) {
        List<RecordPoint> rp = transform(databaseData);
        double[] estimatedPosition = estimate(new RecordPoint(new double[] {0, 0}), rp);

        return estimatedPosition;
    }

    static List<RecordPoint> transform(List<WiFiItem> databaseData) {
        List<RecordPoint> rp = new ArrayList<>();

        for (WiFiItem databaseRow : databaseData) {
            boolean locationExists = false;

            for (RecordPoint recordPoint : rp) {
            }
        }

        return rp;
    }

    static double[] estimate(RecordPoint tp, List<RecordPoint> rp) {
        List<RecordPoint> vrp = interpolation(rp, 3);
        return weightedKNN(tp, vrp, 3, 2, -30, -70);
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

        Set<String> allBSSID = new HashSet<>(tp.getRSSI().keySet());
        for (int i = 0; i < rp.size(); i++) {
            allBSSID.addAll(rp.get(i).getRSSI().keySet());
        }

        Map<RecordPoint, Double> nearDistance = new HashMap<>();
        double maxDistance = Double.MAX_VALUE;
        for (int i = 0; i < candidateRP.size(); i++) {
            double currentDistanceSquare = 0;

            for (String BSSID : allBSSID) {
                if (candidateRP.get(i).getRSSI().containsKey(BSSID) && candidateRP.get(i).getRSSI().get(BSSID) >= minDbm) {
                    currentDistanceSquare += Math.pow(candidateRP.get(i).getRSSI().get(BSSID), 2);
                }
                else {
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
            nearDistance.put(candidateRP.get(i), currentDistance);
            if (nearDistance.size() > K) {
                nearDistance.values().remove(maxDistance);
            }

            maxDistance = Collections.max(nearDistance.values());
        }

        Map<RecordPoint, Double> weight = new HashMap<>();
        for (Entry<RecordPoint, Double> entry : nearDistance.entrySet()) {
            weight.put(entry.getKey(), 1 / (entry.getValue() + 0.01));
        }

        double totalWeight = 0;
        for (double val : weight.values()) {
            totalWeight += val;
        }

        double[] estimatedPosition = {0, 0};
        for (RecordPoint key : nearDistance.keySet()) {
            for (int i = 0; i < 2; i++) {
                double fraction = (weight.get(key) / totalWeight);
                estimatedPosition[i] += fraction * key.getLocation()[i];
            }
        }

        return estimatedPosition;
    }
}
