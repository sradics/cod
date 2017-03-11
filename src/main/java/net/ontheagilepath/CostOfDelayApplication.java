package net.ontheagilepath;/**
 * Created by sebastianradics on 04.03.17.
 */

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.BigDecimalStringConverter;
import net.ontheagilepath.graph.GraphDataBeanContainer;
import net.ontheagilepath.util.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import static net.ontheagilepath.util.DateTimeStringConverter.PATTERN;

@SpringBootApplication
@Configuration
@ComponentScan(excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=App.class)
})
@EnableAutoConfiguration
public class CostOfDelayApplication extends Application {
    private static final Logger log = Logger.getLogger( CostOfDelayApplication.class.getName() );

    private FeatureSequenceModel sequenceModel = new FeatureSequenceModel();
    private TextField projectStartDate;
    private TextField name;
    private TextField costOfDelayPerWeek;
    private TextField costOfDelayStartDate;
    private TextField costOfDelayEndDate;
    private TextField costOfDelayStartWeek;
    private TextField costOfDelayEndWeek;
    private TextField featureBuildDuration;
    private TextField featureSequenceTextField;
    private TextField maxFeatureSequenceTextField;

    private Button addFeatureButton;
    private Button clearButton;
    private Button showChartButton;
    private Button showFeatureScreeButton;
    private Button calculateSequenceButton;
    private Label codValueLabel;
    private Label maxCodValueLabel;
    private TextField statusTextField;
    private TableView<Feature> featureTable;
    private Scene mainScene;
    private Stage stage;
    private static final int SCREEN_WIDTH=900;
    private static final int SCREEN_HEIGHT=600;
    private static final String SCREEN_CSS="/codapplication.css";

    @Override
    public void start(Stage primaryStage) {
        VBox vBox = new VBox();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setId("mainGrid");
        grid.setPadding(new Insets(25, 25, 25, 25));
        addColumnConstraints(vBox, grid);

        MenuBar menuBar = createMenu(grid);
        VBox footer = new VBox();
        footer.setId("footer");
        statusTextField = new TextField();
        statusTextField.setEditable(false);
        footer.getChildren().add(statusTextField);

        vBox.getChildren().addAll(menuBar, grid,footer);
        mainScene = new Scene(vBox, SCREEN_WIDTH, SCREEN_HEIGHT);
        mainScene.getStylesheets().add(SCREEN_CSS);

        primaryStage.setTitle("Cost of Delay Sequence Calculator");

        addProjectStartDateInput(grid);

        addFeatureNameInput(grid);
        addCostOfDelayPerWeekInput(grid);
        addCostOfDelayStartWeekInput(grid);
        addCostOfDelayEndWeekInput(grid);
        addCostOfDelayStartDateInput(grid);
        addCostOfDelayEndDateInput(grid);
        addFeatureBuildDurationInput(grid);
        addSequenceInformation(grid);

        addFeatureButton(grid);
        addCalculateSequenceButton(grid);
        addClearButton(grid);
        addShowChartButton(grid);

        addFeatureTable(grid);

        primaryStage.setScene(mainScene);
        stage = primaryStage;
        primaryStage.show();
    }

