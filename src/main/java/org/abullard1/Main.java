package main.java.org.abullard1;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Main class providing a Swing-based GUI for Arithmetic Encoding/Decoding,
 * The Swing-UI includes the following notable features:
 *  - A top toolbar for quick actions/information.
 *  - A JTabbedPane for "Encode" and "Decode" sections.
 *  - JSplitPane in each tab for resizable input vs. output sections.
 */
public class Main extends JFrame {
    // Tabbed Pane for Encode/Decode
    private JTabbedPane tabbedPane;

    // Encode panel components
    private JTextArea inputTextArea;
    private JTextField encodePrecisionField;
    private JTextArea numericalValueTextArea;
    private JTextArea probabilityTableTextArea;
    private JButton encodeButton;

    // Decode panel components
    private JTextArea numericalValueField;
    private JTextArea probabilityTableField;
    private JTextField stopWordField;
    private JTextArea decodedMessageTextArea;
    private JButton decodeButton;

    public Main() {
        initUI();
    }

    /**
     * Initializes and lays out the user interface.
     */
    private void initUI() {
        // Properties
        setTitle(ConfigLoader.getProperty("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource(ConfigLoader.getProperty("app.icon.path")))).getImage());

        // Top Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(createToolbarButton("About", e -> showAboutDialog()));
        toolBar.add(createToolbarButton("Exit", e -> System.exit(0)));

        // Tabbed Pane for Encode/Decode
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font(ConfigLoader.getProperty("font.family"), Font.PLAIN, Integer.parseInt(ConfigLoader.getProperty("tabs.font.size"))));

        JPanel encodeTab = buildEncodeTab();
        JPanel decodeTab = buildDecodeTab();

        tabbedPane.addTab("Encode", encodeTab);
        tabbedPane.addTab("Decode", decodeTab);

        // Layout and display
        setLayout(new BorderLayout(10, 10));
        add(toolBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        pack();
        setSize(new Dimension(Integer.parseInt(ConfigLoader.getProperty("window.width")), Integer.parseInt(ConfigLoader.getProperty("window.height"))));
        setVisible(true);
    }

    /**
     * Builds the "Encode" tab with a JSplitPane: input on top, output on bottom.
     */
    private JPanel buildEncodeTab() {
        // Panel for top area (input + precision)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Input text
        inputTextArea = createTextArea(6, 50, true);
        inputPanel.add(createLabeledComponent("Input Text:", new JScrollPane(inputTextArea)));

        // Precision field
        encodePrecisionField = new JTextField(ConfigLoader.getProperty("default.precision"), 10);
        inputPanel.add(createLabeledComponent("Precision (Encode):", encodePrecisionField));

        // Encode button
        encodeButton = new JButton("Encode");
        encodeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        encodeButton.addActionListener(e -> encode());

        // Small panel for the encode button (left-aligned)
        JPanel encodeBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        encodeBtnPanel.add(encodeButton);
        encodeBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(encodeBtnPanel);

        // Panel for bottom area (results: numericalValue & probability table)
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Numerical Value TextArea
        numericalValueTextArea = createTextArea(2, 50, false);
        outputPanel.add(createLabeledComponent("Encoded Numerical Value:", new JScrollPane(numericalValueTextArea)));
        outputPanel.add(wrapButton(createCopyButton(numericalValueTextArea)));

        // Probability Table TextArea
        probabilityTableTextArea = createTextArea(5, 50, false);
        outputPanel.add(createLabeledComponent("Probability Table (auto-generated):", new JScrollPane(probabilityTableTextArea)));
        outputPanel.add(wrapButton(createCopyButton(probabilityTableTextArea)));

        // Combines top and bottom with JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.4); // 40% top, 60% bottom

        // Wraps the split pane in a panel
        JPanel encodeTab = new JPanel(new BorderLayout());
        encodeTab.add(splitPane, BorderLayout.CENTER);

        return encodeTab;
    }

    /**
     * Builds the "Decode" tab with a JSplitPane: input on top, output on bottom.
     */
    private JPanel buildDecodeTab() {
        // Top area: numeric value, probability table, optional stopWord
        JPanel decodeInputPanel = new JPanel();
        decodeInputPanel.setLayout(new BoxLayout(decodeInputPanel, BoxLayout.Y_AXIS));
        decodeInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Numeric Value
        numericalValueField = createTextArea(3, 50, true);
        decodeInputPanel.add(createLabeledComponent("Numerical Value:", new JScrollPane(numericalValueField)));

        // Probability Table
        probabilityTableField = createTextArea(5, 50, true);
        decodeInputPanel.add(createLabeledComponent("Probability Table:", new JScrollPane(probabilityTableField)));

        // Stop Word
        stopWordField = new JTextField(20);
        decodeInputPanel.add(createLabeledComponent("Stop Word (optional):", stopWordField));

        // Decode button
        decodeButton = new JButton("Decode");
        decodeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        decodeButton.addActionListener(e -> decode());
        JPanel decodeBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        decodeBtnPanel.add(decodeButton);
        decodeBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        decodeInputPanel.add(decodeBtnPanel);

        // Bottom area: Decoded message
        JPanel decodeOutputPanel = new JPanel();
        decodeOutputPanel.setLayout(new BoxLayout(decodeOutputPanel, BoxLayout.Y_AXIS));
        decodeOutputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        decodedMessageTextArea = createTextArea(6, 50, false);
        decodedMessageTextArea.setCaret(new DefaultCaret() {
            @Override
            public boolean isVisible() {
                return false;
            }
        });
        decodeOutputPanel.add(createLabeledComponent("Decoded Message:", new JScrollPane(decodedMessageTextArea)));
        decodeOutputPanel.add(wrapButton(createCopyButton(decodedMessageTextArea)));

        // Combines the top and bottom with JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, decodeInputPanel, decodeOutputPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5); // half & half

        // Wraps the split pane in a panel
        JPanel decodeTab = new JPanel(new BorderLayout());
        decodeTab.add(splitPane, BorderLayout.CENTER);

        return decodeTab;
    }

    /**
     * Encodes the user-entered text. If a space is present, it will be displayed as "[space]".
     */
    private void encode() {
        String inputText = inputTextArea.getText().trim();  // Trims leading/trailing whitespace
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input text cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parses the user’s desired encode precision
        int encodePrecision;
        try {
            encodePrecision = Integer.parseInt(encodePrecisionField.getText().trim());
            if (encodePrecision < 1) {
                JOptionPane.showMessageDialog(this, "Precision must be at least 1", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid precision format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Performs the Arithmetic Encoding Operation with the specified precision
        BigDecimal encodedValue = ArithmeticEncoder.encodeMessage(inputText, encodePrecision);

        // Displays the encoding result in the numericalValueTextArea
        numericalValueTextArea.setText(encodedValue.toPlainString());

        // Generates the probability table
        TreeMap<Character, Integer> charCounts = getCharacterCounts(inputText);
        TreeMap<Character, BigDecimal> computedProbs = getProbabilities(charCounts, inputText.length(), encodePrecision);

        // Builds a string for the table and displays it in the probabilityTableTextArea (Replacing ' ' with "[space]")
        StringBuilder sb = new StringBuilder();
        for (var entry : computedProbs.entrySet()) {
            char c = entry.getKey();
            String displayKey = (c == ' ') ? ConfigLoader.getProperty("space.string.replacement.token") : String.valueOf(c);

            sb.append(displayKey).append("=")
                    .append(entry.getValue().setScale(encodePrecision, RoundingMode.HALF_UP))
                    .append("\n");
        }

        probabilityTableTextArea.setText(sb.toString().trim());
    }

    /**
     * Decodes the numeric value using the user-provided probability table,
     * converting "[space]" back to ' ' as needed,
     * plus an optional stop word.
     */
    private void decode() {
        String numericalValue = numericalValueField.getText().trim();
        String probabilityTable = probabilityTableField.getText().trim();
        String stopWord = stopWordField.getText().trim();

        if (numericalValue.isEmpty() || probabilityTable.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Numerical value and probability table cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal decodedNumber;
        try {
            decodedNumber = new BigDecimal(numericalValue);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid numerical value format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Automatic precision from numeric scale + buffer
        int autoPrecision = decodedNumber.scale() + Integer.parseInt(ConfigLoader.getProperty("auto.precision.buffer.size"));

        // Parses the probability table text into a TreeMap
        TreeMap<Character, BigDecimal> probs;
        try {
            probs = parseProbabilityTable(probabilityTable);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid probability table format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Decoding the message and displaying it in the decodedMessageTextArea
        String result = ArithmeticDecoder.decodeMessage(decodedNumber, probs, stopWord, autoPrecision);
        decodedMessageTextArea.setText(result);
    }

    /**
     * Simple "About" dialog example in the top toolbar.
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Arithmetic Encoder/Decoder\nVersion 1.0\nUsing FlatLaf Dark Theme\nGitHub: @abullard1",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Helper to create a toolbar button with consistent styling.
     */
    private JButton createToolbarButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.addActionListener(action);
        return btn;
    }

    /**
     * Helper to create a labeled component with a border layout.
     */
    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(ConfigLoader.getProperty("font.family"), Font.PLAIN, Integer.parseInt(ConfigLoader.getProperty("components.font.size"))));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(5,0,5,0));
        return panel;
    }

    /**
     * Creates a JTextArea with the specified rows, columns, and editability.
     */
    private JTextArea createTextArea(int rows, int columns, boolean editable) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(editable);
        textArea.setFont(new Font(ConfigLoader.getProperty("font.family"), Font.PLAIN, Integer.parseInt(ConfigLoader.getProperty("components.font.size"))));
        return textArea;
    }

    /**
     * Creates a copy-to-clipboard JButton for a given JTextComponent.
     */
    private JButton createCopyButton(JTextComponent component) {
        ImageIcon copyIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(ConfigLoader.getProperty("copy.icon.path"))));
        Image img = copyIcon.getImage();
        Image scaledImg = img.getScaledInstance(Integer.parseInt(ConfigLoader.getProperty("copy.icon.size")), Integer.parseInt(ConfigLoader.getProperty("copy.icon.size")), Image.SCALE_SMOOTH);
        copyIcon = new ImageIcon(scaledImg);

        JButton button = new JButton(copyIcon);
        Border padding = new EmptyBorder(5, 5, 5, 5);
        button.setBorder(new CompoundBorder(button.getBorder(), padding));
        button.addActionListener(e -> copyToClipboard(component.getText()));
        return button;
    }

    /**
     * Wraps a button in a FlowLayout-centered panel.
     */
    private JPanel wrapButton(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(button);
        panel.setBorder(new EmptyBorder(5,0,5,0));
        return panel;
    }

    /**
     * Copies the given text to the system clipboard.
     */
    private void copyToClipboard(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * Parses the user-provided probability table.
     * If line is "[space]=0.2", parses as (char=' ', prob=0.2).
     */
    private TreeMap<Character, BigDecimal> parseProbabilityTable(String probabilityTable) {
        TreeMap<Character, BigDecimal> probs = new TreeMap<>();
        String[] lines = probabilityTable.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] tokens = line.split("=");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid probability line format: " + line);
            }

            String leftSide = tokens[0].trim();
            BigDecimal p = new BigDecimal(tokens[1].trim());

            // Converts "[space]" back to ' '
            char c = ' ';
            if (!ConfigLoader.getProperty("space.string.replacement.token").equals(leftSide)) {
                // Catches invalid single-char input if not "[space]"
                if (leftSide.length() != 1) {
                    throw new IllegalArgumentException(
                            "Expected single char or " + ConfigLoader.getProperty("space.string.replacement.token") + ": " + leftSide
                    );
                }
                c = leftSide.charAt(0);
            }
            // Adds the character and probability to the final parsed probability treemap
            probs.put(c, p);
        }
        return probs;
    }

    /**
     * Helper to compute character counts from a string.
     */
    private TreeMap<Character, Integer> getCharacterCounts(String message) {
        TreeMap<Character, Integer> charCounts = new TreeMap<>();
        for (char c : message.toCharArray()) {
            charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
        }
        return charCounts;
    }

    /**
     * Helper to compute probabilities from character counts, using a given precision scale.
     */
    private TreeMap<Character, BigDecimal> getProbabilities(TreeMap<Character, Integer> charCounts, int totalLength, int precision) {
        TreeMap<Character, BigDecimal> probabilities = new TreeMap<>();
        BigDecimal total = BigDecimal.valueOf(totalLength);
        for (var entry : charCounts.entrySet()) {
            BigDecimal prob = BigDecimal.valueOf(entry.getValue())
                    .divide(total, precision, RoundingMode.HALF_UP);
            probabilities.put(entry.getKey(), prob);
        }
        return probabilities;
    }

    /**
     * Main entry point. Initializes the FlatDarkLaf UI and runs the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatDarkLaf Theme");
        }
        SwingUtilities.invokeLater(Main::new);
    }
}