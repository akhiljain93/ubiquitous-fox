
public class TLB
{
	public class PageTableLevel
	{
		long va[] = new long[1024];
		long pa[] = new long[1024];
		boolean dirty[] = new boolean[1024];
		boolean valid[] = new boolean[1024];
		boolean ref[] = new boolean[1024];
		PageTableLevel next[] = new PageTableLevel[1024];
	}
	long va[] = new long[1024];
	long pa[] = new long[1024];
	boolean dirty[] = new boolean[1024];
	boolean valid[] = new boolean[1024];
	public void writeToTLB(long add)
	{
		
	}
}
