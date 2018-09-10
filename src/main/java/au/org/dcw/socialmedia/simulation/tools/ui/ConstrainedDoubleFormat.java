package au.org.dcw.socialmedia.simulation.tools.ui;

import javax.swing.JFormattedTextField;
import java.text.ParseException;

public class ConstrainedDoubleFormat extends JFormattedTextField.AbstractFormatter {

    private final double lowerBound;
    private final double upperBound;

    public ConstrainedDoubleFormat(final double lowerBound, final double upperBound) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException(
                "lowerBound[" + lowerBound + "] must be less than upperBound[" + upperBound + "]"
            );
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        final double d;
        try {
            d = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new ParseException(text + " is not a valid double", 0);
        }
        if (d < lowerBound || d > upperBound) {
            throw new ParseException("Value must be in [" + lowerBound + "," + upperBound + "]", 0);
        }
        return d;
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        return value == null ? "0.0" : value.toString();
    }
}

