package tk.marr.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.glass.events.ViewEvent;

/**
 * Created by Marcin on 2016-08-14.
 */
public class GameScreen extends ScreenAdapter {

    private enum STATE{
        PLAYING, GAME_OVER
    }
    private STATE state = STATE.PLAYING;

    private ShapeRenderer shapeRenderer;
    private static final int GRID_CELL = 32;

    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;
    private SpriteBatch batch;

    private static final float MOVE_TIME = 0.2F;
    private float timer = MOVE_TIME;
    private boolean directionSet = false;

    private static final int SNAKE_MOVEMENT = 32;
    private float snakeX = 0, snakeY = 0;
    private float snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    private static final int RIGHT = 0, LEFT = 1, UP = 2, DOWN = 3;
    private int snakeDirection = RIGHT;

    private boolean appleAvailable = false;
    private float appleX, appleY;

    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    private boolean hasHit = false;

    private BitmapFont bitmapFont;
    private GlyphLayout layout;
    private static final String  GAME_OVER_TEXT = "Game Over! Tap space to restart!";

    private int score = 0;
    private static final int POINTS_PER_APPLE = 5;

    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 480;

    private Viewport viewport;
    private Camera camera;

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
        shapeRenderer = new ShapeRenderer();
        bitmapFont = new BitmapFont();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        switch (state){
            case PLAYING:{
                queryInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
            }
            break;
            case GAME_OVER:{
                checkForRestart();
            }
            break;
        }
        clearScreen();
        //drawGrid();
        draw();
    }

    private void updateSnake(float delta){
        if(!hasHit) {
            timer -= delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutOfBounds();
                updateBodyPartsPosition();
                checkSnakeBodyCollision();
                directionSet = false;
            }
        }
    }

    private void checkAppleCollision() {
        if(appleAvailable && appleX == snakeX && appleY == snakeY){
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            addToScore();
            appleAvailable = false;
        }
    }

    private void drawScore(){
        if(state == STATE.PLAYING){
            String scoreAsString = Integer.toString(score);
            bitmapFont.draw(batch, scoreAsString, (viewport.getWorldWidth() / 2), (viewport.getWorldHeight() / 2));
        }
    }

    private void checkSnakeBodyCollision(){
        for(BodyPart bodyPart : bodyParts){
            if(bodyPart.x == snakeX && bodyPart.y == snakeY) state = STATE.GAME_OVER;
        }
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for(BodyPart bodyPart : bodyParts){
            bodyPart.draw(batch);
        }
        if(appleAvailable)
            batch.draw(apple, appleX, appleY);
        //bitmapFont.draw(batch, text, 0, layout.height);
        if(state == STATE.GAME_OVER){
            layout.setText(bitmapFont, GAME_OVER_TEXT);
            bitmapFont.draw(batch, GAME_OVER_TEXT, (viewport.getWorldWidth() -
                    layout.width) / 2, (viewport.getWorldHeight() - layout.height) / 2);
        }
        drawScore();
        batch.end();
    }

    private void checkForRestart(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) doRestart();
    }

    private void doRestart(){
        state = STATE.PLAYING;
        bodyParts.clear();
        snakeDirection = RIGHT;
        directionSet = false;
        timer = MOVE_TIME;
        snakeX = 0;
        snakeY = 0;
        snakeXBeforeUpdate = 0;
        snakeYBeforeUpdate = 0;
        appleAvailable = false;
        score = 0;
    }

    private void addToScore(){
        score += POINTS_PER_APPLE;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void checkForOutOfBounds(){
        if(snakeX >= viewport.getWorldWidth()){
            snakeX = 0;
        }
        if(snakeX < 0){
            snakeX = viewport.getWorldWidth() - SNAKE_MOVEMENT;
        }
        if(snakeY >= viewport.getWorldHeight()){
            snakeY = 0;
        }
        if(snakeY < 0){
            snakeY = viewport.getWorldHeight() - SNAKE_MOVEMENT;
        }

    }

    private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection){
        if(snakeDirection != oppositeDirection || bodyParts.size == 0) snakeDirection = newSnakeDirection;
    }

    private void updateDirection(int newSnakeDirection){
        if (!directionSet && snakeDirection != newSnakeDirection) {
            directionSet = true;
            switch (newSnakeDirection) {
                case LEFT: {
                    updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
                }
                break;
                case RIGHT: {
                    updateIfNotOppositeDirection(newSnakeDirection, LEFT);
                }
                break;
                case UP: {
                    updateIfNotOppositeDirection(newSnakeDirection, DOWN);
                }
                break;
                case DOWN: {
                    updateIfNotOppositeDirection(newSnakeDirection, UP);
                }
                break;
            }
        }
    }

    private void moveSnake(){
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;
        switch (snakeDirection){
            case RIGHT:{
                snakeX += SNAKE_MOVEMENT;
                return;
            }
            case LEFT:{
                snakeX -= SNAKE_MOVEMENT;
                return;
            }
            case UP:{
                snakeY += SNAKE_MOVEMENT;
                return;
            }
            case DOWN:{
                snakeY -= SNAKE_MOVEMENT;
                return;
            }
        }
    }

    private void updateBodyPartsPosition(){
        if(bodyParts.size > 0){
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void queryInput() {
        boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        if (lPressed) updateDirection(LEFT);
        if (rPressed) updateDirection(RIGHT);
        if (uPressed) updateDirection(UP);
        if (dPressed) updateDirection(DOWN);
    }

    private void checkAndPlaceApple(){
        if(!appleAvailable){
            do {
                appleX = MathUtils.random((int) viewport.getWorldWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random((int) viewport.getWorldHeight() / SNAKE_MOVEMENT - 1) *SNAKE_MOVEMENT;
                appleAvailable = true;
            } while(appleX == snakeX && appleY == snakeY);
        }
    }

    private void drawGrid(){
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int x = 0; x < viewport.getWorldWidth(); x += GRID_CELL){
            for(int y = 0; y < viewport.getWorldHeight(); y += GRID_CELL){
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }

    private class BodyPart {
        private float x, y;
        private Texture texture;

        public BodyPart(Texture texture){
            this.texture = texture;
        }

        public void updateBodyPosition(float x, float y){
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch){
            if(!(x == snakeX && y == snakeY)) batch.draw(texture, x, y);
        }
    }
}