    private void addSequenceInformation(GridPane grid) {
        Label sequenceLabel = new Label("Best Sequence:");
        grid.add(sequenceLabel, 3, 6);
        grid.add(new Label("Worst Sequence:"), 3, 7);

        featureSequenceTextField = new TextField();
        featureSequenceTextField.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!featureSequenceTextField.isFocused() && featureSequenceTextField.getText()!=null && !featureSequenceTextField.getText().isEmpty()) {
                    DateTime startDate = new DateTimeStringConverter().fromString(sequenceModel.getProjectStartDate());
                    Feature[] featureSequence = FeatureSequenceUtil.getSequenceFromLabels(
                            featureSequenceTextField.getText(),sequenceModel.getFeatures().toArray(new Feature[]{})
                    );
                    TotalCostOfDelayCalculator calculator = applicationContext.getBean(TotalCostOfDelayCalculator.class);
                    codValueLabel.setText(calculator.calculateTotalCostOfDelayForSequence(featureSequence,startDate).toString());
                }
            }
        });
        grid.add(featureSequenceTextField, 4, 6, 2,1);

        codValueLabel = new Label("");
        grid.add(codValueLabel, 6, 6);

        maxFeatureSequenceTextField = new TextField();
        maxFeatureSequenceTextField.setEditable(false);
        grid.add(maxFeatureSequenceTextField, 4, 7, 2,1);
        maxCodValueLabel = new Label("");
        grid.add(maxCodValueLabel, 6, 7);
    }

    private void addColumnConstraints(VBox vBox, GridPane grid) {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(100);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(70);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setMinWidth(120);

        ColumnConstraints col5 = new ColumnConstraints();
        col5.setMinWidth(100);

        ColumnConstraints col6 = new ColumnConstraints();
        col6.setMinWidth(100);

        ColumnConstraints col7 = new ColumnConstraints();
        col7.setMinWidth(100);


        grid.getColumnConstraints().addAll(col1,col2,col3,col4,col5,col6,col7);
        vBox.setVgrow(grid, Priority.ALWAYS);
    }

    private MenuBar createMenu(GridPane grid) {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");

        menuBar.getMenus().addAll(menuFile);
        MenuItem loadFile = createLoadFileMenuItem(grid);
        MenuItem loadFileSample = createLoadFileSampleMenuItem(grid);
        MenuItem saveFile = createSaveFileMenuItem(grid);

        menuFile.getItems().addAll(loadFile,loadFileSample,saveFile);
        return menuBar;
    }

    private MenuItem createSaveFileMenuItem(final GridPane grid) {
        MenuItem loadFile = new MenuItem("Save Input...");
        loadFile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Specify file to save");
                File fileToSave  = fileChooser.showSaveDialog(grid.getScene().getWindow());
                applicationContext.getBean(JAXBFileHelper.class).saveToFile(fileToSave,sequenceModel);
            }
        });
        return loadFile;
    }

    private MenuItem createLoadFileSampleMenuItem(final GridPane grid) {
        MenuItem loadFile = new MenuItem("Load Input Sample");
        loadFile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                sequenceModel = applicationContext.getBean(JAXBFileHelper.class).loadInputDataFromStream(getClass().getResourceAsStream("/niceChartData.xml"));
                updateFromModel();
            }
        });
        return loadFile;
    }

    private void updateFromModel() {
        projectStartDate.setText(sequenceModel.getProjectStartDate());
        featureTable.setItems(sequenceModel.getFeatures());
    }




    private MenuItem createLoadFileMenuItem(final GridPane grid) {
        MenuItem loadFile = new MenuItem("Load Input...");
        loadFile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Specify file with input to load");
                File fileToOpen = fileChooser.showOpenDialog(grid.getScene().getWindow());
                sequenceModel = applicationContext.getBean(JAXBFileHelper.class).loadInputDataFromFile(fileToOpen);
                updateFromModel();
            }
        });
        return loadFile;
    }

    private void openChartView(Stage primaryStage){
        Scene scene = new Scene(new Group(),SCREEN_WIDTH,SCREEN_HEIGHT);
        scene.getStylesheets().add(SCREEN_CSS);

        FlowPane flow = new FlowPane();
        flow.setId("flowpane");

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        browser.setId("browser");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);
        scrollPane.setId("graphScrollpane");
        scrollPane.fitToHeightProperty();
        scrollPane.fitToWidthProperty();

        TotalCostOfDelayCalculator calculator = applicationContext.getBean(TotalCostOfDelayCalculator.class);
        Feature[] sequence = FeatureSequenceUtil.getSequenceFromLabels(
                featureSequenceTextField.getText(),
                sequenceModel.getFeatures().toArray(new Feature[]{}));

        GraphDataBeanContainer container = calculator.calculateWeeklyCostOfDelayForSequence(sequence,
                new DateTimeStringConverter().fromString(sequenceModel.getProjectStartDate()));

        webEngine.getLoadWorker().stateProperty()
                .addListener(new ChangeListener<State>() {
                    @Override
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");

                        JavaBridge bridge = new JavaBridge();
                        window.setMember("java", bridge);
                        window.setMember("databeans",container);

                        webEngine.executeScript("console.log = function(message)\n" +
                                "{\n" +
                                "    java.log(message);\n" +
                                "};");

                        if (newState == Worker.State.SUCCEEDED) {
                            primaryStage.setTitle(webEngine.getLocation());
                            //window = (netscape.javascript.JSObject) webEngine.executeScript("window");

                        }

                    }
                });

        webEngine.setJavaScriptEnabled(true);
        injectChartView(webEngine);

        addShowFeatureScreenButton(flow);
        flow.getChildren().add(scrollPane);
        scene.setRoot(flow);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void injectChartView(WebEngine webEngine) {
        String chartView = FileUtil.loadFileToString(getClass(), "/costOfDelayChart.html");
        chartView = injectJavaScript(chartView); //workaround for not working loading of javascript libraries from local
        webEngine.loadContent(chartView);
    }

    private String injectJavaScript(String chartView) {
        String javaScriptToInject = FileUtil.loadFileToString(getClass(),"/scripts.js");
        chartView = chartView.replace("//placeholder__",javaScriptToInject);

        javaScriptToInject = FileUtil.loadFileToString(getClass(),"/initWebView.js");
        chartView = chartView.replace("//do_not_delete_initWebView_placeholder__",javaScriptToInject);

        return chartView;
    }

    private void openFeatureScreenView(Stage primaryStage){
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void addProjectStartDateInput(GridPane grid) {
        Label projectStartDateLabel = new Label("Project Start Date:");
        grid.add(projectStartDateLabel, 0, 1);

        projectStartDate = new TextField(sequenceModel.getProjectStartDate());
        grid.add(projectStartDate, 1, 1);

        projectStartDate.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!projectStartDate.isFocused()) {
                    sequenceModel.setProjectStartDate(projectStartDate.getText());
                }
            }
        });
    }

    private void addFeatureNameInput(GridPane grid) {
        Label nameLabel = new Label("Name:");
        grid.add(nameLabel, 0, 2);

        name = new TextField();
        grid.add(name, 1, 2);
    }

    private void addCostOfDelayPerWeekInput(GridPane grid) {
        Label costOfDelayPerWeekLabel = new Label("Cost of Delay/Week:");
        grid.add(costOfDelayPerWeekLabel, 0, 3);

        costOfDelayPerWeek = new TextField();
        grid.add(costOfDelayPerWeek, 1, 3);
    }

    private void addCostOfDelayStartWeekInput(GridPane grid) {
        Label costOfDelayStartWeekLabel = new Label("CoD Start Week:");
        grid.add(costOfDelayStartWeekLabel, 3, 3);

        costOfDelayStartWeek= new TextField();
        costOfDelayStartWeek.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!costOfDelayStartWeek.isFocused()) {
                    if (sequenceModel.getProjectStartDate() != null && !sequenceModel.getProjectStartDate().isEmpty()) {
                        if (costOfDelayStartWeek.getText() != null && !costOfDelayStartWeek.getText().isEmpty()) {
                            costOfDelayStartDate.setText(
                                    new DateTimeStringConverter()
                                            .fromString(projectStartDate.getText()
                                    ).plusWeeks(Integer.valueOf(costOfDelayStartWeek.getText())).toString(PATTERN));
                        } else {
                            costOfDelayStartDate.setText(null);
                        }
                    }
                }
            }
        });
        grid.add(costOfDelayStartWeek, 4, 3);
    }

    private void addCostOfDelayEndWeekInput(GridPane grid) {
        Label costOfDelayEndWeekLabel = new Label("CoD End Week:");
        grid.add(costOfDelayEndWeekLabel, 5, 3);
        costOfDelayEndWeek= new TextField();
        costOfDelayEndWeek.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!costOfDelayEndWeek.isFocused()) {
                    if (sequenceModel.getProjectStartDate()!=null && !sequenceModel.getProjectStartDate().isEmpty()) {
                        if (costOfDelayEndWeek.getText() != null && !costOfDelayEndWeek.getText().isEmpty()) {
                            costOfDelayEndDate.setText(DateTime.parse(sequenceModel.getProjectStartDate(),
                                    DateTimeFormat.forPattern(PATTERN)).plusWeeks(Integer.valueOf(costOfDelayEndWeek.getText())).toString(PATTERN));
                        }else{
                            costOfDelayEndDate.setText(null);
                        }
                    }
                }
            }
        });
        grid.add(costOfDelayEndWeek, 6, 3);
    }

    private void addCostOfDelayStartDateInput(GridPane grid) {
        Label costOfDelayStartDateLabel = new Label("CoD Start Date:");
        grid.add(costOfDelayStartDateLabel, 3, 4);

        costOfDelayStartDate= new TextField();
        grid.add(costOfDelayStartDate, 4, 4);
    }

    private void addCostOfDelayEndDateInput(GridPane grid) {
        Label costOfDelayEndDateLabel = new Label("CoD End Date:");
        grid.add(costOfDelayEndDateLabel, 5, 4);

        costOfDelayEndDate= new TextField();
        grid.add(costOfDelayEndDate, 6, 4);
    }

    private void addFeatureBuildDurationInput(GridPane grid) {
        Label featureBuildDurationLabel = new Label("Feature Dev Duration:");
        grid.add(featureBuildDurationLabel, 0, 5);

        featureBuildDuration= new TextField();
        grid.add(featureBuildDuration, 1, 5);
    }

    private void addFeatureTable(GridPane grid) {
        featureTable = new TableView(sequenceModel.getFeatures());

        featureTable.setEditable(true);

        TableColumn nrCol = new TableColumn("Nr");
        TableColumn nameCol = new TableColumn("Name");
        TableColumn codPerWeekCol = new TableColumn("CoD/Week");
        TableColumn durationCol = new TableColumn("Duration");
        TableColumn codStartDateCol = new TableColumn("CoD Start Date");
        TableColumn codEndDateCol = new TableColumn("CoD End Date");

        nrCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                return new ReadOnlyObjectWrapper(param.getTableView().getItems().indexOf(param.getValue()));
            }
        });

        nameCol.setCellValueFactory(
                new PropertyValueFactory<Feature,String>("name")
        );

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Feature, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Feature, String> t) {
                        getFeatureFromTablePosition(t).setName(t.getNewValue());
                    }
                }
        );

        codPerWeekCol.setCellValueFactory(
                new PropertyValueFactory<Feature,BigDecimal>("costOfDelayPerWeek")
        );
        codPerWeekCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));

        codPerWeekCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Feature, BigDecimal>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Feature, BigDecimal> t) {
                        getFeatureFromTablePosition(t).setCostOfDelayPerWeek(t.getNewValue());
                    }
                }
        );



        durationCol.setCellValueFactory(
                new PropertyValueFactory<Feature,BigDecimal>("durationInWeeks")
        );
        durationCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));

        durationCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Feature, BigDecimal>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Feature, BigDecimal> t) {
                        getFeatureFromTablePosition(t).setDurationInWeeks(t.getNewValue());
                    }
                }
        );



        codStartDateCol.setCellValueFactory(
                new PropertyValueFactory<Feature,DateTime>("costOfDelayStartDate")
        );
        codStartDateCol.setCellFactory(TextFieldTableCell.forTableColumn(new DateTimeStringConverter()));

        codStartDateCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Feature, DateTime>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Feature, DateTime> t) {
                        getFeatureFromTablePosition(t).setCostOfDelayStartDate(t.getNewValue());
                    }
                }
        );


        codEndDateCol.setCellValueFactory(
                new PropertyValueFactory<Feature,DateTime>("costOfDelayEndDate")
        );
        codEndDateCol.setCellFactory(TextFieldTableCell.forTableColumn(new DateTimeStringConverter()));

        codEndDateCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Feature, DateTime>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Feature, DateTime> t) {
                        getFeatureFromTablePosition(t).setCostOfDelayEndDate(t.getNewValue());
                    }
                }
        );


        featureTable.getColumns().addAll(nrCol,nameCol, codPerWeekCol, durationCol,codStartDateCol,codEndDateCol);

        final Label label = new Label("Features");
        label.setFont(new Font("Arial", 20));
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, featureTable);
        grid.add(vbox,0,9,10,10);
    }

    private Feature getFeatureFromTablePosition(TableColumn.CellEditEvent<Feature, ?> t) {
        return (Feature) t.getTableView().getItems().get(
                t.getTablePosition().getRow());
    }


    private ImageView createImageView(String source){
        Image imageOk = new Image(getClass().getResourceAsStream(source));
        ImageView imageView = new ImageView(imageOk);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(15.0);
        return  imageView;
    }

    private void addFeatureButton(GridPane grid) {
        addFeatureButton = new Button("Add Feature",createImageView("/add.png"));
        addFeatureButton.setId("addFeatureButton");
        addFeatureButton.setAlignment(Pos.BOTTOM_LEFT);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(addFeatureButton);
        grid.add(hbBtn, 0, 6);

        addFeatureButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                sequenceModel.addFeature(
                        name.getText(),
                        costOfDelayPerWeek.getText(),
                        featureBuildDuration.getText(),
                        costOfDelayStartWeek.getText(),
                        costOfDelayEndWeek.getText(),
                        costOfDelayStartDate.getText(),
                        costOfDelayEndDate.getText());
            }
        });
    }

    private void addClearButton(GridPane grid) {

        clearButton = new Button("Clear Input",createImageView("/remove.png"));
        clearButton.setId("clearButton");
        clearButton.setAlignment(Pos.BOTTOM_LEFT);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(clearButton);
        grid.add(hbBtn, 0, 7);

        clearButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                sequenceModel.clear();
                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                summarizer.clear();
            }
        });
    }

    private void addShowChartButton(GridPane grid) {
        showChartButton = new Button("Show Chart",createImageView("/1488765083_chart.png"));
        showChartButton.setId("chartButton");
        showChartButton.setAlignment(Pos.BOTTOM_LEFT);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(showChartButton);
        grid.add(hbBtn, 1, 7,2,1);
        showChartButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                openChartView(stage);
            }
        });
    }

    private void addShowFeatureScreenButton(Pane pane) {
        showFeatureScreeButton = new Button("Show Feature Screen");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(showFeatureScreeButton);
        pane.getChildren().add(hbBtn);

        showFeatureScreeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                openFeatureScreenView(stage);
            }
        });
    }

    private void addCalculateSequenceButton(GridPane grid) {
        calculateSequenceButton = new Button("Calculate Sequence",createImageView("/calculate.png"));
        calculateSequenceButton.setId("calculateSequenceButton");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(calculateSequenceButton);
        grid.add(hbBtn, 1, 6,2,1);

        calculateSequenceButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                DateTime startDate = new DateTimeStringConverter().fromString(sequenceModel.getProjectStartDate());
                Feature[] featureSequence = applicationContext.getBean(Sequencer.class).calculateSequence(
                        sequenceModel.getFeatures(),startDate);
                statusTextField.setText("Wrote full sequence calculation to: "+applicationContext.getBean(SequenceSummarizer.class).getCurrentSummary().getAbsolutePath());

                StringBuilder sequenceResult = new StringBuilder();
                boolean previousExists = false;
                for (Feature feature : featureSequence) {
                    if (previousExists) {
                        sequenceResult.append(",");
                    }
                    sequenceResult.append(feature.getName());
                    previousExists = true;
                }

                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);

                featureSequenceTextField.setText(sequenceResult.toString());
                codValueLabel.setText(applicationContext.getBean(TotalCostOfDelayCalculator.class)
                        .calculateTotalCostOfDelayForSequence(featureSequence,startDate).toString());

                maxCodValueLabel.setText(summarizer.getTotalCostOfDelayMax().getTotalCostOfDelay().toString());
                maxFeatureSequenceTextField.setText(
                        convertStringList(summarizer.getTotalCostOfDelayMax().getFeatureSequence()));

            }
        });
    }

    private String convertStringList(List<String> list){
        StringBuilder result = new StringBuilder();
        boolean append = false;
        for (String s : list) {
            if (append){
                result.append(",");
            }
            append = true;
            result.append(s);
        }
        return result.toString();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContextInput) {
        applicationContext = applicationContextInput;
    }

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CostOfDelayApplication.class).headless(false).run(args);
        setApplicationContext(context);
        launch(args);
    }


}
