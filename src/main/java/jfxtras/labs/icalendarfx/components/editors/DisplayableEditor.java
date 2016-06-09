package jfxtras.labs.icalendarfx.components.editors;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.util.Callback;
import javafx.util.Pair;
import jfxtras.labs.icalendarfx.components.VComponentDisplayable;
import jfxtras.labs.icalendarfx.components.editors.LocatableEditor.RRuleStatus;
import jfxtras.labs.icalendarfx.properties.PropertyType;
import jfxtras.labs.icalendarfx.properties.component.recurrence.rrule.RecurrenceRule2;
import jfxtras.labs.icalendarfx.utilities.DateTimeUtilities;

public abstract class DisplayableEditor<T, U extends VComponentDisplayable<U>> implements DisplayEditable<T, U>
{ 
    @Override public U getVComponentEdited() { return vComponentEdited; }
    private U vComponentEdited;
    @Override public void setVComponentEdited(U vComponentEdited) { this.vComponentEdited = vComponentEdited; }

    @Override public U getVComponentOriginal() { return vComponentOriginal; }
    private U vComponentOriginal;
    @Override public void setVComponentOriginal(U vComponentOriginal) { this.vComponentOriginal = vComponentOriginal; }

    @Override public Temporal getStartRecurrence() { return startRecurrence; }
    private Temporal startRecurrence;
    @Override public void setStartRecurrence(Temporal startRecurrence) { this.startRecurrence = startRecurrence; }
    
    @Override public Callback<Map<ChangeDialogOption, Pair<Temporal,Temporal>>, ChangeDialogOption> getDialogCallback() { return dialogCallback; }
    private Callback<Map<ChangeDialogOption, Pair<Temporal,Temporal>>, ChangeDialogOption> dialogCallback;    
    @Override public void setDialogCallback(Callback<Map<ChangeDialogOption, Pair<Temporal,Temporal>>, ChangeDialogOption> dialogCallback) { this.dialogCallback = dialogCallback; }

