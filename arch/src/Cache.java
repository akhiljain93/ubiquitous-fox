public class Cache
{
	public class Set
	{
		int assoc;
		Integer[] arr;
		boolean fifo;
		
		public Set(int assoc, boolean fifo)
		{
			this.assoc = assoc;
			arr = new Integer[assoc];
			this.fifo = fifo;
		}
		
		public boolean matchTag(int tag)
		{
			for (int i=0; i<assoc; i++)
				if (arr[i] != null && (arr[i] >> 1) == tag)	{
					if(!fifo)	{
						Integer a = arr[i];
						for(int j = i-1; j >= 0; --j)
							arr[j+1] = arr[j];
						arr[0] = a;
					}
					return true;
				}
			return false;
		}
		
		public boolean replace(int tag)	{
			if(matchTag(tag))
				return false;
			boolean modified = (arr[assoc-1] & 1) == 1;
			for(int j = assoc-2; j >= 0; --j)
				arr[j+1] = arr[j];
			arr[0] = tag << 1;
			return modified;
		}
	}
	public class L1
	{
		Set[] L1 = new Set[512];
		public L1()
		{
			for (int i=0; i<512; i++)
				L1[i] = new Set(2, true);
		}
		public boolean searchL1(int Tag)
		{
			Tag>>=5;
			int index=Tag%512;
			Tag>>=9;
			return L1[index].matchTag(Tag);
		}
		
	}	
	public class L2
	{
		Set[] L2 = new Set[2048];
		public L2()
		{
			for (int i=0; i<2048; i++)
				L2[i] = new Set(8, false);
		}
		public boolean searchL2(int Tag)
		{
			Tag>>=7;
			int index=Tag%2048;
			Tag>>=11;
			return L2[index].matchTag(Tag);
		}
		
	}
}
