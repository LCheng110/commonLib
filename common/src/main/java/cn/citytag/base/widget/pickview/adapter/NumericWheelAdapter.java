package cn.citytag.base.widget.pickview.adapter;


/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter {

    /**
     * The default min value
     */
    public static final int DEFAULT_MAX_VALUE = 9;

    /**
     * The default max value
     */
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final String DEFAULT_LAVEL = "";

    // Values
    private int minValue;
    private int maxValue;
    private String label = "";

    /**
     * Default constructor
     */
    public NumericWheelAdapter() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE, DEFAULT_LAVEL);
    }

    /**
     * Constructor
     *
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(int minValue, int maxValue, String label) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.label = label;
    }

    @Override
    public Object getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            return value + label;
        }
        return 0 + label;
    }


    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }

    @Override
    public int indexOf(Object o) {
        return Integer.parseInt(o.toString().replace(label, "")) - minValue;
    }
}
