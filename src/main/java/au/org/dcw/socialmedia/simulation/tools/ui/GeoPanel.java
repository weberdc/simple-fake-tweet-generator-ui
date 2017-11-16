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
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.stream.Stream;

public class GeoPanel extends JPanel {
    private final JRadioButton geoFromGoogle;
    private final JRadioButton geoFromLatLong;
    private final JRadioButton geoFromMap;
    private final JFormattedTextField latLonTF, latTF, lonTF;
    private final JXMapViewer mapUI;

    public GeoPanel() {
        this.setLayout(new GridBagLayout());

        // Set the focus (default: Barr Smith Lawns, University of Adelaide, Adelaide, South Australia)
        final double defaultLatitude = Double.parseDouble(System.getProperty("initial.latitude", "-34.918"));
        final double defaultLongitude = Double.parseDouble(System.getProperty("initial.longitude", "138.604"));


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

//        mapViewer.addPropertyChangeListener("zoom", evt -> {
//            System.out.println("Zoom level: " + evt.getNewValue());
//        });
//
//        mapViewer.addPropertyChangeListener("center", evt -> {
//            double lat = mapViewer.getCenterPosition().getLatitude();
//            double lon = mapViewer.getCenterPosition().getLongitude();
//        });

        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(initialLocation);
        mapViewer.setPreferredSize(new Dimension(500,250));

        return mapViewer;
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
}
