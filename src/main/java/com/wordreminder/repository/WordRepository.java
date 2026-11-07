package com.wordreminder.repository;

import com.wordreminder.data.Database;
import com.wordreminder.model.CefrLevel;
import com.wordreminder.model.Word;
import com.wordreminder.model.WordCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WordRepository {
    private static final String CATALOG_RESOURCE = "/com/wordreminder/data/word-catalog.tsv";
    private final Database database;

    public WordRepository(Database database) {
        this.database = database;
    }

    /** Loads the packaged catalogue and updates existing built-in words by English headword. */
    /** Exports the complete user database, including words, review history and settings. */
    public void backupDatabase(Path destination) throws SQLException, IOException {
        database.backupTo(destination);
    }

    /** Checks the integrity and contents of a previously exported backup. */
    public String verifyDatabaseBackup(Path backupFile) throws SQLException, IOException {
        return database.verifyBackup(backupFile);
    }

    public void seedBuiltInWords() throws SQLException, IOException {
        String updateSql = """
                UPDATE words
                SET english = ?, turkish = ?, example = ?, pronunciation = ?,
                    part_of_speech = ?, level = ?, category = ?
                WHERE english = ? COLLATE NOCASE AND source = 'BUILT_IN'
                  AND NOT EXISTS (
                      SELECT 1 FROM words custom_word
                      WHERE custom_word.source = 'CUSTOM'
                        AND custom_word.english = ? COLLATE NOCASE
                        AND custom_word.turkish = ?
                  )
                """;
        String insertSql = """
                INSERT INTO words(english, turkish, example, pronunciation, part_of_speech, source, level, category)
                VALUES(?, ?, ?, ?, ?, 'BUILT_IN', ?, ?)
                ON CONFLICT(english, turkish) DO NOTHING
                """;
        try (InputStream input = WordRepository.class.getResourceAsStream(CATALOG_RESOURCE)) {
            if (input == null) {
                throw new IOException("Kelime kataloğu bulunamadı: " + CATALOG_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                 Connection connection = database.getConnection();
                 PreparedStatement update = connection.prepareStatement(updateSql);
                 PreparedStatement insert = connection.prepareStatement(insertSql)) {
                connection.setAutoCommit(false);
                try {
                    String line;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (line.isBlank() || line.startsWith("#")) continue;
                        String[] fields = line.split("\\|", -1);
                        if (fields.length != 7) {
                            throw new IOException("Kelime kataloğunda " + lineNumber + ". satır hatalı; 7 alan bekleniyor.");
                        }
                        CefrLevel level;
                        WordCategory category;
                        try {
                            level = CefrLevel.valueOf(fields[0]);
                            category = WordCategory.valueOf(fields[1]);
                        } catch (IllegalArgumentException ex) {
                            throw new IOException("Kelime kataloğunda " + lineNumber + ". satırın seviyesi/kategorisi hatalı.", ex);
                        }

                        update.setString(1, fields[2]);
                        update.setString(2, fields[3]);
                        update.setString(3, fields[6]);
                        update.setString(4, fields[5]);
                        update.setString(5, fields[4]);
                        update.setString(6, level.name());
                        update.setString(7, category.name());
                        update.setString(8, fields[2]);
                        update.setString(9, fields[2]);
                        update.setString(10, fields[3]);
                        int updated = update.executeUpdate();

                        if (updated == 0) {
                            insert.setString(1, fields[2]);
                            insert.setString(2, fields[3]);
                            insert.setString(3, fields[6]);
                            insert.setString(4, fields[5]);
                            insert.setString(5, fields[4]);
                            insert.setString(6, level.name());
                            insert.setString(7, category.name());
                            insert.executeUpdate();
                        }
                    }
                    connection.commit();
                } catch (SQLException | IOException ex) {
                    connection.rollback();
                    throw ex;
                }
            }
        }
        seedExampleSentences();
    }

    private void seedExampleSentences() throws SQLException, IOException {
        String sql = "UPDATE words SET example = ?, example_translation = ? "
                + "WHERE english = ? COLLATE NOCASE AND source = 'BUILT_IN'";
        try (InputStream input = WordRepository.class.getResourceAsStream(
                "/com/wordreminder/data/example-sentences.tsv")) {
            if (input == null) throw new IOException("Örnek cümle kaynağı bulunamadı.");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                 Connection connection = database.getConnection();
                 PreparedStatement update = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);
                try {
                    String line;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (line.isBlank() || line.startsWith("#")) continue;
                        String[] fields = line.split("\\|", -1);
                        if (fields.length != 3 || fields[0].isBlank()
                                || fields[1].isBlank() || fields[2].isBlank()) {
                            throw new IOException("Örnek cümle kaynağında " + lineNumber + ". satır hatalı.");
                        }
                        update.setString(1, fields[1]);
                        update.setString(2, fields[2]);
                        update.setString(3, fields[0]);
                        update.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException | IOException ex) {
                    connection.rollback();
                    throw ex;
                }
            }
        }
    }

    public List<Word> findAll(String searchText) throws SQLException {
        return findAll(searchText, "ALL", "ALL");
    }

    public List<Word> findAll(String searchText, String level, String category) throws SQLException {
        return findAll(searchText, level, category, "ALL");
    }

    /** Filters the word catalogue by learning state without changing stored review data. */
    public List<Word> findAll(String searchText, String level, String category, String learningState) throws SQLException {
        String search = searchText == null ? "" : searchText.trim();
        String selectedLevel = normalizeFilter(level);
        String selectedCategory = normalizeFilter(category);
        String selectedState = normalizeFilter(learningState);
        String sql = """
                SELECT id, english, turkish, example, example_translation, pronunciation, part_of_speech, source,
                       level, category, known_count, repeat_count, is_hard, last_shown_at
                FROM words
                WHERE (? = '' OR english LIKE ? OR turkish LIKE ?)
                  AND (? = 'ALL' OR level = ?)
                  AND (? = 'ALL' OR category = ?)
                  AND (? = 'ALL'
                       OR (? = 'NEW' AND last_reviewed_at IS NULL)
                       OR (? = 'DUE' AND last_reviewed_at IS NOT NULL AND next_due_at <= datetime('now'))
                       OR (? = 'MASTERED' AND interval_days >= 21)
                       OR (? = 'STUDIED' AND last_shown_at IS NOT NULL)
                       OR (? = 'HARD' AND is_hard = 1))
                ORDER BY CASE source WHEN 'CUSTOM' THEN 0 ELSE 1 END,
                         level, category, english COLLATE NOCASE
                """;
        List<Word> words = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, search);
            statement.setString(2, "%" + search + "%");
            statement.setString(3, "%" + search + "%");
            statement.setString(4, selectedLevel);
            statement.setString(5, selectedLevel);
            statement.setString(6, selectedCategory);
            statement.setString(7, selectedCategory);
            statement.setString(8, selectedState);
            statement.setString(9, selectedState);
            statement.setString(10, selectedState);
            statement.setString(11, selectedState);
            statement.setString(12, selectedState);
            statement.setString(13, selectedState);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) words.add(map(result));
            }
        }
        return words;
    }

    public Optional<Word> findNextForReview() throws SQLException {
        return findNextForReview("ALL", "ALL");
    }

    public Optional<Word> findNextForReview(String level, String category) throws SQLException {
        String selectedLevel = normalizeFilter(level);
        String selectedCategory = normalizeFilter(category);
        String sql = """
                SELECT id, english, turkish, example, example_translation, pronunciation, part_of_speech, source,
                       level, category, known_count, repeat_count, is_hard, last_shown_at
                FROM words
                WHERE (? = 'ALL' OR level = ?)
                  AND (? = 'ALL' OR category = ?)
                ORDER BY CASE WHEN next_due_at <= datetime('now') THEN 0 ELSE 1 END,
                         next_due_at ASC,
                         COALESCE(last_shown_at, '1970-01-01 00:00:00') ASC,
                         RANDOM()
                LIMIT 1
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selectedLevel);
            statement.setString(2, selectedLevel);
            statement.setString(3, selectedCategory);
            statement.setString(4, selectedCategory);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                Word word = map(result);
                markShown(connection, word.id());
                return Optional.of(word);
            }
        }
    }

    /** Marks or unmarks a word as difficult without changing its review schedule. */
    public void setHardWord(long wordId, boolean hard) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE words SET is_hard = ? WHERE id = ?")) {
            statement.setInt(1, hard ? 1 : 0);
            statement.setLong(2, wordId);
            statement.executeUpdate();
        }
    }

    public void addCustomWord(String english, String turkish, String example, String exampleTranslation,
                              CefrLevel level, WordCategory category) throws SQLException {
        String sql = """
                INSERT INTO words(english, turkish, example, example_translation, source, level, category)
                VALUES(?, ?, ?, ?, 'CUSTOM', ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, english.trim());
            statement.setString(2, turkish.trim());
            statement.setString(3, example == null ? "" : example.trim());
            statement.setString(4, exampleTranslation == null ? "" : exampleTranslation.trim());
            statement.setString(5, level.name());
            statement.setString(6, category.name());
            statement.executeUpdate();
        }
    }

    public void updateCustomWord(long id, String english, String turkish, String example,
                                 String exampleTranslation, CefrLevel level, WordCategory category) throws SQLException {
        String sql = """
                UPDATE words
                SET english = ?, turkish = ?, example = ?, example_translation = ?, level = ?, category = ?
                WHERE id = ? AND source = 'CUSTOM'
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, english.trim());
            statement.setString(2, turkish.trim());
            statement.setString(3, example == null ? "" : example.trim());
            statement.setString(4, exampleTranslation == null ? "" : exampleTranslation.trim());
            statement.setString(5, level.name());
            statement.setString(6, category.name());
            statement.setLong(7, id);
            statement.executeUpdate();
        }
    }

    public void deleteCustomWord(long id) throws SQLException {
        String sql = "DELETE FROM words WHERE id = ? AND source = 'CUSTOM'";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    /** Records a review using an SM-2-inspired adaptive interval schedule. */
    public void recordReview(long wordId, String result) throws SQLException {
        if (!"KNOWN".equals(result) && !"REPEAT".equals(result)) {
            throw new IllegalArgumentException("Geçersiz tekrar sonucu: " + result);
        }
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement review = connection.prepareStatement(
                    "INSERT INTO word_reviews(word_id, result) VALUES(?, ?)")) {
                review.setLong(1, wordId);
                review.setString(2, result);
                review.executeUpdate();
            }

            String updateSql = """
                    UPDATE words
                    SET known_count = known_count + CASE WHEN ? = 'KNOWN' THEN 1 ELSE 0 END,
                        repeat_count = repeat_count + CASE WHEN ? = 'REPEAT' THEN 1 ELSE 0 END,
                        lapse_count = lapse_count + CASE WHEN ? = 'REPEAT' THEN 1 ELSE 0 END,
                        ease_factor = CASE
                            WHEN ? = 'REPEAT' THEN MAX(1.3, ease_factor - 0.20)
                            ELSE MIN(2.8, ease_factor + 0.05)
                        END,
                        interval_days = CASE
                            WHEN ? = 'REPEAT' THEN 0
                            WHEN interval_days = 0 THEN 1
                            WHEN interval_days = 1 THEN 3
                            ELSE MIN(365, MAX(1, CAST(ROUND(interval_days * ease_factor) AS INTEGER)))
                        END,
                        next_due_at = CASE
                            WHEN ? = 'REPEAT' THEN datetime('now', '+10 minutes')
                            WHEN interval_days = 0 THEN datetime('now', '+1 day')
                            WHEN interval_days = 1 THEN datetime('now', '+3 days')
                            ELSE datetime('now', '+' || MIN(365, MAX(1, CAST(ROUND(interval_days * ease_factor) AS INTEGER))) || ' days')
                        END,
                        last_reviewed_at = datetime('now')
                    WHERE id = ?
                    """;
            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                for (int i = 1; i <= 6; i++) update.setString(i, result);
                update.setLong(7, wordId);
                update.executeUpdate();
            }
            connection.commit();
        }
    }


    /** Deletes only user-created words in one transaction; built-in catalogue entries are never touched. */
    public int deleteCustomWords(List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return 0;
        String sql = "DELETE FROM words WHERE id = ? AND source = 'CUSTOM'";
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Long id : ids) {
                    if (id == null) continue;
                    statement.setLong(1, id);
                    statement.addBatch();
                }
                int deleted = 0;
                for (int count : statement.executeBatch()) {
                    if (count > 0) deleted += count;
                    else if (count == Statement.SUCCESS_NO_INFO) deleted++;
                }
                connection.commit();
                return deleted;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Review activity in the last N local calendar days, including today. */
    public int countReviewsInLastDays(int days) throws SQLException {
        if (days < 1) throw new IllegalArgumentException("days must be positive");
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM word_reviews WHERE date(reviewed_at, 'localtime') >= date('now', 'localtime', ?)")) {
            statement.setString(1, "-" + (days - 1) + " days");
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public int countKnownReviewsInLastDays(int days) throws SQLException {
        if (days < 1) throw new IllegalArgumentException("days must be positive");
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM word_reviews WHERE result = 'KNOWN' AND date(reviewed_at, 'localtime') >= date('now', 'localtime', ?)")) {
            statement.setString(1, "-" + (days - 1) + " days");
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /** Number of distinct words displayed in reminder cards during the last N local calendar days. */
    public int countDistinctWordsShownInLastDays(int days) throws SQLException {
        return countPresentationMetric(days, true);
    }

    /** Total reminder-card displays during the last N local calendar days, including repeated displays. */
    public int countTotalWordDisplaysInLastDays(int days) throws SQLException {
        return countPresentationMetric(days, false);
    }

    private int countPresentationMetric(int days, boolean distinct) throws SQLException {
        if (days < 1) throw new IllegalArgumentException("days must be positive");
        String metric = distinct ? "COUNT(DISTINCT word_id)" : "COUNT(*)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + metric + " FROM word_presentations WHERE date(shown_at, 'localtime') >= date('now', 'localtime', ?)")) {
            statement.setString(1, "-" + (days - 1) + " days");
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM words");
    }

    public int countCustom() throws SQLException {
        return count("SELECT COUNT(*) FROM words WHERE source = 'CUSTOM'");
    }

    public int countKnownReviews() throws SQLException {
        return count("SELECT COUNT(*) FROM word_reviews WHERE result = 'KNOWN'");
    }

    public int countDue() throws SQLException {
        return count("SELECT COUNT(*) FROM words WHERE next_due_at <= datetime('now')");
    }

    public int countWithExamples() throws SQLException {
        return count("SELECT COUNT(*) FROM words WHERE TRIM(COALESCE(example, '')) <> ''");
    }

    public int countMissingExamples() throws SQLException {
        return count("SELECT COUNT(*) FROM words WHERE TRIM(COALESCE(example, '')) = ''");
    }

    public int countMastered() throws SQLException {
        return count("SELECT COUNT(*) FROM words WHERE interval_days >= 21");
    }

    public int countReviewsToday() throws SQLException {
        return count("SELECT COUNT(*) FROM word_reviews WHERE date(reviewed_at, 'localtime') = date('now', 'localtime')");
    }

    /** Returns the active daily review streak, allowing the current day to remain unfinished. */
    public int countCurrentReviewStreak() throws SQLException {
        String sql = "SELECT DISTINCT date(reviewed_at, 'localtime') AS review_day "
                + "FROM word_reviews ORDER BY review_day DESC";
        LocalDate today = LocalDate.now();
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            if (!result.next()) return 0;

            LocalDate firstDay = LocalDate.parse(result.getString("review_day"));
            if (firstDay.isBefore(today.minusDays(1))) return 0;

            int streak = 0;
            LocalDate expectedDay = firstDay;
            do {
                LocalDate reviewDay = LocalDate.parse(result.getString("review_day"));
                if (!reviewDay.equals(expectedDay)) break;
                streak++;
                expectedDay = expectedDay.minusDays(1);
            } while (result.next());
            return streak;
        }
    }

    public int countReviews() throws SQLException {
        return count("SELECT COUNT(*) FROM word_reviews");
    }

    public int countCorrectReviews() throws SQLException {
        return count("SELECT COUNT(*) FROM word_reviews WHERE result = 'KNOWN'");
    }

    public int countMasteredAtLevel(String level) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM words WHERE level = ? AND interval_days >= 21")) {
            statement.setString(1, level);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public int countAtLevel(String level) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM words WHERE level = ?")) {
            statement.setString(1, level);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? "ALL" : value.trim().toUpperCase();
    }

    private void markShown(Connection connection, long wordId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE words SET last_shown_at = datetime('now') WHERE id = ?")) {
            statement.setLong(1, wordId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO word_presentations(word_id) VALUES(?)")) {
            statement.setLong(1, wordId);
            statement.executeUpdate();
        }
    }

    private Word map(ResultSet result) throws SQLException {
        return new Word(
                result.getLong("id"),
                result.getString("english"),
                result.getString("turkish"),
                result.getString("example"),
                result.getString("example_translation"),
                result.getString("pronunciation"),
                result.getString("part_of_speech"),
                result.getString("source"),
                CefrLevel.fromDatabase(result.getString("level")),
                WordCategory.fromDatabase(result.getString("category")),
                result.getInt("known_count"),
                result.getInt("repeat_count"),
                result.getInt("is_hard") != 0,
                result.getString("last_shown_at") != null
        );
    }
}
