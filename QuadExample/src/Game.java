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
	private static final int MAX_TREASURES_COUNT = 2;
	private static final int MAX_MINES_COUNT = 2;
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
	private int speed = 3;
	private int heroSpeed = 10;
	private int score = 0;
	private int highScore = 0;

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

	private void init() throws Exception {
		try {
			initGL(SCREEN_SIZE_WIDTH, SCREEN_SIZE_HEIGHT);
			initTextures();
		} catch (IOException e) {
			e.printStackTrace();
			finished = true;
		}
	}

	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle(GAME_TITLE);
			Display.setFullscreen(false);
			Display.create();

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
		initObjects();
	}

	private void initLevels() throws IOException {
		Texture texture;

		texture = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("res/background.jpg"));
		levelTile[0] = new LevelTile(texture);
	}
	
	private void initGifts() throws IOException {
		Texture texture;
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/gift.png"));
		Random rand = new Random();
		int y = 0 - texture.getImageHeight();
		levelsTreasures = new HashMap<Integer, ArrayList<Entity>>();
		ArrayList<Entity> levelTreasures;
		for (int i = 0; i < MAX_TREASURES_COUNT; i++) {
			levelTreasures = new ArrayList<Entity>();
			levelsTreasures.put(i, levelTreasures);
		}
		for (int i = 0; i < MAX_TREASURES_COUNT; i++) {
			for (int m = 0; m < MAX_TREASURES_COUNT; m++) {
				int randomX = rand.nextInt(SCREEN_SIZE_WIDTH - texture.getImageWidth());
				TreasureEntity objectEntity = new TreasureEntity(new MySprite(texture), randomX, y);
				levelTreasures = levelsTreasures.get(i);
				levelTreasures.add(objectEntity);

			}
		}
	}
	
	private void initBombs() throws IOException {
		Texture texture;
		Random rand = new Random();
		levelsMines = new HashMap<Integer, ArrayList<Entity>>();
		ArrayList<Entity> levelMines;
		for (int i = 0; i < MAX_MINES_COUNT; i++) {
			levelMines = new ArrayList<Entity>();
			levelsMines.put(i, levelMines);
		}
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/bomb.png"));
		int y = 0 - texture.getImageHeight();
		rand = new Random();
		for (int i = 0; i < MAX_MINES_COUNT; i++) {
			for (int m = 0; m < MAX_MINES_COUNT; m++) {
				int randomX = rand.nextInt(SCREEN_SIZE_WIDTH - texture.getImageWidth());
				MineEntity objectEntity = new MineEntity(new MySprite(texture), randomX, y);

				levelMines = levelsMines.get(i);
				levelMines.add(objectEntity);
			}
		}
	}

	private void initObjects() throws IOException {
		Texture texture;
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/cyndraquil.png"));
		heroEntity = new HeroEntity(this, new MySprite(texture),
				Display.getDisplayMode().getWidth() / 2 - texture.getImageWidth() / 2,
				Display.getDisplayMode().getHeight() - texture.getImageHeight());
		 initGifts();
		 initBombs();
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
	}

	// Render frame

	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		Color.white.bind();
		
		drawLevel();
		drawObjects();

		heroEntity.draw();

		try {
			drawHUD();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawLevel() {
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
		ArrayList<Entity> levelTreasures = levelsTreasures.get(0);
		for (int i = 0; i < levelTreasures.size(); i++) {
			Entity entity = levelTreasures.get(i);
			if (entity.isVisible()) {
				entity.draw();
			}
			
			// gifts movement
			if (entity.getY() + entity.getHeight() < Display.getDisplayMode().getHeight()) {
				entity.setY(entity.getY() + speed);
			} else {
				entity.setVisible(false);
				levelsTreasures.get(i).remove(entity);
			}
			if (levelTreasures.size() < MAX_TREASURES_COUNT && lifes > 0) {
				try {
					initGifts();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ArrayList<Entity> levelMines = levelsMines.get(0);
		for (int i = 0; i < levelMines.size(); i++) {
			Entity entity = levelMines.get(i);
			if (entity.isVisible()) {
				entity.draw();
			}
			
			// bombs movement
			if (entity.getY() + entity.getHeight() < Display.getDisplayMode().getHeight()) {
				entity.setY(entity.getY() + speed);
			} else {
				entity.setVisible(false);
				levelsMines.get(i).remove(entity);
			}
			if (levelMines.size() < MAX_MINES_COUNT && lifes > 0) {
				try {
					initBombs();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void drawHUD() throws IOException {
		Texture lifesTexture;
		Texture texture;
		score = treasuresCollected * 10;
		if (score > highScore) {
			highScore = score;
		}
		lifesTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/heart.png"));
		
		// draw score
		font.drawString(10, 0, String.format("Score: %d", score), Color.black);
		
		// draw lifes as hearts
		font.drawString(SCREEN_SIZE_WIDTH - 200, 7, String.format("Lifes:"), Color.black);
		Color.red.bind();
		for (int i = 0; i < lifes; i++) {
			ObjectEntity lifesEntity = new ObjectEntity(new MySprite(lifesTexture),
					SCREEN_SIZE_WIDTH - i * lifesTexture.getImageWidth() - 50, 3);
			lifesEntity.draw();
		}
		
		// draw game over dialog
		if (lifes == 0) {
			Color.white.bind();
			texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/dialog.png"));
			ObjectEntity dialog = new ObjectEntity(new MySprite(texture),
					SCREEN_SIZE_WIDTH / 2 - texture.getImageWidth() / 2,
					SCREEN_SIZE_HEIGHT / 2 - texture.getImageHeight() / 2);
			dialog.draw();
			font.drawString(SCREEN_SIZE_WIDTH / 2 - texture.getImageWidth() / 4 + 35,
					SCREEN_SIZE_HEIGHT / 2 - texture.getImageHeight() / 2 + 30, String.format("Game Over"),
					Color.black);
			font.drawString(SCREEN_SIZE_WIDTH / 2 - texture.getImageWidth() / 4 + 45,
					SCREEN_SIZE_HEIGHT / 2 - texture.getImageHeight() / 2 + 90, String.format("Score: %d", score),
					Color.black);
			font.drawString(SCREEN_SIZE_WIDTH / 2 - texture.getImageWidth() / 4 + 20,
					SCREEN_SIZE_HEIGHT / 2 - texture.getImageHeight() / 2 + 120, String.format("High score: %d", highScore),
					Color.black);
			font.drawString(SCREEN_SIZE_WIDTH / 2 - texture.getImageWidth() / 2 + 20,
					SCREEN_SIZE_HEIGHT / 2 - texture.getImageHeight() / 2 + 170,
					String.format("Press Enter to restart the game"), Color.black);
			if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				cleanup();
				lifes = MAX_LIFES;
				treasuresCollected = 0;
				start();
			}
		}
	}

	private void logicHero() {
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			if (heroEntity.getX() + heroEntity.getWidth() + 10 < Display.getDisplayMode().getWidth()) {
				heroEntity.setX(heroEntity.getX() + heroSpeed);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			if (heroEntity.getX() - 10 >= 0) {
				heroEntity.setX(heroEntity.getX() - heroSpeed);
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
	}

	public void notifyTreasureCollected(Entity notifier, Object object) {
		if (object instanceof TreasureEntity) {
			TreasureEntity treasureEntity = (TreasureEntity) object;
			levelsTreasures.get(0).remove(treasureEntity);
			setTreasuresCollected(getTreasuresCollected() + 1);
			
			// increase speed
			if (treasuresCollected % 10 == 0 && treasuresCollected > 0 && speed <= 15) {
				speed++;
			}
			if (treasuresCollected % 20 == 0 && treasuresCollected > 0 && heroSpeed <= 20) {
				heroSpeed++;
			}
			
		} else if (object instanceof MineEntity) {
			levelsMines.get(0).remove(object);
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
