/*  Student information for assignment:
 *
 *  On OUR honor, Jordi Weber and Mohammad Kashif, this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: jw57744
 *  email address: jw57744@utexas.edu
 *  Grader name: Pranav Chandupatla
 *
 *  Student 2
 *  UTEID: mnk665
 *  email address: mohammadnkashif@utexas.edu
 *
 */

import java.io.Serializable;
import java.util.*;

public class PriorityQueue<E extends Comparable> {

    // Instance Variable: Linked List Implemented
    private LinkedList<E> myCon;

    /**
     * Standard constructor for PriorityQueue.
     * No parameters.
     * pre: none
     * post: Instantiates new LinkedList.
     */
    public PriorityQueue() {
        myCon = new LinkedList<>();
    }

    /**
     * Returns number of elements currently in PriorityQueue.
     * pre: none
     * post: Returns size of Priority Queue.
     */
    public int size() {
        return myCon.size();
    }

    /**
     * Enqueue/Add for PriorityQueue.
     * Adds element in proper position based on priority/
     * comparison to all other elements present.
     * Least to greatest ordering implemented.
     * @param e: Element to be inserted in queue
     * pre: e != null
     * post: Adds element to Queue.
     */
    public boolean enqueue(E e) {
        if (e == null) {
            throw new IllegalArgumentException("Element being added can't be null.");
        }
        ListIterator<E> curr = myCon.listIterator();
        while (curr.hasNext()) {
            if (e.compareTo(curr.next()) < 0) {
                curr.previous();
                curr.add(e);
                return true;
            }
        }
        curr.add(e);
        return true;
    }

    /**
     * Dequeue/ remove method. Removes and returns first value in myCon.
     * No parameters.
     * pre: none
     * post: First/smallest element of queue removed and returned to user.
     */
    public E dequeue() {
        if (isEmpty()) {
            return null;
        }
        return myCon.remove(0);
    }

    /**
     * Peek method, Allows user to peek at first element of queue without removing.
     * No parameters.
     * pre: none
     * post: Returns smallest/first element of Queue.
     */
    public E peek() {
        return myCon.get(0);
    }

    /**
     * isEmpty method, returns boolean indicating if queue is empty.
     * Relies on LinkedList native isEmpty method.
     * No parameters.
     * pre: none
     * post: Returns whether PriorityQueue is currently empty.
     */
    public boolean isEmpty() {
        return myCon.isEmpty();
    }

}
