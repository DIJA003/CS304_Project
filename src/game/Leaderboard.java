package game;

import java.util.*;
import java.io.*;

public class Leaderboard {
    private static final String path = "src//assets//leaderboard.txt";

    public static class Record implements Comparable<Record> {
        String playerName;
        long timeInSec;

        public Record(String playerName, long timeInSec) {
            this.playerName = playerName;
            this.timeInSec = timeInSec;
        }

        @Override
        public int compareTo(Record other) {
            return Long.compare(this.timeInSec, other.timeInSec);
        }

        @Override
        public String toString() {
            long minutes = timeInSec / 60;
            long seconds = timeInSec % 60;
            return playerName + " - " + String.format("%02d:%02d", minutes, seconds);
        }
        public String getTime(){
            long minutes = timeInSec / 60;
            long seconds = timeInSec % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static void addRecord(String name, long timeInSec) {
        List<Record> records = getRecords();
        records.add(new Record(name, timeInSec));
        Collections.sort(records);
        saveRecords(records);
    }

    public static void saveRecords(List<Record> records) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Record rec : records) {
                bw.write(rec.playerName + "," + rec.timeInSec);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Record> getRecords() {
        List<Record> records = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return records;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    long time = Long.parseLong(parts[1]);
                    records.add(new Record(name, time));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
}
