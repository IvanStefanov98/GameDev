import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import java.awt.Font;
public class Game {

	public static final String GAME_TITLE = "My Game";
	private static int SCREEN_SIZE_WIDTH = 800;
	private static int SCREEN_SIZE_HEIGHT = 600;
	private static final int FRAMERATE = 60;
	private static final int MAX_LEVEL = 5;
	private static final int MAX_LIFES = 3;
	private static final int MAX_TREASURES_COUNT = 10;
	private static final int MAX_MINES_COUNT = 5;
	private static final int HERO_START_Y = 50;
	private static final int HERO_START_X = 50;
	private boolean finished;
	private LevelTile[] levelTile = new LevelTile[MAX_LEVEL];
	private ArrayList<Entity> entities;
	private HashMap<Integer, ArrayList<Entity>> levelsTreasures;
	private HashMap<Integer, ArrayList<Entity>> levelsMines;
	private HeroEntity heroEntity;
	private int currentLevel = 1;
	private int lifes = MAX_LIFES;
	private TrueTypeFont font;
	private int treasuresCollected = 0;

	/**
	 * Application init
	 * 
	 * @param args Commandline args
	 */
	public static void main(String[] args) {
		Game myGame = new Game();
		myGame.start();
	}

	public void start() {
		try {
			init();
			run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Sys.alert(GAME_TITLE, "An error occured and the game will exit.");
		} finally {
			cleanup();
		}
		System.exit(0);
	}

	/**
	 * Initialise the game
	 * 
	 * @throws Exception if init fails
	 */
	private void init() throws Exception {
		// Create a fullscreen window with 1:1 orthographic 2D projection, and
		// with
		// mouse, keyboard, and gamepad inputs.
		try {
			initGL(SCREEN_SIZE_WIDTH, SCREEN_SIZE_HEIGHT);
			initTextures();
		} catch (IOException e) {
			e.printStackTrace();
			finished = true;
		}
//		try {
//			tile.texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_1.png"));
//			avatar.texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/avatar.png"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			finished = true;
//		}

	}

	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle(GAME_TITLE);
			Display.setFullscreen(false);
			Display.create();

			// Enable vsync if we can
			Display.setVSyncEnabled(true);

			// Start up the sound system
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
		font = new TrueTypeFont(awtFont, true);
	}

	private void initTextures() throws IOException {
		entities = new ArrayList<Entity>();
		initLevels();
		Texture texture;

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/avatar.png"));

		initTreasures();
	}
	
	private void initLevels() throws IOException {
		Texture texture;
		
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_1.png"));
		levelTile[0] = new LevelTile(texture);

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_2.png"));
		levelTile[1] = new LevelTile(texture);

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_3.png"));
		levelTile[2] = new LevelTile(texture);

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_4.png"));
		levelTile[3] = new LevelTile(texture);

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/tile_5.png"));
		levelTile[4] = new LevelTile(texture);

//		texture = TextureLoader.getTexture("PNG", 
//				ResourceLoader.getResourceAsStream("res/chest.png"));
	}

	private void initTreasures() throws IOException {
		Texture texture;
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/avatar.png"));
		heroEntity = new HeroEntity(this, new MySprite(texture), Display.getDisplayMode().getWidth() / 2 - texture.getImageWidth() / 2, Display.getDisplayMode().getHeight() - texture.getImageHeight());
		levelsTreasures = new HashMap<Integer, ArrayList<Entity>>();
		levelsMines = new HashMap<Integer, ArrayList<Entity>>();

		ArrayList<Entity> levelTreasures;
		for (int i = 0; i < MAX_LEVEL; i++) {
			levelTreasures = new ArrayList<Entity>();
			levelsTreasures.put(i, levelTreasures);
		}

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/gift.png"));
		Random rand = new Random();
		int objectX;
		int objectY;
		for (int i = 0; i < MAX_LEVEL; i++) {
			for (int m = 0; m < MAX_TREASURES_COUNT; m++) {
				objectX = rand.nextInt(SCREEN_SIZE_WIDTH - texture.getImageWidth());
				objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());
				TreasureEntity objectEntity = new TreasureEntity(new MySprite(texture), objectX, objectY);

				levelTreasures = levelsTreasures.get(i);
				levelTreasures.add(objectEntity);
			}
		}

		levelsMines = new HashMap<Integer, ArrayList<Entity>>();

		ArrayList<Entity> levelMines;
		for (int i = 0; i < MAX_LEVEL; i++) {
			levelMines = new ArrayList<Entity>();
			levelsMines.put(i, levelMines);
		}

		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/bomb.png"));
		rand = new Random();

		for (int i = 0; i < MAX_LEVEL; i++) {
			for (int m = 0; m < MAX_MINES_COUNT; m++) {
				objectX = rand.nextInt(SCREEN_SIZE_WIDTH - texture.getImageWidth());
				objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());
				MineEntity objectEntity = new MineEntity(new MySprite(texture), objectX, objectY);

				levelMines = levelsMines.get(i);
				levelMines.add(objectEntity);
			}
		}

