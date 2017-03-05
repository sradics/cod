package net.ontheagilepath;/**
 * Created by sebastianradics on 04.03.17.
 */

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import net.ontheagilepath.binding.FeatureListType;
import net.ontheagilepath.binding.FeatureType;
import net.ontheagilepath.binding.ObjectFactory;
import net.ontheagilepath.graph.GraphDataBeanContainer;
import net.ontheagilepath.util.DateTimeStringConverter;
import net.ontheagilepath.util.FileUtil;
import net.ontheagilepath.util.JavaBridge;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.ontheagilepath.util.DateTimeStringConverter.PATTERN;

@SpringBootApplication
@Configuration
@ComponentScan(excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=App.class)
})
@EnableAutoConfiguration
public class CostOfDeliveryApplication extends Application {

    private FeatureSequenceModel sequenceModel = new FeatureSequenceModel();
    private TextField projectStartDate;
    private TextField name;
    private TextField costOfDelayPerWeek;
    private TextField costOfDelayStartDate;
    private TextField costOfDelayEndDate;
    private TextField costOfDelayStartWeek;
    private TextField costOfDelayEndWeek;
    private TextField featureBuildDuration;
    private Button addFeatureButton;
    private Button clearButton;
    private Button showChartButton;
    private Button showFeatureScreeButton;
    private Button calculateSequenceButton;
    private Button loadButton;
    private Button saveButton;
    private Label sequenceLabel;
    private TableView<Feature> featureTable;
    private Scene mainScene;
    private Stage stage;
    private Feature[] lastCalculatedSequence;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        mainScene = new Scene(grid, 900, 600);

        primaryStage.setTitle("Cost of Delivery Sequence Calculator");

        addProjectStartDateInput(grid);

        addFeatureNameInput(grid);
        addCostOfDelayPerWeekInput(grid);
        addCostOfDelayStartWeekInput(grid);
        addCostOfDelayEndWeekInput(grid);
        addCostOfDelayStartDateInput(grid);
        addCostOfDelayEndDateInput(grid);
        addFeatureBuildDurationInput(grid);

        sequenceLabel = new Label("Sequence:");
        grid.add(sequenceLabel, 3, 6,3,1);

        addFeatureButton(grid);
        addCalculateSequenceButton(grid);
        addLoadButton(grid);
        addSaveButton(grid);
        addClearButton(grid);
        addShowChartButton(grid);

        addFeatureTable(grid);

