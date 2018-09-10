package au.org.dcw.socialmedia.simulation.tools.ui;

import javax.swing.JFormattedTextField;
import java.text.ParseException;

public class LatLongPairFormat extends JFormattedTextField.AbstractFormatter {

    @Override
    public Object stringToValue(String text) throws ParseException {
        String[] parts = text.split(",");
        if (parts.length != 2) {
            int errorOffset = -1;
            if (parts.length > 2)
                errorOffset = parts[0].length() + 1 + parts[1].length();
            throw new ParseException("Wrong number of commas", errorOffset);
        }
        final double lat;
        try {
            lat = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throw new ParseException(parts[0] + " is not a valid double", 0);
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new ParseException("Latitude should be in [-90.0,90.0]", 0);
        }
        final int lonOffset = parts[0].length() + 1;
        final double lon;
        try {
            lon = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            throw new ParseException(parts[1] + " is not a valid double", lonOffset);
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new ParseException("Longitude should be in [-180.0,180.0]", lonOffset);
        }
        return text;
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        return value == null ? "0.0,0.0" : value.toString();
    }
}
