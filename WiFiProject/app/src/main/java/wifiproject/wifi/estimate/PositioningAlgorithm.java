package wifilocation.wifi.estimate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import wifilocation.wifi.model.WiFiItem;

public class PositioningAlgorithm {
    public static EstimatedResult run(List<WiFiItem> userData, List<WiFiItem> databaseData, String targetBuilding, String targetSSID, String targetUUID, String method, int targetGHZ, double standardRecordDistance) {
        int K;
        int minValidRPNum;
        int minDbm;

        if (method.equals("WiFi")) {
            EstimatedResult estimatedResult = performKNN(userData, databaseData, targetBuilding, targetSSID, targetUUID, method, targetGHZ, standardRecordDistance, 0, 2, 1, -70);

            /*
            K = 7;

            EstimatedResult estimatedResult = null;
            for (minDbm = -45; minDbm >= -65; minDbm -= 5) {
                if (minDbm == -45) {
                    minValidRPNum = 1;
                }
                else {
                    minValidRPNum = 2;
                }

                estimatedResult = performKNN(userData, databaseData, targetBuilding, targetSSID, targetUUID, method, targetGHZ, standardRecordDistance, K, minValidRPNum, 1, minDbm);

                if (estimatedResult != null) {
                    break;
                }
            }
             */

            return estimatedResult;
        }
        else if (method.equals("BLE")) {
            K = 3;
            minValidRPNum = 1;
            minDbm = -70;

            return performKNN(userData, databaseData, targetBuilding, targetSSID, targetUUID, method, targetGHZ, standardRecordDistance, K, minValidRPNum, 1, minDbm);
        }
        else if (method.equals("iBeacon")) {
            K = 3;
            minValidRPNum = 1;
            minDbm = -70;

            return performKNN(userData, databaseData, targetBuilding, targetSSID, targetUUID, method, targetGHZ, standardRecordDistance, K, minValidRPNum, 1, minDbm);
        }
        else if (method.equals("combined")) {
            return null;
        }
        else {
            return null;
        }
    }