//		private void initTreasures() {
//		// TODO Auto-generated method stub
//		
//	}
//		ObjectEntity objectEntity = new ObjectEntity(new MySprite(texture),
//				200, 200);
//		entities.add(objectEntity);

		System.out.println("Hero texture width: " + heroEntity.getWidth());
		System.out.println("Hero texture height: " + heroEntity.getHeight());
	}

	/**
	 * Runs the game (the "main loop")
	 */
	private void run() {
		while (!finished) {
			// Always call Window.update(), all the time
			Display.update();

			if (Display.isCloseRequested()) {
				// Check for O/S close requests
				finished = true;
			} else if (Display.isActive()) {
				// The window is in the foreground, so we should play the game
				logic();
				render();
				Display.sync(FRAMERATE);
			} else {
				// The window is not in the foreground, so we can allow other
				// stuff to run and
				// infrequently update
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				logic();
				if (Display.isVisible() || Display.isDirty()) {
					// Only bother rendering if the window is visible or dirty
					render();
				}
			}
		}
	}

	/**
	 * Do any game-specific cleanup
	 */
	private void cleanup() {
		// TODO: save anything you want to disk here

		// Stop the sound
		AL.destroy();

		// Close the window
		Display.destroy();
	}

	/**
	 * Do all calculations, handle input, etc.
	 */
	private void logic() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			finished = true;
		}
		if (lifes > 0) {
			logicHero();

			checkForCollision();
		}

//		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
//			if (heroEntity.getX() + heroEntity.getWidth() + 10 < Display.getDisplayMode().getWidth()) {
//				heroEntity.setX(heroEntity.getX() + 10);
//			} else {
//				if (currentLevel < MAX_LEVEL) {
//					heroEntity.setX(0);
//					currentLevel++;
//				}
//			}
//		}
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
//			if (heroEntity.getX() - 10 >= 0) {
//				heroEntity.setX(heroEntity.getX() - 10);
//			} else {
//				if (currentLevel > 1) {
//					currentLevel--;
//					heroEntity.setX(Display.getDisplayMode().getWidth() - heroEntity.getWidth());
//				}
//			}
//		}
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
//			if (heroEntity.getY() > 0) {
//				heroEntity.setY(heroEntity.getY() - 10);
//			}
//		}
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
//			if (heroEntity.getY() + heroEntity.getHeight() < Display.getDisplayMode().getHeight()) {
//				heroEntity.setY(heroEntity.getY() + 10);
//			}
//		}
//
//		for (int p = 0; p < entities.size(); p++) {
//			for (int s = p + 1; s < entities.size(); s++) {
//				Entity me = entities.get(p);
//				Entity him = entities.get(s);
//				if (me.collidesWith(him)) {
//					me.collidedWith(him);
//					him.collidedWith(me);
//				}
//			}
//		}
	}

	/**
	 * Render the current frame
	 */
	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		Color.white.bind();
		
		drawLevel();

		drawObjects();

		heroEntity.draw();

		drawHUD();

