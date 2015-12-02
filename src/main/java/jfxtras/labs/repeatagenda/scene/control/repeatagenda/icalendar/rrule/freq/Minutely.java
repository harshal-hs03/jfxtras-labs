package jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.rrule.freq;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;

import javafx.beans.property.SimpleObjectProperty;

/** MINUTELY frequency rule as defined by RFC 5545 iCalendar 3.3.10 p39 */
public class Minutely extends FrequencyAbstract
{
    // Constructor
    public Minutely() { super(FrequencyType.MINUTELY, new SimpleObjectProperty<ChronoUnit>(ChronoUnit.MINUTES)); }

    @Override
    public TemporalAdjuster getAdjuster() {
        // TODO Auto-generated method stub
        return null;
    }

}