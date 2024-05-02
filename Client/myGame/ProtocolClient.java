package myGame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.joml.*;
import org.joml.Matrix4f;

import tage.GameObject;
import tage.networking.client.GameConnectionClient;


public class ProtocolClient extends GameConnectionClient
{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	private GhostNPC ghostNPC;
	
	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException 
	{	super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
		//startNPC();
	}
	
	public UUID getID() { return id; }
	
	@Override
	protected void processPacket(Object message)
	{	String strMessage = (String)message;
		System.out.println("message received -->" + strMessage);
		String[] messageTokens = strMessage.split(",");
		
		// Game specific protocol to handle the message
		if(messageTokens.length > 0)
		{
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
			if(messageTokens[0].compareTo("join") == 0)
			{	if(messageTokens[1].compareTo("success") == 0)
				{	System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				}
				if(messageTokens[1].compareTo("failure") == 0)
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
			}	}
			
			// Handle BYE message
			// Format: (bye,remoteId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}
			
			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0))
			{	// create a new ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));

				try
				{	ghostManager.createGhostAvatar(ghostID, ghostPosition);
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
			}
			
			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0)
			{
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			}
			
			// Handle MOVE message
			// Format: (move,remoteId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0)
			{
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
				
				ghostManager.updateGhostAvatar(ghostID, ghostPosition);
			}

			if (messageTokens[0].compareTo("turn") == 0)
			{
				// turns a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				// Parse out the position into a Vector3f
				Matrix4f rotation = new Matrix4f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]),
					Float.parseFloat(messageTokens[5]),
					Float.parseFloat(messageTokens[6]),
					Float.parseFloat(messageTokens[7]),
					Float.parseFloat(messageTokens[8]),
					Float.parseFloat(messageTokens[9]),
					Float.parseFloat(messageTokens[10]),
					Float.parseFloat(messageTokens[11]),
					Float.parseFloat(messageTokens[12]),
					Float.parseFloat(messageTokens[13]),
					Float.parseFloat(messageTokens[14]),
					Float.parseFloat(messageTokens[15]),
					Float.parseFloat(messageTokens[16]),
					Float.parseFloat(messageTokens[17]));

				ghostManager.updateGhostAvatarRotation(ghostID, rotation);
			}

			if (messageTokens[0].compareTo("createNPC") == 0)
			{
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
				try
				{
					createGhostNPC(ghostPosition);
				} catch (Exception e) {
					System.out.println("could not create ghostNPC");
					e.printStackTrace();
				}
			}


			if (messageTokens[0].compareTo("isnear") == 0)
			{
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
			}

			if (messageTokens[0].compareTo("NPCinfo") == 0)
			{
				Vector3f NPCPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
				float gsize = Float.parseFloat(messageTokens[4]);
				updateGhostNPC(NPCPosition, gsize);
			}

			/*if (messageTokens[0].compareTo("mNPC") == 0)
			{
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
				createGhostNPC(ghostPosition);
				System.out.println("npc new created");
			}*/
		}
	}
	
	// The initial message from the game client requesting to join the 
	// server. localId is a unique identifier for the client. Recommend 
	// a random UUID.
	// Message Format: (join,localId)
	
	public void sendJoinMessage()
	{	try 
		{	sendPacket(new String("join," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the client is leaving the server. 
	// Message Format: (bye,localId)

	public void sendByeMessage()
	{	try 
		{	sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the client�s Avatar�s position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position)
	{	try 
		{	String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the local avatar's position. The server then 
	// forwards this message to the client with the ID value matching remoteId. 
	// This message is generated in response to receiving a WANTS_DETAILS message 
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position)
	{	try 
		{	String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the local avatar has changed position.  
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessage(Vector3f position)
	{	try 
		{	String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	public void sendRotationMessage(Matrix4f rotation)
	{
		try {
			String message = new String("turn," + id.toString());
			message += "," + rotation.m00();
			message += "," + rotation.m01();
			message += "," + rotation.m02();
			message += "," + rotation.m03();
			message += "," + rotation.m10();
			message += "," + rotation.m11();
			message += "," + rotation.m12();
			message += "," + rotation.m13();
			message += "," + rotation.m20();
			message += "," + rotation.m21();
			message += "," + rotation.m22();
			message += "," + rotation.m23();
			message += "," + rotation.m30();
			message += "," + rotation.m31();
			message += "," + rotation.m32();
			message += "," + rotation.m33();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ------------- GHOST NPC SECTION --------------
	private void createGhostNPC(Vector3f position)
	{
		try
		{
			if (ghostNPC == null)
			{
				ghostNPC = new GhostNPC(0, game.getNPCshape(), game.getNPCtexture(), position);
				Matrix4f initialTranslation = (new Matrix4f()).translation(-1f, 0f, 1f);
				ghostNPC.setLocalTranslation(initialTranslation);
				ghostNPC.getRenderStates().setModelOrientationCorrection(
					(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
				Matrix4f initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
				ghostNPC.setLocalRotation(initialRotation);
				Matrix4f initialScale = (new Matrix4f()).scaling(0.2f);
				ghostNPC.setLocalScale(initialScale);

				Vector3f loc = ghostNPC.getWorldLocation();
				GameObject terr = game.getTerr();
				float height = terr.getHeight(loc.x(), loc.z());
				ghostNPC.setLocalLocation(new Vector3f(loc.x(), height + 0.75f, loc.z()));
			}


		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	private void updateGhostNPC(Vector3f position, double gsize)
	{
		boolean gs;
		if (ghostNPC == null) {
			try {
				createGhostNPC(position);
			} catch (Exception e) {
				System.out.println("error creating npc");
			}
		}
		Vector3f tempPos = position;
		GameObject terr = game.getTerr();
		float height = terr.getHeight(tempPos.x(), tempPos.z());
		position.add(0, height + 0.75f, 0);
		ghostNPC.setPosition(position);
		//if (gsize == 0.02f) gs=false; else gs=true;
		//ghostNPC.setSize(gs);
	}


	// more additions to the network protocol to handle ghosts:

}
