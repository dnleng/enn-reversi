
public class Match extends Thread
{
	Logic logic;
	
	public Match(Logic log)
	{
		this.logic = log;
		this.setPriority(MAX_PRIORITY);
	}
	
	public void run()
	{
		logic.run = true;
		logic.newGame(AI.BOTPLY);
	}
}
