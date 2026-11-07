package com.wordreminder.ui;

import com.wordreminder.model.Word;
import com.wordreminder.model.WordCategory;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** A compact, scrollable study card with a clear example/translation hierarchy. */
public final class ReminderPopup {
    private static final double CARD_WIDTH = 470;
    private static final double CARD_HEIGHT = 570;

    private final Stage stage;
    private final Consumer<String> reviewAction;
    private boolean finished;
    private boolean hardWord;

    public ReminderPopup(Word word, Consumer<String> reviewAction) {
        this.stage = new Stage(StageStyle.UNDECORATED);
        this.reviewAction = reviewAction;
        this.hardWord = word.hardWord();
        this.stage.setOnHidden(event -> {
            if (!finished) {
                finished = true;
                this.reviewAction.accept("DISMISS");
            }
        });
        build(word);
    }

    public void show() {
        var bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(CARD_WIDTH, bounds.getWidth() - 24);
        double height = Math.min(CARD_HEIGHT, bounds.getHeight() - 24);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(bounds.getMaxX() - width - 18);
        stage.setY(bounds.getMaxY() - height - 18);
        stage.show();
        stage.toFront();
    }

    /** Closes silently during application shutdown; no review action is recorded. */
    public void close() {
        finished = true;
        stage.close();
    }

    private void build(Word word) {
        VBox content = new VBox(15);
        content.getStyleClass().add("popup-content");
        content.setPadding(new Insets(22));
        content.setFillWidth(true);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label brand = new Label("WORDREMINDER");
        brand.getStyleClass().add("popup-brand");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("popup-close");
        closeButton.setAccessibleText("Hatırlatma kartını kapat");
        closeButton.setOnAction(event -> finish("DISMISS"));
        header.getChildren().addAll(brand, spacer, closeButton);

        HBox badges = new HBox(7);
        Label level = new Label(word.level().name());
        level.getStyleClass().add("popup-badge");
        Label category = new Label(word.category() == WordCategory.DAILY
                ? "GÜNLÜK İNGİLİZCE" : "TEKNİK İNGİLİZCE");
        category.getStyleClass().add("popup-badge");
        badges.getChildren().addAll(level, category);

        Label english = new Label(word.english());
        english.getStyleClass().add("popup-word");
        english.setWrapText(true);

        String pos = word.partOfSpeech().isBlank() ? "Kelime türü belirtilmemiş" : word.partOfSpeech();
        String ipa = word.pronunciation().isBlank() ? "IPA kaydı yok" : word.pronunciation();
        Label pronunciation = new Label(pos + "   ·   " + ipa);
        pronunciation.getStyleClass().add("popup-pronunciation");
        pronunciation.setWrapText(true);

        VBox exampleCard = new VBox(8);
        exampleCard.getStyleClass().add("popup-example-card");
        Label exampleHeading = new Label("ÖRNEK CÜMLE");
        exampleHeading.getStyleClass().add("popup-section-label");
        Label example = new Label(word.example().isBlank()
                ? "Bu kelime için örnek cümle henüz hazırlanmadı."
                : "“" + word.example() + "”");
        example.getStyleClass().add("popup-example-sentence");
        example.setWrapText(true);
        Label translation = new Label(word.exampleTranslation().isBlank()
                ? "Türkçe çevirisi henüz hazırlanmadı."
                : word.exampleTranslation());
        translation.getStyleClass().add(word.exampleTranslation().isBlank()
                ? "popup-translation-missing" : "popup-example-translation");
        translation.setWrapText(true);
        exampleCard.getChildren().addAll(exampleHeading, example, translation);

        Label recallHint = new Label("Önce kelimenin Türkçe karşılığını hatırlamaya çalış.");
        recallHint.getStyleClass().add("popup-recall-hint");
        Label meaningHeading = new Label("KELİMENİN TÜRKÇE ANLAMI");
        meaningHeading.getStyleClass().add("popup-section-label");
        Label meaning = new Label(word.turkish());
        meaning.getStyleClass().add("popup-meaning");
        meaning.setWrapText(true);
        VBox meaningCard = new VBox(7, meaningHeading, meaning);
        meaningCard.getStyleClass().add("popup-meaning-card");
        meaningCard.setVisible(false);
        meaningCard.setManaged(false);

        Button reveal = new Button("Türkçe anlamı göster");
        reveal.getStyleClass().add("primary-button");
        reveal.setMaxWidth(Double.MAX_VALUE);

        Button learned = new Button("Öğrendim");
        learned.getStyleClass().add("primary-button");
        learned.setDisable(true);
        learned.setOnAction(event -> finish("KNOWN"));

        Button repeat = new Button("Tekrar et");
        repeat.getStyleClass().add("secondary-button");
        repeat.setDisable(true);
        repeat.setOnAction(event -> finish("REPEAT"));

        Button later = new Button("5 dk sonra");
        later.getStyleClass().add("secondary-button");
        later.setOnAction(event -> finish("SNOOZE"));

        Button hard = new Button(hardWord ? "★ Zor kelimelerden çıkar" : "☆ Zor kelimelere ekle");
        hard.getStyleClass().add("popup-hard-button");
        hard.setMaxWidth(Double.MAX_VALUE);
        hard.setOnAction(event -> {
            boolean nextState = !hardWord;
            reviewAction.accept(nextState ? "HARD" : "UNHARD");
            hardWord = nextState;
            hard.setText(hardWord ? "★ Zor kelimelerden çıkar" : "☆ Zor kelimelere ekle");
            hard.setAccessibleText(hardWord
                    ? "Zor kelimeler listesinden çıkar"
                    : "Zor kelimeler listesine ekle");
        });

        reveal.setOnAction(event -> {
            meaningCard.setVisible(true);
            meaningCard.setManaged(true);
            recallHint.setText("Kendini değerlendir: Bu kelimeyi hatırlayabildin mi?");
            reveal.setVisible(false);
            reveal.setManaged(false);
            learned.setDisable(false);
            repeat.setDisable(false);
        });

        FlowPane actions = new FlowPane(8, 8, learned, repeat, later);
        actions.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(header, badges, english, pronunciation,
                exampleCard, recallHint, meaningCard, reveal, actions, hard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("popup-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Scene scene = new Scene(scroll, CARD_WIDTH, CARD_HEIGHT);
        var css = getClass().getResource("/com/wordreminder/ui/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
    }

    private void finish(String action) {
        if (finished) {
            return;
        }
        finished = true;
        stage.close();
        reviewAction.accept(action);
    }
}
