package net.ontheagilepath;

import net.ontheagilepath.binding.FeatureListType;
import net.ontheagilepath.binding.FeatureType;
import net.ontheagilepath.binding.ObjectFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
@ComponentScan(excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=App.class)})
@EnableAutoConfiguration
public class FeatureSequenceGUI {
    private JButton addFeatureButton;
    private JPanel MajorPanel;
    private JTextField costOfDelayPerWeek;
    private JTextField durationInWeeks;
    private JTextField featureName;
    private JTable featureTable;
    private JButton calculateSequence;
    private JTextField codStartWeek;
    private JTextField codEndWeek;
    private JTextField projectStartDate;
    private JLabel sequenceLabel;
    private JButton clearButton;
    private JTextField codStartDate;
    private JTextField codEndDate;
    private JButton saveInput;
    private JButton loadInputButton;

    private FeatureSequenceModel sequenceModel = new FeatureSequenceModel();

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private ApplicationContext applicationContext;

    public FeatureSequenceGUI() {
        TableModel tableModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return sequenceModel.getFeatures().size();
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public String getColumnName(int columnIndex) {
                if (columnIndex==0)
                    return "Name";
                if (columnIndex==1)
                    return "CoD/week";
                if (columnIndex==2)
                    return "Duration";
                if (columnIndex==3)
                    return "CoD start date";
                if (columnIndex==4)
                    return "CoD end date";
                return null;
            }


            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                Feature feature  = sequenceModel.getFeatures().get(rowIndex);
                if (columnIndex==0)
                    return feature.getName();
                if (columnIndex==1)
                    return feature.getCostOfDelayPerWeek().getCost().toString();
                if (columnIndex==2)
                    return feature.getDurationInWeeks().toString();
                if (columnIndex==3) {
                    return feature.getCostOfDelayStartDate()!=null? feature.getCostOfDelayStartDate().toString(
                            "dd.MM.yyyy"
                    ):null;
                }
                if (columnIndex==4) {
                    return feature.getCostOfDelayEndDate()!=null? feature.getCostOfDelayEndDate().toString(
                            "dd.MM.yyyy"
                    ):null;
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                DateTime projectStartDateDT = DateTime.parse(projectStartDate.getText(), DateTimeFormat.forPattern("dd.MM.yyyy"));
                Feature feature  = sequenceModel.getFeatures().get(rowIndex);
                if (columnIndex==0)
                    feature.setName((String)aValue);
                if (columnIndex==1)
                    feature.setCostOfDelayPerWeek((String)aValue);
                if (columnIndex==2)
                    feature.setDurationInWeeks((String)aValue);
                if (columnIndex==3) {
                    feature.setCostOfDelayStartDate((String)aValue);
                }
                if (columnIndex==4) {
                    feature.setCostOfDelayEndDate((String)aValue);
                }
            }

        };

        featureTable.setModel(tableModel);
        featureTable.setShowGrid(true);


        projectStartDate.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));

        addFeatureButtonActionPerformed();
        addCalculateSequenceAction();
        addClearButtonAction();
        addCoDStartWeekLostFocusListener();
        addCoDEndDateLostFocusListener();
        addSaveInputListener();
        addLoadInputListener();
    }

    private void addLoadInputListener() {
        loadInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify file with input to load");
                int userSelection = fileChooser.showOpenDialog(MajorPanel);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToOpen = fileChooser.getSelectedFile();

                    try {
                        JAXBContext jc = JAXBContext.newInstance( "net.ontheagilepath.binding" );
                        Unmarshaller u = jc.createUnmarshaller();
                        JAXBElement<FeatureListType> doc = (JAXBElement<FeatureListType>)u.unmarshal(
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

                        featureTable.updateUI();


                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
    }

    private void addSaveInputListener() {
        saveInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify file to save");
                int userSelection = fileChooser.showSaveDialog(MajorPanel);
                if (userSelection == JFileChooser.APPROVE_OPTION){
                    File fileToSave = fileChooser.getSelectedFile();
                    List<Feature> features = sequenceModel.getFeatures();
                    ObjectFactory objectFactory = new ObjectFactory();
                    FeatureListType featureList = objectFactory.createFeatureListType();
                    featureList.setProjectStartDate(projectStartDate.getText());
                    for (Feature feature : features) {
                        FeatureType featureType = objectFactory.createFeatureType();
                        if (feature.getCostOfDelayStartDate()!=null)
                            featureType.setCostOfDelayStartDate(feature.getCostOfDelayStartDate().toString("dd.MM.yyyy"));
                        if (feature.getCostOfDelayEndDate()!=null)
                            featureType.setCostOfDelayEndDate(feature.getCostOfDelayEndDate().toString("dd.MM.yyyy"));


                        featureType.setName(feature.getName());
                        if (feature.getDurationInWeeks()!=null)
                            featureType.setDurationInWeeks(feature.getDurationInWeeks().toEngineeringString());
                        if (feature.getCostOfDelayPerWeek()!=null)
                            featureType.setCostOfDelayPerWeek(feature.getCostOfDelayPerWeek().getCost().toEngineeringString());

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

    private void addTableModel() {

    }

    private void addCoDEndDateLostFocusListener() {
        codEndWeek.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (projectStartDate.getText()!=null && !projectStartDate.getText().isEmpty()) {
                    if (codEndWeek.getText() != null && !codEndWeek.getText().isEmpty()) {
                        codEndDate.setText(DateTime.parse(projectStartDate.getText(),
                                DateTimeFormat.forPattern("dd.MM.yyyy")).plusWeeks(Integer.valueOf(codEndWeek.getText())).toString("dd.MM.yyyy"));
                    }else{
                        codEndDate.setText(null);
                    }
                }
            }
        });
    }

    private void addCoDStartWeekLostFocusListener() {
        codStartWeek.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (projectStartDate.getText()!=null && !projectStartDate.getText().isEmpty()) {
                    if (codStartWeek.getText() != null && !codStartWeek.getText().isEmpty()) {
                        codStartDate.setText(DateTime.parse(projectStartDate.getText(),
                                DateTimeFormat.forPattern("dd.MM.yyyy")).plusWeeks(Integer.valueOf(codStartWeek.getText())).toString("dd.MM.yyyy"));
                    }else{
                        codStartDate.setText(null);
                    }
                }
            }
        });
    }

    private void addClearButtonAction() {
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sequenceModel.clear();
                featureTable.updateUI();
                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                summarizer.clear();
            }
        });
    }

    private void addCalculateSequenceAction() {
        calculateSequence.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DateTime startDate = DateTime.now();
                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                summarizer.clear();
                Feature[] featureSequence = applicationContext.getBean(Sequencer.class).calculateSequence(
                        sequenceModel.getFeatures(),startDate);

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
                sequenceLabel.setVisible(true);
                System.out.println(Arrays.asList(featureSequence));
                System.out.println("Done");
            }
        });
    }

    private void addFeatureButtonActionPerformed() {
        addFeatureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sequenceModel.addFeature(
                        featureName.getText(),
                        costOfDelayPerWeek.getText(),
                        durationInWeeks.getText(),
                        codStartWeek.getText(),
                        codEndWeek.getText(),
                        codStartDate.getText(),
                        codEndDate.getText(),
                        projectStartDate.getText());

                featureTable.updateUI();
                featureTable.getTableHeader().updateUI();
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(FeatureSequenceGUI.class).headless(false).run(args);
        FeatureSequenceGUI gui = new FeatureSequenceGUI();
        gui.setApplicationContext(context);
        JFrame frame = new JFrame("FeatureSequenceGUI");
        frame.setContentPane(gui.MajorPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
