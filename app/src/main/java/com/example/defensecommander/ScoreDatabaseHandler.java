package com.example.defensecommander;

import android.util.Log;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScoreDatabaseHandler implements Runnable {

    private final String TAG = getClass().getSimpleName();

    private final MainActivity context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    private Connection conn;
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String STUDENT_TABLE = "AppScores";
    private final static String dbName = "chri5558_missile_defense";
    private final static String dbURL = "jdbc:mysql://christopherhield.com:3306/" + dbName;
    private final static String dbUser = "chri5558_student";
    private final static String dbPass = "ABC.123";

    private final String initials;
    private final int score;
    private final int level;
    private int tenthScore = 0;

    ScoreDatabaseHandler(MainActivity ctx, String initials, int score, int level) {
        context = ctx;
        this.initials = initials;
        this.score = score;
        this.level = level;
    }

    public void run() {

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbURL, dbUser, dbPass);

            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();

            if ((initials != "NUL") && (score != -1) && (level != 0)) {
                Statement stmt = conn.createStatement();

                String sql = "insert into " + STUDENT_TABLE + " values (" +
                        System.currentTimeMillis() + ", '" +
                        initials + "', " + score + ", " + level + ")";
                Log.d(TAG, "run: " + sql);

                int result = stmt.executeUpdate(sql);

                stmt.close();
                sb2.append(getAllTopTen());
            }

            String response = "Top Players\n";

            sb.append(response);
            sb.append(getAllTopTen());

            tenthScore = getTenthScore();
            context.setResults(sb.toString(), sb2.toString(), tenthScore);
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAllTopTen() throws SQLException {
        Statement stmt = conn.createStatement();

        String sql = "select * from " + STUDENT_TABLE + " order by SCORE desc limit 10";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.getDefault(),
                "%-5s %-12s %-10s %-10s  %12s%n", "#", "Init", "Level",
                "Score", "Date/Time"));
        int number = 0;
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String initials = rs.getString(2);
            int level = rs.getInt(4);
            int score = rs.getInt(3);
            long millis = rs.getLong(1);
            number+=1;
            sb.append(String.format(Locale.getDefault(),
                    "%-5d %-12s %-10d %-10d  %12s%n", number, initials, level, score, sdf.format(new Date(millis))));
        }
        rs.close();
        stmt.close();

        return sb.toString();
    }
    private int getTenthScore() throws SQLException {
        Statement stmt = conn.createStatement();

        String sql = "select * from " + STUDENT_TABLE + " order by SCORE desc limit 10";

        StringBuilder sb = new StringBuilder();
        int number = 0;
        int finalScore = 0;
        int nextScore = 0;
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String initials = rs.getString(2);
            int level = rs.getInt(4);
            nextScore = rs.getInt(3);
            long millis = rs.getLong(1);
            number+=1;
            sb.append(String.format(Locale.getDefault(),
                    "%-5d", nextScore));
        }
        if (number < 10){
            finalScore = 0;
        }
        else {
            finalScore = nextScore;
        }
        rs.close();
        stmt.close();

        return finalScore;
    }
}
