package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.input.action.FwdAction;
import tage.input.action.PitchAction;
import tage.input.action.TurnAction;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;

import java.net.UnknownHostException;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter=0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject tor, avatar, x, y, z, terr, weopon;
	private ObjShape torS, ghostS, avaS, linxS, linyS, linzS, terrS, dolS, wepS;
	private TextureImage avaT, ghostT, hills, grass, dolT, wepT;

	private Light light;


	private int fluffyClouds, lakeIslands;

	private CameraOrbit3D cameraOrbit3D;


	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{
		dolS = new ImportedModel("dolphinLowPoly.obj");
		wepS = new ImportedModel("weopon.obj");
		ghostS = new ImportedModel("human1.2.obj");
		avaS = new ImportedModel("human1.2.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		terrS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures()
	{
		ghostT = new TextureImage("human1.2color.png");
		avaT = new TextureImage("human1.2color.png");
		dolT = new TextureImage("Dolphin_HighPolyUV.png");
		wepT = new TextureImage("weopon.jpg");
		hills = new TextureImage("heightmap1.jpg");
		grass = new TextureImage("sand.png");

	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep;

		// build dolphin avatar
		avatar = new GameObject(GameObject.root(), avaS, avaT);
		initialTranslation = (new Matrix4f()).translation(-1f,0f,1f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.getRenderStates().setModelOrientationCorrection(
			(new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f)));
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		avatar.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.2f);
		avatar.setLocalScale(initialScale);

		//build avatar weopon
		weopon = new GameObject(GameObject.root(), wepS, wepT);
		initialTranslationWep = (new Matrix4f()).translation(-0.35f,0.1f,0.6f);
		//weopon.setLocalTranslation(initialTranslationWep);
		weopon.getRenderStates().setModelOrientationCorrection(
		(new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f)));
		initialRotationWep = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		//weopon.setLocalRotation(initialRotationWep);
		initialScaleWep = (new Matrix4f()).scaling(0.2f);
		//weopon.setLocalScale(initialScaleWep);
		weopon.setParent(avatar);
		//weopon.propagateTranslation(true);
		//weopon.propagateRotation(true);
		weopon.applyParentRotationToPosition(true);
		weopon.setLocalScale(initialScaleWep);
		//weopon.setLocalRotation(initialRotationWep);
		weopon.setLocalTranslation(initialTranslationWep);
		

		// build torus along X axis
		tor = new GameObject(GameObject.root(), dolS, dolT);
		initialTranslation = (new Matrix4f()).translation(1,0,0);
		tor.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.25f);
		tor.setLocalScale(initialScale);

		// add X,Y,-Z axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f));
		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f));
		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f));

		terr = new GameObject(GameObject.root(), terrS, grass);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);
		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(.5f, .5f, .5f);

		light = new Light();
		light.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(light);
	}

	@Override
	public void initializeGame()
	{	prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ----------------- initialize camera ----------------
		positionCameraBehindAvatar();

		// ----------------- INPUTS SECTION -----------------------------
		im = engine.getInputManager();

		// build some action objects for doing things in response to user input
		FwdAction fwdAction = new FwdAction(this);
		TurnAction turnAction = new TurnAction(this);
		PitchAction pitchAction = new PitchAction(this);

		String gpName = im.getFirstGamepadName();
		Camera c = (engine.getRenderSystem())
			.getViewport("MAIN").getCamera();
		cameraOrbit3D = new CameraOrbit3D(
			c, avatar, gpName, engine);

		//keyboard inputs
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.W,
			fwdAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.S,
			fwdAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.A,
			turnAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.D,
			turnAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);



		//controller input
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Axis.Y,
			fwdAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Axis.X,
			turnAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		setupNetworking();
		
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	public GameObject getAvatar() { return avatar; }

	@Override
	public void update()
	{	elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		amt = elapsedTime * 0.03;
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		
		// build and set HUD
		int elapsTimeSec = Math.round((float)(System.currentTimeMillis()-startTime)/1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "camera position = "
			+ (c.getLocation()).x()
			+ ", " + (c.getLocation()).y()
			+ ", " + (c.getLocation()).z();
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(1,1,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		// update inputs and camera
		im.update((float)elapsedTime);
		cameraOrbit3D.updateCameraPosition();
		//positionCameraBehindAvatar();
		//processNetworking((float)elapsedTime);
		processNetworking(2f);

		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height+0.75f, loc.z()));
	}

	private void positionCameraBehindAvatar()
	{	Vector4f u = new Vector4f(-1f,0f,0f,1f);
		Vector4f v = new Vector4f(0f,1f,0f,1f);
		Vector4f n = new Vector4f(0f,0f,1f,1f);
		u.mul(avatar.getWorldRotation());
		v.mul(avatar.getWorldRotation());
		n.mul(avatar.getWorldRotation());
		Matrix4f w = avatar.getWorldTranslation();
		Vector3f position = new Vector3f(w.m30(), w.m31(), w.m32());
		position.add(-n.x()*2f, -n.y()*2f, -n.z()*2f);
		position.add(v.x()*.75f, v.y()*.75f, v.z()*.75f);
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		c.setLocation(position);
		c.setU(new Vector3f(u.x(),u.y(),u.z()));
		c.setV(new Vector3f(v.x(),v.y(),v.z()));
		c.setN(new Vector3f(n.x(),n.y(),n.z()));
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_W:
			{	Vector3f oldPosition = avatar.getWorldLocation();
				Vector4f fwdDirection = new Vector4f(0f,0f,1f,1f);
				fwdDirection.mul(avatar.getWorldRotation());
				fwdDirection.mul(0.05f);
				Vector3f newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
				avatar.setLocalLocation(newPosition);
				protClient.sendMoveMessage(avatar.getWorldLocation());
				break;
			}
			case KeyEvent.VK_D:
			{	Matrix4f oldRotation = new Matrix4f(avatar.getWorldRotation());
				Vector4f oldUp = new Vector4f(0f,1f,0f,1f).mul(oldRotation);
				Matrix4f rotAroundAvatarUp = new Matrix4f().rotation(-.01f, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
				Matrix4f newRotation = oldRotation;
				newRotation.mul(rotAroundAvatarUp);
				avatar.setLocalRotation(newRotation);
				break;
			}
		}
		super.keyPressed(e);
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	@Override
	public void loadSkyBoxes()
	{
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}


	public GameObject getDol()
	{
		return avatar;
	}
}