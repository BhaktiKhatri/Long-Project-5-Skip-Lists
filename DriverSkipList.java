/**
* driver function for skiplist
* @author 	Lopamudra 
*  			Bhakti Khatri			
* 			Gautam Gunda 			
* 			Sangeeta Kadambala
*/
package cs6301.g45;

public class DriverSkipList {
	public static void main(String[] args) {
		SkipList<Integer> s = new SkipList<>();
		s.add(2);
		s.add(16);
		s.add(12);
		s.add(77);
		s.add(17);
		s.add(99);
		s.add(33);
		s.add(54);
		s.add(34);
		s.add(24);
		s.add(14);
		s.add(19);
		s.add(94);
		s.add(-65);
		s.add(55);
		System.out.println(s);
		s.printListSpan();

		System.out.println("ceiling :" + s.ceiling(-20));
		System.out.println("contains :" + s.contains(94));
		System.out.println("first element : "+ s.first());
		System.out.println("floor :" + s.floor(-4));
		System.out.println("isEmpty : "+ s.isEmpty());
		System.out.println("size :"+ s.size());
		System.out.println("Last element :" + s.last());
		int index = 10;
		System.out.println("element at index "+ index+ " :" + s.get(index));
		s.remove(12);
		s.rebuild();
		System.out.println("-----------After rebuild----------");
		System.out.println(s);
		s.printListSpan();
	}
}
