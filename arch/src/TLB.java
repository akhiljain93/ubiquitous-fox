import java.util.LinkedList;

public class TLB {

	public class Row {
		long va, pa;
		boolean dirty, ref;

		Row() {
			va = pa = 0;
			dirty = ref = false;
		}
	}

	public class PageTableLevel2 {
		Row table[] = new Row[1024];
		int vaPref;

		public PageTableLevel2(int va) {
			vaPref = va << 10;
			for (int i = 0; i < 1024; i++)	{
				table[i] = new Row();
				table[i].va = vaPref|i;
			}
		}
	}

	PageTableLevel2 pt[] = new PageTableLevel2[1024];
	Row Buffer[] = new Row[1024];
	LinkedList<Row> LRUCalculator = new LinkedList<Row>();
	int hardDiskAccessTime = 0;
	public int accesses, misses;
	
	TLB() {
		for (int i = 0; i < 1024; i++)
			pt[i] = new PageTableLevel2(i);
	}

	public int access(long add, boolean write) {
		add >>>= 12;
		accesses++;
		for (int i = 0; i < 1024; i++)
			if (Buffer[i] != null && add == Buffer[i].va) {
				// TLB Hit
				Buffer[i].dirty |= write;
				LRUCalculator.addFirst(LRUCalculator.remove(LRUCalculator.indexOf(Buffer[i])));
				return 0;
			}
		
		misses++;
		// TLB Miss
		int pt1 = (int) ((add & 0xFFC00) >>> 10);
		int pt2 = (int) (add & 0x3FF);
		
		// validity : page fault
		if (pt[pt1] == null)
			return hardDiskAccessTime;
		
		Row found = pt[pt1].table[pt2];
		if(found == null)
			return hardDiskAccessTime;
		
		// Page Table Hit
		found.dirty |= write;
		Row del = null;
		if (LRUCalculator.size() == 1024) {
			del = LRUCalculator.removeLast();
			for (int i = 0; i < 1024; i++)
				if (del == Buffer[i]) {
					Buffer[i] = found;
					break;
				}
		}
		else	{
			for(int i = 0; i < 1024; ++i)
				if(Buffer[i] == null)	{
					Buffer[i] = found;
					break;
				}
		}
		
		LRUCalculator.addFirst(found);
		found.ref = true;
		
		// write-back evicted value
		if(del != null)
			del.ref = false;
		
		return 30;
	}

}
