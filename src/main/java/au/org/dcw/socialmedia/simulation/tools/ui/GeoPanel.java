package au.org.dcw.socialmedia.simulation.tools.ui;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class GeoPanel extends JPanel {
    private final JRadioButton geoFromGoogle;
    private final JRadioButton geoFromLatLong;
    private final JFormattedTextField latLonTF, latTF, lonTF;

    public GeoPanel() {
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
        latLabel.setLabelFor(lonTF);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        this.add(lonTF, gbc);

        // BEHAVIOUR
        final ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(geoFromGoogle);
        radioGroup.add(geoFromLatLong);
    }

    public double[] getLatLon() {
        if (geoFromGoogle.isSelected()) {
            return Stream.of(latLonTF.getText().split(",")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        } else if (geoFromLatLong.isSelected()) {
            return new double[]{
                Double.parseDouble(latTF.getText()),
                Double.parseDouble(lonTF.getText()),
            };
        }
        return new double[]{-1.0,-1.0};
    }
}
