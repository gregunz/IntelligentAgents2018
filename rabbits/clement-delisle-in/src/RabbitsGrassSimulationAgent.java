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
        Optional<Position2D> maybeNextMove = getRandomNeighborCell(this.pos);
        maybeNextMove.ifPresent(nextMove -> space.moveRabbitAt(this.pos, nextMove));
        energy -= moveCost;
        eatGrass();
    }

    public Optional<RabbitsGrassSimulationAgent> tryToReproduce(int birthThreshold, int initialEnergy, int birthCost) {
        if (energy >= birthThreshold) {
            Optional<Position2D> maybeNextPos = getRandomNeighborCell(this.pos);
            if (maybeNextPos.isPresent()) {
                energy -= birthCost;
                return Optional.of(new RabbitsGrassSimulationAgent(maybeNextPos.get(), initialEnergy, space));
            }
        }
        return Optional.empty();
    }

    private void eatGrass() {
        energy += space.eatGrassAt(this.pos);
    }

    private Optional<Position2D> getRandomNeighborCell(Position2D pos) {
        List<Position2D> availableCells = new ArrayList<>();
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
            return Optional.empty();
        } else {
            return Optional.of(availableCells.get(new Random().nextInt(availableCells.size())));
        }
    }
}