//		LevelTile currentLevelTile;
//		currentLevelTile = levelTile[currentLevel - 1];
//		currentLevelTile.getTexture().bind();
//		for (int a = 0; a * currentLevelTile.getHeight() < SCREEN_SIZE_HEIGHT; a++) {
//			for (int b = 0; b * currentLevelTile.getWidth() < SCREEN_SIZE_HEIGHT; b++) {
//				int textureX = currentLevelTile.getWidth() * b;
//				int textureY = currentLevelTile.getHeight() * a;
//				currentLevelTile.draw(textureX, textureY);
//			}
//		}
//
//		if (entities != null) {
//			for (Entity entity : entities) {
//				if (entity.isVisible()) {
//					entity.draw();
//				}
//			}
//		}
//		font.drawString(10, 0, "Treasures collected " + treasuresCollected,
//				Color.black);
//		Color.white.bind();
//		heroEntity.draw();
	}
	
	private void drawLevel() {
		// TODO Auto-generated method stub
		LevelTile currentLevelTile;
		currentLevelTile = levelTile[currentLevel - 1];
		currentLevelTile.getTexture().bind();
		for (int a = 0; a * currentLevelTile.getHeight() < SCREEN_SIZE_HEIGHT; a++) {
			for (int b = 0; b * currentLevelTile.getWidth() < SCREEN_SIZE_WIDTH; b++) {
				int textureX = currentLevelTile.getWidth() * b;
				int textureY = currentLevelTile.getHeight() * a;
				currentLevelTile.draw(textureX, textureY);
			}
		}
	}
	
	private void drawObjects() {
		ArrayList<Entity> levelTreasures = levelsTreasures.get(currentLevel - 1);
		for (Entity entity : levelTreasures) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}

		ArrayList<Entity> levelMines = levelsMines.get(currentLevel - 1);
		for (Entity entity : levelMines) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}
	}
	
	private void drawHUD() { 
		font.drawString(10, 0, String.format("Score: %d/%d", treasuresCollected, MAX_TREASURES_COUNT * MAX_LEVEL), Color.black);

		font.drawString(SCREEN_SIZE_WIDTH - 120, 0, String.format("Lifes: %d/%d", lifes, MAX_LIFES), Color.black); 
	}
	
	private void logicHero() {

		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			if (heroEntity.getX() + heroEntity.getWidth() + 10 < Display.getDisplayMode().getWidth()) {
				heroEntity.setX(heroEntity.getX() + 10);
			} else {
				if (currentLevel < MAX_LEVEL) {
					heroEntity.setX(0);
					currentLevel++;
				}
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			if (heroEntity.getX() - 10 >= 0) {
				heroEntity.setX(heroEntity.getX() - 10);
			} else {
				if (currentLevel > 1) {
					currentLevel--;
					heroEntity.setX(Display.getDisplayMode().getWidth() - heroEntity.getWidth());
				}
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			if (heroEntity.getY() > 0) {
				heroEntity.setY(heroEntity.getY() - 10);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			if (heroEntity.getY() + heroEntity.getHeight() < Display.getDisplayMode().getHeight()) {
				heroEntity.setY(heroEntity.getY() + 10);
			}
		}

	}
	
	private void checkForCollision() {
		for (int p = 0; p < entities.size(); p++) {
			for (int s = p + 1; s < entities.size(); s++) {
				Entity me = entities.get(p);
				Entity him = entities.get(s);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}

		ArrayList<Entity> levelTreasures = levelsTreasures.get(currentLevel - 1);
		for (int i = 0; i < levelTreasures.size(); i++) {
			Entity him = levelTreasures.get(i);

			if (heroEntity.collidesWith(him)) {
				heroEntity.collidedWith(him);
				him.collidedWith(heroEntity);
			}
		}
		ArrayList<Entity> levelMines = levelsMines.get(currentLevel - 1);
		for (int i = 0; i < levelMines.size(); i++) {
			Entity him = levelMines.get(i);

			if (heroEntity.collidesWith(him)) {
				heroEntity.collidedWith(him);
				him.collidedWith(heroEntity);

			}
		}

//		Entity me = heroEntity;
//		Entity him;
//		for (int p = 0; p < entities.size(); p++) {
//			him = entities.get(p);
//
//			if (me.collidesWith(him)) {
//				me.collidedWith(him);
//				him.collidedWith(me);
	}

	public void notifyTreasureCollected(Entity notifier, Object object) {
		if (object instanceof TreasureEntity) {
			TreasureEntity treasureEntity = (TreasureEntity) object;
			levelsTreasures.get(currentLevel - 1).remove(treasureEntity);
			setTreasuresCollected(getTreasuresCollected() + 1);
		} else if (object instanceof MineEntity) {
			levelsMines.get(currentLevel - 1).remove(object);
			lifes--;
		}
	}

	public int getTreasuresCollected() {
		return treasuresCollected;
	}

	public void setTreasuresCollected(int treasuresCollected) {
		this.treasuresCollected = treasuresCollected;
	}
}
