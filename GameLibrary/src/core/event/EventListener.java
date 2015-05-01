package core.event;

/**
 * The event listener defines the interface for classes that wish
 * to be subscribers to events in the {@link EventManager}.
 */
public interface EventListener {
   /**
    * Notifies the {@link EventListener} that an {@link Event} has occured.
    * @param event the {@link Event} that has been fired.
    */
   public void notify(Event event);
   
}//End interface EventListener
