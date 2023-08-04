package main;
import main.AsyncParser.*;

import java.sql.*;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        ArrayList<MusicFile> unity = AsyncParser.getMusicFilesAsync().get();
        String jdbcURL = "jdbc:postgresql://localhost:5432/java_parser_musify";
        String username = "avalance";
        String password = "";
        try {
            Connection connection = DriverManager.getConnection(jdbcURL);
            System.out.println("Connected to SQL");
            int counter1 = 0;
            int counter2 = 0;
            String query = "INSERT INTO songs (author, name_song, mp3) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            for (MusicFile musicFile: unity) {
                statement.setString(1, musicFile.getArtist());
                statement.setString(2, musicFile.getSong());
                statement.setString(3, musicFile.getMp3());
                int rowsInserted = statement.executeUpdate();
            }

            System.out.println("Ура, победа");
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Время выполнения программы: " + totalTime + "мс");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error");
            e.printStackTrace();
        }


    }
}