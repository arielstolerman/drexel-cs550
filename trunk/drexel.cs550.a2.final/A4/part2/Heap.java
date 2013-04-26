import java.util.*;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;


/**
 * The heap elements.
 */
class Elem {
	
	// car
	private int value;
	private int heapIndex;
	private boolean isList;
	
	// cdr
	private int nextIndex;
	
	// Mark-and-Sweep metadata
	private Heap heap;
	private boolean marked;
	
	/**
	 * Constructor for heap element -- to be called only by the heap
	 * @param value element value (can be an number or a heap index, i.e. a
	 * list).
	 * @param isList whether value is a number or an index to another element in
	 * the heap.
	 * @param nextIndex the next pointer that points to the next element in the
	 * heap.
	 * @param heap the heap this element belongs to.
	 */
	public Elem(int value, int heapIndex, boolean isList, int nextIndex, Heap heap) {
		this.value = value;
		this.heapIndex = heapIndex;
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
	
	/**
	 * Returns true iff this element has a next element (i.e. its cdr is not
	 * NULL).
	 */
	public boolean hasNext() {
		return nextIndex != Heap.NULL;
	}
	
	// getters
	
	/**
	 * Returns the index of this element on the heap, or NULL if not on the heap.
	 */
	public int getHeapIndex() {
		return heapIndex;
	}
	
	/**
	 * Returns the integer value if this element is not a list, otherwise
	 * returns null.
	 */
	public Integer getInt() {
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
	 * If this element is a list, return that list's first element, otherwise
	 * returns null.
	 */
	public Elem getList() {
		if (isList && value != Heap.NULL)
			return heap.elemAt(value);
		else
			return null;
	}
	
	/**
	 * Returns the int value of the element (can be either an integer or a
	 * list index).
	 */
	public int getRawValue() {
		return value;
	}
	
	/**
	 * Returns the next element, or NULL if none exists.
	 */
	public Elem getNext() {
		if (nextIndex == Heap.NULL)
			return null;
		return heap.elemAt(nextIndex);
	}
	
	/**
	 * Returns the index in the heap of the next element.
	 */
	public int getNextIndex() {
		return nextIndex;
	}
	
	@Override
	public String toString() {
		if (!isList)
			return value + "";
		else {
			String res = "[";
			Elem curr = getList();
			while (curr != null) {
				res += curr.toString() + ", ";
				curr = curr.getNext();
			}
			if (res.endsWith(", "))
				res = res.substring(0,res.length() - 2);
			res += "]";
			return res;
		}
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
	public LinkedList<Elem> tmpToMark = new LinkedList<>();
	
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
			data[i] = new Elem(0, i, false, i + 1, this);
		}
		data[size - 1].setNextIndex(NULL);
	}
	
	// constructors for local int/list elements not on the heap
	
	/**
	 * Creates new local element for an integer value, i.e. this element does
	 * not reside on the heap.
	 */
	public Elem getLocalIntElem(int value) {
		return new Elem(value, Heap.NULL, false, Heap.NULL, this);
	}

	/**
	 * Creates new local element for a list value, i.e. this element does not
	 * reside on the heap.
	 */
	public Elem getLocalListElem(Elem elem) {
		int ind = elem == null ? Heap.NULL : elem.getHeapIndex();
		return new Elem(ind, Heap.NULL, true, Heap.NULL, this);
	}
	
	/**
	 * Runs the mark-and-sweep garbage collector and updates the avail pointer
	 * and available heap size accordingly. 
	 */
	private void gc(Elem car, Elem cdr, HashMap<String, Elem> nametable) {
		mark(car,cdr,nametable);
		sweep();
		clearMarks(car);
	}
	
	/**
	 * Marks all currently pointed elements and temporary elements still in use
	 * in the heap recursively.
	 */
	private void mark(Elem car, Elem cdr, HashMap<String, Elem> nametable) {
		// mark referenced lists on the heap
		for (Elem e: nametable.values())
			if (e.isList())
				data[e.getListIndex()].mark();
		// mark temporary elements on the heap
		for (Elem e: tmpToMark)
			e.mark();
		// mark car (still not referenced)
		car.mark();
		// mark cdr (if not null; still not referenced)
		if (cdr != null)
			cdr.mark();
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
	 * Clears all marks in the heap, and the car mark (since not yet added to
	 * the heap).
	 */
	private void clearMarks(Elem car) {
		for (Elem elem: data)
			elem.unmark();
		car.unmark();
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
	public Elem cons(Elem car, Elem cdr, HashMap<String,Elem> nametable)
			throws RuntimeException {		
		// call gc if necessary
		if (availSize == 0)
			gc(car, cdr, nametable);
		// if heap full, throw
		if (availSize == 0)
			throw new RuntimeException(
					"Heap full, no room for " + car + "\n" +
					"Heap image at crash:\n" +
					toString(nametable));
		// add car and change avail accordingly
		int newAvail = data[avail].getNextIndex();
		Elem toAdd = new Elem(
				car.getRawValue(),
				avail,
				car.isList(),
				cdr == null ? NULL : cdr.getHeapIndex(),
				this);
		data[avail] = toAdd;
		avail = newAvail;
		availSize--;
		return toAdd;
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
	
	public String toString(HashMap<String, Elem> nametable) {
		boolean vars = nametable != null;
		String lines = "------------------------------------" +
				(vars ? "--------" : "");
		String res =
				"AVAIL: " + avail + " AVAIL SIZE: " + availSize + "\n" +
				lines + "\n" +
				"AVAIL | IND | CAR    | l | CDR | m " +
				(vars ? "| VARNAME" : "") + "\n" + lines + "\n";
		for (int i = 0; i < data.length; i++) {
			// get variable name, if exists
			String var = "";
			if (vars)
			{
				Elem tmp;
				for (Entry<String, Elem> e: nametable.entrySet())
					if ((tmp = e.getValue().getList()) != null && // non-empty list
					tmp.getHeapIndex() == i)
						var = e.getKey();
			}
					
			res += String.format(
					"  %s  | %03d | %-6d | %s | %03d | %s | %s\n",
					i == avail ? "->" : "  ",
					i, data[i].getRawValue(),
					data[i].isList() ? "*" : " ",
					data[i].getNextIndex(),
					data[i].isMarked() ? "*" : " ",
					var);
			if (i > 0 && i % 10 == 0)
				res += lines + "\n";
		}
		return res;
	}
	
	@Override
	public String toString() {
		return toString(null);
	}
	
	// for testing
//	public static void main(String[] args) {
//		Heap h = new Heap(4);
//		HashMap<String,Elem> nametable = new HashMap<>();
//		System.out.println(h);
//		System.out.println();
//		
//		// add x
//		Elem x = h.cons(Elem.getLocalIntElem(10), null, nametable);
//		nametable.put("x",x);
//		System.out.println(h);
//		System.out.println();
//		
//		// add y
//		Elem y = h.cons(Elem.getLocalIntElem(20), null, nametable);
//		nametable.put("y",y);
//		System.out.println(h);
//		System.out.println();
//		
//		// add z
//		Elem z = h.cons(Elem.getLocalIntElem(30), null, nametable);
//		nametable.put("z",z);
//		System.out.println(h);
//		System.out.println();
//		
//		//add w
//		Elem w = h.cons(Elem.getLocalIntElem(40), null, nametable);
//		nametable.put("w",w);
//		System.out.println(h);
//		System.out.println();
//		
//		System.out.println(nametable);
//		
//		//add u
//		Elem u = h.cons(Elem.getLocalIntElem(50), null, nametable);
//		nametable.put("u",u);
//		System.out.println(h);
//		System.out.println();
//		
//	}
}
