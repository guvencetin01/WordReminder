package com.wordreminder.ui;

import com.wordreminder.model.CefrLevel;
import com.wordreminder.model.Word;
import com.wordreminder.model.WordCategory;
import com.wordreminder.repository.SettingsRepository;
import com.wordreminder.repository.WordRepository;
import com.wordreminder.service.WindowsStartupManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.SelectionMode;
import java.util.function.IntConsumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class MainWindow {
    private static final String ALL_LEVELS = "Tüm seviyeler";
    private static final String ALL_CATEGORIES = "Tüm alanlar";

    private final Stage stage;
    private final WordRepository wordRepository;
    private final SettingsRepository settingsRepository;
    private final WindowsStartupManager startupManager;
    private final IntConsumer intervalChanged;
    private final Runnable showNowAction;
    private final Executor databaseExecutor;
    private final AtomicLong dashboardRequestSequence = new AtomicLong();
    private final AtomicLong tableRequestSequence = new AtomicLong();
    private PauseTransition searchDebounce;
    private boolean suppressFilterEvents;
    private Tab progressTab;
    private final TableView<Word> wordTable = new TableView<>();
    private TableColumn<Word, String> pronunciationColumn;
    private TableColumn<Word, String> partOfSpeechColumn;
    private TableColumn<Word, String> sourceColumn;
    private final ObservableList<Word> tableItems = FXCollections.observableArrayList();
    private final Label totalWordsValue = new Label("0");
    private final Label customWordsValue = new Label("0");
    private final Label knownReviewsValue = new Label("0");
    private final Label dueWordsValue = new Label("0");
    private final Label statusLabel = new Label("Kelime kataloğu hazırlanıyor...");
    private final Label resultCountLabel = new Label("0 kelime");
    private final Spinner<Integer> intervalSpinner = new Spinner<>();
    private final Spinner<Integer> dailyGoalSpinner = new Spinner<>();
    private final ComboBox<String> tableLevelFilter = new ComboBox<>();
    private final ComboBox<String> tableCategoryFilter = new ComboBox<>();
    private final ComboBox<String> tableLearningFilter = new ComboBox<>();
    private final ComboBox<String> reminderLevelCombo = new ComboBox<>();
    private final ComboBox<String> reminderCategoryCombo = new ComboBox<>();
    private final CheckBox startWithWindowsCheck = new CheckBox("Windows oturum açıldığında WordReminder başlatılsın");
    private final CheckBox startMinimizedCheck = new CheckBox("Başlangıçta ana pencereyi açmadan sistem tepsisine küçült");
    private TextField searchField;
    private Button hideButton;
    private Button editButton;
    private Button deleteButton;

    public MainWindow(Stage stage,
                      WordRepository wordRepository,
                      SettingsRepository settingsRepository,
                      WindowsStartupManager startupManager,
                      IntConsumer intervalChanged,
                      Runnable showNowAction,
                      Executor databaseExecutor) {
        this.stage = stage;
        this.wordRepository = wordRepository;
        this.settingsRepository = settingsRepository;
        this.startupManager = startupManager;
        this.intervalChanged = intervalChanged;
        this.showNowAction = showNowAction;
        this.databaseExecutor = databaseExecutor;
        build();
        loadSettings();
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public void hide() {
        stage.hide();
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    public void setTrayAvailable(boolean available) {
        if (hideButton != null) {
            hideButton.setDisable(!available);
            hideButton.setTooltip(new Tooltip(available
                    ? "Uygulamayı sistem tepsisine küçült"
                    : "Bu bilgisayarda sistem tepsisi kullanılamıyor"));
        }
    }

    /**
     * Refreshes dashboard statistics and word rows away from the JavaFX application thread.
     * Request sequence numbers prevent slower, older queries from overwriting newer UI state.
     */
    public void refreshData() {
        long request = dashboardRequestSequence.incrementAndGet();
        databaseExecutor.execute(() -> {
            try {
                int totalWords = wordRepository.countAll();
                int customWords = wordRepository.countCustom();
                int knownReviews = wordRepository.countKnownReviews();
                int dueWords = wordRepository.countDue();
                ProgressData progress = loadProgressData();

                Platform.runLater(() -> {
                    if (request != dashboardRequestSequence.get()) return;
                    totalWordsValue.setText(Integer.toString(totalWords));
                    customWordsValue.setText(Integer.toString(customWords));
                    knownReviewsValue.setText(Integer.toString(knownReviews));
                    dueWordsValue.setText(Integer.toString(dueWords));
                    if (progressTab != null) {
                        progressTab.setContent(wrapScrollable(buildProgressPanel(progress), "progress-scroll-pane"));
                    }
                    statusLabel.setText("Kelime havuzu güncel");
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    if (request == dashboardRequestSequence.get()) {
                        showError("Veriler yüklenemedi", ex.getMessage());
                    }
                });
            }
        });
        filterTable();
    }

    private void build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        VBox content = new VBox(13);
        content.setPadding(new Insets(18, 22, 12, 22));
        content.setFillWidth(true);
        content.getChildren().addAll(buildHeader(), buildStats(), buildTabs(), buildFooter());
        root.setCenter(content);

        Scene scene = new Scene(root, 1080, 760);
        var css = getClass().getResource("/com/wordreminder/ui/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("WordReminder");
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(scene);
    }

    private FlowPane buildHeader() {
        VBox titleBlock = new VBox(5);
        titleBlock.setMinWidth(250);
        Label eyebrow = new Label("KİŞİSEL KELİME ANTRENÖRÜ");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("WordReminder");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Günlük İngilizce ve yazılım mühendisliği terminolojisi.");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(430);
        titleBlock.getChildren().addAll(eyebrow, title, subtitle);

        Button remindButton = new Button("Şimdi kelime göster");
        remindButton.getStyleClass().add("primary-button");
        remindButton.setOnAction(event -> showNowAction.run());
        hideButton = new Button("Sistem tepsisine küçült");
        hideButton.getStyleClass().add("secondary-button");
        hideButton.setOnAction(event -> hide());

        FlowPane actions = new FlowPane(8, 8, remindButton, hideButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        FlowPane header = new FlowPane(14, 10, titleBlock, actions);
        header.getStyleClass().add("header-flow");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);
        return header;
    }

    private HBox buildStats() {
        HBox cards = new HBox(8,
                statCard("KELİME HAVUZU", totalWordsValue, "Hazır + kişisel"),
                statCard("KİŞİSEL KELİMELER", customWordsValue, "Senin eklediklerin"),
                statCard("ÖĞRENİLDİ İŞARETİ", knownReviewsValue, "Toplam işaretleme"),
                statCard("TEKRAR BEKLEYEN", dueWordsValue, "Sırası gelen kelimeler"));
        cards.getStyleClass().add("stats-row");
        cards.setAlignment(Pos.CENTER);
        return cards;
    }

    private VBox statCard(String caption, Label value, String footnote) {
        VBox card = new VBox(7);
        card.getStyleClass().add("stat-card");
        Label heading = new Label(caption);
        heading.getStyleClass().add("stat-caption");
        heading.setWrapText(true);
        value.getStyleClass().add("stat-value");
        Label note = new Label(footnote);
        note.getStyleClass().add("stat-note");
        note.setWrapText(true);
        card.getChildren().addAll(heading, value, note);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("main-tabs");
        Tab wordsTab = new Tab("Kelime havuzu", buildWordsPanel());
        wordsTab.setClosable(false);
        progressTab = new Tab("İstatistikler", wrapScrollable(buildProgressPanel(null), "progress-scroll-pane"));
        progressTab.setClosable(false);
        Tab settingsTab = new Tab("Ayarlar", wrapScrollable(buildSettingsPanel(), "settings-scroll-pane"));
        settingsTab.setClosable(false);
        tabs.getTabs().addAll(wordsTab, progressTab, settingsTab);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return tabs;
    }

    /** Keeps long tab content reachable at normal and reduced window heights. */
    private ScrollPane wrapScrollable(VBox content, String styleClass) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add(styleClass);
        content.setMaxWidth(Double.MAX_VALUE);
        return scrollPane;
    }

    private VBox buildWordsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15, 2, 4, 2));

        FlowPane actions = new FlowPane(8, 8);
        actions.getStyleClass().add("toolbar-flow");
        actions.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("İngilizce veya Türkçe ara...");
        searchField.setPrefWidth(270);
        searchField.setMinWidth(190);
        searchField.setMaxWidth(340);
        searchDebounce = new PauseTransition(Duration.millis(250));
        searchDebounce.setOnFinished(event -> filterTable());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!suppressFilterEvents) searchDebounce.playFromStart();
        });

        Button addButton = new Button("+ Kelime ekle");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(event -> showWordDialog(null));
        editButton = new Button("Düzenle");
        editButton.getStyleClass().add("secondary-button");
        editButton.setDisable(true);
        editButton.setOnAction(event -> editSelectedWord());
        deleteButton = new Button("Seçileni sil");
        deleteButton.getStyleClass().add("secondary-button");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelectedWord());
        Button deleteMultipleButton = new Button("Seçilen kişisel kelimeleri sil");
        deleteMultipleButton.getStyleClass().add("secondary-button");
        deleteMultipleButton.setOnAction(event -> deleteSelectedWords());
        actions.getChildren().addAll(searchField, addButton, editButton, deleteButton, deleteMultipleButton);

        FlowPane filters = new FlowPane(10, 8);
        filters.getStyleClass().add("filter-flow");
        Label levelLabel = new Label("Seviye:");
        levelLabel.getStyleClass().add("field-label");
        tableLevelFilter.getItems().setAll(ALL_LEVELS, "A1", "A2", "B1", "B2", "C1", "C2");
        tableLevelFilter.setValue(ALL_LEVELS);
        tableLevelFilter.setPrefWidth(130);
        tableLevelFilter.setMinWidth(115);
        tableLevelFilter.setOnAction(event -> {
            if (!suppressFilterEvents) filterTable();
        });

        Label categoryLabel = new Label("Alan:");
        categoryLabel.getStyleClass().add("field-label");
        tableCategoryFilter.getItems().setAll(ALL_CATEGORIES, "Günlük", "Teknik");
        tableCategoryFilter.setValue(ALL_CATEGORIES);
        tableCategoryFilter.setPrefWidth(145);
        tableCategoryFilter.setMinWidth(125);
        tableCategoryFilter.setOnAction(event -> {
            if (!suppressFilterEvents) filterTable();
        });
        Label learningLabel = new Label("Öğrenme durumu:");
        learningLabel.getStyleClass().add("field-label");
        tableLearningFilter.getItems().setAll("Tüm durumlar", "Henüz çalışılmayan", "Çalışılan Kelimeler",
                "Tekrar zamanı gelen", "Öğrenilen", "Zor Kelimeler");
        tableLearningFilter.setValue("Tüm durumlar");
        tableLearningFilter.setPrefWidth(205);
        tableLearningFilter.setMinWidth(165);
        tableLearningFilter.setOnAction(event -> {
            if (!suppressFilterEvents) filterTable();
        });

        Button resetFiltersButton = new Button("Filtreleri sıfırla");
        resetFiltersButton.getStyleClass().add("secondary-button");
        resetFiltersButton.setOnAction(event -> resetWordFilters());

        resultCountLabel.getStyleClass().add("hint-label");
        filters.getChildren().addAll(levelLabel, tableLevelFilter, categoryLabel, tableCategoryFilter,
                learningLabel, tableLearningFilter, resetFiltersButton, resultCountLabel);

        configureWordTable();
        wordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        wordTable.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<Word>) change -> {
                    List<Word> selected = wordTable.getSelectionModel().getSelectedItems();
                    boolean oneCustom = selected.size() == 1 && selected.get(0).isCustom();
                    editButton.setDisable(!oneCustom);
                    deleteButton.setDisable(!oneCustom);
                });
        wordTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) editSelectedWord();
        });
        VBox.setVgrow(wordTable, Priority.ALWAYS);
        Label hint = new Label("A1–C2 seviyelerini ve Günlük / Teknik alanlarını filtreleyebilirsin. Hazır kelimeler düzenlenemez; kişisel kelimelerini düzenleyebilir veya silebilirsin. Dar pencerelerde yardımcı tablo sütunları otomatik gizlenir; pencereyi büyütünce yeniden görünür.");
        hint.getStyleClass().add("hint-label");
        hint.setWrapText(true);
        hint.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().addAll(actions, filters, wordTable, hint);
        return panel;
    }

    private void configureWordTable() {
        wordTable.setPlaceholder(new Label("Bu filtrelerle eşleşen kelime bulunamadı."));
        wordTable.getStyleClass().add("word-table");
        wordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        wordTable.setItems(tableItems);

        TableColumn<Word, String> english = new TableColumn<>("İNGİLİZCE");
        // Java records expose english(), not getEnglish(); PropertyValueFactory does not resolve this accessor.
        english.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().english()));
        english.setMinWidth(145);

        pronunciationColumn = new TableColumn<>("ABD IPA");
        pronunciationColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().pronunciation()));
        pronunciationColumn.setMinWidth(105);

        TableColumn<Word, String> turkish = new TableColumn<>("TÜRKÇE");
        turkish.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().turkish()));
        turkish.setMinWidth(150);

        partOfSpeechColumn = new TableColumn<>("KELİME TÜRÜ");
        partOfSpeechColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().partOfSpeech()));
        partOfSpeechColumn.setMinWidth(90);

        TableColumn<Word, String> level = new TableColumn<>("SEVİYE");
        level.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().level().name()));
        level.setMinWidth(65);
        level.setMaxWidth(85);

        TableColumn<Word, String> category = new TableColumn<>("ALAN");
        category.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().category() == WordCategory.DAILY ? "Günlük" : "Teknik"));
        category.setMinWidth(95);

        sourceColumn = new TableColumn<>("KAYNAK");
        sourceColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().isCustom() ? "Kişisel" : "Hazır"));
        sourceColumn.setMinWidth(70);
        sourceColumn.setMaxWidth(100);

        TableColumn<Word, String> progress = new TableColumn<>("TEKRAR");
        progress.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().knownCount() + " öğrendim / " + cell.getValue().repeatCount() + " tekrar"));
        progress.setMinWidth(160);

        TableColumn<Word, String> hardMarker = new TableColumn<>("");
        hardMarker.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                cell.getValue().hardWord() ? "*" : ""));
        hardMarker.setMinWidth(24);
        hardMarker.setMaxWidth(30);
        hardMarker.setSortable(false);

        wordTable.getColumns().setAll(english, pronunciationColumn, turkish, partOfSpeechColumn,
                level, category, sourceColumn, progress, hardMarker);
        wordTable.setMinHeight(130);
        wordTable.widthProperty().addListener((observable, oldWidth, newWidth) ->
                applyResponsiveTableColumns(newWidth.doubleValue()));
    }

    /** Hides secondary columns on narrow windows instead of forcing a horizontal scroll. */
    private void applyResponsiveTableColumns(double width) {
        if (pronunciationColumn == null || partOfSpeechColumn == null || sourceColumn == null) return;
        sourceColumn.setVisible(width >= 910);
        partOfSpeechColumn.setVisible(width >= 820);
        pronunciationColumn.setVisible(width >= 730);
    }

    /** Loads all report values on a database worker; no SQL runs while building JavaFX nodes. */
    private ProgressData loadProgressData() throws SQLException {
        int mastered = wordRepository.countMastered();
        int reviews = wordRepository.countReviews();
        int correct = wordRepository.countCorrectReviews();
        int withExamples = wordRepository.countWithExamples();
        int missingExamples = wordRepository.countMissingExamples();
        int todayReviews = wordRepository.countReviewsToday();
        int currentStreak = wordRepository.countCurrentReviewStreak();
        int dailyGoal = settingsRepository.getDailyGoal();

        List<PeriodData> periods = new ArrayList<>();
        int[] periodDays = {1, 7, 30};
        String[] periodNames = {"Bugün", "Son 7 gün", "Son 30 gün"};
        for (int i = 0; i < periodDays.length; i++) {
            int days = periodDays[i];
            periods.add(new PeriodData(
                    periodNames[i],
                    wordRepository.countReviewsInLastDays(days),
                    wordRepository.countKnownReviewsInLastDays(days),
                    wordRepository.countDistinctWordsShownInLastDays(days),
                    wordRepository.countTotalWordDisplaysInLastDays(days)));
        }

        List<LevelData> levels = new ArrayList<>();
        for (CefrLevel level : CefrLevel.values()) {
            levels.add(new LevelData(level.name(),
                    wordRepository.countMasteredAtLevel(level.name()),
                    wordRepository.countAtLevel(level.name())));
        }
        return new ProgressData(mastered, reviews, correct, withExamples, missingExamples,
                todayReviews, currentStreak, dailyGoal, List.copyOf(periods), List.copyOf(levels));
    }

    /** Builds the statistics view using a preloaded immutable snapshot only. */
    private VBox buildProgressPanel(ProgressData data) {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20, 8, 8, 8));
        Label heading = new Label("Öğrenme istatistikleri");
        heading.getStyleClass().add("section-title");
        Label description = new Label("Kelime kartlarının gösterimlerini, tamamlanan tekrarları ve CEFR seviyelerindeki ilerlemeni ayrı ayrı takip et. Bir kelimenin tekrar aralığı 21 güne ulaştığında öğrenilmiş kabul edilir.");
        description.getStyleClass().add("hint-label");
        description.setWrapText(true);
        panel.getChildren().addAll(heading, description);

        if (data == null) {
            Label loading = new Label("İstatistikler arka planda yükleniyor...");
            loading.getStyleClass().add("hint-label");
            panel.getChildren().add(loading);
            return panel;
        }

        double accuracy = data.reviews() == 0 ? 0.0 : (double) data.correct() / data.reviews();
        Label summary = new Label("Öğrenilmiş kelime: " + data.mastered()
                + "   •   Tamamlanan tekrar: " + data.reviews()
                + "   •   Doğru yanıt oranı: " + Math.round(accuracy * 100) + "%"
                + "   •   Örnek cümle kapsamı: " + data.withExamples() + "/"
                + (data.withExamples() + data.missingExamples()));
        summary.getStyleClass().add("word-metadata");
        summary.setWrapText(true);
        summary.setMaxWidth(Double.MAX_VALUE);
        Label streakLabel = new Label("Günlük öğrenme serisi: " + data.currentStreak()
                + " gün  •  Bugün " + data.todayReviews() + " tekrar tamamlandı");
        streakLabel.getStyleClass().add("field-label");
        streakLabel.setWrapText(true);
        streakLabel.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().addAll(summary, streakLabel);

        Label reportHeading = new Label("Çalışma özeti");
        reportHeading.getStyleClass().add("section-title");
        Label reportHint = new Label("Çalışılan farklı kelime, kartı gösterilen benzersiz kelimeleri; toplam gösterim ise aynı kelimenin tekrar gösterimleri dâhil tüm kart gösterimlerini ifade eder. Tekrar sayısı yalnızca cevaplanan çalışma kayıtlarını sayar.");
        reportHint.getStyleClass().add("hint-label");
        reportHint.setWrapText(true);
        Label historyNote = new Label("Not: 2.6.0 öncesinde kart gösterimlerinin ayrıntılı geçmişi tutulmuyordu. Eski verilerde yalnızca bilinen son gösterim aktarılır; ayrıntılı dönem istatistikleri bu sürümden itibaren birikir.");
        historyNote.getStyleClass().add("hint-label");
        historyNote.setWrapText(true);
        GridPane reportGrid = new GridPane();
        reportGrid.setHgap(12);
        reportGrid.setVgap(8);
        reportGrid.setMaxWidth(Double.MAX_VALUE);
        for (int column = 0; column < 3; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.333);
            constraints.setHgrow(Priority.ALWAYS);
            reportGrid.getColumnConstraints().add(constraints);
        }
        for (int i = 0; i < data.periods().size(); i++) {
            PeriodData period = data.periods().get(i);
            VBox reportCard = new VBox(4);
            reportCard.getStyleClass().add("settings-card");
            Label periodLabel = new Label(period.label());
            periodLabel.getStyleClass().add("field-label");
            Label distinctLabel = new Label("Çalışılan farklı kelime");
            distinctLabel.getStyleClass().add("hint-label");
            Label distinctValue = new Label(Integer.toString(period.distinctWords()));
            distinctValue.getStyleClass().add("stat-value");
            Label displayLabel = new Label("Toplam kelime gösterimi: " + period.totalDisplays());
            displayLabel.getStyleClass().add("word-metadata");
            Label reviewLabel = new Label("Tamamlanan tekrar: " + period.reviews());
            reviewLabel.getStyleClass().add("word-metadata");
            Label ratio = new Label("Öğrendim: " + period.known() + "  ·  Doğru yanıt: "
                    + (period.reviews() == 0 ? 0 : Math.round((double) period.known() / period.reviews() * 100)) + "%");
            ratio.getStyleClass().add("hint-label");
            reportCard.getChildren().addAll(periodLabel, distinctLabel, distinctValue, displayLabel, reviewLabel, ratio);
            reportGrid.add(reportCard, i, 0);
            GridPane.setHgrow(reportCard, Priority.ALWAYS);
        }
        panel.getChildren().addAll(reportHeading, reportHint, historyNote, reportGrid);

        Label goalHeading = new Label("Günlük hedef");
        goalHeading.getStyleClass().add("section-title");
        Label dailyGoalLabel = new Label("Bugünkü tekrar hedefi: " + Math.min(data.todayReviews(), data.dailyGoal())
                + " / " + data.dailyGoal() + "  ("
                + Math.min(100, Math.round((double) data.todayReviews() / data.dailyGoal() * 100)) + "%)");
        dailyGoalLabel.getStyleClass().add("field-label");
        ProgressBar dailyGoalBar = new ProgressBar(Math.min(1.0, (double) data.todayReviews() / data.dailyGoal()));
        dailyGoalBar.getStyleClass().add("cefr-progress");
        dailyGoalBar.setMinHeight(14);
        dailyGoalBar.setPrefHeight(14);
        dailyGoalBar.setMaxWidth(Double.MAX_VALUE);
        dailyGoalBar.setAccessibleText("Bugünkü tekrar hedefi: " + data.todayReviews() + " / " + data.dailyGoal());
        panel.getChildren().addAll(goalHeading, dailyGoalLabel, dailyGoalBar);

        Label cefrHeading = new Label("CEFR seviyelerine göre ilerleme");
        cefrHeading.getStyleClass().add("section-title");
        panel.getChildren().add(cefrHeading);
        for (LevelData level : data.levels()) {
            double progress = level.total() == 0 ? 0.0 : (double) level.mastered() / level.total();
            Label label = new Label(level.level() + "     " + level.mastered() + " / " + level.total()
                    + " kelime  (" + Math.round(progress * 100) + "%)");
            label.getStyleClass().add("field-label");
            ProgressBar bar = new ProgressBar(progress);
            bar.getStyleClass().add("cefr-progress");
            bar.setMinHeight(14);
            bar.setPrefHeight(14);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setAccessibleText(level.level() + " öğrenme ilerlemesi yüzde " + Math.round(progress * 100));
            VBox.setMargin(bar, new Insets(3, 0, 7, 0));
            panel.getChildren().addAll(label, bar);
        }
        return panel;
    }

    private VBox buildSettingsPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(16, 10, 12, 10));
        panel.setFillWidth(true);

        Label heading = new Label("Uygulama ayarları");
        heading.getStyleClass().add("section-title");
        Button saveButton = new Button("Ayarları kaydet");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(event -> saveSettings());
        FlowPane headingRow = new FlowPane(10, 8, heading, saveButton);
        headingRow.setAlignment(Pos.CENTER_LEFT);
        headingRow.getStyleClass().add("settings-heading-row");

        VBox intervalBox = new VBox(8);
        intervalBox.getStyleClass().add("settings-card");
        Label intervalHeading = new Label("Hatırlatma sıklığı");
        intervalHeading.getStyleClass().add("section-title");
        Label intervalText = new Label("Süre, kart gösterildiğinde başlar. Bir kart en fazla 2 dakika açık kalır; süre dolunca etkileşim yoksa sessizce kapanır. Açık kart varken yenisi açılmaz.");
        intervalText.getStyleClass().add("hint-label");
        intervalText.setWrapText(true);
        intervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 10));
        intervalSpinner.setEditable(true);
        intervalSpinner.setPrefWidth(110);
        intervalSpinner.setMinWidth(100);
        Label minutes = new Label("dakika");
        HBox intervalRow = new HBox(10, intervalSpinner, minutes);
        intervalRow.setAlignment(Pos.CENTER_LEFT);
        intervalBox.getChildren().addAll(intervalHeading, intervalText, intervalRow);

        VBox goalBox = new VBox(8);
        goalBox.getStyleClass().add("settings-card");
        Label goalHeading = new Label("Günlük tekrar hedefi");
        goalHeading.getStyleClass().add("section-title");
        Label goalDescription = new Label("Her gün tamamlamak istediğin kelime tekrar sayısını belirle.");
        goalDescription.getStyleClass().add("hint-label");
        goalDescription.setWrapText(true);
        dailyGoalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        dailyGoalSpinner.setEditable(true);
        dailyGoalSpinner.setPrefWidth(110);
        dailyGoalSpinner.setMinWidth(100);
        HBox goalRow = new HBox(10, dailyGoalSpinner, new Label("tekrar / gün"));
        goalRow.setAlignment(Pos.CENTER_LEFT);
        goalBox.getChildren().addAll(goalHeading, goalDescription, goalRow);

        VBox learningBox = new VBox(10);
        learningBox.getStyleClass().add("settings-card");
        Label learningHeading = new Label("Öğrenme kapsamı");
        learningHeading.getStyleClass().add("section-title");
        Label learningDescription = new Label("Hatırlatma pencerelerinde hangi seviyeden ve hangi alandan kelime gösterileceğini seç.");
        learningDescription.getStyleClass().add("hint-label");
        learningDescription.setWrapText(true);
        reminderLevelCombo.getItems().setAll(ALL_LEVELS, "A1", "A2", "B1", "B2", "C1", "C2");
        reminderLevelCombo.setValue(ALL_LEVELS);
        reminderLevelCombo.setPrefWidth(160);
        reminderLevelCombo.setMinWidth(130);
        reminderCategoryCombo.getItems().setAll(ALL_CATEGORIES, "Günlük", "Teknik");
        reminderCategoryCombo.setValue(ALL_CATEGORIES);
        reminderCategoryCombo.setPrefWidth(180);
        reminderCategoryCombo.setMinWidth(145);
        FlowPane learningFilters = new FlowPane(10, 8);
        learningFilters.getChildren().addAll(
                fieldLabel("Seviye"), reminderLevelCombo,
                fieldLabel("Kelime alanı"), reminderCategoryCombo);
        learningFilters.setAlignment(Pos.CENTER_LEFT);
        learningBox.getChildren().addAll(learningHeading, learningDescription, learningFilters);

        VBox startupBox = new VBox(10);
        startupBox.getStyleClass().add("settings-card");
        Label startupHeading = new Label("Windows ile başlatma");
        startupHeading.getStyleClass().add("section-title");
        Label startupDescription = new Label("Bu seçenek, yalnızca mevcut Windows kullanıcısı için Başlangıç klasörüne kayıt ekler. Yönetici izni gerekmez.");
        startupDescription.getStyleClass().add("hint-label");
        startupDescription.setWrapText(true);
        startWithWindowsCheck.setText("");
        startMinimizedCheck.setText("");
        startWithWindowsCheck.setAccessibleText("Windows oturumu açıldığında WordReminder'ı otomatik başlat");
        startMinimizedCheck.setAccessibleText("Otomatik başlatıldığında ana pencereyi göstermeden sistem tepsisinde çalıştır");
        startWithWindowsCheck.setDisable(!startupManager.isSupported());
        startMinimizedCheck.setDisable(!startupManager.isSupported());
        Label startupOptionTitle = new Label("Windows oturumu açıldığında WordReminder'ı otomatik başlat");
        startupOptionTitle.getStyleClass().add("startup-option-title");
        startupOptionTitle.setWrapText(true);
        Label startupOptionHint = new Label("Program, mevcut Windows kullanıcısı oturum açtığında kendiliğinden çalışır.");
        startupOptionHint.getStyleClass().add("hint-label");
        startupOptionHint.setWrapText(true);
        VBox startupOptionText = new VBox(3, startupOptionTitle, startupOptionHint);
        HBox startupOptionRow = new HBox(10, startWithWindowsCheck, startupOptionText);
        startupOptionRow.setAlignment(Pos.CENTER_LEFT);
        startupOptionRow.setOnMouseClicked(event -> {
            if (event.getTarget() != startWithWindowsCheck) {
                startWithWindowsCheck.setSelected(!startWithWindowsCheck.isSelected());
            }
        });

        Label minimizedOptionTitle = new Label("Başlangıçta pencereyi gösterme, sistem tepsisinde çalış");
        minimizedOptionTitle.getStyleClass().add("startup-option-title");
        minimizedOptionTitle.setWrapText(true);
        Label minimizedOptionHint = new Label("Yalnızca yukarıdaki otomatik başlatma seçeneği açıkken uygulanır.");
        minimizedOptionHint.getStyleClass().add("hint-label");
        minimizedOptionHint.setWrapText(true);
        VBox minimizedOptionText = new VBox(3, minimizedOptionTitle, minimizedOptionHint);
        HBox minimizedOptionRow = new HBox(10, startMinimizedCheck, minimizedOptionText);
        minimizedOptionRow.setAlignment(Pos.CENTER_LEFT);
        minimizedOptionRow.setOnMouseClicked(event -> {
            if (!startMinimizedCheck.isDisabled() && event.getTarget() != startMinimizedCheck) {
                startMinimizedCheck.setSelected(!startMinimizedCheck.isSelected());
            }
        });
        startWithWindowsCheck.selectedProperty().addListener((observable, oldValue, selected) ->
                startMinimizedCheck.setDisable(!startupManager.isSupported() || !selected));
        startupBox.getChildren().addAll(startupHeading, startupDescription,
                startupOptionRow, minimizedOptionRow);

        VBox backupBox = new VBox(8);
        backupBox.getStyleClass().add("settings-card");
        Label backupHeading = new Label("Verilerini yedekle");
        backupHeading.getStyleClass().add("section-title");
        Label backupDescription = new Label("Kelime havuzunu, kişisel kelimelerini, öğrenme geçmişini ve ayarlarını tek bir SQLite yedek dosyasına aktar.");
        backupDescription.getStyleClass().add("hint-label");
        backupDescription.setWrapText(true);
        Button backupButton = new Button("Veritabanını yedekle…");
        backupButton.getStyleClass().add("secondary-button");
        backupButton.setOnAction(event -> exportDatabaseBackup());
        Button verifyBackupButton = new Button("Yedek dosyasını doğrula…");
        verifyBackupButton.getStyleClass().add("secondary-button");
        verifyBackupButton.setOnAction(event -> verifyDatabaseBackup());
        FlowPane backupActions = new FlowPane(8, 8, backupButton, verifyBackupButton);
        backupActions.setAlignment(Pos.CENTER_LEFT);
        backupBox.getChildren().addAll(backupHeading, backupDescription, backupActions);

        GridPane settingsGrid = new GridPane();
        settingsGrid.getStyleClass().add("settings-grid");
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(12);
        settingsGrid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setPercentWidth(50);
        firstColumn.setHgrow(Priority.ALWAYS);
        firstColumn.setFillWidth(true);
        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setPercentWidth(50);
        secondColumn.setHgrow(Priority.ALWAYS);
        secondColumn.setFillWidth(true);
        settingsGrid.getColumnConstraints().setAll(firstColumn, secondColumn);
        for (VBox card : new VBox[]{goalBox, learningBox, startupBox, backupBox}) {
            card.setMinWidth(0);
            card.setMaxWidth(Double.MAX_VALUE);
        }
        settingsGrid.add(goalBox, 0, 0);
        settingsGrid.add(learningBox, 1, 0);
        settingsGrid.add(startupBox, 0, 1);
        settingsGrid.add(backupBox, 1, 1);

        Label info = new Label("Akıllı tekrar: ‘Tekrar et’ seçimi kelimeyi 10 dakika sonrasına alır ve öğrenme aralığını sıfırlar. ‘Öğrendim’ seçiminde ilk aralık 1 gün, sonraki 3 gün olur; devamında kişisel kolaylık katsayısına göre en fazla 365 güne kadar uzar.");
        info.setWrapText(true);
        info.setMaxWidth(Double.MAX_VALUE);
        info.getStyleClass().add("settings-info");

        panel.getChildren().addAll(headingRow, intervalBox, settingsGrid, info);
        return panel;
    }

    private void exportDatabaseBackup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("WordReminder veritabanı yedeğini kaydet");
        chooser.setInitialFileName("wordreminder-backup.db");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite veritabanı", "*.db"));
        File selected = chooser.showSaveDialog(stage);
        if (selected == null) return;
        Path destination = selected.toPath();
        if (!destination.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".db")) {
            destination = destination.resolveSibling(destination.getFileName() + ".db");
        }
        try {
            wordRepository.backupDatabase(destination);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setTitle("Yedekleme tamamlandı");
            alert.setHeaderText("Veritabanı yedeği oluşturuldu");
            alert.setContentText("Kelime kataloğu, kişisel kelimeler, tekrar geçmişi ve ayarlar kaydedildi:\n" + destination.toAbsolutePath());
            alert.showAndWait();
            statusLabel.setText("Veritabanı yedeği oluşturuldu");
        } catch (SQLException | IOException ex) {
            showError("Yedek oluşturulamadı", ex.getMessage());
        }
    }

    private void verifyDatabaseBackup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Doğrulanacak WordReminder yedeğini seç");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite veritabanı", "*.db"));
        File selected = chooser.showOpenDialog(stage);
        if (selected == null) return;
        try {
            String report = wordRepository.verifyDatabaseBackup(selected.toPath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setTitle("Yedek doğrulama sonucu");
            alert.setHeaderText("Yedek dosyası başarıyla doğrulandı");
            alert.setContentText(report);
            alert.showAndWait();
            statusLabel.setText("Yedek dosyası doğrulandı");
        } catch (SQLException | IOException ex) {
            showError("Yedek doğrulanamadı", ex.getMessage());
        }
    }

    private HBox buildFooter() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        statusLabel.getStyleClass().add("status-label");
        Label version = new Label("WordReminder • 3.3.0");
        version.getStyleClass().add("version-label");
        HBox footer = new HBox(statusLabel, spacer, version);
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    private void filterTable() {
        if (searchDebounce != null) searchDebounce.stop();
        String search = searchField == null ? "" : searchField.getText();
        String level = selectedLevelCode(tableLevelFilter.getValue());
        String category = selectedCategoryCode(tableCategoryFilter.getValue());
        String learningState = selectedLearningStateCode(tableLearningFilter.getValue());
        long request = tableRequestSequence.incrementAndGet();

        databaseExecutor.execute(() -> {
            try {
                List<Word> words = wordRepository.findAll(search, level, category, learningState);
                Platform.runLater(() -> {
                    if (request != tableRequestSequence.get()) return;
                    tableItems.setAll(words);
                    resultCountLabel.setText(tableItems.size() + " kelime gösteriliyor");
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    if (request == tableRequestSequence.get()) {
                        showError("Arama yapılamadı", ex.getMessage());
                    }
                });
            }
        });
    }

    private void showWordDialog(Word existing) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Yeni kelime ekle" : "Kelimeyi düzenle");

        TextField english = new TextField(existing == null ? "" : existing.english());
        english.setPromptText("Örnek: thoughtful");
        TextField turkish = new TextField(existing == null ? "" : existing.turkish());
        turkish.setPromptText("Örnek: düşünceli");
        TextArea example = new TextArea(existing == null ? "" : existing.example());
        example.setPromptText("İngilizce örnek cümle (isteğe bağlı)");
        example.setPrefRowCount(3);
        example.setWrapText(true);
        TextArea exampleTranslation = new TextArea(existing == null ? "" : existing.exampleTranslation());
        exampleTranslation.setPromptText("Örnek cümlenin Türkçe çevirisi (isteğe bağlı)");
        exampleTranslation.setPrefRowCount(3);
        exampleTranslation.setWrapText(true);

        ComboBox<String> level = new ComboBox<>();
        level.getItems().setAll("A1", "A2", "B1", "B2", "C1", "C2");
        level.setValue(existing == null ? "B1" : existing.level().name());
        level.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> category = new ComboBox<>();
        category.getItems().setAll("Günlük", "Teknik");
        category.setValue(existing == null ? "Teknik" : categoryLabel(existing.category()));
        category.setMaxWidth(Double.MAX_VALUE);

        Label validation = new Label();
        validation.getStyleClass().add("error-label");
        Button cancel = new Button("Vazgeç");
        cancel.getStyleClass().add("secondary-button");
        cancel.setOnAction(event -> dialog.close());
        Button save = new Button(existing == null ? "Kelimeyi ekle" : "Değişiklikleri kaydet");
        save.getStyleClass().add("primary-button");
        save.setOnAction(event -> {
            String englishValue = english.getText() == null ? "" : english.getText().trim();
            String turkishValue = turkish.getText() == null ? "" : turkish.getText().trim();
            if (englishValue.isBlank() || turkishValue.isBlank()) {
                validation.setText("İngilizce kelime ve Türkçe anlam zorunludur.");
                return;
            }
            try {
                CefrLevel selectedLevel = CefrLevel.valueOf(level.getValue());
                WordCategory selectedCategory = category.getValue().equals("Günlük")
                        ? WordCategory.DAILY : WordCategory.TECHNICAL;
                if (existing == null) {
                    wordRepository.addCustomWord(englishValue, turkishValue, example.getText(),
                            exampleTranslation.getText(), selectedLevel, selectedCategory);
                } else {
                    wordRepository.updateCustomWord(existing.id(), englishValue, turkishValue,
                            example.getText(), exampleTranslation.getText(), selectedLevel, selectedCategory);
                }
                refreshData();
                dialog.close();
            } catch (SQLException ex) {
                validation.setText(ex.getMessage() != null && ex.getMessage().contains("UNIQUE")
                        ? "Bu kelime ve anlamı zaten kayıtlı." : "Kelime kaydedilemedi: " + ex.getMessage());
            }
        });

        VBox form = new VBox(9,
                fieldLabel("İngilizce kelime / ifade"), english,
                fieldLabel("Türkçe anlam"), turkish,
                fieldLabel("CEFR seviyesi"), level,
                fieldLabel("Kelime alanı"), category,
                fieldLabel("İngilizce örnek cümle"), example,
                fieldLabel("Cümlenin Türkçe çevirisi"), exampleTranslation,
                validation,
                new HBox(10, cancel, save));
        form.setPadding(new Insets(22));
        form.setPrefWidth(460);
        ScrollPane dialogScroll = new ScrollPane(form);
        dialogScroll.setFitToWidth(true);
        dialogScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialogScroll.getStyleClass().add("settings-scroll-pane");
        Scene scene = new Scene(dialogScroll, 480, 620);
        var css = getClass().getResource("/com/wordreminder/ui/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private void editSelectedWord() {
        Word selected = wordTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!selected.isCustom()) {
            showInfo("Hazır kelime düzenlenemez", "Hazır havuzdaki kelimeler korunur. Kendi eklediğin kelimeleri düzenleyebilirsin.");
            return;
        }
        showWordDialog(selected);
    }

    private void deleteSelectedWord() {
        deleteSelectedWords();
    }

    private void deleteSelectedWords() {
        List<Word> selected = new ArrayList<>(wordTable.getSelectionModel().getSelectedItems());
        List<Word> customWords = selected.stream().filter(Word::isCustom).toList();
        if (customWords.isEmpty()) {
            showInfo("Kişisel kelime seçilmedi",
                    "Toplu silme yalnızca kendi eklediğin kelimeleri etkiler. Hazır kelime kataloğu korunur.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                customWords.size() + " kişisel kelime silinsin mi? Hazır kelimeler ve diğer öğrenme verileri korunur.",
                ButtonType.YES, ButtonType.NO);
        confirmation.initOwner(stage);
        confirmation.setTitle("Kişisel kelimeleri sil");
        confirmation.setHeaderText("Toplu silme onayı");
        if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            List<Long> ids = customWords.stream().map(Word::id).toList();
            int deleted = wordRepository.deleteCustomWords(ids);
            refreshData();
            showInfo("Silme tamamlandı", deleted + " kişisel kelime silindi. Hazır katalog ve diğer kelimelerin tekrar geçmişi değiştirilmedi.");
        } catch (SQLException ex) {
            showError("Kelimeler silinemedi", ex.getMessage());
        }
    }

    private void resetWordFilters() {
        if (searchDebounce != null) searchDebounce.stop();
        suppressFilterEvents = true;
        try {
            if (searchField != null) searchField.clear();
            tableLevelFilter.setValue(ALL_LEVELS);
            tableCategoryFilter.setValue(ALL_CATEGORIES);
            tableLearningFilter.setValue("Tüm durumlar");
        } finally {
            suppressFilterEvents = false;
        }
        filterTable();
    }

    private void loadSettings() {
        try {
            intervalSpinner.getValueFactory().setValue(settingsRepository.getReminderMinutes());
            dailyGoalSpinner.getValueFactory().setValue(settingsRepository.getDailyGoal());
            startWithWindowsCheck.setSelected(settingsRepository.isStartWithWindows());
            startMinimizedCheck.setSelected(settingsRepository.isStartMinimized());
            startMinimizedCheck.setDisable(!startupManager.isSupported() || !startWithWindowsCheck.isSelected());
            reminderLevelCombo.setValue(levelLabel(settingsRepository.getReminderLevel()));
            reminderCategoryCombo.setValue(categoryLabel(settingsRepository.getReminderCategory()));
        } catch (SQLException ex) {
            showError("Ayarlar okunamadı", ex.getMessage());
        }
    }

    private void saveSettings() {
        int minutes = intervalSpinner.getValue();
        boolean startup = startWithWindowsCheck.isSelected();
        boolean minimized = startup && startMinimizedCheck.isSelected();
        String level = selectedLevelCode(reminderLevelCombo.getValue());
        String category = selectedCategoryCode(reminderCategoryCombo.getValue());
        try {
            int previousMinutes = settingsRepository.getReminderMinutes();
            settingsRepository.setReminderMinutes(minutes);
            settingsRepository.setDailyGoal(dailyGoalSpinner.getValue());
            settingsRepository.setStartWithWindows(startup);
            settingsRepository.setStartMinimized(minimized);
            settingsRepository.setReminderLevel(level);
            settingsRepository.setReminderCategory(category);
            startupManager.setEnabled(startup);
            if (minutes != previousMinutes) {
                intervalChanged.accept(minutes);
            }
            statusLabel.setText("Ayarlar kaydedildi");
            showInfo("Ayarlar kaydedildi", "Hatırlatma aralığı " + minutes + " dakika. Süre kart gösterildiğinde başlar. Günlük hedef: " + dailyGoalSpinner.getValue() + ". Seçim: "
                    + levelLabel(level) + " / " + categoryLabel(category) + ".");
        } catch (Exception ex) {
            showError("Ayarlar kaydedilemedi", ex.getMessage());
        }
    }

    private String selectedLevelCode(String label) {
        return label == null || ALL_LEVELS.equals(label) ? "ALL" : label;
    }

    private String selectedLearningStateCode(String label) {
        if ("Henüz çalışılmayan".equals(label)) return "NEW";
        if ("Çalışılan Kelimeler".equals(label)) return "STUDIED";
        if ("Zor Kelimeler".equals(label)) return "HARD";
        if ("Tekrar zamanı gelen".equals(label)) return "DUE";
        if ("Öğrenilen".equals(label)) return "MASTERED";
        return "ALL";
    }

    private String selectedCategoryCode(String label) {
        if (label == null || ALL_CATEGORIES.equals(label)) return "ALL";
        return "Günlük".equals(label) ? "DAILY" : "TECHNICAL";
    }

    private String levelLabel(String code) {
        return code == null || code.equalsIgnoreCase("ALL") ? ALL_LEVELS : code.toUpperCase();
    }

    private String categoryLabel(String code) {
        if (code == null || code.equalsIgnoreCase("ALL")) return ALL_CATEGORIES;
        return code.equalsIgnoreCase("DAILY") ? "Günlük" : "Teknik";
    }

    private String categoryLabel(WordCategory category) {
        return category == WordCategory.DAILY ? "Günlük" : "Teknik";
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Bilinmeyen hata" : message);
        alert.showAndWait();
    }
    private record PeriodData(String label, int reviews, int known, int distinctWords, int totalDisplays) { }

    private record LevelData(String level, int mastered, int total) { }

    private record ProgressData(int mastered, int reviews, int correct, int withExamples,
                                int missingExamples, int todayReviews, int currentStreak, int dailyGoal,
                                List<PeriodData> periods, List<LevelData> levels) { }
}
