package net.ontheagilepath;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Configuration
@ComponentScan
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

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sequenceModel.clear();
                featureTable.updateUI();
                SequenceSummarizer summarizer = applicationContext.getBean(SequenceSummarizer.class);
                summarizer.clear();
            }
        });
        codStartWeek.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (projectStartDate.getText()!=null && !projectStartDate.getText().isEmpty()) {
                    if (codStartWeek.getText() != null && !codStartWeek.getText().isEmpty()) {
                        codStartDate.setText(DateTime.parse(projectStartDate.getText(),
                                DateTimeFormat.forPattern("dd.MM.yyyy")).plusWeeks(Integer.valueOf(codStartWeek.getText())).toString("dd.MM.yyyy"));
                    }
                }
            }
        });
        codEndWeek.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (projectStartDate.getText()!=null && !projectStartDate.getText().isEmpty()) {
                    if (codEndWeek.getText() != null && !codEndWeek.getText().isEmpty()) {
                        codEndDate.setText(DateTime.parse(projectStartDate.getText(),
                                DateTimeFormat.forPattern("dd.MM.yyyy")).plusWeeks(Integer.valueOf(codEndWeek.getText())).toString("dd.MM.yyyy"));
                    }
                }
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
