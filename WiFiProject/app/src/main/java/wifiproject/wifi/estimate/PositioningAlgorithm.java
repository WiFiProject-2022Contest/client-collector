package wifilocation.wifi.estimate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import wifilocation.wifi.model.WiFiItem;

public class PositioningAlgorithm {
    static List<RecordPoint> tp;
    static List<RecordPoint> rp;
    static List<WiFiItem> previousDatabase = null;

    static String lastMethod = "";
    static int lastGHZ = 0;
    static int lastK = 0;
    static int lastMinValidAPNum = 0;
    static int lastMinDbm = 0;

    public static EstimatedResult run(List<WiFiItem> userData, List<WiFiItem> databaseData, String targetBuilding, String targetSSID, String targetUUID, String method, int targetGHZ, double standardRecordDistance) {
        int K;
        int minValidRPNum;
        int minDbm;

        if (method.equals("WiFi")) {
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
        // 데이터베이스에 대한 작업은 기존에 변환한 정보가 없거나 받은 데이터베이스 정보가 변경되었을 때만 시행합니다.
        tp = getRecordPointList(userData, targetBuilding, method, targetSSID, targetGHZ, minDbm);
        if (tp.size() == 0) {
            return null;
        }

        if (databaseData != previousDatabase || !method.equals(lastMethod) || lastGHZ != targetGHZ || lastK != K || lastMinValidAPNum != minValidAPNum || lastMinDbm != minDbm) {
            rp = getRecordPointList(databaseData, targetBuilding, method, targetSSID, targetGHZ, minDbm);

            previousDatabase = databaseData;
            lastMethod = method;
            lastGHZ = targetGHZ;
            lastK = K;
            lastMinValidAPNum = minValidAPNum;
            lastMinDbm = minDbm;
        }

        // 변환된 정보를 함수에 넣어서 추정값을 반환받습니다.
        EstimatedResult estimatedResult = new EstimatedResult(targetBuilding, targetSSID, targetUUID, method + "-" + targetGHZ + "Ghz", K, minDbm, 2);
        double[] positionResult = estimate(tp.get(0), rp, K, minValidRPNum, minValidAPNum, minDbm, standardRecordDistance, estimatedResult.getEstimateReason());
        if (positionResult == null) {
            return null;
        }

        estimatedResult.setPositionEstimatedX(positionResult[0]);
        estimatedResult.setPositionEstimatedY(positionResult[1]);

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
            workingRP.getRSSI().put(databaseRow.getBSSID(), databaseRow.getRSSI());
        }

        return rp;
    }

    static double[] estimate(RecordPoint tp, List<RecordPoint> rp, int K, int minValidRPNum, int minValidAPNum, int minDbm, double standardRecordDistance, StringBuilder estimateReason) {
        List<RecordPoint> vrp = interpolation(rp, standardRecordDistance);
        return weightedKNN(tp, vrp, K, minValidRPNum, minValidAPNum, minDbm, estimateReason);
    }

    static List<RecordPoint> interpolation(List<RecordPoint> rp, double standardRecordDistance) {
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
                    double distanceSum = dbmToDistance(rp.get(i).getRSSI().get(BSSID)) + dbmToDistance(rp.get(j).getRSSI().get(BSSID));
                    newRP.getRSSI().put(BSSID, distanceToDbm(distanceSum / 2));
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

    static double[] weightedKNN(RecordPoint tp, List<RecordPoint> rp, int K, int minValidRPNum, int minValidAPNum, int minDbm, StringBuilder estimateReason) {
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

        Map<RecordPoint, Double> nearDistance = getKNearDistance(tp, candidateRP, K, maxDbm, minDbm);
        // 아무것도 추정되지 않은 경우, 서비스 지역이 있지 않은 경우임
        if (nearDistance.size() < minValidRPNum) {
            return null;
        }

        // K개의 최근접 AP를 토대로 평가용 가중치를 산정하는 과정
        Map<RecordPoint, Double> evaluateDistance = getKNearDistance(tp, new ArrayList<RecordPoint>(nearDistance.keySet()), K, maxDbm, minDbm);

        Map<RecordPoint, Double> weight = new HashMap<>();
        for (Entry<RecordPoint, Double> entry : evaluateDistance.entrySet()) {
            weight.put(entry.getKey(), 1 / Math.pow(Math.E, entry.getValue()));
        }

        double totalWeight = 0;
        for (double val : weight.values()) {
            totalWeight += val;
        }

        // 최종 위치를 추정하는 과정
        double[] estimatedPosition = {0, 0};
        int nth = 0;
        for (Entry<RecordPoint, Double> entry : evaluateDistance.entrySet()) {
            double fraction = (weight.get(entry.getKey()) / totalWeight);

            for (int i = 0; i < 2; i++) {
                estimatedPosition[i] += fraction * entry.getKey().getLocation()[i];
            }

            estimateReason.append(nth++ + " (" + String.format("%.6f", entry.getKey().getLocation()[0]) + ", " + String.format("%.6f", entry.getKey().getLocation()[1]) + ") ");
            estimateReason.append(String.format("%.6f", fraction));
            estimateReason.append("\n");
        }

        return estimatedPosition;
    }

    static Map<RecordPoint, Double> getKNearDistance(RecordPoint tp, List<RecordPoint> rp, int K, int maxDbm, int minDbm) {
        Set<String> allBSSID = new HashSet<>(tp.getRSSI().keySet());
        for (int i = 0; i < rp.size(); i++) {
            allBSSID.addAll(rp.get(i).getRSSI().keySet());
        }

        Map<RecordPoint, Double> nearDistanceSquareSum = new HashMap<>();
        double maxDistanceSquareSum = Double.MAX_VALUE;
        for (int i = 0; i < rp.size(); i++) {
            double currentDistanceSquareSum = 0;

            for (String BSSID : allBSSID) {
                if (tp.getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().containsKey(BSSID) && rp.get(i).getRSSI().get(BSSID) >= minDbm) {
                    currentDistanceSquareSum += Math.pow(dbmToDistance(tp.getRSSI().get(BSSID)) - dbmToDistance(rp.get(i).getRSSI().get(BSSID)), 2);
                } else {
                    currentDistanceSquareSum += Math.pow(Math.abs(dbmToDistance(maxDbm) - dbmToDistance(minDbm - 10)), 2);
                }

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

    static double dbmToDistance(int dbm) {
        return Math.pow(10, (dbm + 35) / -32.2);
    }

    static int distanceToDbm(double distance) {
        return (int) Math.round(-32.2 * Math.log10(distance) - 35);
    }
}
