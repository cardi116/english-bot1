package com.englishbot;

import java.sql.*;
import java.util.*;

public class Database {

    private static final String DB_URL = "jdbc:h2:./data/english_bot_db";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;

    public Database() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
            System.out.println("✅ База данных подключена!");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Таблица пользователей
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT PRIMARY KEY,
                username VARCHAR(100),
                first_name VARCHAR(100),
                registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        // Таблица слов (с примерами и значением)
        String createWordsTable = """
            CREATE TABLE IF NOT EXISTS user_words (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                word VARCHAR(100) NOT NULL,
                translation VARCHAR(200) NOT NULL,
                example VARCHAR(500),
                meaning VARCHAR(500),
                correct_count INT DEFAULT 0,
                wrong_count INT DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """;
// Таблица состояний пользователей (для тренировки)
        String createStatesTable = """
        CREATE TABLE IF NOT EXISTS user_states (
            user_id BIGINT PRIMARY KEY,
            state VARCHAR(50),
            current_word VARCHAR(100),
            FOREIGN KEY (user_id) REFERENCES users(user_id)
        )
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createWordsTable);
            stmt.execute(createStatesTable);  // Добавить эту строку
            System.out.println("✅ Таблицы созданы/проверены");
        }
    }

    // Регистрация нового пользователя
    public void registerUser(long userId, String username, String firstName) {
        String sql = "MERGE INTO users (user_id, username, first_name) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, firstName);
            pstmt.executeUpdate();

            // Добавляем встроенные слова новому пользователю
            addBuiltInWords(userId);

        } catch (SQLException e) {
            System.out.println("❌ Ошибка регистрации пользователя: " + e.getMessage());
        }
    }

    // Добавить встроенные слова пользователю
    public void addBuiltInWords(long userId) {
        String checkSql = "SELECT COUNT(*) FROM user_words WHERE user_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setLong(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO user_words (user_id, word, translation, example, meaning) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    for (Dictionary.WordEntry entry : Dictionary.getAllWords().values()) {
                        insertStmt.setLong(1, userId);
                        insertStmt.setString(2, entry.getWord());
                        insertStmt.setString(3, entry.getTranslation());
                        insertStmt.setString(4, entry.getExample());
                        insertStmt.setString(5, entry.getMeaning());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                    System.out.println("✅ Добавлено " + Dictionary.size() + " встроенных слов для пользователя " + userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления встроенных слов: " + e.getMessage());
        }
    }

    // Добавление своего слова
    public boolean addWord(long userId, String word, String translation) {
        String sql = "INSERT INTO user_words (user_id, word, translation, example, meaning) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, word.toLowerCase());
            pstmt.setString(3, translation.toLowerCase());
            pstmt.setString(4, "Добавлено пользователем");
            pstmt.setString(5, "Пользовательское слово");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления слова: " + e.getMessage());
            return false;
        }
    }

    // Получение случайного слова для тренировки
    public Map<String, String> getRandomWord(long userId) {
        String sql = "SELECT word, translation FROM user_words WHERE user_id = ? ORDER BY RANDOM() LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, String> word = new HashMap<>();
                word.put("word", rs.getString("word"));
                word.put("translation", rs.getString("translation"));
                return word;
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения слова: " + e.getMessage());
        }
        return null;
    }

    // Получить перевод слова (для проверки тренировки)
    public String getTranslation(long userId, String word) {
        String sql = "SELECT translation FROM user_words WHERE user_id = ? AND word = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, word.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("translation");
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения перевода: " + e.getMessage());
        }
        return null;
    }

    // Обновление статистики слова
    public void updateWordStats(long userId, String word, boolean isCorrect) {
        String sql;
        if (isCorrect) {
            sql = "UPDATE user_words SET correct_count = correct_count + 1 WHERE user_id = ? AND word = ?";
        } else {
            sql = "UPDATE user_words SET wrong_count = wrong_count + 1 WHERE user_id = ? AND word = ?";
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, word.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка обновления статистики: " + e.getMessage());
        }
    }

    // Получение статистики пользователя
    public String getUserStats(long userId) {
        String sql = "SELECT COUNT(*) as total, SUM(correct_count) as correct FROM user_words WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int correct = rs.getInt("correct");
                if (total == 0) {
                    return "📭 У тебя пока нет слов в словаре!\n\nДобавь слова командой /add";
                }
                int progress = (correct * 100) / (total * 10);
                if (progress > 100) progress = 100;

                return "📊 *Твоя статистика*\n\n" +
                        "📚 Всего слов: " + total + "\n" +
                        "✅ Правильных ответов: " + correct + "\n" +
                        "🎯 Прогресс: " + progress + "%\n\n" +
                        "Продолжай тренироваться! 💪";
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка статистики: " + e.getMessage());
        }
        return "❌ Не удалось получить статистику";
    }

    // Получить список всех слов пользователя
    public List<Map<String, String>> getUserWords(long userId) {
        List<Map<String, String>> words = new ArrayList<>();
        String sql = "SELECT word, translation, correct_count, wrong_count FROM user_words WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> word = new HashMap<>();
                word.put("word", rs.getString("word"));
                word.put("translation", rs.getString("translation"));
                word.put("correct", String.valueOf(rs.getInt("correct_count")));
                word.put("wrong", String.valueOf(rs.getInt("wrong_count")));
                words.add(word);
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения списка слов: " + e.getMessage());
        }
        return words;
    }

    // Проверка, существует ли пользователь
    public boolean userExists(long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка проверки пользователя: " + e.getMessage());
        }
        return false;
    }

    // Закрытие соединения
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("✅ Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка закрытия БД: " + e.getMessage());
        }

    }
    // Сохранить состояние пользователя
    public void setUserState(long userId, String state, String currentWord) {
        String sql = "MERGE INTO user_states (user_id, state, current_word) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, state);
            pstmt.setString(3, currentWord);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения состояния: " + e.getMessage());
        }
    }

    // Получить состояние пользователя
    public Map<String, String> getUserState(long userId) {
        String sql = "SELECT state, current_word FROM user_states WHERE user_id = ?";
        Map<String, String> state = new HashMap<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                state.put("state", rs.getString("state"));
                state.put("current_word", rs.getString("current_word"));
            } else {
                state.put("state", "IDLE");
                state.put("current_word", null);
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения состояния: " + e.getMessage());
        }
        return state;
    }

    // Сбросить состояние
    public void resetUserState(long userId) {
        String sql = "DELETE FROM user_states WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сброса состояния: " + e.getMessage());
        }
    }
    // Получение неправильных вариантов перевода для кнопок викторины
    public List<String> getWrongTranslations(String correctTranslation, int count) {
        List<String> wrongs = new ArrayList<>();
        // В базе H2 для случайной сортировки используется RAND()
        String sql = "SELECT DISTINCT translation FROM user_words WHERE translation != ? ORDER BY RAND() LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correctTranslation);
            pstmt.setInt(2, count);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                wrongs.add(rs.getString("translation"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения вариантов перевода: " + e.getMessage());
        }

        // Защита от пустой базы:
        // Если в словаре мало слов и база не смогла найти 3 неправильных варианта,
        // мы добавим стандартные слова-заглушки, чтобы программа не вылетела с ошибкой.
        List<String> fallbacks = Arrays.asList("яблоко", "машина", "книга", "дом", "собака", "университет", "компьютер", "солнце");
        int fallbackIndex = 0;

        while (wrongs.size() < count) {
            String fallback = fallbacks.get(fallbackIndex % fallbacks.size());
            // Добавляем заглушку, только если она не совпадает с правильным ответом
            if (!wrongs.contains(fallback) && !fallback.equalsIgnoreCase(correctTranslation)) {
                wrongs.add(fallback);
            }
            fallbackIndex++;
        }

        return wrongs;
    }
}