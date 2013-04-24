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
	
	// heap data
	private Elem[] data;
	private int avail;
	private int availSize;
	// to hold all currently active cells for mark-and-sweep
	private Set<Elem> activeCells;
	
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
		activeCells = new HashSet<>();
	}
	
	/**
	 * Runs the mark-and-sweep garbage collector and updates the avail pointer
	 * and available heap size accordingly. 
	 */
	private void gc() {
		if (availSize > 0) return;
		mark();
		sweep();
		clearMarks();
	}
	
	/**
	 * Marks all currently pointed elements in the heap recursively.
	 */
	private void mark() {
		for (Elem elem: activeCells) {
			elem.mark();
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
	
	public Elem cons() {
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
}
