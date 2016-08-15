package tk.marr.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Marcin on 2016-08-14.
 */
public class GameScreen extends ScreenAdapter {

    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;
    private SpriteBatch batch;

    private static final float MOVE_TIME = 0.2F;
    private float timer = MOVE_TIME;

    private static final int SNAKE_MOVEMENT = 32;
    private int snakeX = 0, snakeY = 0;
    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    private static final int RIGHT = 0, LEFT = 1, UP = 2, DOWN = 3;
    private int snakeDirection = RIGHT;

    private boolean appleAvailable = false;
    private int appleX, appleY;

    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    @Override
    public void show() {
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
    }

    @Override
    public void render(float delta) {
        queryInput();
        timer -= delta;
        if(timer <= 0){
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
            updateBodyPartsPosition();
            checkAppleCollision();
        }
        checkAndPlaceApple();
        clearScreen();
        draw();
    }

    private void checkAppleCollision() {
        if(appleAvailable && appleX == snakeX && appleY == snakeY){
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private void draw() {
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for(BodyPart bodyPart : bodyParts){
            bodyPart.draw(batch);
        }
        if(appleAvailable)
            batch.draw(apple, appleX, appleY);
        batch.end();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void checkForOutOfBounds(){
        if(snakeX >= Gdx.graphics.getWidth()){
            snakeX = 0;
        }
        if(snakeX < 0){
            snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        }
        if(snakeY >= Gdx.graphics.getHeight()){
            snakeY = 0;
        }
        if(snakeY < 0){
            snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
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
        if (lPressed) snakeDirection = LEFT;
        if (rPressed) snakeDirection = RIGHT;
        if (uPressed) snakeDirection = UP;
        if (dPressed) snakeDirection = DOWN;
    }

    private void checkAndPlaceApple(){
        if(!appleAvailable){
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) *SNAKE_MOVEMENT;
                appleAvailable = true;
            } while(appleX == snakeX && appleY == snakeY);
        }
    }

    private class BodyPart {
        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture){
            this.texture = texture;
        }

        public void updateBodyPosition(int x, int y){
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch){
            if(!(x == snakeX && y == snakeY)) batch.draw(texture, x, y);
        }
    }
}