package agents.myAgent;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public class Agent implements MarioAgent {
    private boolean[] action = new boolean[MarioActions.numberOfActions()];

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        // Mario is always running right in speed, jump only when needed
        action[MarioActions.RIGHT.getValue()] = true;
        action[MarioActions.SPEED.getValue()] = true;
        action[MarioActions.JUMP.getValue()] = false;
    }

    // function from ForwardAgent.java - I did not write this
    private byte[][] decode(MarioForwardModel model, int[][] state) {
        // Create byte[][] to match the oberservation grid
        byte[][] dstate = new byte[model.obsGridWidth][model.obsGridHeight];

        // fill all elements of the array with the default value 2.
        // This value is used to denote shells that are out of bounds or have not yet
        // been processed.
        for (int i = 0; i < dstate.length; ++i)
            for (int j = 0; j < dstate[0].length; ++j)
                dstate[i][j] = 2;

        // Iterate through the state array,
        // setting each value to 1 if it is not 0, and to 0 if it is 0.
        // If something exists, it's 1 if not it's 0.
        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if (state[x][y] != 0) {
                    dstate[x][y] = 1;
                } else {
                    dstate[x][y] = 0;
                }
            }
        }
        return dstate;
    }

    // print the byte map
    private void printGrid(byte[][] map) {
        for (int y = 0; y < map[0].length; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < map.length; x++) {
                row.append(map[x][y]).append(' ');
            }
            System.out.println(row.toString());
        }
        System.out.println("----");
    }

    private boolean dangerFromEnemies(byte[][] enemiesFromBitmap) {
        // Check for enemies in a 5x3 area in front of Mario
        // Mario is at position (8,8) in the observation grid
        // Check from x=8-12 (Mario's position to 4 tiles ahead) and y=7-9 (head to foot
        // level)
        for (int y = 7; y <= 9; y++) {
            for (int x = 8; x <= 12; x++) {
                // Skip Mario's own position (8,8) and check for enemies (value 1)
                if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean dangerFromScene(byte[][] levelSceneFromBitmap) {
        // Check for gaps (empty space) directly in front of Mario
        // Mario is at position (8,8) in the observation grid
        // Check 2-3 tiles ahead and 1 tile below Mario's feet for gaps
        for (int x = 10; x <= 12; x++) { // 2-4 tiles ahead
            if (x < levelSceneFromBitmap.length) {
                // Check if there's a gap (0) at Mario's foot level (y=9) and below
                if (levelSceneFromBitmap[x][9] == 0) { // Gap at foot level
                    return true;
                }
                // Also check one tile below foot level for deeper gaps
                if (9 < levelSceneFromBitmap[0].length - 1 && levelSceneFromBitmap[x][10] == 0) {
                    return true;
                }
            }
        }

        // Check for walls directly in front of Mario
        // Look for solid blocks (1) at Mario's head level (y=7) and body level (y=8)
        for (int x = 9; x <= 11; x++) { // 1-3 tiles ahead
            if (x < levelSceneFromBitmap.length) {
                if (levelSceneFromBitmap[x][7] == 1 || levelSceneFromBitmap[x][8] == 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
        byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

        // Check for dangers and jump if needed
        if (dangerFromEnemies(enemiesFromBitmap) || dangerFromScene(levelSceneFromBitmap)) {
            action[MarioActions.JUMP.getValue()] = true;
        } else {
            // Reset jump action if no danger detected
            action[MarioActions.JUMP.getValue()] = false;
        }

        return action;
    }

    @Override
    public String getAgentName() {
        return "myAgent";
    }
}
