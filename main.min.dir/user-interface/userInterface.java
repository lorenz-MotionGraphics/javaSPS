import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.util.List;

class Appliance {
    String name;
    double wattage;
    double hoursPerDay;

    public Appliance(String name, double wattage, double hoursPerDay) {
        this.name = name;
        this.wattage = wattage;
        this.hoursPerDay = hoursPerDay;
    }

    public double getDailyConsumption() {
        return wattage * hoursPerDay;
    }
}

class JsonStorage {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<Appliance> loadAppliances() {
        try (FileReader reader = new FileReader("appliances.json")) {
            return gson.fromJson(reader, new TypeToken<List<Appliance>>() {}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveAppliances(List<Appliance> appliances) {
        try (FileWriter writer = new FileWriter("appliances.json")) {
            gson.toJson(appliances, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving: " + e.getMessage());
        }
    }

    public static void saveConfig(Map<String, Double> config) {
        try (FileWriter writer = new FileWriter("calcConfig.json")) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving config: " + e.getMessage());
        }
    }

    public static Map<String, Double> loadConfig() {
        try (FileReader reader = new FileReader("calcConfig.json")) {
            return gson.fromJson(reader, new TypeToken<Map<String, Double>>() {}.getType());
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
}

class SolarCalculator {
    List<Appliance> appliances;
    double sunHours, systemVoltage, panelWatt, daysOfAutonomy, dod, inverterEfficiency;

    public SolarCalculator(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public double getTotalDailyEnergy() {
        return appliances.stream().mapToDouble(Appliance::getDailyConsumption).sum();
    }

    public void compute(double sunHours, double systemVoltage, double panelWatt,
                        double daysOfAutonomy, double dod, double inverterEfficiency) {
        this.sunHours = sunHours;
        this.systemVoltage = systemVoltage;
        this.panelWatt = panelWatt;
        this.daysOfAutonomy = daysOfAutonomy;
        this.dod = dod;
        this.inverterEfficiency = inverterEfficiency;
    }

    public String getReport() {
        double totalDailyEnergy = getTotalDailyEnergy();
        double totalSolarPower = totalDailyEnergy / sunHours;
        int numberOfPanels = (int) Math.ceil(totalSolarPower / panelWatt);
        double batteryCapacityWh = (totalDailyEnergy * daysOfAutonomy) / (dod * inverterEfficiency);
        double batteryCapacityAh = batteryCapacityWh / systemVoltage;
        double inverterSize = totalDailyEnergy / inverterEfficiency;
        double chargeControllerCurrent = (panelWatt * numberOfPanels) / systemVoltage * 1.25;

        return String.format("""
                ══════════════════════════════════════════════
                        SOLAR POWER SYSTEM REPORT
                ══════════════════════════════════════════════
                
                Total Daily Load:              %.2f Wh
                Total Solar Power Needed:      %.2f W
                Number of Panels Required:     %d panels
                Battery Capacity Required:     %.2f Ah
                System Voltage:                %.2f V
                Inverter Size Recommended:     %.2f W
                Charge Controller Current:     %.2f A
                
                ══════════════════════════════════════════════
                NOTE: Add 10-20%% safety margin to all values
                ══════════════════════════════════════════════
                """,
                totalDailyEnergy, totalSolarPower, numberOfPanels,
                batteryCapacityAh, systemVoltage, inverterSize, chargeControllerCurrent);
    }

    public void exportReport(String format, String report) {
        try {
            if (format.equalsIgnoreCase("csv")) {
                Files.writeString(Path.of("solar_report.csv"),
                        report.replace(":", ",").replace("=", ""));
                JOptionPane.showMessageDialog(null, "Report exported to solar_report.csv");
            } else {
                Files.writeString(Path.of("solar_report.txt"), report);
                JOptionPane.showMessageDialog(null, "Report exported to solar_report.txt");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Export failed: " + e.getMessage());
        }
    }
}

public class userInterFace extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JTextArea reportArea;
    private JTextField txtName, txtWatt, txtHours;
    private JTextField txtSunHours, txtVoltage, txtPanel, txtDays, txtDod, txtInvEff;
    private List<Appliance> appliances = new ArrayList<>();
    private SolarCalculator calc;
    private final Color PRIMARY_COLOR = new Color(255, 152, 0);  // Orange
    private final Color SECONDARY_COLOR = new Color(33, 150, 243);  // Blue
    private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_COLOR = new Color(33, 33, 33);

    public userInterFace() {
        setTitle("Solar Power System Calculator");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);
        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainContainer, BorderLayout.CENTER);
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(750);
        centerSplit.setBackground(BACKGROUND_COLOR);
        centerSplit.setBorder(null);
        centerSplit.setLeftComponent(createAppliancesPanel());
        centerSplit.setRightComponent(createReportPanel());
        mainContainer.add(centerSplit, BorderLayout.CENTER);
        mainContainer.add(createParametersPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("Solar Power System Calculator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Design your off-grid solar power system");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        return headerPanel;
    }

    private JPanel createAppliancesPanel() {
        JPanel appliancesPanel = new JPanel(new BorderLayout(10, 10));
        appliancesPanel.setBackground(PANEL_BG);
        appliancesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Appliances & Load");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        appliancesPanel.add(titleLabel, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Appliance Name", "Wattage (W)", "Hours/Day", "Daily Energy (Wh)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.setSelectionBackground(new Color(33, 150, 243, 50));
        table.setGridColor(new Color(230, 230, 230));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        appliancesPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(PANEL_BG);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        txtName = createStyledTextField();
        txtWatt = createStyledTextField();
        txtHours = createStyledTextField();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        inputPanel.add(createLabel("Appliance Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        inputPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        inputPanel.add(createLabel("Wattage (W):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        inputPanel.add(txtWatt, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.3;
        inputPanel.add(createLabel("Hours/Day:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.35;
        inputPanel.add(txtHours, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        
        JButton btnAdd = createStyledButton("+ Add Appliance", SECONDARY_COLOR);
        JButton btnRemove = createStyledButton("Remove Selected", new Color(244, 67, 54));
        JButton btnLoad = createStyledButton("Load from JSON", new Color(76, 175, 80));
        JButton btnSave = createStyledButton("Save to JSON", new Color(76, 175, 80));
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(btnLoad);
        buttonPanel.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        inputPanel.add(buttonPanel, gbc);

        appliancesPanel.add(inputPanel, BorderLayout.SOUTH);
        btnAdd.addActionListener(e -> addAppliance());
        btnRemove.addActionListener(e -> removeAppliance());
        btnLoad.addActionListener(e -> loadAppliances());
        btnSave.addActionListener(e -> saveAppliances());

        return appliancesPanel;
    }

    private JPanel createReportPanel() {
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));
        reportPanel.setBackground(PANEL_BG);
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("System Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        reportPanel.add(titleLabel, BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reportArea.setBackground(new Color(248, 249, 250));
        reportArea.setForeground(TEXT_COLOR);
        reportArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reportArea.setText("\n\n\n\n          Configure parameters and click\n          'Calculate System' to generate report");
        
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        reportPanel.add(reportScroll, BorderLayout.CENTER);

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        exportPanel.setBackground(PANEL_BG);
        
        JButton btnExportTXT = createStyledButton("Export TXT", new Color(96, 125, 139));
        JButton btnExportCSV = createStyledButton("Export CSV", new Color(96, 125, 139));
        
        exportPanel.add(btnExportTXT);
        exportPanel.add(btnExportCSV);
        
        reportPanel.add(exportPanel, BorderLayout.SOUTH);

        btnExportTXT.addActionListener(e -> exportReport("txt"));
        btnExportCSV.addActionListener(e -> exportReport("csv"));

        return reportPanel;
    }

    private JPanel createParametersPanel() {
        JPanel paramsPanel = new JPanel(new BorderLayout(10, 10));
        paramsPanel.setBackground(PANEL_BG);
        paramsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("System Parameters");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        paramsPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 6, 15, 10));
        gridPanel.setBackground(PANEL_BG);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        txtSunHours = createStyledTextField();
        txtVoltage = createStyledTextField();
        txtPanel = createStyledTextField();
        txtDays = createStyledTextField();
        txtDod = createStyledTextField();
        txtInvEff = createStyledTextField();

        txtSunHours.setText("5.0");
        txtVoltage.setText("12");
        txtPanel.setText("100");
        txtDays.setText("2");
        txtDod.setText("0.8");
        txtInvEff.setText("0.9");

        gridPanel.add(createLabel("Sun Hours/Day:"));
        gridPanel.add(createLabel("System Voltage (V):"));
        gridPanel.add(createLabel("Panel Wattage (W):"));
        gridPanel.add(createLabel("Days of Autonomy:"));
        gridPanel.add(createLabel("Depth of Discharge:"));
        gridPanel.add(createLabel("Inverter Efficiency:"));
        
        gridPanel.add(txtSunHours);
        gridPanel.add(txtVoltage);
        gridPanel.add(txtPanel);
        gridPanel.add(txtDays);
        gridPanel.add(txtDod);
        gridPanel.add(txtInvEff);

        paramsPanel.add(gridPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBackground(PANEL_BG);

        JButton btnCalc = createStyledButton("Calculate System", new Color(255, 152, 0));
        btnCalc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCalc.setPreferredSize(new Dimension(200, 40));
        
        JButton btnSaveConfig = createStyledButton("Save Config", new Color(103, 58, 183));
        JButton btnLoadConfig = createStyledButton("Load Config", new Color(103, 58, 183));

        actionPanel.add(btnSaveConfig);
        actionPanel.add(btnLoadConfig);
        actionPanel.add(btnCalc);

        paramsPanel.add(actionPanel, BorderLayout.SOUTH);

        btnCalc.addActionListener(e -> calculate());
        btnSaveConfig.addActionListener(e -> saveConfig());
        btnLoadConfig.addActionListener(e -> loadConfig());

        return paramsPanel;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return textField;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void addAppliance() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter appliance name.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double watt = Double.parseDouble(txtWatt.getText());
            double hrs = Double.parseDouble(txtHours.getText());
            
            if (watt <= 0 || hrs <= 0) {
                JOptionPane.showMessageDialog(this, "Wattage and hours must be positive values.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Appliance a = new Appliance(name, watt, hrs);
            appliances.add(a);
            model.addRow(new Object[]{name, String.format("%.1f", watt), String.format("%.1f", hrs), String.format("%.1f", a.getDailyConsumption())});
            
            txtName.setText("");
            txtWatt.setText("");
            txtHours.setText("");
            txtName.requestFocus();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for wattage and hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeAppliance() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            appliances.remove(selectedRow);
            model.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an appliance to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadAppliances() {
        appliances = JsonStorage.loadAppliances();
        model.setRowCount(0);
        for (Appliance a : appliances)
            model.addRow(new Object[]{a.name, String.format("%.1f", a.wattage), String.format("%.1f", a.hoursPerDay), String.format("%.1f", a.getDailyConsumption())});
        JOptionPane.showMessageDialog(this, "Appliances loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveAppliances() {
        JsonStorage.saveAppliances(appliances);
        JOptionPane.showMessageDialog(this, "Appliances saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void calculate() {
        try {
            if (appliances.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add at least one appliance before calculating.", "No Appliances", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double sunHours = Double.parseDouble(txtSunHours.getText());
            double voltage = Double.parseDouble(txtVoltage.getText());
            double panel = Double.parseDouble(txtPanel.getText());
            double days = Double.parseDouble(txtDays.getText());
            double dod = Double.parseDouble(txtDod.getText());
            double invEff = Double.parseDouble(txtInvEff.getText());

            if (sunHours <= 0 || voltage <= 0 || panel <= 0 || days <= 0 || dod <= 0 || dod > 1 || invEff <= 0 || invEff > 1) {
                JOptionPane.showMessageDialog(this, "Please check parameter values:\n- All values must be positive\n- DoD and Efficiency must be between 0 and 1", "Invalid Parameters", JOptionPane.ERROR_MESSAGE);
                return;
            }

            calc = new SolarCalculator(appliances);
            calc.compute(sunHours, voltage, panel, days, dod, invEff);
            reportArea.setText(calc.getReport());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please fill all parameters with valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConfig() {
        try {
            Map<String, Double> cfg = new HashMap<>();
            cfg.put("sunHours", Double.parseDouble(txtSunHours.getText()));
            cfg.put("voltage", Double.parseDouble(txtVoltage.getText()));
            cfg.put("panel", Double.parseDouble(txtPanel.getText()));
            cfg.put("days", Double.parseDouble(txtDays.getText()));
            cfg.put("dod", Double.parseDouble(txtDod.getText()));
            cfg.put("invEff", Double.parseDouble(txtInvEff.getText()));
            JsonStorage.saveConfig(cfg);
            JOptionPane.showMessageDialog(this, "Configuration saved successfully to calcConfig.json", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid data in configuration fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadConfig() {
        Map<String, Double> cfg = JsonStorage.loadConfig();
        if (cfg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No configuration file found!", "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        txtSunHours.setText(String.valueOf(cfg.getOrDefault("sunHours", 5.0)));
        txtVoltage.setText(String.valueOf(cfg.getOrDefault("voltage", 12.0)));
        txtPanel.setText(String.valueOf(cfg.getOrDefault("panel", 100.0)));
        txtDays.setText(String.valueOf(cfg.getOrDefault("days", 2.0)));
        txtDod.setText(String.valueOf(cfg.getOrDefault("dod", 0.8)));
        txtInvEff.setText(String.valueOf(cfg.getOrDefault("invEff", 0.9)));
        JOptionPane.showMessageDialog(this, "Configuration loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportReport(String format) {
        if (calc == null || reportArea.getText().isEmpty() || reportArea.getText().contains("Configure parameters")) {
            JOptionPane.showMessageDialog(this, "Please calculate the system first before exporting!", "No Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        calc.exportReport(format, reportArea.getText());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new userInterFace().setVisible(true));
    }
}