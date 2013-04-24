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
	private boolean marked;
	
	/**
	 * Constructor for heap element from value, whether it is a list (i.e.
	 * whether value is a number or an index to another element in the heap)
	 * and the next pointer that points to the next element in the heap.
	 */
	public Elem(int value, boolean isList, int nextIndex) {
		this.value = value;
		this.nextIndex = nextIndex;
		
	}
	
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
			data[i] = new Elem(0, false, i + 1);
		}
		data[size - 1].setNextIndex(NULL);
		activeCells = new HashSet<>();
	}
	
	/**
	 * Runs the mark-and-sweep garbage collector and updates the avail pointer
	 * and available heap size accordingly. 
	 */
	private int gc() {
		// TODO
		return 0;
	}
	
	private void mark() {
		// TODO
	}
	
	private void sweep() {
		// TODO
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
}
