package myGame;

import tage.*;
import tage.audio.*;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
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

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter = 0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject tor, avatar, x, y, z, terr, weopon, dol1, dol2, plane, gernade;
	private ObjShape torS, ghostS, avaS, linxS, linyS, linzS, terrS, dolS, wepS, gernadeS;
	private TextureImage avaT, ghostT, hills, grass, dolT, wepT, gernadeT;
	private AnimatedShape robS;
	private ObjShape npcShape;
	private TextureImage npcTex;


	private Light light;
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, caps2P, planeP, gernadeP, explosionP;
	private boolean running = false;
	private float vals[] = new float[16];


	private int fluffyClouds, lakeIslands;

	private CameraOrbit3D cameraOrbit3D;


	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	private boolean throwGernade = false;
	private Timer timer;

	private IAudioManager audioMgr;
	private Sound explosion, desertSound, bounce;

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		dolS = new ImportedModel("dolphinLowPoly.obj");
		wepS = new ImportedModel("weopon.obj");
		ghostS = new ImportedModel("human1.2.obj");
		avaS = new ImportedModel("human1.2.obj");
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));

		robS = new AnimatedShape("robot.rkm", "robot.rks");
		robS.loadAnimation("WAVE", "robotWave.rka");
		robS.loadAnimation("WALK", "robotWalk.rka");

		npcShape = new ImportedModel("human1.2.obj");

		gernadeS = new Sphere();

		terrS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		ghostT = new TextureImage("human1.2color.png");
		avaT = new TextureImage("human1.2color.png");
		dolT = new TextureImage("Dolphin_HighPolyUV.png");
		wepT = new TextureImage("weopon.jpg");
		hills = new TextureImage("heightmap1.jpg");
		grass = new TextureImage("sand.png");

		gernadeT = new TextureImage("dirt.png");

		npcTex = new TextureImage("dirt.png");

	}

	@Override
	public void loadSounds()
	{ AudioResource resource1, resource2, resource3;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("assets/sounds/explode.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/sounds/desert.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("assets/sounds/bounce.wav", AudioResourceType.AUDIO_SAMPLE);
		explosion = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
		desertSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		bounce = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		explosion.initialize(audioMgr);
		desertSound.initialize(audioMgr);
		bounce.initialize(audioMgr);
		explosion.setMaxDistance(10.0f);
		explosion.setMinDistance(0.5f);
		explosion.setRollOff(5.0f);
		desertSound.setMaxDistance(10.0f);
		desertSound.setMinDistance(0.5f);
		desertSound.setRollOff(5.0f);

		bounce.setMaxDistance(10.0f);
		bounce.setMinDistance(0.5f);
		bounce.setRollOff(5.0f);
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep;

		// build avatar
		avatar = new GameObject(GameObject.root(), robS, avaT);
		initialTranslation = (new Matrix4f()).translation(-1f, 0f, 1f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.getRenderStates().setModelOrientationCorrection(
			(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
		initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
		avatar.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.2f);
		avatar.setLocalScale(initialScale);

		avatar.getRenderStates().hasLighting(true);
		avatar.getRenderStates().isEnvironmentMapped(true);

		//build avatar weopon
		weopon = new GameObject(GameObject.root(), wepS, wepT);
		initialTranslationWep = (new Matrix4f()).translation(-0.35f, 0.1f, 0.6f);
		//weopon.setLocalTranslation(initialTranslationWep);
		weopon.getRenderStates().setModelOrientationCorrection(
			(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
		initialRotationWep = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
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
		//dol1 = new GameObject(GameObject.root(), dolS, dolT);
		//initialTranslation = (new Matrix4f()).translation(1, 0, 0);
		//dol1.setLocalTranslation(initialTranslation);
		//initialScale = (new Matrix4f()).scaling(0.25f);
		//dol1.setLocalScale(initialScale);

		// add X,Y,-Z axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		terr = new GameObject(GameObject.root(), terrS, grass);
		initialTranslation = (new Matrix4f()).translation(0f, 0f, 0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);
		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(.5f, .5f, .5f);

		light = new Light();
		light.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(light);
	}

	@Override
	public void initializeGame() {
		prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ----------------- initialize camera ----------------
		positionCameraBehindAvatar();

		// ----------------- INPUTS SECTION -----------------------------
		setupNetworking();
		im = engine.getInputManager();

		// build some action objects for doing things in response to user input
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
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


		// --- initialize physics system ---
		float[] gravity = {0f, -9.8f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		// --- create physics world ---
		float mass = 1.0f;
		float up[] = {0, 1, 0};
		float radius = 0.75f;
		float size[] = {1, 1, 1};
		float height = 0.0f;
		double[] tempTransform;
		Matrix4f translation = new Matrix4f();
		//tempTransform = toDoubleArray(translation.get(vals));
		//caps1P = (engine.getSceneGraph()).addPhysicsCapsuleX(mass, tempTransform, radius, height);
		//caps1P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		//caps1P.setBounciness(0.5f);
		//caps1P.setFriction(5f);
		//float velocity[] = {1, 0, 1};
		//caps1P.setLinearVelocity(velocity);
		//dol1.setPhysicsObject(caps1P);

		//translation = new Matrix4f(weopon.getLocalTranslation());
		//tempTransform = toDoubleArray(translation.get(vals));
		//caps2P = (engine.getSceneGraph()).addPhysicsCapsuleX(mass, tempTransform, radius, height);
		//caps2P.setBounciness(0.1f);
		//caps2P.setFriction(3f);
		//weopon.setPhysicsObject(caps2P);

		translation = new Matrix4f(terr.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		planeP.setBounciness(0.5f);
		terr.setPhysicsObject(planeP);
		engine.enableGraphicsWorldRender();
		engine.enablePhysicsWorldRender();

		// initial sound settings
		desertSound.setLocation(avatar.getWorldLocation());
		setEarParameters();
		desertSound.play();


		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	public GameObject getAvatar() {
		return avatar;
	}

	@Override
	public void update() {
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		amt = elapsedTime * 0.03;
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		// update sound
		desertSound.setLocation(avatar.getWorldLocation());
		setEarParameters();


		// build and set HUD
		int elapsTimeSec = Math.round((float) (System.currentTimeMillis() - startTime) / 1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "camera position = "
			+ (c.getLocation()).x()
			+ ", " + (c.getLocation()).y()
			+ ", " + (c.getLocation()).z();
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(1, 1, 1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		// update inputs and camera
		im.update((float) elapsedTime);
		cameraOrbit3D.updateCameraPosition();
		//positionCameraBehindAvatar();
		processNetworking((float) elapsedTime);

		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		robS.updateAnimation();
		avatar.setLocalLocation(new Vector3f(loc.x(), height + 0.75f, loc.z()));

		Matrix4f currentTranslation, currentRotation;
		double totalTime = System.currentTimeMillis() - startTime;
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		amt = elapsedTime * 0.03;
		double amtt = totalTime * 0.001;
		if (true) {
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float) amtt);
			for (GameObject go : engine.getSceneGraph().getGameObjects()) {
				if (go.getPhysicsObject() != null) { // set translation
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3, 0, mat.m30());
					mat2.set(3, 1, mat.m31());
					mat2.set(3, 2, mat.m32());
					go.setLocalTranslation(mat2);
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
				}
			}
		}

		if(throwGernade & gernadeP == null)
		{
			gernade = new GameObject(GameObject.root(), gernadeS, gernadeT);
			//initialTranslation = (new Matrix4f()).translation(1, 0, 0);
			Vector3f location = avatar.getWorldLocation();
			gernade.setLocalLocation(location);

			//dol1.setLocalTranslation(initialTranslation);
			initialScale = (new Matrix4f()).scaling(0.10f);
			gernade.setLocalScale(initialScale);
			float mass = 1.0f;
			float up[] = {0, 1, 0};
			float radius = 0.10f;
			float size[] = {1, 1, 1};
			float y = 0.0f;
			double[] tempTransform;
			Matrix4f translation = new Matrix4f(gernade.getLocalTranslation());
			tempTransform = toDoubleArray(translation.get(vals));
			gernadeP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
			//caps1P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
			gernadeP.setBounciness(1f);
			gernadeP.setFriction(30f);
			Vector3f where = avatar.getLocalForwardVector();
			float velocity[] = new float[3];
			velocity[0] = where.x()*3;
			velocity[1] = 5f;
			velocity[2] = where.z()*3;
			gernadeP.setLinearVelocity(velocity);
			gernade.setPhysicsObject(gernadeP);
		}

		if(throwGernade)
		{
			float capVel[] = gernadeP.getLinearVelocity();
			System.out.println(gernade.getLocalLocation().y());
			if(gernade.getLocalLocation().y() < 0.13)
			{
				bounce.setLocation(gernade.getWorldLocation());
				setEarParameters();
				bounce.play();
			}
			if(Math.sqrt(Math.pow(capVel[0], 2) +  Math.pow(capVel[2], 2)) < 1.25f)
			{
				float mass = 1.0f;
				float radius = 0.90f;
				double[] tempTransform;
				Matrix4f translation = new Matrix4f(gernade.getLocalTranslation());
				tempTransform = toDoubleArray(translation.get(vals));
				explosionP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
				//caps1P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
				explosionP.setBounciness(0f);
				explosionP.setFriction(30f);
				bounce.stop();
				gernade.setPhysicsObject(explosionP);
				explosion.setLocation(gernade.getWorldLocation());
				setEarParameters();
				explosion.play();
				engine.getSceneGraph().removePhysicsObject(gernadeP);
				timer = new Timer();
				timer.schedule(
					new TimerTask()
					{
						@Override
						public void run()
						{
							engine.getSceneGraph().removePhysicsObject(explosionP);
							engine.getSceneGraph().removeGameObject(gernade);
							gernadeP = null;
							explosionP = null;
							gernade = null;
						}
					}, 3000);
				throwGernade = false;
			}
		}


	}

	private void positionCameraBehindAvatar() {
		Vector4f u = new Vector4f(-1f, 0f, 0f, 1f);
		Vector4f v = new Vector4f(0f, 1f, 0f, 1f);
		Vector4f n = new Vector4f(0f, 0f, 1f, 1f);
		u.mul(avatar.getWorldRotation());
		v.mul(avatar.getWorldRotation());
		n.mul(avatar.getWorldRotation());
		Matrix4f w = avatar.getWorldTranslation();
		Vector3f position = new Vector3f(w.m30(), w.m31(), w.m32());
		position.add(-n.x() * 2f, -n.y() * 2f, -n.z() * 2f);
		position.add(v.x() * .75f, v.y() * .75f, v.z() * .75f);
		Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		c.setLocation(position);
		c.setU(new Vector3f(u.x(), u.y(), u.z()));
		c.setV(new Vector3f(v.x(), v.y(), v.z()));
		c.setN(new Vector3f(n.x(), n.y(), n.z()));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W: {
				robS.stopAnimation();
				robS.playAnimation("WALK", 0.5f,
					AnimatedShape.EndType.LOOP, 0);
				break;
			}
			case KeyEvent.VK_SPACE: {
				System.out.println("starting physics");
				running = true;
				break;
			}

			case KeyEvent.VK_G: {
				throwGernade = true;
				break;
			}
			default:
				;
			{
				robS.stopAnimation();
				break;
			}
		}
		super.keyPressed(e);
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public GhostManager getGhostManager() {
		return gm;
	}

	public Engine getEngine() {
		return engine;
	}

	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (protClient == null) {
			System.out.println("missing protocol host");
		} else {        // Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	protected void processNetworking(float elapsTime) {        // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() {
		return avatar.getWorldLocation();
	}

	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	public ObjShape getNPCshape() { return npcShape; }
	public TextureImage getNPCtexture() { return npcTex; }

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}

	@Override
	public void loadSkyBoxes() {
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}


	public GameObject getDol() {
		return avatar;
	}

	private float[] toFloatArray(double[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}

	private double[] toDoubleArray(float[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}

	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		dynamicsWorld = ((JBulletPhysicsEngine) physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i = 0; i < manifoldCount; i++) {
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++) {
				contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f) {
					//System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				}
			}
		}
	}

	public void setEarParameters()
	{ Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	public GameObject getTerr()
	{
		return terr;
	}
}