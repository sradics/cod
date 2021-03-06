package net.ontheagilepath;/**
 * Created by sebastianradics on 04.03.17.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import net.ontheagilepath.aspects.CancelCalculationEvent;
import net.ontheagilepath.aspects.MessageEvent;
import net.ontheagilepath.aspects.ProgressUpdateAspect;
import net.ontheagilepath.aspects.SummaryFinishedListener;
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
import java.util.Observer;
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
    private Task worker;

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
    private TextField wsjfFeatureSequenceTextField;

    private ProgressBar progressBar;

    private Button addFeatureButton;
    private Button clearButton;
    private Button showChartButton;
    private Button showFeatureScreeButton;
    private Button calculateSequenceButton;
    private Label codValueLabel;
    private Label maxCodValueLabel;
    private Label wsjfCodValueLabel;
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

        SummaryFinishedListener listener = applicationContext.getBean(SummaryFinishedListener.class);
        listener.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                Platform.runLater(new Runnable() {//UI update must run the JavaFX thread!
                    @Override
                    public void run() {
                        statusTextField.setText(((MessageEvent)arg).getMessage());
                    }
                });
            }
        });

        statusTextField.setEditable(false);
        footer.getChildren().add(statusTextField);

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setId("menubarBox");

        //hb.setAlignment(Pos.CENTER_LEFT);
        hb.setAlignment(Pos.CENTER_LEFT);
        progressBar = new ProgressBar(0.0);
        progressBar.setMinWidth(290);
        ProgressUpdateAspect progressUpdateAspect = applicationContext.getBean(ProgressUpdateAspect.class);
        progressUpdateAspect.setProgressBar(progressBar);
        hb.getChildren().addAll(menuBar,progressBar);

        vBox.getChildren().addAll(hb, grid,footer);
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
        grid.add(new Label("Wsjf Sequence:"), 3, 8);

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

        wsjfFeatureSequenceTextField = new TextField();
        wsjfFeatureSequenceTextField.setEditable(false);
        grid.add(wsjfFeatureSequenceTextField, 4, 8, 2,1);
        wsjfCodValueLabel = new Label("");
        grid.add(wsjfCodValueLabel, 6, 8);

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
        menuBar.setId("menubar");
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
        resetCalculationDisplay();
    }

    private void resetCalculationDisplay() {
        codValueLabel.setText("");
        maxCodValueLabel.setText("");
        wsjfCodValueLabel.setText("");
        featureSequenceTextField.setText("");
        maxFeatureSequenceTextField.setText("");
        wsjfFeatureSequenceTextField.setText("");
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
        final VBox vbox = createFeatureTable();
        grid.add(vbox,0,10,10,10);
    }

    private VBox createFeatureTable() {
        featureTable = new TableView(sequenceModel.getFeatures());

        featureTable.setEditable(true);

        TableColumn nrCol = new TableColumn("Nr");
        nrCol.setId("column_nr");
        TableColumn nameCol = new TableColumn("Name");
        nameCol.setId("column_name");
        TableColumn codPerWeekCol = new TableColumn("CoD/Week");
        codPerWeekCol.setId("column_cod_per_week");
        TableColumn durationCol = new TableColumn("Duration");
        durationCol.setId("column_duration");
        TableColumn codStartDateCol = new TableColumn("CoD Start Date");
        codStartDateCol.setId("column_cod_start_date");
        TableColumn codEndDateCol = new TableColumn("CoD End Date");
        codEndDateCol.setId("column_cod_end_date");
        TableColumn codWsjf = new TableColumn("CoD/Duration");
        codWsjf.setId("column_wsjf");


        nrCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                return new ReadOnlyObjectWrapper(param.getTableView().getItems().indexOf(param.getValue()));
            }
        });
        nrCol.setEditable(false);

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

        codWsjf.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Feature,BigDecimal>, ObservableValue<BigDecimal>>(){
                    @Override
                    public ObservableValue<BigDecimal> call(TableColumn.CellDataFeatures<Feature, BigDecimal> param) {
                        Feature feature = param.getValue();
                        return new ReadOnlyObjectWrapper<BigDecimal>(feature.calculateCD3());
                    }
                }
        );
        codWsjf.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        codWsjf.setEditable(false);


        featureTable.getColumns().addAll(nrCol,nameCol, codPerWeekCol, durationCol,codStartDateCol,codEndDateCol,codWsjf);

        final Label label = new Label("Features");
        label.setFont(new Font("Arial", 20));
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, featureTable);
        return vbox;
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
                resetCalculationDisplay();
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
                if (!calculateSequenceButton.getText().equals("Cancel calculation")){
                    calculateSequenceButton.setText("Cancel calculation");
                    statusTextField.setText("Start sequence calculation");
                    progressBar.setProgress(0);
                    worker = createWorker();
                    worker.messageProperty().addListener(new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> observable,
                                            String oldValue, String newValue) {
                            progressBar.setProgress(1);
                        }
                    });
                    Thread t = new Thread(worker);
                    t.setDaemon(true);
                    t.start();
                }else{
                    calculateSequenceButton.setText("Calculate Sequence");
                    applicationContext.publishEvent(new CancelCalculationEvent(this));
                    progressBar.setProgress(0);
                }

            }
        });
    }

    private Task createWorker(){
        return new Task(){
            @Override
            protected Object call() throws Exception {
                DateTime startDate = new DateTimeStringConverter().fromString(sequenceModel.getProjectStartDate());
                Feature[] featureSequence = applicationContext.getBean(Sequencer.class).calculateSequence(
                        sequenceModel.getFeatures(),startDate);
                String cod = applicationContext.getBean(TotalCostOfDelayCalculator.class)
                        .calculateTotalCostOfDelayForSequence(featureSequence,startDate).toString();

                Feature[] featureSequenceWsjf = applicationContext.getBean(Sequencer.class).calculateWsjfSequence(
                        sequenceModel.getFeatures());
                String codWsjf = applicationContext.getBean(TotalCostOfDelayCalculator.class)
                        .calculateTotalCostOfDelayForSequence(featureSequenceWsjf,startDate).toString();

                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                Platform.runLater(new Runnable() {//UI update must run the JavaFX thread!
                    @Override
                    public void run() {
                        codValueLabel.setText(cod);
                        featureSequenceTextField.setText(getSequenceAsString(featureSequence));

                        maxCodValueLabel.setText(summarizer.getTotalCostOfDelayMax().getTotalCostOfDelay().toString());
                        maxFeatureSequenceTextField.setText(
                                StringUtil.convertStringList(summarizer.getTotalCostOfDelayMax().getFeatureSequence()));

                        wsjfCodValueLabel.setText(codWsjf);
                        wsjfFeatureSequenceTextField.setText(getSequenceAsString(featureSequenceWsjf));
                        calculateSequenceButton.setText("Calculate Sequence");
                    }
                });

                updateProgress(100,100);
                return true;
            }
        };

    }

    private String getSequenceAsString(Feature[] featureSequence) {
        StringBuilder sequenceResultStb = new StringBuilder();
        boolean previousExists = false;
        for (Feature feature : featureSequence) {
            if (previousExists) {
                sequenceResultStb.append(",");
            }
            sequenceResultStb.append(feature.getName());
            previousExists = true;
        }
        return sequenceResultStb.toString();
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
