/**
* Implementation of skiplist
* @author 	Lopamudra 
*  			Bhakti Khatri			
* 			Gautam Gunda 			
* 			Sangeeta Kadambala
*/

package cs6301.g45;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Skeleton for skip list implementation.
public class SkipList<T extends Comparable<? super T>> {
	private SkipListEntry<T> head;
	public static final int MAX_LEVEL = 32;
	public static int[] rank = null;

	private int size;
	private int currentMaxlevel;

	// Constructor
	public SkipList() {
		size = 0;
		currentMaxlevel = 1;
		head = new SkipListEntry<T>((T) new Integer(Integer.MIN_VALUE), MAX_LEVEL);
	}

	public SkipList(int maxlevel) {
		head = new SkipListEntry<T>((T) new Integer(Integer.MIN_VALUE), maxlevel);
		this.currentMaxlevel = maxlevel;
		// Initialize individul entries in head & tail
		for (int j = 0; j < maxlevel; j++) {
			head.pointer[j].next = null;
			head.pointer[j].span = 0;
		}
	}

	public class SkipListEntry<T> {
		public T element; // the key
		public int level;
		public Pointer<T> pointer[]; // for storing span info

		public SkipListEntry(T element, int level) {
			this.element = element;
			this.level = level;
			pointer = new Pointer[level];
			for (int i = 0; i < level; i++)
				pointer[i] = new Pointer<T>(0);
		}

		public SkipListEntry() {
		}

		/* It will have the span and pointer to next element information */
		public class Pointer<T> {
			// the array of pointers
			public SkipListEntry<T> next;
			public int span;

			public Pointer(int span) {
				this.next = null;
				this.span = span;
			}
		}
	}

	// Add x to list. If x already exists, replace it. Returns true if new node is
	// added to list
	public boolean add(T x) {
		SkipListEntry<T>[] prev = find(x);
		// printArray(rank);

		if (prev[0].pointer[0].next != null && x.compareTo(prev[0].pointer[0].next.element) == 0) {
			prev[0].pointer[0].next.element = x;
			return false;
		} else {
			int level = chooseLevel();
			if (level == 0)
				level = 1;
			if (level > currentMaxlevel) {
				for (int i = currentMaxlevel; i < level; i++) {
					rank[i] = 0;
					prev[i] = head;
					prev[i].pointer[i].span = size;
				}
				currentMaxlevel = level;
			}
			// System.out.println("Level of new node :"+ level);
			SkipListEntry<T> newNode = new SkipListEntry<T>(x, level);
			for (int i = 0; i < level; i++) {
				newNode.pointer[i].next = prev[i].pointer[i].next;
				prev[i].pointer[i].next = newNode;
				// Update the span info while adding
				newNode.pointer[i].span = prev[i].pointer[i].span - (rank[0] - rank[i]);
				prev[i].pointer[i].span = (rank[0] - rank[i]) + 1;
			}

			/* Increment span for upper levels if exist */
			for (int i = level; i < currentMaxlevel; i++) {
				prev[i].pointer[i].span++;
			}
			size++; // increase the size of skip list after adding
			return true;
		}
	}

	// Find smallest element that is greater or equal to x
	public T ceiling(T x) {
		SkipListEntry<T>[] prev = find(x);
		if (prev[0].pointer[0].next != null)
			return prev[0].pointer[0].next.element;
		else
			return null; // Returning null if the ceiling for a given element not in skip list
	}

	// Does list contain x?
	public boolean contains(T x) {
		SkipListEntry<T>[] prev = find(x);
		if (prev[0].pointer[0].next != null && x.compareTo(prev[0].pointer[0].next.element) == 0)
			return true;
		return false;
	}

	// Return first element of list
	public T first() {
		return head.pointer[0].next.element;
	}

	// Find largest element that is less than or equal to x
	public T floor(T x) {
		SkipListEntry<T>[] prev = find(x);
		if (prev[0].pointer[0].next != null && x.compareTo(prev[0].pointer[0].next.element) == 0)
			return prev[0].pointer[0].next.element;
		else if (prev[0] != head)
			return prev[0].element;
		else
			return null; // if the floor element is not present in the skip list returning null.
	}

	// Return element at index n of list. First element is at index 0.
	public T get(int n) {
		if (n > size - 1)
			return null;
		SkipListEntry<T> p = head;
		int index = n + 1;
		for (int i = currentMaxlevel - 1; i >= 0; i--) {
			while (p != null && index > p.pointer[i].span) {
				index -= p.pointer[i].span;
				if (index != 0)
					p = p.pointer[i].next;
				// System.out.println("currentMaxlevel : "+ currentMaxlevel + " ,new p :"+ p.element);
			}
		}
		return p.pointer[0].next.element;
	}

