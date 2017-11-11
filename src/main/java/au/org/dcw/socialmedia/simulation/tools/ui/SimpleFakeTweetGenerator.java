package au.org.dcw.socialmedia.simulation.tools.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class SimpleFakeTweetGenerator extends JPanel {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int ID_LENGTH = 16;
    private static final Random R = new Random();

    private JTextField nameTF;
    private JTextArea textArea;
    private JCheckBox useGeoCheckbox;
    private GeoPanel geoPanel;

    public static void main(String[] args) {
        SimpleFakeTweetGenerator theApp = new SimpleFakeTweetGenerator();

        theApp.run();
    }

    public void run() {
        // Create and set up the window
        JFrame frame = new JFrame("Create JSON for fake tweet");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        buildUI();
        frame.setContentPane(this);

        // Display the window
//        frame.setSize(500, 700);
        frame.pack();
        frame.setVisible(true);
    }

    private void buildUI() {

        // STRUCTURE
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // Row 1: name
        int row = 1;
        final JLabel nameLabel = new JLabel("<html>Screen<br>Name:</html>");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 5, 5);
        this.add(nameLabel, gbc);

        nameTF = new JTextField(15);
        nameLabel.setLabelFor(nameTF);

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(nameTF, gbc);

        // Row 2: text field
        row = 2;
        final JLabel textLabel = new JLabel("Text:");

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 5, 5);
        this.add(textLabel, gbc);

        textArea = new JTextArea(4, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(nameTF.getBorder());

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        this.add(textArea, gbc);

        // Row 3: use geo checkbox
        row = 3;
        useGeoCheckbox = new JCheckBox("Use geo?");
        useGeoCheckbox.setSelected(true);

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(useGeoCheckbox, gbc);

        // Row 4: geo panel
        row = 4;
        geoPanel = new GeoPanel();

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(geoPanel, gbc);

        // Row 5: generate button
        row = 5;
        final JButton genButton = new JButton("Push JSON to global clipboard");

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(genButton, gbc);

        // BEHAVIOUR
        useGeoCheckbox.addActionListener(e -> {
            recursivelyEnable(geoPanel, useGeoCheckbox.isSelected());
        });
        genButton.addActionListener(e -> {
            final Map<String, Object> tweet = buildSimpleTweet();
            final String json = generateJSON(tweet);
            if (json != null) {
                pushToClipboard(json);
                System.out.println(json);
            }
        });

    }

    private String generateJSON(final Map<String, Object> tweet) {
        try {
            return JSON.writeValueAsString(tweet);
        } catch (JsonProcessingException e1) {
            JOptionPane.showMessageDialog(
                SimpleFakeTweetGenerator.this,
                "Error creating JSON:\n" + e1.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE
            );
            e1.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> buildSimpleTweet() {
        Map<String, Object> tweet = Maps.newTreeMap();
        String id = generateID().toString();
        tweet.put("id", id);
        tweet.put("id_str", id);
        tweet.put("text", textArea.getText());
        tweet.put("full_text", textArea.getText());
        Map<String, Object> user = Maps.newTreeMap();
        user.put("screen_name", nameTF.getText());
        tweet.put("user", user);
        if (useGeoCheckbox.isSelected()) {
            double[] latlon = geoPanel.getLatLon();
            Map<String, Object> coordinates = Maps.newTreeMap();
            coordinates.put("type", "Point");
            coordinates.put("coordinates", latlon);
            tweet.put("coordinates", coordinates);
        }
        return tweet;
    }

    /**
     * Creates a plausible tweet ID.
     *
     * @return A plausible tweet ID.
     */
    private static Long generateID() {
        StringBuilder idStr = new StringBuilder(Long.toString(System.currentTimeMillis()));
        while (idStr.length() < ID_LENGTH) {
            idStr.append(R.nextInt(10)); // 0-9
        }
        return Long.valueOf(idStr.toString());
    }


    private void pushToClipboard(String s) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(s), null);
    }

    private void recursivelyEnable(JComponent component, boolean enabled) {
        component.setEnabled(enabled);
        final int numChildren = component.getComponentCount();
        if (numChildren > 0) {
            IntStream.range(0, numChildren).forEach(i -> {
                if (component.getComponent(i) instanceof JComponent) {
                    recursivelyEnable((JComponent) component.getComponent(i), enabled);
                }
            });
        }
    }
}
