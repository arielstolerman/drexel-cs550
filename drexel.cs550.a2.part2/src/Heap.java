import java.util.*;


/**
 * The heap elements.
 */
class Elem {
	
	// car
	private int value;
	private boolean isList;
	
	// cdr
	private int nextIndex;
	
	// Mark-and-Sweep metadata
	private Heap heap;
	private boolean marked;
	
	/**
	 * Constructor for heap element.
	 * @param value element value (can be an number or a heap index, i.e. a
	 * list).
	 * @param isList whether value is a number or an index to another element in
	 * the heap.
	 * @param nextIndex the next pointer that points to the next element in the
	 * heap.
	 * @param heap the heap this element belongs to.
	 */
	public Elem(int value, boolean isList, int nextIndex, Heap heap) {
		this.value = value;
		this.isList = isList;
		this.nextIndex = nextIndex;
		this.heap = heap;
	}
	
	/**
	 * Constructor for numeric elements used by the program, but are NOT stored
	 * on the heap.
	 */
	public Elem(int value) {
		this(value,false,Heap.NULL,null);
	}
	
	// queries
	
	/**
	 * Returns true if this element holds a list.
	 */
	public boolean isList() {
		return isList;
	}
	
	/**
	 * Returns true if this element holds an integer.
	 * @return
	 */
	public boolean isInt() {
		return !isList;
	}
	
	/**
	 * Returns true if this element is marked.
	 */
	public boolean isMarked() {
		return marked;
	}
	
	/**
	 * Returns true iff this element has a next element (i.e. its cdr is not
	 * NULL).
	 */
	public boolean hasNext() {
		return nextIndex != Heap.NULL;
	}
	
	// getters
	
	/**
	 * Returns the integer value if this element is not a list, otherwise
	 * returns null.
	 */
	public Integer getValue() {
		if (!isList)
			return value;
		else
			return null;
	}
	
	/**
	 * Returns the index value if this element IS a list, otherwise
	 * returns null.
	 */
	public Integer getListIndex() {
		if (isList)
			return value;
		else
			return null;
	}
	
	/**
	 * Returns the next element, or NULL if none exists.
	 */
	public Elem getNext() {
		if (nextIndex == Heap.NULL)
			return null;
		return heap.elemAt(nextIndex);
	}
	
	// setters
	
	/**
	 * Sets the pointer to the next element to the given index.
	 */
	public void setNextIndex(int nextIndex) {
		this.nextIndex = nextIndex;
	}
	
	/**
	 * Sets the value to the input integer, and sets this element as an integer
	 * element (i.e. not a list).
	 */
	public void setToIntVal(int value) {
		this.value = value;
		isList = false;
	}
	
	/**
	 * Sets the value to the input list pointer (heap element index) and sets
	 * this element as a list element (i.e. not an integer value).
	 */
	public void setTpListVal(int value) {
		this.value = value;
		isList = true;
	}
	
	/**
	 * Marks this element recursively.
	 */
	public void mark() {
		marked = true;
		if (isList)
			heap.elemAt(value).mark();
		if (nextIndex != Heap.NULL)
			heap.elemAt(nextIndex).mark();
	}
	
	/**
	 * Unmarks this element.
	 */
	public void unmark() {
		marked = false;
	}
}

/**
 * The heap.
 */
public class Heap {
	
	// mark null indices, i.e. end of list
	public static final int NULL = -1;
	
	// temporary elements to be marked
	public static LinkedList<Elem> tmpToMark = new LinkedList<>();
	
	// heap data
	private Elem[] data;
	private int avail;
	private int availSize;
	
	/**
	 * Constructor for heap; initializes the heap to the given size, with all
	 * values as integers set to 0, every element points to the next after it
	 * in the underlying array, and the last points to NULL (-1).
	 * @param size
	 */
	public Heap(int size) {
		data = new Elem[size];
		avail = 0;
		availSize = size;
		for (int i = 0; i < size; i++) {
			data[i] = new Elem(0, false, i + 1, this);
		}
		data[size - 1].setNextIndex(NULL);
	}
	
	/**
	 * Runs the mark-and-sweep garbage collector and updates the avail pointer
	 * and available heap size accordingly. 
	 */
	private void gc(HashMap<String,Elem> nametable) {
		mark(nametable);
		sweep();
		clearMarks();
	}
	
	/**
	 * Marks all currently pointed elements and temporary elements still in use
	 * in the heap recursively.
	 */
	private void mark(HashMap<String,Elem> nametable) {
		Set<Elem> toMark = new HashSet<>();
		toMark.addAll(nametable.values());
		toMark.addAll(tmpToMark);
		for (Elem e: toMark) {
			if (e.isList())
				data[e.getValue()].mark(); 
		}
	}
	
	/**
	 * Sweeps all unpointed elements in the heap, udpates their next references
	 * and updated avail and availSize accordingly.
	 */
	private void sweep() {
		Elem elem;
		for (int i = 0; i < data.length; i++) {
			elem = data[i];
			// skip if this element is marked
			if (elem.isMarked())
				continue;
			// otherwise reset element, update availability and size
			elem.setToIntVal(0);
			if (avail == NULL)
				elem.setNextIndex(NULL);
			else
				elem.setNextIndex(avail);
			avail = i;
			availSize++;
		}
	}
	
	/**
	 * Clears all marks in the heap.
	 */
	private void clearMarks() {
		for (Elem elem: data)
			elem.unmark();
	}
	
	/**
	 * Cons the car and the cdr fields. Assumes that the cdr fields were already
	 * allocated using cons. For the car field, however, allocates a new cell on
	 * the heap and connects it to point cdr.
	 * If cdr is NULL, allocates car with its next pointer as NULL.
	 * @param car
	 * @param cdr
	 * @param nametable
	 * @return
	 */
	public Elem cons(Elem car, Elem cdr, HashMap<String,Elem> nametable) {
		// TODO
		return null;
	}
	
	// getters
	
	public int avail() {
		return avail;
	}
	
	public int availSize() {
		return availSize;
	}
	
	public Elem elemAt(int index) {
		return data[index];
	}
	
	/**
	 * @param index desired list start index.
	 * @return the list that starts at the given index, or NULL if none exists.
	 */
	public Elem getListAt(int index) {
		Elem e;
		if (!(e = data[index]).isList())
			return null;
		return e;
	}
}
