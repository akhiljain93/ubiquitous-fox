public class Cache
{
	public class Set
	{
		int assoc;
		int[] arr;
		int tagLength;
		public Set(int assoc, int tag)
		{
			this.assoc = assoc;
			arr = new int[assoc];
			tagLength = tag;
		}
		public boolean matchTag(int Tag)
		{
			for (int i=0; i<assoc; i++)
				if (arr[i] % (1<<assoc) == Tag)
					return true;
			return false;
		}	
	}
	public class L1
	{
		Set[] L1 = new Set[512];
		public L1()
		{
			for (int i=0; i<512; i++)
				L1[i] = new Set(2, 18);
		}
		public boolean searchL1(int Tag, int index)
		{
			return L1[index].matchTag(Tag);
		}
		public void goToL2(int Tag, int index)
		{
			if (!searchL1(Tag, index))
				
		}
	}	
	public class L2
	{
		Set[] L2 = new Set[2048];
		public L2()
		{
			for (int i=0; i<2048; i++)
				L2[i] = new Set(8, 14);
		}
		public boolean searchL2(int Tag, int index)
		{
			return L2[index].matchTag(Tag);
		}
		
	}
}
