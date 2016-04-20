package jfxtras.labs.icalendarfx.components;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import jfxtras.labs.icalendarfx.properties.PropertyEnum;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Comment;
import jfxtras.labs.icalendarfx.properties.component.time.DateTimeStart;
import jfxtras.labs.icalendarfx.utilities.DateTimeUtilities;
import jfxtras.labs.icalendarfx.utilities.DateTimeUtilities.DateTimeType;

/**
 * Components with the following properties:
 * COMMENT, DTSTART
 * 
 * @author David Bal
 * @see VEventNewInt
 * @see VTodoInt
 * @see VJournalInt
 * @see VFreeBusy
 * @see VTimeZone
 *  */
public interface VComponentPrimary<T> extends VComponentNew<T>
{
    /**
     *  COMMENT: RFC 5545 iCalendar 3.8.1.12. page 83
     * This property specifies non-processing information intended
      to provide a comment to the calendar user.
     * Example:
     * COMMENT:The meeting really needs to include both ourselves
         and the customer. We can't hold this meeting without them.
         As a matter of fact\, the venue for the meeting ought to be at
         their site. - - John
     * */
    ObservableList<Comment> getComments();
    void setComments(ObservableList<Comment> properties);
    default T withComments(ObservableList<Comment> comments) { setComments(comments); return (T) this; }
    default T withComments(String...comments)
    {
        Arrays.stream(comments).forEach(c -> PropertyEnum.COMMENT.parse(this, c));
        return (T) this;
    }

    /**
     * DTSTART: Date-Time Start, from RFC 5545 iCalendar 3.8.2.4 page 97
     * Start date/time of repeat rule.  Used as a starting point for making the Stream<LocalDateTime> of valid
     * start date/times of the repeating events.  Can be either type LocalDate or LocalDateTime
     */
    ObjectProperty<DateTimeStart<? extends Temporal>> dateTimeStartProperty();
    default DateTimeStart<? extends Temporal> getDateTimeStart() { return dateTimeStartProperty().get(); }
    default void setDateTimeStart(DateTimeStart<? extends Temporal> dtStart) { dateTimeStartProperty().set(dtStart); }
    default void setDateTimeStart(Temporal temporal)
    {
        if (temporal instanceof LocalDate)
        {
            setDateTimeStart(new DateTimeStart<LocalDate>((LocalDate) temporal));            
        } else if (temporal instanceof LocalDateTime)
        {
            setDateTimeStart(new DateTimeStart<LocalDateTime>((LocalDateTime) temporal));            
        } else if (temporal instanceof ZonedDateTime)
        {
            setDateTimeStart(new DateTimeStart<ZonedDateTime>((ZonedDateTime) temporal));            
        } else
        {
            throw new DateTimeException("Only LocalDate, LocalDateTime and ZonedDateTime supported. "
                    + temporal.getClass().getSimpleName() + " is not supported");
        }
    }
    default T withDateTimeStart(DateTimeStart<? extends Temporal> dtStart) { setDateTimeStart(dtStart); return (T) this; }
    default T withDateTimeStart(String dtStart) { return withDateTimeStart(DateTimeUtilities.temporalFromString(dtStart)); }
    default T withDateTimeStart(Temporal temporal) { setDateTimeStart(temporal); return (T) this; }

    default DateTimeType getDateTimeType() { return DateTimeType.of(getDateTimeStart().getValue()); };
    default ZoneId getZoneId()
    {
        if (getDateTimeType() == DateTimeType.DATE_WITH_LOCAL_TIME_AND_TIME_ZONE)
        {
            return ((ZonedDateTime) getDateTimeStart().getValue()).getZone();
        }
        return null;
    }
}