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

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Objects;
import java.util.Observer;
import java.util.stream.Stream;

public class GeoPanel extends JPanel {
    public static final int DEFAULT_MAP_WIDTH = 500;
    public static final int DEFAULT_MAP_HEIGHT = 250;

    private final JRadioButton geoFromGoogle;
    private final JRadioButton geoFromLatLong;
    private final JRadioButton geoFromMap;
    private final JFormattedTextField latLonTF, latTF, lonTF;
    private final JXMapViewer mapUI;

    private final PropertyChangeSupport observable = new PropertyChangeSupport(this);

    public void fireUpdate(GeoPosition newCentre) {
        observable.firePropertyChange("centre", null, newCentre);
    }

    public GeoPanel(final double defaultLatitude, final double defaultLongitude) {

        this.setLayout(new GridBagLayout());

        // Row 1: Paste from Google, i.e., lat,lon
        geoFromGoogle = new JRadioButton();
        geoFromGoogle.setToolTipText("Paste coordinates from, e.g., Google, as latitude, longitude");
        geoFromGoogle.setSelected(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(geoFromGoogle, gbc);

        latLonTF = new JFormattedTextField(new LatLongPairFormat());
        latLonTF.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        latLonTF.setToolTipText("Paste coordinates from, e.g., Google, as latitude, longitude");
        latLonTF.setText(defaultLatitude + "," + defaultLongitude);
        latLonTF.setEditable(true);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(latLonTF, gbc);

        // Row 2: Specific lat/long fields
        geoFromLatLong = new JRadioButton();

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(geoFromLatLong, gbc);

        final JLabel latLabel = new JLabel("Latitude: ");

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 5, 5);
        this.add(latLabel, gbc);

        latTF = new JFormattedTextField(new ConstrainedDoubleFormat(-90, 90));
        latTF.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        latTF.setText(Double.toString(defaultLatitude));
        latLabel.setLabelFor(latTF);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(latTF, gbc);

        final JLabel lonLabel = new JLabel("Longitude: ");

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 10, 5, 5);
        this.add(lonLabel, gbc);

        lonTF = new JFormattedTextField(new ConstrainedDoubleFormat(-180, 180));
        lonTF.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        lonTF.setText(Double.toString(defaultLongitude));
        latLabel.setLabelFor(lonTF);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(lonTF, gbc);

        // Row 3: map gui
        geoFromMap = new JRadioButton();
        geoFromMap.setToolTipText("Use left mouse button to pan, mouse wheel to zoom and right mouse to select");

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(geoFromMap, gbc);

        mapUI = createMapUI(defaultLatitude, defaultLongitude);
        mapUI.setBorder(latLonTF.getBorder());

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(mapUI, gbc);


        // BEHAVIOUR
        final ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(geoFromGoogle);
        radioGroup.add(geoFromLatLong);
        radioGroup.add(geoFromMap);

        latLonTF.addMouseListener(new Clicker(() -> radioGroup.setSelected(geoFromGoogle.getModel(), true)));
        latTF.addMouseListener(new Clicker(() -> radioGroup.setSelected(geoFromLatLong.getModel(), true)));
        lonTF.addMouseListener(new Clicker(() -> radioGroup.setSelected(geoFromLatLong.getModel(), true)));
        mapUI.addMouseListener(new Clicker(() -> radioGroup.setSelected(geoFromMap.getModel(), true)));

        latLonTF.addMouseListener(new SelectAllText(latLonTF));
        latTF.addMouseListener(new SelectAllText(latTF));
        lonTF.addMouseListener(new SelectAllText(lonTF));

