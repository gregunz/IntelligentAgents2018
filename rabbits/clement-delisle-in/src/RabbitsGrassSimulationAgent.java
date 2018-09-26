import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import utils.Position2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 * @authors Maxime Delisle & Grégoire Clément
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    private Position2D pos;
    private int energy;
    private RabbitsGrassSimulationSpace space;

    public RabbitsGrassSimulationAgent(Position2D pos, int energy, RabbitsGrassSimulationSpace space) {
        this.pos = pos;
        this.energy = energy;

        space.addRabbitAt(pos, this);
        this.space = space;
    }

    public Position2D getPos() {
        return this.pos;
    }

    public void setPos(Position2D newPos) {
        pos = newPos;
    }

    public int getX() {
        return this.pos.getX();
    }

    public int getY() {
        return this.pos.getY();
    }


    public boolean isDead() {
        return this.energy <= 0;
    }

    public void draw(SimGraphics G) {
        G.drawRect(Color.red);
    }

    public void moveThenEat(int moveCost) {
        Position2D nextMove = getRandomNeighborCell(this.pos);
        move(nextMove);
        energy -= moveCost;
        eatGrass();
    }

    public Optional<RabbitsGrassSimulationAgent> tryToReproduce(int birthThreshold, int initialEnergy, int birthCost) {
        if (energy >= birthThreshold) {
            Position2D newPos = getRandomNeighborCell(this.pos);
            if (this.pos.isDifferent(newPos)) {
                energy -= birthCost;
                return Optional.of(new RabbitsGrassSimulationAgent(newPos, initialEnergy, space));

            }
        }

        return Optional.empty();
    }

    private boolean move(Position2D nextMove) {
        if (this.pos.isDifferent(nextMove)) {
            space.moveRabbitAt(this.pos, nextMove);
            return true;
        }
        return false;
    }

    private void eatGrass() {
        energy += space.eatGrassAt(this.pos);
    }

    private Position2D getRandomNeighborCell(Position2D pos) {
        List<Position2D> availableCells = new ArrayList<>();
        //availableCells.add(pos); //TODO: decide whether staying is a legal move
        int posX = pos.getX();
        int posY = pos.getY();

        if (space.isCellFree(posX + 1, posY)) {
            availableCells.add(new Position2D(posX + 1, posY));
        }
        if (space.isCellFree(posX - 1, posY)) {
            availableCells.add(new Position2D(posX - 1, posY));
        }
        if (space.isCellFree(posX, posY + 1)) {
            availableCells.add(new Position2D(posX, posY + 1));
        }
        if (space.isCellFree(posX, posY - 1)) {
            availableCells.add(new Position2D(posX, posY - 1));
        }

        if (availableCells.isEmpty()) {
            return pos;
        } else {
            return availableCells.get(new Random().nextInt(availableCells.size()));
        }
    }
}
