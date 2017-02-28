package net.ontheagilepath;

import org.joda.time.DateTime;
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
                return 3;
            }

            @Override
            public String getColumnName(int columnIndex) {
                if (columnIndex==0)
                    return "Name";
                if (columnIndex==1)
                    return "COD/week";
                if (columnIndex==2)
                    return "Duration";
                return null;
            }


            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
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
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

            }

        };

        featureTable.setModel(tableModel);
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
                        projectStartDate.getText());

                featureTable.updateUI();
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

                summarizer.printSummary();

                System.out.println(Arrays.asList(featureSequence));
                System.out.println("Done");
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