    public static List<EstimatedResult> runRange(List<WiFiItem> userData, List<WiFiItem> databaseData, String targetBuilding, String targetSSID, String targetUUID, String method,
                                                 int targetGHZ, double standardRecordDistance, int[] infoK, int[] infoMinValidAPNum, int[] infoMinDbm) {
        List<EstimatedResult> results = new ArrayList<>();

        for (int K = infoK[0]; K < infoK[1]; K += infoK[2]) {
            for (int minDbm = infoMinDbm[0]; minDbm < infoMinDbm[1]; minDbm += infoMinDbm[2]) {
                EstimatedResult result = performKNN(userData, databaseData, targetBuilding, targetSSID, targetUUID, method, targetGHZ, standardRecordDistance, K, 1, 1, minDbm);

                if (result != null) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    public static EstimatedResult performKNN(List<WiFiItem> userData, List<WiFiItem> databaseData, String targetBuilding, String targetSSID, String targetUUID,
                                             String method, int targetGHZ, double standardRecordDistance, int K, int minValidRPNum, int minValidAPNum, int minDbm) {
        // 데이터베이스는 한 줄에 하나의 AP 정보가 담겨있기 때문에
        // 이것을 다루기 쉽게 한 측정 지점에서 측정한 RSSI 값들을 모두 하나의 RecordPoint 객체에 담아주는 과정입니다.
        List<RecordPoint> tp = getRecordPointList(userData, targetBuilding, method, targetSSID, targetGHZ, minDbm);
        if (tp.size() == 0) {
            return null;
        }
        List<RecordPoint> rp = getRecordPointList(databaseData, targetBuilding, method, targetSSID, targetGHZ, minDbm);

        // 변환된 정보를 함수에 넣어서 추정값을 반환받습니다.
        String resultMethodName;
        if (method.equals("WiFi")) {
            resultMethodName = method + "-" + targetGHZ + "Ghz";
        }
        else {
            resultMethodName = method;
        }
        EstimatedResult estimatedResult = new EstimatedResult(targetBuilding, targetSSID, targetUUID, resultMethodName, K, minDbm, 5);
        List<RecordPoint> vrp = interpolation(rp, method, standardRecordDistance);
        double[][] positionResult = weightedKNN(tp.get(0), vrp, method, 2, K, minValidRPNum, minValidAPNum, minDbm, estimatedResult.getEstimateReason());
        if (positionResult == null) {
            return null;
        }

        estimatedResult.setPositionEstimatedX(positionResult[0][0]);
        estimatedResult.setPositionEstimatedY(positionResult[0][1]);
        estimatedResult.setK((int) positionResult[1][0]);

        return estimatedResult;
    }

    static List<RecordPoint> getRecordPointList(List<WiFiItem> databaseData, String targetBuilding, String method, String targetSSID, int targetGHZ, int minDbm) {
        List<RecordPoint> rp = new ArrayList<>();

        for (WiFiItem databaseRow : databaseData) {
            RecordPoint workingRP = null;

            // 유사 위치를 동일 좌표로 간주하기 위해서 스캔 좌표 소수점을 반올림 처리 (현실적 판단)
            databaseRow.setX(Math.round(databaseRow.getX()));
            databaseRow.setY(Math.round(databaseRow.getY()));

            if (!targetBuilding.equals(databaseRow.getBuilding())
                    || !method.equals(databaseRow.getMethod())
                    || !targetSSID.equals(databaseRow.getSSID())
                    || method.equals("WiFi") && databaseRow.getFrequency() / 1000 != targetGHZ
                    || databaseRow.getRSSI() < minDbm) {
                continue;
            }

            for (RecordPoint recordPoint : rp) {
                if (databaseRow.getX() == recordPoint.getLocation()[0] && databaseRow.getY() == recordPoint.getLocation()[1]) {
                    workingRP = recordPoint;

                    break;
                }
            }

            if (workingRP == null) {
                workingRP = new RecordPoint(new double[] {databaseRow.getX(), databaseRow.getY()});
                rp.add(workingRP);
            }
            workingRP.getMethod().put(databaseRow.getBSSID(), databaseRow.getMethod());
            workingRP.getRSSI().put(databaseRow.getBSSID(), databaseRow.getRSSI());
            workingRP.getFrequency().put(databaseRow.getBSSID(), databaseRow.getFrequency());
        }

        return rp;
    }

    static List<RecordPoint> interpolation(List<RecordPoint> rp, String method, double standardRecordDistance) {
        List<RecordPoint> candidate = new ArrayList<>();

        for (int i = 0; i < rp.size(); i++) {
            for (int j = i + 1; j < rp.size(); j++) {
                double distanceSquare = 0;
                for (int k = 0; k < 2; k++) {
                    distanceSquare += Math.pow(rp.get(i).getLocation()[k] - rp.get(j).getLocation()[k], 2);
                }

                if (distanceSquare < Math.pow(standardRecordDistance, 2) * 0.6 || distanceSquare > Math.pow(standardRecordDistance, 2) * 2) {
                    continue;
                }

                Set<String> intersectBSSID = new HashSet<>(rp.get(i).getRSSI().keySet());
                intersectBSSID.retainAll(rp.get(j).getRSSI().keySet());

                RecordPoint newRP = new RecordPoint();
                for (String BSSID : intersectBSSID) {
                    if (method.equals("WiFi")) {
                        double rssiSum = dbmToDistanceWiFi(rp.get(i).getRSSI().get(BSSID)) + dbmToDistanceWiFi(rp.get(j).getRSSI().get(BSSID));
                        newRP.getRSSI().put(BSSID, distanceToDbmWiFi(rssiSum / 2));
                    }
                    else {
                        double rssiSum = rp.get(i).getRSSI().get(BSSID) + rp.get(j).getRSSI().get(BSSID);
                        newRP.getRSSI().put(BSSID, (int) Math.round(rssiSum / 2));
                    }
                    newRP.getMethod().put(BSSID, rp.get(i).getMethod().get(BSSID));
                    newRP.getFrequency().put(BSSID, rp.get(i).getFrequency().get(BSSID));
                }
                for (int k = 0; k < 2; k++) {
                    newRP.getLocation()[k] = (rp.get(i).getLocation()[k] + rp.get(j).getLocation()[k]) / 2;
                }

                candidate.add(newRP);
            }
        }

        List<RecordPoint> vrp = new ArrayList<>(rp);
        // 기존 RP와 지나치게 가까운 VRP 후보 삭제
        for (int i = 0; i < candidate.size(); i++) {
            boolean adjacent = true;

            for (int j = 0; j < rp.size(); j++) {
                for (int k = 0; k < 2; k++) {
                    if (Math.abs(candidate.get(i).getLocation()[k] - rp.get(j).getLocation()[k]) > 1) {
                        adjacent = false;
                        break;
                    }
                }

                if (adjacent) {
                    break;
                }
            }

            if (!adjacent) {
                vrp.add(candidate.get(i));
            }
        }

        return vrp;
    }

    static double[][] weightedKNN(RecordPoint tp, List<RecordPoint> rp, String method, double standardRecordDistance, int K, int minValidRPNum, int minValidAPNum, int minDbm, StringBuilder estimateReason) {
        // K개의 최근접 RP를 선별하는 과정
        List<RecordPoint> candidateRP = new ArrayList<>();
        for (int i = 0; i < rp.size(); i++) {
            Set<String> intersectBSSID = new HashSet<>(rp.get(i).getRSSI().keySet());
            intersectBSSID.retainAll(tp.getRSSI().keySet());

            if (intersectBSSID.size() < minValidAPNum) {
                continue;
            }

            candidateRP.add(rp.get(i));
        }

        int maxDbm = Integer.MIN_VALUE;
        for (RecordPoint recordPoint : candidateRP) {
            int maxDbmInRecordPoint = Collections.max(recordPoint.getRSSI().values());
            if (maxDbmInRecordPoint > maxDbm) {
                maxDbm = maxDbmInRecordPoint;
            }
        }

        boolean dynamicMode = false;
        if (K == 0) {
            K = candidateRP.size();
            dynamicMode = true;
        }
        Map<RecordPoint, Double> nearDistance = getKNearDistanceSingle(tp, candidateRP, method, K, maxDbm, minDbm);

        if (dynamicMode) {
            double minDistance = Collections.min(nearDistance.values());
            //double maxDistance = Collections.max(nearDistance.values());
            double ratio = 1.2;

            Map<RecordPoint, Double> adaptiveMap = new HashMap<>();
            for (Map.Entry<RecordPoint, Double> entry : nearDistance.entrySet()) {
                if (entry.getValue() <= minDistance * ratio) {
                    adaptiveMap.put(entry.getKey(), entry.getValue());
                }
            }

            // Delete the redundancy
            List<RecordPoint> keyList = new ArrayList<>(adaptiveMap.keySet());
            Collections.sort(keyList, new Comparator<RecordPoint>() {
                @Override
                public int compare(RecordPoint r1, RecordPoint r2) {
                    double diff = adaptiveMap.get(r1) - adaptiveMap.get(r2);
                    if (diff > 0) {
                        return 1;
                    }
                    else if (diff < 0) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });

            for (int i = 0; i < keyList.size(); i++) {
                for (int j = i + 1; j < keyList.size(); j++) {
                    double squareSum = 0;
                    for (int k = 0; k < 2; k++) {
                        squareSum += Math.pow(keyList.get(i).getLocation()[k] - keyList.get(j).getLocation()[k], 2);
                    }

                    if (Math.sqrt(squareSum) <= standardRecordDistance * Math.sqrt(2)) {
                        adaptiveMap.remove(keyList.get(j));
                    }
                }
            }

            nearDistance = adaptiveMap;
            K = adaptiveMap.size();

            // 기준에 맞는 RP 개수가 2개 이하면 강제로 2개로 재시도
            if (K < 2) {
                K = 2;
                nearDistance = getKNearDistanceSingle(tp, candidateRP, method, K, maxDbm, minDbm);
            }
        }

        // 기준에 맞는 RP 개수가 기준치에 미달하는 경우 실패
        if (nearDistance.size() < minValidRPNum) {
            return null;
        }

        Map<RecordPoint, Double> weight = new HashMap<>();
        for (Entry<RecordPoint, Double> entry : nearDistance.entrySet()) {
            double w = 1 / (entry.getValue() + 0.000001);
            weight.put(entry.getKey(), w);
        }

        double totalWeight = 0;
        for (double val : weight.values()) {
            totalWeight += val;
        }

        // 최종 위치를 추정하는 과정
        double[][] result = {null, {K}};
        double[] estimatedPosition = new double[2];
        int nth = 0;
        for (Entry<RecordPoint, Double> entry : nearDistance.entrySet()) {
            double fraction = weight.get(entry.getKey()) / totalWeight;

            for (int i = 0; i < 2; i++) {
                estimatedPosition[i] += fraction * entry.getKey().getLocation()[i];
            }

            estimateReason.append(nth++ + " (" + String.format("%.6f", entry.getKey().getLocation()[0]) + ", " + String.format("%.6f", entry.getKey().getLocation()[1]) + ") ");
            estimateReason.append(String.format("%.6f", fraction));
            estimateReason.append("\n");
        }

        result[0] = estimatedPosition;
        return result;
    }

    static Map<RecordPoint, Double> getKNearDistanceSingle(RecordPoint tp, List<RecordPoint> rp, String method, int K, int maxDbm, int minDbm) {
        Set<String> allBSSID = new HashSet<>(tp.getRSSI().keySet());

        Map<RecordPoint, Double> nearDistanceSquareSum = new HashMap<>();
        double maxDistanceSquareSum = Double.MAX_VALUE;
        for (int i = 0; i < rp.size(); i++) {
            double currentDistanceSquareSum = 0;

            for (String BSSID : allBSSID) {
                double distanceDiff;

                if (tp.getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().get(BSSID) >= minDbm) {
                    distanceDiff = tp.getRSSI().get(BSSID) - rp.get(i).getRSSI().get(BSSID);
                    /*
                    if (method.equals("WiFi")) {
                        distanceDiff = dbmToDistanceWiFi(tp.getRSSI().get(BSSID)) - dbmToDistanceWiFi(rp.get(i).getRSSI().get(BSSID));
                    }
                    else {
                        distanceDiff = tp.getRSSI().get(BSSID) - rp.get(i).getRSSI().get(BSSID);
                    }

                     */
                } else {
                    distanceDiff = maxDbm - minDbm + 10;
                    /*
                    if (method.equals("WiFi")) {
                        distanceDiff = Math.abs(dbmToDistanceWiFi(maxDbm) - dbmToDistanceWiFi(minDbm - 10));
                    }
                    else {
                        distanceDiff = maxDbm - minDbm + 10;
                    }

                     */
                }
                currentDistanceSquareSum += Math.pow(distanceDiff, 2);

                if (nearDistanceSquareSum.size() >= K && currentDistanceSquareSum >= maxDistanceSquareSum) {
                    break;
                }
            }

            if (nearDistanceSquareSum.size() >= K && currentDistanceSquareSum >= maxDistanceSquareSum) {
                continue;
            }

            nearDistanceSquareSum.put(rp.get(i), currentDistanceSquareSum);
            if (nearDistanceSquareSum.size() > K) {
                nearDistanceSquareSum.values().remove(maxDistanceSquareSum);
            }

            maxDistanceSquareSum = Collections.max(nearDistanceSquareSum.values());
        }

        Map<RecordPoint, Double> nearDistance = new HashMap<>();
        for (Entry<RecordPoint, Double> entry : nearDistanceSquareSum.entrySet()) {
            nearDistance.put(entry.getKey(), Math.sqrt(entry.getValue()) / rp.size());
        }

        return nearDistance;
    }

    static double dbmToDistanceWiFi(int dbm) {
        return Math.pow(10, (dbm + 35) / -32.2);
    }

    static int distanceToDbmWiFi(double distance) {
        return (int) Math.round(-32.2 * Math.log10(distance) - 35);
    }
}