	// Is the list empty?
	public boolean isEmpty() {
		return size == 0;
	}

	// Iterate through the elements of list in sorted order
	public Iterator<T> iterator() {
		return new SkipListIterator(this);
	}

	// Return last element of list
	public T last() {
		SkipListEntry<T> curr = head;
		SkipListEntry<T> prev = null;

		if (curr == null)
			return null;
		for (int i = currentMaxlevel - 1; i >= 0; i--) {
			while (curr.pointer[i].next != null) {
				curr = curr.pointer[i].next;
				prev = curr;
			}
		}
		return prev.element;
	}


	// Reorganize the elements of the list into a perfect skip list
	public void rebuild() {
		SkipListEntry<T>[] entryList;
		SkipList<T> list = this;
		List<T> arraylist = new ArrayList<>();
		Iterator<T> itr = list.iterator();
		while (itr.hasNext())
			arraylist.add(itr.next());

		// rebuild_helper(arraylist);
		entryList = new SkipListEntry[size];
		rebuild_levels(arraylist, entryList, 0, size);
		connect_levels(entryList);
	}

	//Helper function for rebuild() to connect each levels of skiplistEntry elements
	private void connect_levels(SkipListEntry<T>[] list) {
		for (int i = 0; i < list.length; i++) {
			SkipListEntry<T> prev = head;
			for (int j = 0; j < list.length; j++) {
				if (list[j].level >= i + 1) {
					prev.pointer[i].next = list[j];
					//System.out.println("connecting level" + i + " prev: " + prev.element + " next:" + list[j].element);
					prev = list[j];
				}
			}
		}

	}

	//Helper function for rebuild
	private void rebuild_levels(List<T> list, SkipListEntry<T>[] entryList, int left, int right) {
		if (left < right) {
			int length = right - left;
			int maxlevel = (int) Math.floor((Math.log(length + 1) / Math.log(2)));
			int mid = left + (right - left) / 2;
			SkipListEntry<T> newMiddle = new SkipListEntry<T>(list.get(mid), maxlevel);
			entryList[mid] = newMiddle;

			rebuild_levels(list, entryList, left, mid);
			rebuild_levels(list, entryList, mid + 1, right);
		}
	}

		
 	/*
	public SkipListEntry<T> contains_(T x) {
		SkipListEntry<T>[] prev = find(x);
		if (prev[0].pointer[0].next != null && x.compareTo(prev[0].pointer[0].next.element) == 0)
			return prev[0].pointer[0].next;
		return null;
	}private void rebuild_helper(List<T> list) {
		if (list.size() == 0)
			return;
		int size = list.size();
		T first = list.get(0);
		T last = (T) list.get(size - 1);
	
		SkipListEntry<T> new_tail = new SkipListEntry<T>((T) new Integer(Integer.MAX_VALUE), MAX_LEVEL);
		SkipListEntry<T> new_head = new SkipListEntry<T>((T) new Integer(Integer.MIN_VALUE), MAX_LEVEL);
		this.head = new_head;
		setPointer(list, 0, size, new_head, null);
	}

	private void setPointer(List<T> list, int left, int right, SkipListEntry<T> head, SkipListEntry<T> tail) {
		SkipListEntry<T> newMiddle;
		if (left <= right) {
			int length = right - left;
			System.out.println("length : " + length);
			int maxlevel = (int) Math.floor((Math.log(length + 1) / Math.log(2)));
			int mid = left + (right - left) / 2;
			System.out.println("Get the index of the list: " + contains(list.get(mid)));
			newMiddle = contains_(list.get(mid));
			if (newMiddle == null) {
				newMiddle = new SkipListEntry<T>(list.get(mid), maxlevel);
				head.pointer[maxlevel - 1].next = newMiddle;
			}
			newMiddle.pointer[maxlevel - 1].next = tail;
			
			 * if(maxlevel - 2 == 0) newMiddle.pointer[maxlevel - 2].next = tail;
			 
			System.out.println("left : " + left + " mid : " + mid + " right : " + right + " maxlevel: " + maxlevel);
			if (tail != null)
				System.out.println("head : " + head.element + " middle : " + newMiddle.element + " tail: "
						+ tail.element + " maxlevel : " + maxlevel);
			if (head.pointer[maxlevel - 1].next != null)
				System.out.println("head points to : " + head.pointer[maxlevel - 1].next.element);
			if (newMiddle.pointer[maxlevel - 1].next != null)
				System.out.println("middle element points to : " + newMiddle.pointer[maxlevel - 1].next.element);
			// System.out.println("left : " + left + " mid : " +mid + " right : "+ right + "
			// maxlevel: "+ maxlevel);
			System.out.println("--------------------------------");
			if (maxlevel == 1) {
				System.out.println("TERMINAL: left : " + left + " right : " + right + " maxlevel: " + maxlevel);
				return;
			}
			setPointer(list, left, mid, head, newMiddle);
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

			setPointer(list, mid + 1, right, newMiddle, tail);

		}

	}*/

