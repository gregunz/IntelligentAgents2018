import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

public class CarryDropModel extends SimModelImpl {
    // Default Values
    private static final int NUMAGENTS = 100;
    private static final int WORLDXSIZE = 40;
    private static final int WORLDYSIZE = 40;
    private static final int TOTALMONEY = 1000;
    private static final int AGENT_MIN_LIFESPAN = 30;
    private static final int AGENT_MAX_LIFESPAN = 50;

    private int numAgents = NUMAGENTS;
    private int worldXSize = WORLDXSIZE;
    private int worldYSize = WORLDYSIZE;
    private int money = TOTALMONEY;
    private int agentMinLifespan = AGENT_MIN_LIFESPAN;
    private int agentMaxLifespan = AGENT_MAX_LIFESPAN;

    private Schedule schedule;

    private CarryDropSpace cdSpace;

    private ArrayList agentList;

    private DisplaySurface displaySurf;

    private OpenSequenceGraph amountOfMoneyInSpace;
    private OpenHistogram agentWealthDistribution;

    class moneyInSpace implements DataSource, Sequence {

        public Object execute() {
            return new Double(getSValue());
        }

        public double getSValue() {
            return (double)cdSpace.getTotalMoney();
        }
    }

    class agentMoney implements BinDataSource{
        public double getBinValue(Object o) {
            CarryDropAgent cda = (CarryDropAgent)o;
            return (double)cda.getMoney();
        }
    }

    public String getName(){
        return "Carry And Drop";
    }

    public void setup(){
        System.out.println("Running setup");
        cdSpace = null;
        agentList = new ArrayList();
        schedule = new Schedule(1);

        // Tear down Displays
        if (displaySurf != null){
            displaySurf.dispose();
        }
        displaySurf = null;

        if (amountOfMoneyInSpace != null){
            amountOfMoneyInSpace.dispose();
        }
        amountOfMoneyInSpace = null;

        if (agentWealthDistribution != null){
            agentWealthDistribution.dispose();
        }
        agentWealthDistribution = null;

        // Create Displays
        displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");
        amountOfMoneyInSpace = new OpenSequenceGraph("Amount Of Money In Space",this);
        agentWealthDistribution = new OpenHistogram("Agent Wealth", 8, 0);

        // Register Displays
        registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
        this.registerMediaProducer("Plot", amountOfMoneyInSpace);
    }

    public void begin(){
        buildModel();
        buildSchedule();
        buildDisplay();

        displaySurf.display();
        amountOfMoneyInSpace.display();
        agentWealthDistribution.display();
    }

    public void buildModel(){
        System.out.println("Running BuildModel");
        cdSpace = new CarryDropSpace(worldXSize, worldYSize);
        cdSpace.spreadMoney(money);

        for(int i = 0; i < numAgents; i++){
            addNewAgent();
        }
        for(int i = 0; i < agentList.size(); i++){
            CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
            cda.report();
        }
    }

    public void buildSchedule(){
        System.out.println("Running BuildSchedule");

        class CarryDropStep extends BasicAction {
            public void execute() {
                SimUtilities.shuffle(agentList);
                for(int i =0; i < agentList.size(); i++){
                    CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
                    cda.step();
                }

                int deadAgents = reapDeadAgents();
                for(int i =0; i < deadAgents; i++){
                    addNewAgent();
                }

                displaySurf.updateDisplay();
            }
        }

        schedule.scheduleActionBeginning(0, new CarryDropStep());

        class CarryDropCountLiving extends BasicAction {
            public void execute(){
                countLivingAgents();
            }
        }

        schedule.scheduleActionAtInterval(10, new CarryDropCountLiving());

        class CarryDropUpdateMoneyInSpace extends BasicAction {
            public void execute(){
                amountOfMoneyInSpace.step();
            }
        }

        schedule.scheduleActionAtInterval(10, new CarryDropUpdateMoneyInSpace());

        class CarryDropUpdateAgentWealth extends BasicAction {
            public void execute(){
                agentWealthDistribution.step();
            }
        }

        schedule.scheduleActionAtInterval(10, new CarryDropUpdateAgentWealth());
    }

    public void buildDisplay(){
        System.out.println("Running BuildDisplay");

        ColorMap map = new ColorMap();

        for(int i = 1; i<16; i++){
            map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
        }
        map.mapColor(0, Color.white);

        Value2DDisplay displayMoney =
                new Value2DDisplay(cdSpace.getCurrentMoneySpace(), map);

        Object2DDisplay displayAgents = new Object2DDisplay(cdSpace.getCurrentAgentSpace());
        displayAgents.setObjectList(agentList);

        displaySurf.addDisplayableProbeable(displayMoney, "Money");
        displaySurf.addDisplayableProbeable(displayAgents, "Agents");

        amountOfMoneyInSpace.addSequence("Money In Space", new moneyInSpace());
        agentWealthDistribution.createHistogramItem("Agent Wealth",agentList,new agentMoney());

    }

    private void addNewAgent(){
        CarryDropAgent a = new CarryDropAgent(agentMinLifespan, agentMaxLifespan);
        agentList.add(a);
        cdSpace.addAgent(a);
    }

    private int reapDeadAgents(){
        int count = 0;
        for(int i = (agentList.size() - 1); i >= 0 ; i--){
            CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
            if(cda.getStepsToLive() < 1){
                cdSpace.removeAgentAt(cda.getX(), cda.getY());
                cdSpace.spreadMoney(cda.getMoney());
                agentList.remove(i);
                count++;
            }
        }
        return count;
    }

    private int countLivingAgents(){
        int livingAgents = 0;
        for(int i = 0; i < agentList.size(); i++){
            CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
            if(cda.getStepsToLive() > 0) livingAgents++;
        }
        System.out.println("Number of living agents is: " + livingAgents);

        return livingAgents;
    }

    public Schedule getSchedule(){
        return schedule;
    }

    public String[] getInitParam(){
        String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize", "Money", "AgentMinLifespan", "AgentMaxLifespan"};
        return initParams;
    }

    public int getNumAgents(){
        return numAgents;
    }

    public void setNumAgents(int na){
        numAgents = na;
    }

    public int getWorldXSize(){
        return worldXSize;
    }

    public void setWorldXSize(int wxs){
        worldXSize = wxs;
    }

    public int getWorldYSize(){
        return worldYSize;
    }

    public void setWorldYSize(int wys){
        worldYSize = wys;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int i) {
        money = i;
    }

    public int getAgentMaxLifespan() {
        return agentMaxLifespan;
    }

    public int getAgentMinLifespan() {
        return agentMinLifespan;
    }

    public void setAgentMaxLifespan(int i) {
        agentMaxLifespan = i;
    }

    public void setAgentMinLifespan(int i) {
        agentMinLifespan = i;
    }

    public static void main(String[] args) {
        SimInit init = new SimInit();
        CarryDropModel model = new CarryDropModel();
        init.loadModel(model, "", false);
    }

}