        primaryStage.setScene(mainScene);
        stage = primaryStage;
        primaryStage.show();


    }

    private void openChartView(Stage primaryStage){
        Scene scene = new Scene(new Group());

        FlowPane flow = new FlowPane();

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);

        TotalCostOfDelayCalculator calculator = applicationContext.getBean(TotalCostOfDelayCalculator.class);
        GraphDataBeanContainer container = calculator.calculateWeeklyCostOfDelayForSequence(lastCalculatedSequence,
                new DateTimeStringConverter().fromString(projectStartDate.getText()));

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

        flow.getChildren().add(scrollPane);
        addShowFeatureScreenButton(flow);
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
        return chartView;
    }

    private void openFeatureScreenView(Stage primaryStage){
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void addProjectStartDateInput(GridPane grid) {
        Label projectStartDateLabel = new Label("Project Start Date:");
        grid.add(projectStartDateLabel, 0, 1);

        projectStartDate = new TextField();
        grid.add(projectStartDate, 1, 1);
        projectStartDate.setText(new SimpleDateFormat(PATTERN).format(new Date()));
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
                    if (projectStartDate.getText() != null && !projectStartDate.getText().isEmpty()) {
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
                    if (projectStartDate.getText()!=null && !projectStartDate.getText().isEmpty()) {
                        if (costOfDelayEndWeek.getText() != null && !costOfDelayEndWeek.getText().isEmpty()) {
                            costOfDelayEndDate.setText(DateTime.parse(projectStartDate.getText(),
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

        TableColumn nameCol = new TableColumn("Name");
        TableColumn codPerWeekCol = new TableColumn("CoD/Week");
        TableColumn durationCol = new TableColumn("Duration");
        TableColumn codStartDateCol = new TableColumn("CoD Start Date");
        TableColumn codEndDateCol = new TableColumn("CoD End Date");

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


        featureTable.getColumns().addAll(nameCol, codPerWeekCol, durationCol,codStartDateCol,codEndDateCol);

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

    private void addSaveButton(GridPane grid) {
        saveButton = new Button("Save input");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(saveButton);
        grid.add(hbBtn, 1, 7);

        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Specify file to save");
                File fileToSave  = fileChooser.showSaveDialog(grid.getScene().getWindow());
                if (fileToSave != null){
                    List<Feature> features = sequenceModel.getFeatures();
                    ObjectFactory objectFactory = new ObjectFactory();
                    FeatureListType featureList = objectFactory.createFeatureListType();
                    featureList.setProjectStartDate(projectStartDate.getText());
                    for (Feature feature : features) {
                        FeatureType featureType = objectFactory.createFeatureType();
                        if (feature.getCostOfDelayStartDate()!=null)
                            featureType.setCostOfDelayStartDate(feature.getCostOfDelayStartDate().toString(PATTERN));
                        if (feature.getCostOfDelayEndDate()!=null)
                            featureType.setCostOfDelayEndDate(feature.getCostOfDelayEndDate().toString(PATTERN));


                        featureType.setName(feature.getName());
                        if (feature.getDurationInWeeks()!=null)
                            featureType.setDurationInWeeks(feature.getDurationInWeeks().toEngineeringString());
                        if (feature.getCostOfDelayPerWeek()!=null)
                            featureType.setCostOfDelayPerWeek(feature.getCostOfDelayPerWeek().toEngineeringString());

                        featureType.setCostOfDelayStartWeek(null);
                        featureType.setCostOfDelayEndWeek(null);
                        featureList.getFeature().add(featureType);
                    }


                    try {
                        JAXBElement<FeatureListType> gl =
                                objectFactory.createFeatures( featureList );
                        JAXBContext jc = JAXBContext.newInstance( "net.ontheagilepath.binding" );
                        Marshaller m = jc.createMarshaller();
                        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                        m.marshal( gl, new FileOutputStream(fileToSave) );
                    } catch( Exception jbe ){
                        throw new RuntimeException(jbe);
                    }
                }
            }
        });
    }

    private void addFeatureButton(GridPane grid) {
        addFeatureButton = new Button("Add Feature");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
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
                        costOfDelayEndDate.getText(),
                        projectStartDate.getText());
            }
        });
    }

    private void addClearButton(GridPane grid) {
        clearButton = new Button("Clear Input");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(clearButton);
        grid.add(hbBtn, 3, 7);

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
        showChartButton = new Button("Show Chart");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(showChartButton);
        grid.add(hbBtn, 4, 7);

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
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(showFeatureScreeButton);
        pane.getChildren().add(hbBtn);

        showFeatureScreeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                openFeatureScreenView(stage);
            }
        });
    }


    private void addLoadButton(GridPane grid) {
        loadButton = new Button("Load input data");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loadButton);
        grid.add(hbBtn, 0, 7);

        loadButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Specify file with input to load");
                File fileToOpen = fileChooser.showOpenDialog(grid.getScene().getWindow());
                if (fileToOpen != null) {
                    try {
                        JAXBContext jc = JAXBContext.newInstance("net.ontheagilepath.binding");
                        Unmarshaller u = jc.createUnmarshaller();
                        JAXBElement<FeatureListType> doc = (JAXBElement<FeatureListType>) u.unmarshal(
                                new FileInputStream(fileToOpen)
                        );

                        sequenceModel.clear();
                        SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                        summarizer.clear();

                        FeatureListType featureListType = doc.getValue();
                        projectStartDate.setText(featureListType.getProjectStartDate());
                        List<FeatureType> features = featureListType.getFeature();
                        for (FeatureType feature : features) {
                            sequenceModel.addFeature(
                                    feature.getName(),
                                    feature.getCostOfDelayPerWeek(),
                                    feature.getDurationInWeeks(),
                                    feature.getCostOfDelayStartWeek(),
                                    feature.getCostOfDelayEndWeek(),
                                    feature.getCostOfDelayStartDate(),
                                    feature.getCostOfDelayEndDate(),
                                    featureListType.getProjectStartDate()
                            );
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
    }

    private void addCalculateSequenceButton(GridPane grid) {
        calculateSequenceButton = new Button("Calculate Sequence");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(calculateSequenceButton);
        grid.add(hbBtn, 1, 6);

        calculateSequenceButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                DateTime startDate = DateTime.now();
                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                summarizer.clear();
                Feature[] featureSequence = applicationContext.getBean(Sequencer.class).calculateSequence(
                        sequenceModel.getFeatures(),startDate);
                lastCalculatedSequence = featureSequence;

                TotalCostOfDelayCalculator calculator = applicationContext.getBean(TotalCostOfDelayCalculator.class);


                summarizer.printSummary();

                StringBuilder sequenceResult = new StringBuilder();
                boolean previousExists = false;
                for (Feature feature : featureSequence) {
                    if (previousExists)
                        sequenceResult.append(",");
                    sequenceResult.append(feature.getName());
                    previousExists = true;
                }
                sequenceResult.append(" Total-CoD:");
                sequenceResult.append(calculator.calculateTotalCostOfDelayForSequence(featureSequence,startDate).toString());
                sequenceLabel.setText(sequenceResult.toString());
                System.out.println(Arrays.asList(featureSequence));
                System.out.println("Done");
            }
        });
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContextInput) {
        applicationContext = applicationContextInput;
    }

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CostOfDeliveryApplication.class).headless(false).run(args);
        setApplicationContext(context);
        launch(args);
    }


}
