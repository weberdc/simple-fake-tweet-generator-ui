/*
 * Copyright 2017 Derek Weber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.org.dcw.socialmedia.simulation.tools.ui;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

public class SimpleFakeTweetGeneratorUI extends JPanel {

    public static final DateTimeFormatter TWITTER_TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZZ yyyy", Locale.ENGLISH);

    @Parameter(names = {"--skip-date"}, description = "Don't bother creating a 'created_at' field.")
    private static boolean skipDate = false;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int ID_LENGTH = 16;
    private static final Random R = new Random();
    private static boolean help = false;

    private JTextField nameTF;
    private JTextArea textArea;
    private JCheckBox useGeoCheckbox;
    private GeoPanel geoPanel;

    public static void main(String[] args) {
        SimpleFakeTweetGeneratorUI theApp = new SimpleFakeTweetGeneratorUI();

        // JCommander instance parses args, populates fields of theApp
        JCommander argsParser = JCommander.newBuilder()
            .addObject(theApp)
            .programName("bin/simple-fake-tweet-generator-ui[.bat]")
            .build();
        try {
            argsParser.parse(args);
        } catch (ParameterException e) {
            System.err.println("Unknown argument parameter:\n  " + e.getMessage());
            help = true;
        }

        if (help) {
            StringBuilder sb = new StringBuilder();
            argsParser.usage(sb);
            System.out.println(sb.toString());
            System.exit(-1);
        }

        loadProxyProperties();

        theApp.run();
    }

    /**
     * Loads proxy information from <code>"./proxy.properties"</code> if it is
     * present. If a proxy host and username are specified by no password, the
     * user is asked to type it in via stdin.
     *
     * @return A {@link Properties} map with proxy credentials.
     */
    private static Properties loadProxyProperties() {
        final Properties properties = new Properties();
        final String proxyFile = "./proxy.properties";
        if (new File(proxyFile).exists()) {
            boolean success = true;
            try (Reader fileReader = Files.newBufferedReader(Paths.get(proxyFile))) {
                properties.load(fileReader);
            } catch (IOException e) {
                System.err.println("Attempted and failed to load " + proxyFile + ": " + e.getMessage());
                success = false;
            }
            if (success && !properties.containsKey("http.proxyPassword")) {
                char[] password = System.console().readPassword("Please type in your proxy password: ");
                properties.setProperty("http.proxyPassword", new String(password));
                properties.setProperty("https.proxyPassword", new String(password));
            }
            properties.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
        }
        return properties;
    }

    private void run() {
        // Create and set up the window
        JFrame frame = new JFrame("Create JSON for fake tweet");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        buildUI();
        frame.setContentPane(this);

        // Display the window
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
        gbc.weightx = 1.0;
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

        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        gbc = new GridBagConstraints();
        gbc.gridy = row - 1;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);

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
        gbc.weightx = 1.0;
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
            recursivelySetEnabled(geoPanel, useGeoCheckbox.isSelected());
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
                SimpleFakeTweetGeneratorUI.this,
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
        if (! skipDate) {
            tweet.put("created_at", TWITTER_TIMESTAMP_FORMAT.format(ZonedDateTime.now()));
        }
        tweet.put("id", BigDecimal.valueOf(Double.parseDouble(id)));
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
            coordinates.put("coordinates", new double[]{latlon[1],latlon[0]}); // long,lat required
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

    private void recursivelySetEnabled(JComponent component, boolean enabled) {
        component.setEnabled(enabled);
        final int numChildren = component.getComponentCount();
        if (numChildren > 0) {
            IntStream.range(0, numChildren).forEach(i -> {
                if (component.getComponent(i) instanceof JComponent) {
                    recursivelySetEnabled((JComponent) component.getComponent(i), enabled);
                }
            });
        }
    }
}