        // coordinate changes between the fields
        // latLonTF updates latTF, lonTF and mapUI
        addChangeListener(latLonTF, e -> {
            final String[] parts = latLonTF.getText().split(",");
            if (parts.length > 1) {
                boolean changed = false;
                if (! latTF.getText().equals(parts[0])) {
                    latTF.setText(parts[0]);
                    changed = true;
                }
                if (! lonTF.getText().equals(parts[1])) {
                    lonTF.setText(parts[1]);
                    changed = true;
                }
                if (changed) {
                    final double lat = Double.parseDouble(parts[0]);
                    final double lon = Double.parseDouble(parts[1]);
                    mapUI.setCenterPosition(new GeoPosition(lat, lon));
                }
            }
        });
        // latTF updates latLonTF and mapUI
        addChangeListener(latTF, e -> {
            final String lonStr = latLonTF.getText().substring(latLonTF.getText().indexOf(',') + 1);
            final String newLatLon = latTF.getText() + "," + lonStr;
            if (! latLonTF.getText().equals(newLatLon)) {
                latLonTF.setText(newLatLon);
                final double lat = Double.parseDouble(latTF.getText());
                final GeoPosition centre = mapUI.getCenterPosition();
                mapUI.setCenterPosition(new GeoPosition(lat, centre.getLongitude()));
                fireUpdate(mapUI.getCenterPosition());
            }
        });
        // lonTF updates latLonTF and mapUI
        addChangeListener(lonTF, e -> {
            final String latStr = latLonTF.getText().substring(0, latLonTF.getText().indexOf(','));
            final String newValue = latStr + "," + lonTF.getText();
            if (! latLonTF.getText().equals(newValue)) {
                latLonTF.setText(newValue);
                final double lon = Double.parseDouble(lonTF.getText());
                final GeoPosition centre = mapUI.getCenterPosition();
                mapUI.setCenterPosition(new GeoPosition(centre.getLatitude(), lon));
                fireUpdate(mapUI.getCenterPosition());
            }
        });
        // mapUI updates only latLonTF and relies on other handlers above to update latTF and lonTF
        mapUI.addPropertyChangeListener("centerPosition", evt -> {
            final GeoPosition centre = mapUI.getCenterPosition();
            final double lat = centre.getLatitude();
            final double lon = centre.getLongitude();
            if (latLonTF.getText().indexOf(',') == -1) {
                latLonTF.setText(lat + "," + lon);
                fireUpdate(mapUI.getCenterPosition());
            } else {
                final String[] parts = latLonTF.getText().split(",");
                if (Math.abs(Double.parseDouble(parts[0]) - lat) > 0.00001 ||
                    Math.abs(Double.parseDouble(parts[1]) - lon) > 0.00001) {
                    latLonTF.setText(lat + "," + lon);
                    fireUpdate(mapUI.getCenterPosition());
                }
            }
        });
    }

    /**
     * Installs a listener to receive notification when the text of any
     * {@code JTextComponent} is changed. Internally, it installs a
     * {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect
     * if the {@code Document} itself is replaced.
     *
     * @param text any text component, such as a {@link JTextField}
     *        or {@link JTextArea}
     * @param changeListener a listener to receieve {@link ChangeEvent}s
     *        when the text is changed; the source object for the events
     *        will be the text component
     * @throws NullPointerException if either parameter is null
     * @see <a href="https://stackoverflow.com/a/27190162">java - Value Change Listener to JTextField - Stack Overflow</a>
     */
    public static void addChangeListener(final JTextComponent text, final ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document) e.getOldValue();
            Document d2 = (Document) e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }


    private JXMapViewer createMapUI(final double latitude, final double longitude) {
        final JXMapViewer mapViewer = new JXMapViewer();

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8);

        final GeoPosition initialLocation = new GeoPosition(latitude, longitude);

        // Add interactions
        final MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Add a selection painter
        final SelectionAdapter sa = new SelectionAdapter(mapViewer);
        final SelectionPainter sp = new SelectionPainter(sa);
        mapViewer.addMouseListener(sa);
        mapViewer.addMouseMotionListener(sa);
        mapViewer.setOverlayPainter(sp);

        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(initialLocation);
        mapViewer.setPreferredSize(new Dimension(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT));

        return mapViewer;
    }

    public void setCentre(double latitude, double longitude) {
        latLonTF.setText(latitude + "," + longitude); // this should update the other fields
    }

    class Clicker extends MouseAdapter {
        private final Runnable runnable;

        public Clicker(Runnable runnable) {
            this.runnable = runnable;
        }

        // We need to use mouseReleased, rather than mouseClicked, because
        // if you drag within the map view before 'clicking' in it, it
        // doesn't register as a click
        @Override
        public void mouseReleased(MouseEvent e) {
            runnable.run();
        }
    }

    class SelectAllText extends MouseAdapter {
        private final JTextField tf;

        public SelectAllText(JTextField tf) {
            this.tf = tf;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            tf.setSelectionStart(0);
            tf.setSelectionEnd(tf.getText().length());
        }
    }

    public double[] getLatLon() {
        if (geoFromGoogle.isSelected()) {
            return Stream.of(latLonTF.getText().split(","))
                .mapToDouble(s -> Double.parseDouble(s))
                .toArray();
        } else if (geoFromLatLong.isSelected()) {
            return new double[]{
                Double.parseDouble(latTF.getText()),
                Double.parseDouble(lonTF.getText()),
            };
        } else if (geoFromMap.isSelected()) {
            return new double[]{
                mapUI.getCenterPosition().getLatitude(),
                mapUI.getCenterPosition().getLongitude()
            };

        }
        return new double[]{-1.0,-1.0};
    }

    public void addObserver(PropertyChangeListener l) {
        observable.addPropertyChangeListener(l);
    }
}
