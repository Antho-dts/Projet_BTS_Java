//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import org.jfree.chart.ChartPanel;

import Utils.Tempo;

//--------------------- Main Class -----------------------//
public class PatientPanel {

    //--------------------- Attributes -----------------------//
    private WS_Client wsClient;

    private JPanel homePanel;
    private JPanel mainContentPanel;
    private JPanel patientsListPanel;
    private JScrollPane scrollPatientsListPane;

    private JPanel panelsContainer;
    private JScrollPane scrollPanelsPane;

    private GridBagConstraints gridBagConstraints;
    private Component[] panelComponents;

    private JPanel patientPanel, headerPanel, graphPanel, infoPanel, actionPanel, alertPanel, statusIndicator;

    private JLabel nameLabel, lastUpdateLabel, statusLabel;
    private JButton detailsButton;

    private int age;

    private ChartPanel oxygenChartPanel, pulseChartPanel;

    //--------------------- Constructor -----------------------//
    public PatientPanel(WS_Client wsClient) {
        this.wsClient = wsClient;

        // Home panel setup
        this.homePanel = new JPanel(new BorderLayout());

        // Patients list panel
        this.patientsListPanel = new JPanel();
        this.patientsListPanel.setLayout(new BoxLayout(this.patientsListPanel, BoxLayout.Y_AXIS));
        this.patientsListPanel.setBackground(new Color(240, 240, 240));
        this.patientsListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
                "Patients List",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        this.scrollPatientsListPane = new JScrollPane(this.patientsListPanel);
        this.scrollPatientsListPane.setPreferredSize(new Dimension(260, Integer.MAX_VALUE));
        this.scrollPatientsListPane.setMinimumSize(new Dimension(260, 0));
        this.scrollPatientsListPane.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        // Main content panel
        this.mainContentPanel = new JPanel(new BorderLayout());
        this.mainContentPanel.setBackground(Color.WHITE);
        this.mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome to the Monitoring Interface", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        this.mainContentPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Container for patient panels
        this.panelsContainer = new JPanel(new GridBagLayout());
        this.panelsContainer.setBackground(Color.LIGHT_GRAY);

        this.scrollPanelsPane = new JScrollPane(this.panelsContainer);
        this.scrollPanelsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPanelsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.mainContentPanel.add(this.scrollPanelsPane, BorderLayout.CENTER);

        // Assemble full home layout
        this.homePanel.add(this.scrollPatientsListPane, BorderLayout.WEST);
        this.homePanel.add(this.mainContentPanel, BorderLayout.CENTER);
    }

    //--------------------- Panel Management -----------------------//
    public void resizePanels() {
        this.gridBagConstraints = new GridBagConstraints();
        this.gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        this.gridBagConstraints.fill = GridBagConstraints.NONE;
        this.gridBagConstraints.weightx = 0.0;
        this.gridBagConstraints.weighty = 0.0;

        this.panelComponents = panelsContainer.getComponents();
        this.panelsContainer.removeAll();

        int columns = (int) Math.floor(this.scrollPanelsPane.getWidth() / 400);
        if (columns < 1) columns = 1;

        for (int i = 0; i < panelComponents.length; i++) {
            this.gridBagConstraints.gridx = i % columns;
            this.gridBagConstraints.gridy = i / columns;
            panelComponents[i].setPreferredSize(new Dimension(500, 500));
            this.panelsContainer.add(panelComponents[i], this.gridBagConstraints);
        }

        this.panelsContainer.revalidate();
        this.panelsContainer.repaint();
    }

