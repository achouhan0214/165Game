import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer 
{
	private GameServerUDP  gameServerUDP;


	public NetworkingServer(int serverPort)
	{
		try
		{

			gameServerUDP =  new GameServerUDP(serverPort);

		}
		catch (IOException e) 
		{	e.printStackTrace();
		}

	}

	public static void main(String[] args) 
	{	if(args.length > 1)
		{	NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]));
		}
	}

}