    /** Edit VEvent or VTodo or VJournal */
    @Override
    public Collection<U> edit()
    {
        U vComponentEdited = getVComponentEdited();
        U vComponentOriginal = getVComponentOriginal();
        Temporal startRecurrence = getStartRecurrence();
//        System.out.println("shiftAmout:" + shiftAmount + " " + vComponentEdited.getDateTimeStart().getValue() + " " + ((VEvent) vComponentEdited).getDateTimeEnd().getValue());
        if (! vComponentEdited.isValid())
        {
            throw new RuntimeException("Can't revise. Edited component is invalid:" + System.lineSeparator() + 
                    vComponentEdited.errors().stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() +
                    vComponentEdited.toContent());
        }
        if (! vComponentOriginal.isValid())
        {
            throw new RuntimeException("Can't revise. Original component is invalid:" + System.lineSeparator() + 
                    vComponentEdited.errors().stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() +
                    vComponentEdited.toContent());
        }
        
        Collection<T> vComponents = new ArrayList<>(); // new components that should be added to main list
        validateStartRecurrenceAndDTStart(startRecurrence);
        final RRuleStatus rruleType = RRuleStatus.getRRuleType(vComponentOriginal.getRecurrenceRule(), vComponentEdited.getRecurrenceRule());
        System.out.println("rruleType:" + rruleType);
        boolean incrementSequence = true;
//        Collection<R> newRecurrences = null;
//        Collection<R> allRecurrences = recurrences;
        switch (rruleType)
        {
        case HAD_REPEAT_BECOMING_INDIVIDUAL:
            becomeNonRecurring();
//            vComponentEdited.becomeNonRecurring(vComponentOriginal, startRecurrence, endRecurrence);
            // fall through
        case WITH_NEW_REPEAT: // no dialog
        case INDIVIDUAL:
            vComponentEdited.adjustDateTime(startOriginalRecurrence, startRecurrence, endRecurrence);
            
//            if (! vComponentEditedCopy.equals(vComponentOriginal))
//            {
//                newRecurrences = updateRecurrences(vComponentEditedCopy);
//            }
            break;
        case WITH_EXISTING_REPEAT:
            // Find which properties changed
            // TODO - PUT findChangedProperties IN STATIC
            List<PropertyType> changedProperties = vComponentEdited.findChangedProperties(
                    vComponentOriginal,
                    startOriginalRecurrence,
                    startRecurrence,
                    endRecurrence);
            /* Note:
             * time properties must be checked separately because changes are stored in startRecurrence and endRecurrence,
             * not the VComponents DTSTART and DTEND yet.  The changes to DTSTART and DTEND are made after the dialog
             * question is answered. */
//            changedProperties.addAll(changedStartAndEndDateTime(startOriginalRecurrence, startRecurrence, endRecurrence));
            // determine if any changed properties warrant dialog
//            changedProperties.stream().forEach(a -> System.out.println("changed property:" + a));
            boolean provideDialog = changedProperties.stream()
                .map(p -> DIALOG_REQUIRED_PROPERTIES.contains(p))
                .anyMatch(b -> b == true);
//            boolean provideDialog = requiresChangeDialog(changedProperties);
            if (changedProperties.size() > 0) // if changes occurred
            {
                List<T> relatedVComponents = Arrays.asList(vComponentEdited); // TODO - support related components
                final ChangeDialogOption changeResponse;
                if (provideDialog)
                {
                    Map<ChangeDialogOption, Pair<Temporal,Temporal>> choices = ChangeDialogOption.makeDialogChoices(
                            vComponentOriginal,
                            vComponentEdited,
                            startOriginalRecurrence,
                            changedProperties);
                    changeResponse = dialogCallback.call(choices);
                    System.out.println("changeResponse:" + changeResponse);
                } else
                {
                    changeResponse = ChangeDialogOption.ALL;
                }
                switch (changeResponse)
                {
                case ALL:
                    System.out.println("components size:" + relatedVComponents.size());
                    if (relatedVComponents.size() == 1)
                    {
                        vComponentEdited.adjustDateTime(startOriginalRecurrence, startRecurrence, endRecurrence);
//                        if (vComponentEditedCopy.childComponentsWithRecurrenceIDs().size() > 0)
//                        {
                        // Adjust children components with RecurrenceIDs
                        vComponentEdited.childComponents()
                                .stream()
//                                .map(c -> c.getRecurrenceId())
                                .forEach(v ->
                                {
                                    TemporalAmount shiftAmount = DateTimeUtilities.temporalAmountBetween(startOriginalRecurrence, startRecurrence);
                                    Temporal newRecurreneId = v.getRecurrenceId().getValue().plus(shiftAmount);
                                    v.setRecurrenceId(newRecurreneId);
                                });
//                        }
//                        newRecurrences = updateRecurrences(vComponentEditedCopy);
                    } else
                    {
                        throw new RuntimeException("Only 1 relatedVComponents currently supported");
                    }
                    break;
                case CANCEL:
//                    vComponentEdited.copyComponentFrom(vComponentOriginal);  // return to original
//                    return Arrays.asList(vComponentOriginal); // return original
                    return null;
                case THIS_AND_FUTURE:
                    editThisAndFuture(
                            vComponentOriginal,
                            vComponentEdited,
                            vComponents,
                            startOriginalRecurrence,
                            startRecurrence,
                            endRecurrence
//                            shiftAmount
                            );
                    break;
                case ONE:
                    editOne(
                            vComponentOriginal,
                            vComponentEdited,
                            vComponents,
                            startOriginalRecurrence,
                            startRecurrence,
                            endRecurrence
//                            shiftAmount
                            );
                    break;
                default:
                    throw new RuntimeException("Unknown response:" + changeResponse);
                }
            }
        }
        if (! vComponentEdited.isValid())
        {
            throw new RuntimeException("Invalid component:" + System.lineSeparator() + 
                    vComponentEdited.errors().stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() +
                    vComponentEdited.toContent());
        }
        if (incrementSequence)
        {
            vComponentEdited.incrementSequence();
        }
        System.out.println("endedit:" +vComponents.size());
//        vComponents.remove(vComponentOriginal);
//        System.out.println(vComponents.size());
//        if (! vComponents.isEmpty())
//        {
        vComponents.add(vComponentEdited);
//        }
//        System.out.println(vComponents.size());
//        if (newRecurrences != null)
//        {
////            allRecurrences.clear();
//            allRecurrences.addAll(newRecurrences);
//        }
        return vComponents;
    }
    
    /** If startRecurrence isn't valid due to a RRULE change, change startRecurrence and
     * endRecurrence to closest valid values
     */
     // TODO - VERITFY THIS WORKS - changed from old version
    void validateStartRecurrenceAndDTStart(Temporal startRecurrence)
    {
        if (getVComponentEdited().getRecurrenceRule() != null)
        {
            Temporal firstTemporal = getVComponentEdited().getRecurrenceRule().getValue()
                    .streamRecurrences(getVComponentEdited().getDateTimeStart().getValue())
                    .findFirst()
                    .get();
            if (! firstTemporal.equals(getVComponentEdited().getDateTimeStart().getValue()))
            {
                getVComponentEdited().setDateTimeStart(firstTemporal);
            }
        }
    }
    
    void becomeNonRecurring()
    {
        getVComponentEdited().setRecurrenceRule((RecurrenceRule2) null);
        getVComponentEdited().setRecurrenceDates(null);
        getVComponentEdited().setExceptionDates(null);
    }
}
