package com.wordreminder.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final int SCHEMA_VERSION = 7;
    private final String jdbcUrl;
    private final Path databaseFile;

    public Database() throws IOException {
        Path appDirectory = resolveAppDirectory();
        Files.createDirectories(appDirectory);
        this.databaseFile = appDirectory.resolve("wordreminder.db");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
    }

    private Path resolveAppDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "WordReminder");
        }
        return Path.of(System.getProperty("user.home"), ".wordreminder");
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    /** Creates a consistent standalone SQLite snapshot without modifying the live database. */
    public void backupTo(Path destination) throws SQLException, IOException {
        Path target = destination.toAbsolutePath().normalize();
        if (target.equals(databaseFile.toAbsolutePath().normalize())) {
            throw new IOException("Yedek dosyası uygulamanın aktif veritabanının üzerine yazamaz.");
        }
        Path parent = target.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "wordreminder-backup-", ".tmp");
        Files.delete(temporary); // SQLite VACUUM INTO requires a destination that does not exist.
        try {
            String escapedPath = temporary.toString().replace("'", "''");
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("VACUUM INTO '" + escapedPath + "'");
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Validates a standalone backup without changing either the backup or the live database. */
    public String verifyBackup(Path backupFile) throws SQLException, IOException {
        Path source = backupFile.toAbsolutePath().normalize();
        if (!Files.isRegularFile(source) || Files.size(source) == 0) {
            throw new IOException("Seçilen dosya bulunamadı veya boş.");
        }
        String url = "jdbc:sqlite:" + source;
        try (Connection connection = DriverManager.getConnection(url)) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
                if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1))) {
                    throw new IOException("SQLite bütünlük kontrolü başarısız. Bu dosyayı yedek olarak kullanma.");
                }
            }
            int version;
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("PRAGMA user_version")) {
                version = result.next() ? result.getInt(1) : 0;
            }
            if (version < 2 || version > SCHEMA_VERSION) {
                throw new IOException("Desteklenmeyen veritabanı şeması: " + version);
            }
            long words = countRows(connection, "words");
            long reviews = countRows(connection, "word_reviews");
            long settings = countRows(connection, "app_settings");
            long personalWords;
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM words WHERE source = 'CUSTOM'")) {
                personalWords = result.next() ? result.getLong(1) : 0;
            }
            return "Yedek dosyası sağlam.\n\n"
                    + "Şema sürümü: " + version + "\n"
                    + "Toplam kelime: " + words + "\n"
                    + "Kişisel kelime: " + personalWords + "\n"
                    + "Tekrar kaydı: " + reviews + "\n"
                    + "Ayar kaydı: " + settings + "\n\n"
                    + "Canlı veritabanında herhangi bir değişiklik yapılmadı.";
        }
    }

    private long countRows(Connection connection, String table) throws SQLException {
        // Table names are fixed internal constants, never user input.
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return result.next() ? result.getLong(1) : 0;
        }
    }

    public void initialize() throws SQLException {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode = WAL");
            }
            connection.setAutoCommit(false);
            try {
                int currentVersion;
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery("PRAGMA user_version")) {
                    currentVersion = result.next() ? result.getInt(1) : 0;
                }

                if (currentVersion < 2) {
                    // Data from pre-1.2 schemas is intentionally not carried into the clean learning catalogue.
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("DROP TABLE IF EXISTS word_reviews");
                        statement.execute("DROP TABLE IF EXISTS words");
                        statement.execute("DROP TABLE IF EXISTS app_settings");
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS words (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                english TEXT NOT NULL COLLATE NOCASE,
                                turkish TEXT NOT NULL,
                                example TEXT NOT NULL DEFAULT '',
                                source TEXT NOT NULL DEFAULT 'CUSTOM',
                                level TEXT NOT NULL DEFAULT 'A2',
                                category TEXT NOT NULL DEFAULT 'DAILY',
                                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                                last_shown_at TEXT,
                                next_due_at TEXT NOT NULL DEFAULT (datetime('now')),
                                known_count INTEGER NOT NULL DEFAULT 0,
                                repeat_count INTEGER NOT NULL DEFAULT 0,
                                UNIQUE(english, turkish)
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS word_reviews (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                word_id INTEGER NOT NULL,
                                reviewed_at TEXT NOT NULL DEFAULT (datetime('now')),
                                result TEXT NOT NULL,
                                FOREIGN KEY(word_id) REFERENCES words(id) ON DELETE CASCADE
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS app_settings (
                                setting_key TEXT PRIMARY KEY,
                                setting_value TEXT NOT NULL
                            )
                            """);
                    if (currentVersion < 3) {
                        statement.execute("ALTER TABLE words ADD COLUMN pronunciation TEXT NOT NULL DEFAULT ''");
                        statement.execute("ALTER TABLE words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT ''");
                    }
                    if (currentVersion < 4) {
                        statement.execute("ALTER TABLE words ADD COLUMN ease_factor REAL NOT NULL DEFAULT 2.5");
                        statement.execute("ALTER TABLE words ADD COLUMN interval_days INTEGER NOT NULL DEFAULT 0");
                        statement.execute("ALTER TABLE words ADD COLUMN lapse_count INTEGER NOT NULL DEFAULT 0");
                        statement.execute("ALTER TABLE words ADD COLUMN last_reviewed_at TEXT");
                    }
                    if (currentVersion < 5) {
                        statement.execute("ALTER TABLE words ADD COLUMN is_hard INTEGER NOT NULL DEFAULT 0");
                    }
                    if (currentVersion < 6) {
                        statement.execute("""
                                CREATE TABLE IF NOT EXISTS word_presentations (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    word_id INTEGER NOT NULL,
                                    shown_at TEXT NOT NULL DEFAULT (datetime('now')),
                                    FOREIGN KEY(word_id) REFERENCES words(id) ON DELETE CASCADE
                                )
                                """);
                        statement.execute("""
                                INSERT INTO word_presentations(word_id, shown_at)
                                SELECT id, last_shown_at FROM words
                                WHERE last_shown_at IS NOT NULL
                                """);
                    }
                    if (currentVersion < 7) {
                        statement.execute("ALTER TABLE words ADD COLUMN example_translation TEXT NOT NULL DEFAULT ''");
                    }
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_presentations_shown ON word_presentations(shown_at)");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_presentations_word ON word_presentations(word_id)");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_words_due ON words(next_due_at)");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_words_level_category ON words(level, category)");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_reviews_word ON word_reviews(word_id)");
                    statement.execute("PRAGMA user_version = " + SCHEMA_VERSION);
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
