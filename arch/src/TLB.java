import java.util.LinkedList;

public class TLB {
	final int size = 1024;

	public class Row {
		long va, pa;
		boolean dirty, ref;

		Row() {
			va = pa = 0;
			dirty = ref = false;
		}
	}

	public class PageTableLevel2 {
		Row table[] = new Row[size];

		public PageTableLevel2() {
			for (int i = 0; i < size; i++)
				table[i] = new Row();
		}
	}

	PageTableLevel2 pt[] = new PageTableLevel2[size];
	Row Buffer[] = new Row[size];
	LinkedList<Row> LRUCalculator = new LinkedList<Row>();

	TLB() {
		for (int i = 0; i < size; i++) {
			Buffer[i] = new Row();
			pt[i] = new PageTableLevel2();
		}
	}

	public int writeToTLB(long add) {
		for (int i = 0; i < size; i++)
			if (add == Buffer[i].va) {
				System.out.println("TLB Hit");
				LRUCalculator.addFirst(LRUCalculator.remove(LRUCalculator
						.indexOf(Buffer[i])));
				return 0;
			}
		System.out.println("TLB Miss");
		int pt1 = (int) (add & 0xFFC00000) >>> 22;
		int pt2 = (int) (add & 0x3FF000) >>> 12;
		if (pt[pt1] == null) { // serves purpose of valid bit
			int hardDiskAccessTime = 0;
			System.out.println("Page Fault!!");
			return hardDiskAccessTime;
		}
		System.out.println("Page Table Hit");
		Row found = pt[pt1].table[pt2];
		found.dirty = true;
		if (LRUCalculator.size() == size) {
			Row del = LRUCalculator.removeLast();
			for (int i = 0; i < size; i++)
				if (del == Buffer[i]) {
					Buffer[i] = found;
					break;
				}
		}
		LRUCalculator.addFirst(found);
		found.ref = true;
		return 30;
	}

	public int readFromTLB(long add) {
		for (int i = 0; i < size; i++)
			if (add == Buffer[i].va) {
				System.out.println("TLB Hit");
				LRUCalculator.addFirst(LRUCalculator.remove(LRUCalculator
						.indexOf(Buffer[i])));
				return 0;
			}
		System.out.println("TLB Miss");
		int pt1 = (int) (add & 0xFFC00000) >>> 22;
		int pt2 = (int) (add & 0x3FF000) >>> 12;
		if (pt[pt1] == null) { // serves purpose of valid bit
			int hardDiskAccessTime = 0;
			System.out.println("Page Fault!!");
			return hardDiskAccessTime;
		}
		System.out.println("Page Table Hit");
		Row found = pt[pt1].table[pt2];
		LRUCalculator.addFirst(found);
		found.ref = true;
		return 30;
	}
}
