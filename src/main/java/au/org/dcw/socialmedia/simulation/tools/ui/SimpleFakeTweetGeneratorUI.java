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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Font;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

public class SimpleFakeTweetGeneratorUI extends JPanel {

    class TweetModel {
        JsonNode root;

        Object get(final String path) {
            return getNested(root, path);
        }

        Object getNested(final JsonNode obj, final String path) {
            if (path.contains(".")) {
                final String head = path.substring(0, path.indexOf('.'));
                final String tail = path.substring(path.indexOf('.') + 1);
                if (obj.has(head)) {
                    return getNested(obj.get(head), tail);
                } else {
                    System.err.println("Could not find sub-path: " + tail);
                    return null; // error!
                }
            } else {
                return obj.get(path);
            }
        }

        public void set(String path, Object value) {
            setNested((ObjectNode) root, path, value);
        }

        void setNested(final ObjectNode obj, final String path, final Object value) {
            if (path.contains(".")) {
                final String head = path.substring(0, path.indexOf('.'));
                final String tail = path.substring(path.indexOf('.') + 1);
                if (obj.has(head)) {
                    setNested((ObjectNode) obj.get(head), tail, value);
                } else {
                    System.err.println("Could not find sub-path: " + tail);
                }
            } else {
                final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
                if (value == null) {
                    obj.set(path, jsonNodeFactory.nullNode());
                } else if (value instanceof JsonNode) {
                    obj.set(path, (JsonNode) value);
                } else if (value instanceof String) {
                    obj.set(path, jsonNodeFactory.textNode(value.toString()));
                } else if (value instanceof double[]) { //value.getClass().isArray()) {
                    final ArrayNode arrayNode = jsonNodeFactory.arrayNode();
                    double[] array = (double[]) value;
                    arrayNode.add(array[0]);
                    arrayNode.add(array[1]);
                    obj.set(path, arrayNode);
                }
            }
        }
    }

    private static final DateTimeFormatter TWITTER_TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZZ yyyy", Locale.ENGLISH);

    @Parameter(names = {"--skip-date"}, description = "Don't bother creating a 'created_at' field.")
    private boolean skipDate = false;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int ID_LENGTH = 16;
    private static final Random R = new Random();
    private static boolean help = false;

    private JTextField nameTF;
    private JTextArea textArea;
    private JCheckBox useGeoCheckbox;
    private GeoPanel geoPanel;
    private JTextArea jsonTextArea;

    private TweetModel model = new TweetModel();

    public static void main(String[] args) throws IOException {
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

    SimpleFakeTweetGeneratorUI() throws IOException {
        initModel();
    }

    private void initModel() throws IOException {
        final String initJSON = "{\"coordinates\":{\"coordinates\":[138.604,-34.918],\"type\":\"Point\"}," +
            "\"created_at\":\"Fri Nov 17 10:06:17 +1030 2017\",\"full_text\":\"\"," +
            "\"id\":1510875377905183,\"id_str\":\"1510875377905183\",\"text\":\"\"," +
            "\"user\":{\"screen_name\":\"\"}}";

        model.root = JSON.readValue(initJSON, JsonNode.class);
    }

    private void run() {
        // Create and set up the window
        JFrame frame = new JFrame("Tweet Editor");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        buildUI();
        frame.setContentPane(this);

        // Display the window
//        frame.pack();
        frame.setSize(700, 500);
        frame.setVisible(true);
    }

    private void buildUI() {

        // STRUCTURE
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // set up left and right panels
        final JPanel left = new JPanel(new GridBagLayout());
        final JPanel right = new JPanel(new BorderLayout());

        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(400);

        this.add(splitPane, BorderLayout.CENTER);

        // LEFT

        // Row 1: name
        int row = 0;
        final JLabel nameLabel = new JLabel("<html>Screen<br>Name:</html>");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 5, 5);
        left.add(nameLabel, gbc);

        nameTF = new JTextField(15);
        nameLabel.setLabelFor(nameTF);
        final Object screenName = model.get("user.screen_name");
        nameTF.setText(screenName != null ? screenName.toString() : "");

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        left.add(nameTF, gbc);

        // Row 2: text field
        row++;
        final JLabel textLabel = new JLabel("Text:");

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 5, 5);
        left.add(textLabel, gbc);

        textArea = new JTextArea(4, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        Object text = model.get("text");
        if (text == null || text.toString().length() == 0) {
            text = model.get("full_text");
        }
        textArea.setText(text != null ? text.toString() : "");

        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.25;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        left.add(scrollPane, gbc);

        // Row 3: use geo checkbox
        row++;
        useGeoCheckbox = new JCheckBox("Use geo?");
        useGeoCheckbox.setSelected(model.get("coordinates") != null);

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        left.add(useGeoCheckbox, gbc);

        // Row 4: geo panel
        row++;
        double[] latLon = lookupLatLon();
        geoPanel = new GeoPanel(latLon[0], latLon[1]);

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 5, 0);
        left.add(geoPanel, gbc);

        // Row 5: generate button
        row++;
        final JButton genButton = new JButton("Push JSON to global clipboard");

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        left.add(genButton, gbc);

        // RIGHT

        jsonTextArea = new JTextArea();
        jsonTextArea.setLineWrap(true);
        jsonTextArea.setWrapStyleWord(true);
        jsonTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));

        // random 'words'
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 250; i++) {
//            sb.append((char) (Math.floor(Math.random() * 26) + 'a'));
//            if (Math.random() > 0.9) sb.append(' ');
//        }
//        jsonTextArea.setText(sb.toString());
        updateTextArea();

        final JScrollPane jsonScrollPane = new JScrollPane(
            jsonTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        right.add(jsonScrollPane, BorderLayout.CENTER);


        // BEHAVIOUR
        useGeoCheckbox.addActionListener(e -> {
            recursivelySetEnabled(geoPanel, useGeoCheckbox.isSelected());
            if (! useGeoCheckbox.isSelected()) {
                model.set("geo", null);
                model.set("coordinates", null);
            } else {
                final double[] ll = geoPanel.getLatLon();
                model.set("geo", makeLatLonJsonNode(ll[0], ll[1]));
                model.set("coordinates", makeLatLonJsonNode(ll[1], ll[0]));
            }
            updateTextArea();
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

    private JsonNode makeLatLonJsonNode(double first, double second) {
        try {
            return JSON.readValue("{\"coordinates\":[" + first + "," + second + "],\"type\":\"Point\"}", JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonNodeFactory.instance.nullNode();
        }
    }

    private void updateTextArea() {
        SwingUtilities.invokeLater(() -> {
            try {
                jsonTextArea.setText(JSON.writeValueAsString(model.root));
            } catch (JsonProcessingException e) {
                System.err.println("Error generating JSON");
                e.printStackTrace();
            }
        });
    }

    private double[] lookupLatLon() {
        if (model.get("coordinates") == null) {
            // Set the focus (default: Barr Smith Lawns, University of Adelaide, Adelaide, South Australia)
            final double defaultLatitude =
                Double.parseDouble(System.getProperty("initial.latitude", "-34.918"));
            final double defaultLongitude =
                Double.parseDouble(System.getProperty("initial.longitude", "138.604"));

            return new double[]{ defaultLatitude, defaultLongitude };
        }

        final double lat = ((JsonNode) model.get("coordinates.coordinates")).get(1).asDouble();
        final double lon = ((JsonNode) model.get("coordinates.coordinates")).get(0).asDouble();

        return new double[]{ lat, lon };
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


    private void pushToClipboard(final String s) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(s), null);
    }

    private void recursivelySetEnabled(final JComponent component, final boolean enabled) {
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
}
