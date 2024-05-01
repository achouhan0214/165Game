import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer 
{
	private GameAIServerUDP  gameAIServerUDP;
	private GameServerUDP thisUDPServer;
	private GameServerTCP thisTCPServer;
	private NPCcontroller npcCtrl;

	public NetworkingServer(int serverPort, String protocol) 
	{
		npcCtrl = new NPCcontroller();
		try
		{

			gameAIServerUDP =  new GameAIServerUDP(serverPort, npcCtrl);
			//thisUDPServer = new GameServerUDP(serverPort);

			/*if(protocol.toUpperCase().compareTo("TCP") == 0)
			{
				thisTCPServer = new GameServerTCP(serverPort);
			}
			else
			{

			}*/
			npcCtrl.start(gameAIServerUDP);
		}
		catch (IOException e) 
		{	e.printStackTrace();
		}

	}

	public static void main(String[] args) 
	{	if(args.length > 1)
		{	NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
	}

}