	// Remove x from list. Removed element is returned. Return null if x not in list
	public T remove(T x) {
		SkipListEntry<T>[] prev = find(x);
		SkipListEntry<T> node = prev[0].pointer[0].next;
		// If node is the last element
		if (node == null)
			return null;
		if (node.element.compareTo(x) != 0)
			return null;
		else {
			for (int i = 0; i < currentMaxlevel; i++) {
				if (prev[i].pointer[i].next == node) {
					prev[i].pointer[i].span += node.pointer[i].span - 1;
					prev[i].pointer[i].next = node.pointer[i].next;
				} else {
					prev[i].pointer[i].span -= 1;
					break;
				}
			}
			while (currentMaxlevel > 1 && head.pointer[currentMaxlevel - 1].next == null)
				currentMaxlevel--;
			size--;
			return node.element;
		}
	}

	// Return the number of elements in the list
	public int size() {
		return size;
	}

	//Update the prev array for implementing other function
	private SkipListEntry<T>[] find(T x) {
		SkipListEntry<T>[] prev = new SkipListEntry[MAX_LEVEL];
		rank = new int[MAX_LEVEL];
		SkipListEntry<T> p = head;
		for (int i = currentMaxlevel - 1; i >= 0; i--) {

			if (i == currentMaxlevel - 1)
				rank[i] = 0;
			else
				rank[i] = rank[i + 1];

			while (p.pointer[i].next != null && p.pointer[i].next.element.compareTo(x) < 0) {
				rank[i] += p.pointer[i].span;
				p = p.pointer[i].next;
			}
			prev[i] = p;
		}
		return prev;
	}

	//Random creation of level of skipListEntry
	private int chooseLevel() {
		int level = 0;
		Random random = new Random();
		int randomNum = random.nextInt();
		level = Integer.numberOfTrailingZeros(randomNum & getMask());
		if (level > MAX_LEVEL)
			return MAX_LEVEL + 1;
		else
			return level;
	}

	//Helper function for chooseLevel for generating mask
	int getMask() {
		int mask = (1 << MAX_LEVEL - 1) - 1;
		// System.out.println("mask :"+ mask);
		return mask;
	}

	//Print the skip list with span information
	public void printListSpan() {
		SkipListEntry<T> current = head;
		while(current.pointer[0].next != null) {
			printSpan(current);
			current = current.pointer[0].next;
		}
			
	}
	//Helper function for printListSpan() :Print the span information
	void printSpan(SkipListEntry current) {
		System.out.print(current.element + ":");
		for (int i = 0; i < current.pointer.length; i++) {
			System.out.print(current.pointer[i].span+" ");
			//System.out.print("[]");
		}
		System.out.println();
	}

	//Skip list Iterator class
	public class SkipListIterator<T extends Comparable<T>> implements Iterator<T> {
		SkipList<T> list;
		SkipList<T>.SkipListEntry<T> current;

		public SkipListIterator(SkipList<T> list) {
			this.list = list;
			this.current = list.head;
		}

		@Override
		public boolean hasNext() {
			return current.pointer[0].next != null;
		}

		@Override
		public T next() {
			current = current.pointer[0].next;
			return current.element;
		}
	}

	
	//To print the skiplist
	public String toString() {
		Iterator<T> itr = iterator();
		StringBuilder s = new StringBuilder("[");
		while (itr.hasNext()) {
			s.append(itr.next());
			if (itr.hasNext())
				s.append(",");
		}
		s.append(']');
		return s.toString();
	}
	
	

	//To Print the array
	public void printArray(int[] arr) {
		if (arr == null)
			System.out.println("Array is empty.");
		for (int i = 0; i < arr.length; i++)
			System.out.print(arr[i] + " ");
		System.out.println();
	}
}