    //--------------------- Patient Panel Creation -----------------------//
    public JPanel createPatientPanel(String fullName, PatientMonitorThread patientThread) {
        this.patientPanel = new JPanel(new BorderLayout(10, 10));
        this.patientPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        this.patientPanel.setBackground(new Color(240, 240, 245));
        this.patientPanel.setPreferredSize(new Dimension(450, 450));

        //--------------------- Header -----------------------//
        this.headerPanel = new JPanel(new BorderLayout());
        this.headerPanel.setBackground(new Color(52, 73, 94));
        this.headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        this.nameLabel = new JLabel(fullName);
        this.nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        this.nameLabel.setForeground(Color.WHITE);
        this.headerPanel.add(this.nameLabel, BorderLayout.WEST);

        this.lastUpdateLabel = new JLabel("Last update: " + getCurrentTimestamp());
        this.lastUpdateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        this.lastUpdateLabel.setForeground(new Color(200, 200, 200));
        this.headerPanel.add(this.lastUpdateLabel, BorderLayout.EAST);

        //--------------------- Content -----------------------//
        if (this.wsClient != null) {
            this.wsClient.sendAgeRequest(fullName);
            new Tempo(10);
        }
        this.age = wsClient.getCalculatedAge();

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        contentPanel.setOpaque(false);

        this.graphPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        this.graphPanel.setOpaque(false);

        this.oxygenChartPanel = patientThread.getGetChartMonThreadJFreeChartOxy();
        this.pulseChartPanel = patientThread.getGetChartMonThreadJFreeChartPulse();
        this.graphPanel.add(this.oxygenChartPanel);
        this.graphPanel.add(this.pulseChartPanel);

        this.infoPanel = new JPanel();
        this.infoPanel.setLayout(new BoxLayout(this.infoPanel, BoxLayout.Y_AXIS));
        this.infoPanel.setOpaque(false);
        this.infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));

        this.infoPanel.add(createInfoField("Age", this.age + " years"));
        this.infoPanel.add(createInfoField("Blood Group", "..."));
        this.infoPanel.add(createInfoField("Weight", "... kg"));
        this.infoPanel.add(createInfoField("Height", "... cm"));

        this.alertPanel = new JPanel(new BorderLayout(5, 0));
        this.alertPanel.setOpaque(false);
        this.alertPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        this.statusLabel = new JLabel("Status: Normal");
        this.statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        this.statusLabel.setForeground(new Color(39, 174, 96));

        this.statusIndicator = new JPanel();
        this.statusIndicator.setBackground(new Color(39, 174, 96));
        this.statusIndicator.setPreferredSize(new Dimension(15, 15));
        this.statusIndicator.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        this.alertPanel.add(this.statusIndicator, BorderLayout.WEST);
        this.alertPanel.add(this.statusLabel, BorderLayout.CENTER);
        this.infoPanel.add(this.alertPanel);

        this.actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.actionPanel.setOpaque(false);
        this.actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        this.detailsButton = new JButton("Full Details");
        this.detailsButton.setBackground(new Color(52, 73, 94));
        this.detailsButton.setForeground(Color.WHITE);
        this.detailsButton.setFocusPainted(false);
        this.actionPanel.add(this.detailsButton);

        this.infoPanel.add(this.actionPanel);

        contentPanel.add(this.graphPanel);
        contentPanel.add(this.infoPanel);

        this.patientPanel.add(this.headerPanel, BorderLayout.NORTH);
        this.patientPanel.add(contentPanel, BorderLayout.CENTER);

        return patientPanel;
    }

    //--------------------- Utility Methods -----------------------//
    private static JPanel createInfoField(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(labelComponent, BorderLayout.WEST);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(valueComponent, BorderLayout.CENTER);

        return panel;
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date());
    }

    //--------------------- Getter and Setter Management -----------------------//
    public JPanel getHomePanel() {
        return this.homePanel;
    }

    public JPanel getmainContentPanel() {
        return this.mainContentPanel;
    }

    public JPanel getPanelsContainer() {
        return this.panelsContainer;
    }

    public JPanel getPatientsListPanel() {
        return this.patientsListPanel;
    }

    public void setPatientsListPanel(JPanel patientsListPanel) {
        this.patientsListPanel = patientsListPanel;
    }

    public void setPanelsContainer(JPanel panelsContainer) {
        this.panelsContainer = panelsContainer;
    }
}
