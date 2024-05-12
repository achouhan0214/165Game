package myGame;

import tage.*;
import tage.audio.*;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.Random;
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
	private Vector3f currentPosition, WorldRightVector, WorldUpVector, NewN, FinalFwd, FinalUp, Y_AXIS, X_AXIS;
	private Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep;
	private double startTime, prevTime, elapsedTime, amt;

	private GameObject avatar, x, y, z, terr, weopon, gernade, bullet, enemyBullet, enemyGarnade, aimsphere, smallaimsphere, locaimsphere;;
	private ObjShape ghostS, avaS, linxS, linyS, linzS, terrS, dolS, wepS, gernadeS, aimsphereS;
	private TextureImage avaT, ghostT, hills, grass, dolT, wepT, gernadeT,glockT;
	private AnimatedShape robS, glockS;
	private ObjShape npcShape;
	private TextureImage npcTex;


	private Light light, flash;
	private PhysicsEngine physicsEngine;
	private PhysicsObject planeP, gernadeP, explosionP, avatarP, npcP, bulletP, enemyBulletP,  enemyGarnadeP, enemyExplosionP;
	private boolean running = false;
	private float vals[] = new float[16];


	private int fluffyClouds, lakeIslands;

	private CameraOrbit3D cameraOrbit3D;

	private Robot robot; // these are additional variable declarations
	private float curMouseX, curMouseY, centerX, centerY;
	private float prevMouseX, prevMouseY; // loc of mouse prior to move
	private boolean isRecentering; //indicates the Robot is in action	


	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	private boolean throwGernade = false;
	private boolean shoot = true;
	private Timer timer;

	private IAudioManager audioMgr;
	private Sound explosion, desertSound, bounce, shootSound;


	private String avatarShape, avatarTexture;
	private String avaShapePath, dolShapePath;

	private String avaTexturePath, dolTexturePath;
	private int kills, deaths;

	private NPCController npcController;
	private boolean single = true;

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
		{
			this.serverProtocol = ProtocolType.TCP;
		}
		else
		{
			this.serverProtocol = ProtocolType.UDP;
			//single =false;
		}

	}

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		dolShapePath = "dolphinLowPoly.obj";
		dolS = new ImportedModel(dolShapePath);

		wepS = new ImportedModel("weopon.obj");
		ghostS = new ImportedModel("human1.2.obj");

		avaShapePath = "human1.2.obj";
		avaS = new ImportedModel(avaShapePath);
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));

		robS = new AnimatedShape("robot.rkm", "robot.rks");
		robS.loadAnimation("WAVE", "robotWave.rka");
		robS.loadAnimation("WALK", "robotWalk.rka");
		glockS = new AnimatedShape("glock2.rkm", "glock2.rks");
		glockS.loadAnimation("SHOOT", "glock2.rka");

		npcShape = new ImportedModel("human1.2.obj");

		gernadeS = new Sphere();
		aimsphereS = new Sphere();


		terrS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		ghostT = new TextureImage("human1.2color.png");

		avaTexturePath = "human1.2color.png";
		avaT = new TextureImage(avaTexturePath);

		dolTexturePath = "Dolphin_HighPolyUV.png";
		dolT = new TextureImage(dolTexturePath);

		wepT = new TextureImage("weopon.jpg");
		hills = new TextureImage("heightmap1.jpg");
		grass = new TextureImage("sand.png");

		gernadeT = new TextureImage("dirt.png");

		npcTex = new TextureImage("dirt.png");
		glockT = new TextureImage("weopon.jpg");
	}

	@Override
	public void loadSounds()
	{ AudioResource resource1, resource2, resource3, resource4;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("assets/sounds/explode.wav", AudioResourceType.AUDIO_SAMPLE);
		//resource2 = audioMgr.createAudioResource("assets/sounds/desert.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("assets/sounds/bounce.wav", AudioResourceType.AUDIO_SAMPLE);
		resource4 = audioMgr.createAudioResource("assets/sounds/shoot.wav", AudioResourceType.AUDIO_SAMPLE);
		explosion = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
		//desertSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		bounce = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		shootSound = new Sound(resource4, SoundType.SOUND_EFFECT, 100, false);
		explosion.initialize(audioMgr);
		shootSound.initialize(audioMgr);
		//desertSound.initialize(audioMgr);
		bounce.initialize(audioMgr);
		explosion.setMaxDistance(10.0f);
		explosion.setMinDistance(0.5f);
		explosion.setRollOff(0.2f);

		shootSound.setMaxDistance(5.0f);
		shootSound.setMinDistance(0.5f);
		shootSound.setRollOff(0.2f);

		bounce.setMaxDistance(10.0f);
		bounce.setMinDistance(0.5f);
		bounce.setRollOff(5.0f);
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialRotation, initialScale, initialTranslationWep, initialRotationWep, initialScaleWep,initialScaleAim,
		initialTranslationSmallAim, initialTranslationAim, initialScaleSmallAim;

		// build avatar
		avatarShape = avaShapePath;
		avatarTexture = avaTexturePath;

		avatar = new GameObject(GameObject.root(), avaS, avaT);
		initialTranslation = (new Matrix4f()).translation(-1f, 1f, 1f); 
		avatar.setLocalTranslation(initialTranslation);
		avatar.getRenderStates().setModelOrientationCorrection(
			(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
		initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
		avatar.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.2f);
		avatar.setLocalScale(initialScale);
		characterSelect("human");
		avatar.getRenderStates().hasLighting(true);
		//avatar.getRenderStates().isEnvironmentMapped(true);

		//build aimsphere
		aimsphere = new GameObject(GameObject.root(),aimsphereS, glockT);
		initialScaleAim = (new Matrix4f()).scaling(5f);
		aimsphere.setLocalScale(initialScaleAim);
		aimsphere.setParent(avatar);

		initialTranslationAim = (new Matrix4f()).translation(0f,0.5f, 0f);
		aimsphere.setLocalTranslation(initialTranslationAim);
		
		aimsphere.getRenderStates().setWireframe(true);
	    aimsphere.applyParentRotationToPosition(true);
		aimsphere.getRenderStates().disableRendering();

		//build avatar weopon
		weopon = new GameObject(GameObject.root(), glockS, glockT);
		initialTranslationWep = (new Matrix4f()).translation(-0.35f, 0.1f, 0.6f);
		weopon.getRenderStates().setModelOrientationCorrection(
		(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(270.0f)));
		initialRotationWep = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
		initialScaleWep = (new Matrix4f()).scaling(0.4f);
		weopon.setParent(avatar);
		weopon.applyParentRotationToPosition(true);
		weopon.setLocalScale(initialScaleWep);
		weopon.setLocalTranslation(initialTranslationWep);
		weopon.getRenderStates().hasLighting(true);
		
		

		//build smallaimsphere
		smallaimsphere = new GameObject(GameObject.root(),aimsphereS,glockT);
		initialScaleSmallAim = (new Matrix4f()).scaling(0.1f);
		smallaimsphere.setLocalScale(initialScaleSmallAim);
		initialTranslationSmallAim = (new Matrix4f()).translation(0f,0f, 10f);
		smallaimsphere.setLocalTranslation(initialTranslationSmallAim);
		smallaimsphere.setParent(aimsphere);
		smallaimsphere.applyParentRotationToPosition(true);
		smallaimsphere.getRenderStates().disableRendering();
		//smallaimsphere.propagateRotation(true);
		
		locaimsphere =	new GameObject(GameObject.root(),aimsphereS,glockT);
		locaimsphere.setLocalScale(initialScaleAim);
		locaimsphere.setParent(aimsphere);
		



		//build terrain
		
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

		flash = new Light();
		flash.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(flash);

	}

	@Override
	public void initializeGame() {
		prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ----------------- initialize camera ----------------
		positionCameraBehindAvatar();
		//initMouseMode();

		// ----------------- INPUTS SECTION -----------------------------
		setupNetworking();
		im = engine.getInputManager();

		// build some action objects for doing things in response to user input
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);

		ThrowGernade throwingGernade = new ThrowGernade(this, protClient);
		Shoot shoot = new Shoot(this, protClient);
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

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.G,
			throwingGernade, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.U,
			shoot, InputManager
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
			im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button.LEFT,
			turnAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button.RIGHT,
			turnAction, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._1,
			shoot, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._2,
			throwingGernade, InputManager
				.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				


		// --- initialize physics system ---
		float[] gravity = {0f, -9.8f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		// --- create physics world ---
		float mass = 1.0f;
		float up[] = {0, 1, 0};
		float radius = 0.75f;
		float height = 0.0f;
		double[] tempTransform;
		Matrix4f translation = new Matrix4f();
		translation = new Matrix4f(terr.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		planeP.setBounciness(0.5f);
		terr.setPhysicsObject(planeP);


		npcController = new NPCController();

		if(single)
		{
			npcController.start(5, GameObject.root(), ghostS, npcTex, terr);
		}


		//engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();



		// initial sound settings
		//desertSound.setLocation(avatar.getWorldLocation());
		//setEarParameters();
		//desertSound.play();
		kills = 0;
		deaths = 0;


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

		if(avatar == null)
		{
			java.util.Random rn = new Random();
			float x = 50-rn.nextFloat(100);
			float y = 50-rn.nextFloat(100);
			float z = 50-rn.nextFloat(100);
			avatar = new GameObject(GameObject.root(), avaS, avaT);
			Matrix4f initialTranslation = (new Matrix4f()).translation(x, 1f, z);
			avatar.setLocalTranslation(initialTranslation);
			avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
			Matrix4f initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
			avatar.setLocalRotation(initialRotation);
			initialScale = (new Matrix4f()).scaling(0.2f);
			avatar.setLocalScale(initialScale);

			weopon = new GameObject(GameObject.root(), wepS, wepT);
			Matrix4f initialTranslationWep = (new Matrix4f()).translation(-0.35f, 0.1f, 0.6f);
			weopon.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
			Matrix4f initialRotationWep = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
			Matrix4f initialScaleWep = (new Matrix4f()).scaling(0.2f);
			weopon.setParent(avatar);
			weopon.applyParentRotationToPosition(true);
			weopon.setLocalScale(initialScaleWep);
			weopon.setLocalTranslation(initialTranslationWep);
		}


		if(single)
		{
			npcController.npcLoop(avatar, GameObject.root(), ghostS, npcTex);
		}
		// update sound
		//desertSound.setLocation(avatar.getWorldLocation());
		//setEarParameters();


		// build and set HUD
		int elapsTimeSec = Math.round((float) (System.currentTimeMillis() - startTime) / 1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(kills);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = Integer.toString(deaths);
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(1, 1, 1);
		(engine.getHUDmanager()).setHUD1(counterStr, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		// update inputs and camera
		im.update((float) elapsedTime);
		cameraOrbit3D.updateCameraPosition();
		positionCameraBehindAvatar();
		//initMouseMode();
		processNetworking((float) elapsedTime);

		if(avatar != null)
		{
			Vector3f loc = avatar.getWorldLocation();
			float height = terr.getHeight(loc.x(), loc.z());
			robS.updateAnimation();
			glockS.updateAnimation();
			avatar.setLocalLocation(new Vector3f(loc.x(), height + 0.75f, loc.z()));
		}

		
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



		//multi
		if(!single)
		{
			if(enemyBullet != null && enemyBulletP != null)
			{
				if(avatar.getLocalLocation().equals(enemyBullet.getLocalLocation(), 1f)){
					if(avatarP == null){
						float mass = 1.0f;
						float size[] = {.5f, 1f, 0.5f};
						float radius = 0.10f;
						double[] tempTransform;
						Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
						tempTransform = toDoubleArray(translation.get(vals));
						avatarP = (engine.getSceneGraph()).addPhysicsCapsule(mass, tempTransform, radius, 1f);
						avatarP.setBounciness(0f);
						avatarP.setFriction(30f);
						float[] where = enemyBulletP.getLinearVelocity();
						float velocity[] = new float[3];
						velocity[0] = where[0]*0.0002f;
						velocity[1] = where[1]*0.0002f;
						velocity[2] = where[2]*0.0002f;
						avatarP.setLinearVelocity(velocity);
						avatar.setPhysicsObject(avatarP);
						timer = new Timer();
						timer.schedule(
							new TimerTask()
							{
								@Override
								public void run()
								{
									engine.getSceneGraph().removePhysicsObject(avatarP);
									engine.getSceneGraph().removeGameObject(weopon);
									engine.getSceneGraph().removeGameObject(avatar);
									weopon = null;
									deaths++;
									avatarP = null;
									avatar = null;

								}
							}, 300);
						protClient.sendDeathMessage();
					}
				}
			}

			if(enemyGarnadeP != null && planeP != null) {
				if (this.checkForCollisionsTwoPhysics(enemyGarnadeP, planeP)) {
					bounce.setLocation(enemyGarnade.getWorldLocation());
					setEarParameters();
					bounce.play();
				}
			}

			if(enemyGarnade != null) {
				float capVel[] = enemyGarnadeP.getLinearVelocity();
				if(Math.sqrt(Math.pow(capVel[0], 2) +  Math.pow(capVel[2], 2)) < 1.5f && enemyExplosionP == null)
				{
					float mass = 1.0f;
					float radius = 0.90f;
					double[] tempTransform;
					Matrix4f translation = new Matrix4f(enemyGarnade.getLocalTranslation());
					tempTransform = toDoubleArray(translation.get(vals));
					enemyExplosionP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
					//caps1P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
					enemyExplosionP.setBounciness(0f);
					enemyExplosionP.setFriction(30f);
					bounce.stop();
					enemyGarnade.setPhysicsObject(enemyExplosionP);
					explosion.setLocation(enemyGarnade.getWorldLocation());
					setEarParameters();
					explosion.play();
					engine.getSceneGraph().removePhysicsObject(enemyGarnadeP);
					timer = new Timer();
					timer.schedule(
						new TimerTask()
						{
							@Override
							public void run()
							{
								if(enemyGarnade.getLocalLocation().equals(avatar.getLocalLocation(), 0.9f)){
									if(avatarP == null) {
										float mass = 1.0f;
										float size[] = {0.5f, 1f, 0.5f};
										float radius = 0.10f;
										double[] tempTransform;
										Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
										tempTransform = toDoubleArray(translation.get(vals));
										avatarP = (engine.getSceneGraph()).addPhysicsCapsule(mass, tempTransform, radius, 1f);
										avatarP.setBounciness(0f);
										avatarP.setFriction(30f);
										Vector3f where = avatar.getLocalForwardVector();
										float velocity[] = new float[3];
										velocity[0] = -where.x() * 3;
										velocity[1] = -5f;
										velocity[2] = -where.z() * 3;
										avatarP.setLinearVelocity(velocity);
										avatar.setPhysicsObject(avatarP);
										engine.getSceneGraph().removePhysicsObject(avatarP);
										engine.getSceneGraph().removeGameObject(weopon);
										engine.getSceneGraph().removeGameObject(avatar);
										weopon = null;
										deaths++;
										avatarP = null;
										avatar = null;
										protClient.sendDeathMessage();
									}
								}
								engine.getSceneGraph().removePhysicsObject(enemyExplosionP);
								engine.getSceneGraph().removeGameObject(enemyGarnade);
								enemyGarnadeP = null;
								enemyExplosionP = null;
								enemyGarnade = null;
							}
						}, 300);
				}
			}



			if(enemyGarnade != null && avatar!= null && avatar.getLocalLocation().equals(enemyGarnade.getLocalLocation(), 1f) && enemyExplosionP == null)
			{
				float mass = 1.0f;
				float radius = 0.90f;
				double[] tempTransform;
				Matrix4f translation = new Matrix4f(enemyGarnade.getLocalTranslation());
				tempTransform = toDoubleArray(translation.get(vals));
				enemyExplosionP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
				enemyExplosionP.setBounciness(0f);
				enemyExplosionP.setFriction(30f);
				bounce.stop();
				enemyGarnade.setPhysicsObject(enemyExplosionP);
				explosion.setLocation(enemyGarnade.getWorldLocation());
				setEarParameters();
				explosion.play();
				engine.getSceneGraph().removePhysicsObject(enemyGarnadeP);

				if(avatarP == null) {
					mass = 1.0f;
					float size[] = {0.5f, 1f, 0.5f};
					radius = 0.10f;
					translation = new Matrix4f(avatar.getLocalTranslation());
					tempTransform = toDoubleArray(translation.get(vals));
					avatarP = (engine.getSceneGraph()).addPhysicsCapsule(mass, tempTransform, radius, 1f);
					avatarP.setBounciness(0f);
					avatarP.setFriction(30f);
					Vector3f where = avatar.getLocalForwardVector();
					float velocity[] = new float[3];
					velocity[0] = -where.x() * 3;
					velocity[1] = -5f;
					velocity[2] = -where.z() * 3;
					avatarP.setLinearVelocity(velocity);
					avatar.setPhysicsObject(avatarP);
					protClient.sendDeathMessage();
				}
				timer = new Timer();
				timer.schedule(
					new TimerTask()
					{
						@Override
						public void run()
						{
							engine.getSceneGraph().removePhysicsObject(avatarP);
							engine.getSceneGraph().removeGameObject(weopon);
							engine.getSceneGraph().removeGameObject(avatar);
							weopon = null;
							deaths++;
							avatarP = null;
							avatar = null;
							engine.getSceneGraph().removePhysicsObject(enemyExplosionP);
							engine.getSceneGraph().removeGameObject(enemyGarnade);
							enemyGarnadeP = null;
							enemyExplosionP = null;
							enemyGarnade = null;
							throwGernade = false;
						}
					}, 300);
			}
		}




		//single player
		if(single)
		{


			if(bullet != null && bulletP != null && !npcController.isEmpty() && npcP== null)
			{
				int who = npcController.gotHit(bullet.getLocalLocation(), 0.5f);
				if(who != -1){
					float mass = 100.0f;
					float size[] = {0.5f, 1f, 0.5f};
					float radius = 0.10f;
					double[] tempTransform;
					NPC temp = npcController.getNPC(who);
					Matrix4f translation = new Matrix4f(temp.getLocalTranslation());
					tempTransform = toDoubleArray(translation.get(vals));
					npcP = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
					npcP.setBounciness(0f);
					npcP.setFriction(30f);
					float[] where = bulletP.getLinearVelocity();
					float velocity[] = new float[3];
					velocity[0] = where[0]*0.0002f;
					velocity[1] = where[1]*0.0002f;
					velocity[2] = where[2]*0.0002f;
					npcP.setLinearVelocity(velocity);
					temp.setPhysicsObject(npcP);
					kills++;
					timer = new Timer();
					timer.schedule(
						new TimerTask()
						{
							@Override
							public void run()
							{
								if(npcP != null)
								{
									engine.getSceneGraph().removePhysicsObject(npcP);
									engine.getSceneGraph().removeGameObject(temp);
									npcP = null;
									npcController.setNPC(who, null);
								}

							}
						}, 1000);
				}
			}

			if(gernadeP != null && planeP != null) {
				if (this.checkForCollisionsTwoPhysics(gernadeP, planeP)) {
					bounce.setLocation(gernade.getWorldLocation());
					setEarParameters();
					bounce.play();
				}
			}


			if(throwGernade && gernade != null) {
				float capVel[] = gernadeP.getLinearVelocity();
				if(Math.sqrt(Math.pow(capVel[0], 2) +  Math.pow(capVel[2], 2)) < 1.5f && explosionP == null)
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
					flash.setLocation(gernade.getLocalLocation());
					flash.setRange(2f);
					flash.setAmbient(200,0,0);
					timer = new Timer();
					timer.schedule(
						new TimerTask()
						{
							@Override
							public void run()
							{
								int who = npcController.gotHit(gernade.getLocalLocation(), 2f);
								if(who != -1){
									float mass = 1.0f;
									float size[] = {0.5f, 1f, 0.5f};
									float radius = 0.10f;
									double[] tempTransform;
									NPC temp = npcController.getNPC(who);
									Matrix4f translation = new Matrix4f(temp.getLocalTranslation());
									tempTransform = toDoubleArray(translation.get(vals));
									npcP = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
									npcP.setBounciness(0f);
									npcP.setFriction(30f);
									float[] where = gernadeP.getLinearVelocity();
									float velocity[] = new float[3];
									velocity[0] = where[0]*0.0002f;
									velocity[1] = where[1]*0.0002f;
									velocity[2] = where[2]*0.0002f;
									npcP.setLinearVelocity(velocity);
									temp.setPhysicsObject(npcP);
									if(npcP != null)
									{
										engine.getSceneGraph().removePhysicsObject(npcP);
										engine.getSceneGraph().removeGameObject(temp);
										npcP = null;
										npcController.setNPC(who, null);
									}
									kills++;
								}
								engine.getSceneGraph().removePhysicsObject(explosionP);
								engine.getSceneGraph().removeGameObject(gernade);
								flash.setAmbient(0, 0, 0);
								flash.setDiffuse(0, 0, 0);
								flash.setSpecular(0, 0, 0);
								gernadeP = null;
								explosionP = null;
								gernade = null;
								throwGernade = false;
							}
						}, 300);
				}
			}



			if(gernade != null && !npcController.isEmpty() && explosionP == null)
			{

				int who = npcController.gotHit(gernade.getWorldLocation(), 1f);
				if(who != -1) {

					float mass = 1.0f;
					float radius = 0.90f;
					double[] tempTransform;
					Matrix4f translation = new Matrix4f(gernade.getLocalTranslation());
					tempTransform = toDoubleArray(translation.get(vals));
					explosionP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
					explosionP.setBounciness(0f);
					explosionP.setFriction(30f);
					bounce.stop();
					gernade.setPhysicsObject(explosionP);
					explosion.setLocation(gernade.getWorldLocation());
					setEarParameters();
					explosion.play();
					engine.getSceneGraph().removePhysicsObject(gernadeP);
					flash.setLocation(gernade.getLocalLocation());
					flash.setRange(2f);
					flash.setAmbient(200,0,0);
					mass = 100.0f;
					float size[] = {0.5f, 1f, 0.5f};
					NPC temp = npcController.getNPC(who);
					translation = new Matrix4f(temp.getLocalTranslation());
					tempTransform = toDoubleArray(translation.get(vals));
					npcP = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
					npcP.setBounciness(0f);
					npcP.setFriction(30f);
					float[] where = gernadeP.getLinearVelocity();
					float velocity[] = new float[3];
					velocity[0] = where[0] * 0.0002f;
					velocity[1] = where[1] * 0.0002f;
					velocity[2] = where[2] * 0.0002f;
					npcP.setLinearVelocity(velocity);
					temp.setPhysicsObject(npcP);
					kills++;
					timer = new Timer();
					timer.schedule(
						new TimerTask() {
							@Override
							public void run() {
								if (npcP != null) {
									engine.getSceneGraph().removePhysicsObject(npcP);
									engine.getSceneGraph().removeGameObject(temp);
									npcP = null;
									npcController.setNPC(who, null);
								}

							}
						}, 1000);
					timer = new Timer();
					timer.schedule(
						new TimerTask() {
							@Override
							public void run() {
								engine.getSceneGraph().removePhysicsObject(explosionP);
								engine.getSceneGraph().removeGameObject(gernade);
								flash.setAmbient(0, 0, 0);
								flash.setDiffuse(0, 0, 0);
								flash.setSpecular(0, 0, 0);
								gernadeP = null;
								explosionP = null;
								gernade = null;
								throwGernade = false;
							}
						}, 300);


				}


			}

			if(avatar != null) {
				int who = npcController.gotHit(avatar.getLocalLocation(), 0.5f);
				if (who != -1) {
					System.out.println("you died");
					if (avatarP == null) {
						float mass = 1.0f;
						float size[] = {0.5f, 1f, 0.5f};
						float radius = 0.10f;
						double[] tempTransform;
						Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
						tempTransform = toDoubleArray(translation.get(vals));
						NPC temp = npcController.getNPC(who);
						avatarP = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
						avatarP.setBounciness(0f);
						avatarP.setFriction(30f);
						Vector3f where = temp.getLocalForwardVector();
						float velocity[] = new float[3];
						velocity[0] = where.x() * 3;
						velocity[1] = 5f;
						velocity[2] = where.z() * 3;
						avatarP.setLinearVelocity(velocity);
						avatar.setPhysicsObject(avatarP);
						timer = new Timer();
						timer.schedule(
							new TimerTask() {
								@Override
								public void run() {
									engine.getSceneGraph().removePhysicsObject(avatarP);
									engine.getSceneGraph().removeGameObject(weopon);
									engine.getSceneGraph().removeGameObject(avatar);
									deaths++;
									weopon = null;
									avatarP = null;
									avatar = null;

								}
							}, 300);
					}

				}
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
		//c.setU(new Vector3f(u.x(), u.y(), u.z()));
		//c.setV(new Vector3f(v.x(), v.y(), v.z()));
		//c.setN(new Vector3f(n.x(), n.y(), n.z()));
	}

	@Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            glockS.stopAnimation();
				glockS.playAnimation("SHOOT", 1f,
			AnimatedShape.EndType.STOP, 0);
			shootSound.setLocation(avatar.getWorldLocation());
			setEarParameters();
			shootSound.play();
			shoot();
            
        }
    }
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_U: {
				break;
			}
			case KeyEvent.VK_SPACE: {
				System.out.println("starting physics");
				running = true;
				break;
			}

			
			case KeyEvent.VK_O: {
				this.changeChar();
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

	private void initMouseMode()
	{ RenderSystem rs = engine.getRenderSystem();
	Viewport vw = rs.getViewport("MAIN");
	float left = vw.getActualLeft();
	float bottom = vw.getActualBottom();
	float width = vw.getActualWidth();
	float height = vw.getActualHeight();
	centerX = (int) (left + width/2);
	centerY = (int) (bottom - height/2);
	isRecentering = false;
	try // note that some platforms may not support the Robot class
	{ robot = new Robot(); } catch (AWTException ex)
	{ throw new RuntimeException("Couldn't create Robot!"); }
	recenterMouse();
	prevMouseX = centerX; // 'prevMouse' defines the initial
	prevMouseY = centerY; // mouse position
	 //also change the cursor
	Cursor crosshair = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	rs.getGLCanvas().setCursor(crosshair);

	}

	private void recenterMouse()
	{ // use the robot to move the mouse to the center point.
	// Note that this generates one MouseEvent.
	RenderSystem rs = engine.getRenderSystem();
	Viewport vw = rs.getViewport("MAIN");
	float left = vw.getActualLeft();
	float bottom = vw.getActualBottom();
	float width = vw.getActualWidth();
	float height = vw.getActualHeight();
	double centerX = (double) (left + width/2.0f);
	double centerY = (double) (bottom - height/2.0f);
	isRecentering = true;
	robot.mouseMove((int)centerX, (int)centerY);
	}
	public void initRoll() {
	Camera c = engine.getRenderSystem()
	.getViewport("MAIN").getCamera();
	WorldUpVector = c.getV();
	WorldRightVector = c.getU();
	}

	public void yaw(float mouseDeltaX)
	{ float tilt;
		Camera c = engine.getRenderSystem()
		.getViewport("MAIN").getCamera();
		Vector3f rightVector = c.getU();
		Vector3f upVector = c.getV();
		Vector3f fwdVector = c.getN();
		if (mouseDeltaX < 0.0) tilt = -1.0f;
		else if (mouseDeltaX > 0.0) tilt = 1.0f;
		else tilt = 0.0f;
		rightVector.rotateAxis(0.01f*tilt, upVector.x(),
		upVector.y(), upVector.z());
		fwdVector.rotateAxis(0.01f*tilt, upVector.x(),
		upVector.y(), upVector.z());
		c.setU(rightVector);
		c.setN(fwdVector);
	}

	public void pitch(float mouseDeltaY) {
		float tilt;
		Camera c = engine.getRenderSystem().getViewport("MAIN").getCamera();
		X_AXIS = new Vector3f().add(1,0,0);
		Vector3f rightVector = c.getU();
		Vector3f upVector = c.getV();
		Vector3f fwdVector = c.getN();
		
		if (mouseDeltaY < 0.0) {
			tilt = -1.0f;
		} else if (mouseDeltaY > 0.0) {
			tilt = 1.0f;
		} else {
			tilt = 0.0f;
		}
		//avatar.pitch(0.01f*tilt);
		//Quaternionf pitchRotation = new Quaternionf().rotateAxis(0.01f*tilt, rightVector);
    	//pitchRotation.transform(upVector);
    	//pitchRotation.transform(fwdVector);

		
		
		//c.lookAt(fwdVector);
		//QuatFwd = new Quaternionf().fromAxisAngleDeg(fwdVector, 0.01f*tilt);
		//QuatUp = new Quaternionf().fromAxisAngleDeg(upVector, 0.01f*tilt);

		//QuatFwd.getEulerAnglesXYZ(fwdVector);
		//QuatUp.getEulerAnglesXYZ(upVector);
		upVector.rotateAxis(0.01f*tilt, rightVector.x(),
		rightVector.y(), rightVector.z());
		fwdVector.rotateAxis(0.01f*tilt, rightVector.x(),
		rightVector.y(), rightVector.z());

		c.setV(upVector);
		c.setN(fwdVector);
		
		//Vector3f Roll = upVector.orthogonalize(rightVector);
		//c.setV(Roll);
		//NewN = rightVector.cross(upVector);
		//c.setN(NewN);
		//c.setU(WorldRightVector);
			
	}

	// Function to check if a vector is horizontal
    public static boolean isHorizontal(Vector3f vector) {
        // Assuming "horizontal" means having a small y-component and large x- and z-components
        // You can adjust the threshold values according to your needs
        float threshold = 0.1f;
        return Math.abs(vector.y) < threshold; // Assuming y-component is up direction
    }

	public void pitchyaw(float mouseDeltaX, float mouseDeltaY)
	{	Matrix4f leftRotation, rightRotation, upRotation, downRotation;
		float tiltX;
		float tiltY;
		Camera c = engine.getRenderSystem()
		.getViewport("MAIN").getCamera();
		Vector3f rightVector = c.getU();
		Vector3f upVector = c.getV();
		Vector3f fwdVector = c.getN();
		

		if (mouseDeltaX < 0.0) {
		tiltX = 0.1f;
		}
		else if (mouseDeltaX > 0.0) {
		 tiltX = -0.1f;
		}
		else {tiltX = 0.0f;

		}
		//MouseDeltaY
		if (mouseDeltaY < 0.0) {
			tiltY = -0.1f;
			
		} else if (mouseDeltaY > 0.0) {
			tiltY = 0.1f;
		} else {
			tiltY = 0.0f;
		}

	Vector3f cameraForward = c.getN();
        

	Vector3f worldUp = new Vector3f(0,1,0);
	Vector3f worldDown = new Vector3f(0,-1,0);



	float angleUp = cameraForward.angle(worldUp);
	float angleDown = cameraForward.angle(worldUp);
	
	// Convert the angle from radians to degrees
	float angleDegreesUp = (float) Math.toDegrees(angleUp);
	float angleDegreesDown = (float) Math.toDegrees(angleDown);
	

	if (avatar != null) {
		if (angleDegreesDown < 130)	{
			if (tiltX != 0) {
				avatar.yaw(tiltX); }
				if (tiltY != 0) {
					avatar.pitch(tiltY); }
				c.lookAt(smallaimsphere);

		}
		if (angleDegreesUp > 30)	
		{
			if (tiltX != 0) {
				avatar.yaw(tiltX); }
				if (tiltY != 0) {
					avatar.pitch(tiltY); }
				c.lookAt(smallaimsphere);

		}
		if (angleDegreesUp <= 30) {
			avatar.pitch(-.2f);
			c.lookAt(smallaimsphere);


		}
		 if (angleDegreesUp >= 130) {
			avatar.pitch(.2f);
			c.lookAt(smallaimsphere);


		}
	}
		
		/* 
	if (tiltX != 0) {
		avatar.yaw(tiltX); }
		if (tiltY != 0) {
			avatar.pitch(tiltY); }
		c.lookAt(smallaimsphere);
		*/
	}
	
	protected void processNetworking(float elapsTime) {        // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() {
		return avatar.getWorldLocation();
	}

	public ObjShape getPlayerShape(String shape){
		if(shape.equals(avaShapePath))
		{
			return avaS;
		}
		if(shape.equals(dolShapePath))
		{
			return dolS;
		}
		return null;
	}

	public TextureImage getPlayerTexture(String texture){
		if(texture.equals(avaTexturePath))
		{
			return avaT;
		}
		if(texture.equals(dolTexturePath))
		{
			return dolT;
		}
		return null;
	}

	public void enemyBullet(Vector3f location, float[] velocity)
	{
		enemyBullet = new GameObject(GameObject.root(), gernadeS, gernadeT);
		enemyBullet.setLocalLocation(location);
		initialScale = (new Matrix4f()).scaling(0.10f);
		enemyBullet.setLocalScale(initialScale);
		float mass = 1.0f;
		float radius = 0.10f;
		double[] tempTransform;
		Matrix4f translation = new Matrix4f(enemyBullet.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		enemyBulletP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
		enemyBulletP.setBounciness(0f);
		enemyBulletP.setFriction(0f);

		enemyBulletP.setLinearVelocity(velocity);
		enemyBullet.setPhysicsObject(enemyBulletP);
		timer = new Timer();
		timer.schedule(
			new TimerTask()
			{
				@Override
				public void run()
				{
					if(enemyBulletP != null)
					{
						engine.getSceneGraph().removePhysicsObject(enemyBulletP);
						engine.getSceneGraph().removeGameObject(enemyBullet);
						enemyBulletP = null;
						enemyBullet = null;
					}

				}
			}, 300);
	}

	public void enemyGernade(Vector3f location, float[] velocity)
	{
		enemyGarnade = new GameObject(GameObject.root(), gernadeS, gernadeT);
		enemyGarnade.setLocalLocation(location);
		initialScale = (new Matrix4f()).scaling(0.10f);
		enemyGarnade.setLocalScale(initialScale);
		float mass = 1.0f;
		float radius = 0.10f;
		double[] tempTransform;
		Matrix4f translation = new Matrix4f(enemyGarnade.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		enemyGarnadeP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
		enemyGarnadeP.setBounciness(1f);
		enemyGarnadeP.setFriction(30f);

		enemyGarnadeP.setLinearVelocity(velocity);
		enemyGarnade.setPhysicsObject(enemyGarnadeP);
	}


	public String getAvaShapePath()
	{
		return avatarShape;
	}

	public String getAvaTexturePath()
	{
		return avatarTexture;
	}

	public boolean characterSelect(String chara)
	{
		if(chara.equals("dol"))
		{
			avatarShape = dolShapePath;
			avatarTexture = dolTexturePath;
			avatar.setShape(dolS);
			avatar.setTextureImage(dolT);
			avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(0f)));
			if(protClient != null)
			{
				protClient.sendChangeChar(avatarShape, avatarTexture);
			}

			return true;
		}
		if(chara.equals("human"))
		{
			avatarShape = avaShapePath;
			avatarTexture = avaTexturePath;
			avatar.setShape(avaS);
			avatar.setTextureImage(avaT);
			avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));
			if(protClient != null)
			{
				protClient.sendChangeChar(avatarShape, avatarTexture);
			}

			return true;
		}
		return false;
	}

	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	public ObjShape getNPCshape() {
		return npcShape;
	}
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
/* 
	@Override
	public void mouseMoved(MouseEvent e)
	{ // if robot is recentering and the MouseEvent location is in the center,
	// then this event was generated by the robot
	if (isRecentering &&
	centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
	{ // mouse recentered, recentering complete
	isRecentering = false;
	}
	else
	{ // event was due to a user mouse-move, and must be processed
	curMouseX = e.getXOnScreen();
	curMouseY = e.getYOnScreen();
	float mouseDeltaX = prevMouseX - curMouseX;
	float mouseDeltaY = prevMouseY - curMouseY;
	//yaw(mouseDeltaX);
	//pitch(mouseDeltaY);
	pitchyaw(mouseDeltaX, mouseDeltaY);
	prevMouseX = curMouseX;
	prevMouseY = curMouseY;
	// tell robot to put the cursor to the center (since user just moved it)
	recenterMouse();
	prevMouseX = centerX; // reset prev to center
	prevMouseY = centerY;
	}
	}
*/


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
			//object1 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody0();
			//object2 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody1();
			//JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			//JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++) {
				contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f) {
					//System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				}
			}
		}
	}

	private boolean checkForCollisionsTwoPhysics(PhysicsObject obj12, PhysicsObject obj22) {
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
					if((obj1.getUID() == obj12.getUID()) && (obj2.getUID() == obj22.getUID()))
					{
						return true;
					}
					return false;
				}
			}
		}
		return false;
	}



	public void setEarParameters()
	{ Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	public void throwGerande()
	{
		throwGernade = true;
		if(throwGernade && gernadeP == null)
		{
			gernade = new GameObject(GameObject.root(), gernadeS, gernadeT);
			Vector3f location = avatar.getWorldLocation();
			gernade.setLocalLocation(location);

			initialScale = (new Matrix4f()).scaling(0.10f);
			gernade.setLocalScale(initialScale);
			float mass = 1.0f;
			float radius = 0.10f;
			float y = 0.0f;
			double[] tempTransform;
			Matrix4f translation = new Matrix4f(gernade.getLocalTranslation());
			tempTransform = toDoubleArray(translation.get(vals));
			gernadeP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
			gernadeP.setBounciness(1f);
			gernadeP.setFriction(30f);
			Vector3f where = avatar.getLocalForwardVector();
			float velocity[] = new float[3];
			velocity[0] = where.x()*3;
			velocity[1] = 5f;
			velocity[2] = where.z()*3;
			gernadeP.setLinearVelocity(velocity);
			gernade.setPhysicsObject(gernadeP);
			if(protClient != null)
			{
				protClient.sendGernadeInfo(gernade.getLocalLocation(), velocity);
			}
		}
	}

	public void shoot()
	{

		if(shoot)
		{
			shoot = false;
			Camera c = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
			bullet = new GameObject(GameObject.root(), gernadeS, gernadeT);
			Vector3f location = avatar.getWorldLocation();
			Vector3f bulletLocation = new Vector3f(location.x(), 1f, location.z());
			bullet.setLocalLocation(bulletLocation);
			initialScale = (new Matrix4f()).scaling(0.10f);
			bullet.setLocalScale(initialScale);
			float mass = 1.0f;
			float radius = 0.10f;
			float y = 0.0f;
			double[] tempTransform;
			Matrix4f translation = new Matrix4f(bullet.getLocalTranslation());
			tempTransform = toDoubleArray(translation.get(vals));
			bulletP = (engine.getSceneGraph()).addPhysicsSphere(mass, tempTransform, radius);
			bulletP.setBounciness(0f);
			bulletP.setFriction(0f);
			Vector3f where = c.getN();
			float velocity[] = new float[3];
			velocity[0] = where.x()*100;
			velocity[1] = 0f;
			velocity[2] = where.z()*100;
			bulletP.setLinearVelocity(velocity);
			bullet.setPhysicsObject(bulletP);
			timer = new Timer();
			timer.schedule(
				new TimerTask()
				{
					@Override
					public void run()
					{
						if(bulletP != null)
						{
							engine.getSceneGraph().removePhysicsObject(bulletP);
							engine.getSceneGraph().removeGameObject(bullet);
							bulletP = null;
							bullet = null;
							shoot = true;

						}

					}
				}, 3000);
			if(protClient != null)
			{
				protClient.sendBulletInfo(bullet.getLocalLocation(), velocity);
			}
		}
	}

	public void changeChar()
	{
		boolean selected = true;
		while(selected)
		{
			Scanner myObj = new Scanner(System.in);  // Create a Scanner object
			System.out.println("Selcet char dol or human:");

			String name = myObj.nextLine();  // Read user input

			selected = !this.characterSelect(name);
		}
	}

	public void addKills()
	{
		kills++;
	}

	public GameObject getTerr()
	{
		return terr;
	}
}