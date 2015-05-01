package core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The EventManager is a singleton that is responsible for keeping track of subscriptions
 * to events within the system, as well as raising events and passing them onto the subscribing
 * event listeners.
 */
public class EventManager {
   /** The EventManager singleton.**/
   private static EventManager eventManager;
   /** The event queue for this manager **/
   private List<Event> eventQueue;
   /** Map of {@link EventListener} mapped to the {@link Event}s they subscribe to **/
   private Map<Class<? extends Event>, List<EventListener>> subscriptionMap;

   /**
    * Constructs a new {@link EventManager}.
    */
   private EventManager(){
      subscriptionMap = new HashMap<Class<? extends Event>, List<EventListener>>();
      eventQueue = new ArrayList<Event>();
   }//End constructor

   /**
    * Subscribes an {@link EventListener} for a specified {@link Event}.
    * @param eventClass the {@link Class} of the {@link Event} to subscribe for.
    * @param listener the {@link EventListener} to be notified when the subscribed {@link Event} is fired.
    */
   public void registerFor(Class<? extends Event> eventClass, EventListener listener){
      if(!subscriptionMap.containsKey(eventClass)){
         subscriptionMap.put(eventClass, new ArrayList<EventListener>());
      }//End if
      List<EventListener> subscribers = subscriptionMap.get(eventClass);
      if(!subscribers.contains(listener)){
         subscribers.add(listener);
      }//End if
   }//End method registerFor


   /**
    * Puts the event on the event queue to be processed on the next update.
    * @param event the event to fire.
    */
   public synchronized void fireEvent(Event event){
      eventQueue.add(event);
   }//End method fireEvent

   /**
    * Processes the event queue
    */
   public void processEventQueue(){
      List<Event> eventQueueClone = new ArrayList<Event>();
      eventQueueClone.addAll(eventQueue);
      eventQueue.clear();
      for(Iterator<Event> it = eventQueueClone.iterator(); it.hasNext();){
         processEvent(it.next());
      }//End for
   }//End method processEventQueue

   /**
    * Processes the given event and notifies all subscribers.
    * @param event the event to process.
    */
   private void processEvent(Event event){
      List<EventListener> subscribers = subscriptionMap.get(event.getClass());
      if(subscribers != null){
         for(Iterator<EventListener> it = subscribers.iterator(); it.hasNext();){
            it.next().notify(event);
         }//End for
      }//End if
   }//End method processEvent

   /**
    * Gets the {@link EventManager} singleton for this application.
    * @return the {@link EventManager} singleton for this application.
    */
   public synchronized static EventManager getEventManager(){
      if(eventManager == null){
         eventManager = new EventManager();
      }//End if
      return eventManager;
   }//End method getWindowManager
}//End class EventManager
