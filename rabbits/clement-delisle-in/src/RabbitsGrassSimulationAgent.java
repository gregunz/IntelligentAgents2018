import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    private int x;
    private int y;
    private int vX;
    private int vY;
    private int energy;
    private boolean hasDied = false;
    private static int IDNumber = 0;
    private int ID;
    private RabbitsGrassSimulationSpace rgsSpace;

    public RabbitsGrassSimulationAgent(int x, int y, int energy, RabbitsGrassSimulationSpace space){
        this.x = x;
        this.y = y;
        this.energy = energy;
        setVxVy();
        IDNumber++;
        ID = IDNumber;
        space.addRabbit(x, y, this);
        setRabbitsGrassSimulationSpace(space);
    }

    private void setVxVy(){
        vX = 0;
        vY = 0;
        while(((vX == 0) == ( vY == 0))){
            vX = (int)Math.floor(Math.random() * 3) - 1;
            vY = (int)Math.floor(Math.random() * 3) - 1;
        }
    }

    public void setXY(int newX, int newY){
        x = newX;
        y = newY;
    }

    public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs){
        rgsSpace = rgs;
    }

    public String getID(){
        return "Rabbit -" + ID;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public boolean hasDied() {
        return hasDied;
    }

    public void draw(SimGraphics G){

        G.drawCircle(Color.white);
    }

    public RabbitsGrassSimulationAgent step(int moveCost, int birthThreshold, int initialEnergy, int birthCost){


        if (energy <= 0){
            rgsSpace.removeRabbitAt(x, y);
            hasDied = true;
            return null;
        }

        int newX = x + vX;
        int newY = y + vY;



        if(tryMove(newX, newY)){
            eatGrass();
        }else{
            // do  something because of collision
            eatGrass();
        }

        setVxVy();

        energy -= moveCost;

        return tryToReproduce(birthThreshold, initialEnergy, birthCost);
    }

    private boolean tryMove(int newX, int newY){
        return rgsSpace.moveRabbitAt(x, y, newX, newY);
    }

    private void eatGrass() {
        energy += rgsSpace.getEnergy(x, y);
    }

    private RabbitsGrassSimulationAgent tryToReproduce(int birthThreshold, int initialEnergy, int birthCost) {
        if (energy >= birthThreshold) {
            int newX = x;
            int newY = y;

            if (!rgsSpace.isCellOccupied(x + 1, y)) {
                newX += 1;
            } else if (!rgsSpace.isCellOccupied(x - 1, y)) {
                newX -= 1;
            } else if (!rgsSpace.isCellOccupied(x, y + 1)) {
                newY += 1;
            } else if (!rgsSpace.isCellOccupied(x, y - 1)) {
                newY -= 1;
            } else {
                return null;
            }
            energy -= birthCost;
            return new RabbitsGrassSimulationAgent(newX, newY, initialEnergy, rgsSpace);
        } else {
            return null;
        }


    }
}
