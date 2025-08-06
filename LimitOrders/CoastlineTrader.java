import java.util.LinkedList;

public class CoastlineTrader {

    private Runner[] runners;
    private LimitOrder buyLimitOrder;
    private LimitOrder sellLimitOrder;
    private float originalUnitSize; // size of the initial traded volume
    private float uniteSizeFromInventory; // inventory dictates the current unit size, this one
    private boolean initialized;
    private LocalLiquidity localLiquidityIndicator;
    private float inventory;
    private int longShort; // +1 for only Long, -1 for only Short
    private LinkedList<LimitOrder> disbalancedOrders; // list of all filled limit orders which have not been balanced by
    // an opposite order.
    private double realizedProfit; // is the total profit of all closed positions
    private double positionRealizedProfit; // is the total profit of all de-cascading orders
    private double originalDelta;
    private double targetAbsPnL = 0.0;

    private double takeProfit = 0.0;
    private double stopLoss = 0.0;
    public int properRunnerIndex = 0;

    private int goodTrades = 0;
    private int badTrades = 0;

    private String logFileName;    // File which stores the log for this trader

    /**
     * @param originalDelta used to define H1 and H1 of the liquidity indicator.
     * @param longShort  +1 if want only Long trades, -1 only Short.
     */
    public CoastlineTrader(double originalDelta, int longShort, String filename){
        this.originalDelta = originalDelta;
        this.originalUnitSize = 1; // computed using knowledge about the smallest volume (unite * 0.25 * 0.1). Be careful!
        this.longShort = longShort;
        initiateRunners(originalDelta);
        buyLimitOrder = null;
        sellLimitOrder = null;
        initialized = false;
        inventory = 0;
        localLiquidityIndicator = new LocalLiquidity(originalDelta, originalDelta * 2.525729, 50.0);
        disbalancedOrders = new LinkedList<>();
        realizedProfit = 0.0;
        positionRealizedProfit = 0.0;

        //Create each traders log file
        logFileName = filename;
        Tools.CreateFile(logFileName);
    }


    private void initiateRunners(double originalDelta){
        runners = new Runner[3];
        runners[0] = new Runner(originalDelta, originalDelta, originalDelta, originalDelta);
        if (longShort == 1){
            runners[1] = new Runner(0.75 * originalDelta, 1.50 * originalDelta, 0.75 * originalDelta, 0.75 * originalDelta);
            runners[2] = new Runner(0.50 * originalDelta, 2.00 * originalDelta, 0.50 * originalDelta, 0.50 * originalDelta);
        } else {
            runners[1] = new Runner(1.50 * originalDelta, 0.75 * originalDelta, 0.75 * originalDelta, 0.75 * originalDelta);
            runners[2] = new Runner(2.00 * originalDelta, 0.50 * originalDelta, 0.50 * originalDelta, 0.50 * originalDelta);
        }

    }



    public void run(Price price){
        localLiquidityIndicator.computation(price);
        int[] events = new int[3];
      //  double[] dc = new double[3];
       // dc[0] = runners[0].getExpectedDcLevel();
        events[0] = runners[0].run(price);
      //  dc[1] = runners[1].getExpectedDcLevel();
        events[1] = runners[1].run(price);
      //  dc[2] = runners[2].getExpectedDcLevel();
        events[2] = runners[2].run(price);

        if (!initialized){
            initialized = true;
            correctThresholdsAndVolumes(inventory);
            putOrders(price);
        } else {
            if (checkBuyFilled(price)){
                makeBuyFilled(price);
                //cancelSellLimitOrder();
            } else if (checkSellFilled(price)){
                makeSellFilled(price);
                //cancelBuyLimitOrder();
            }
            properRunnerIndex = findProperRunnerIndex();

            if (runners[properRunnerIndex].getMode() == -1) {
                if(buyLimitOrder == null){
                    putOrders(price);
                }
                double expectedDcLevel = runners[properRunnerIndex].getExpectedDcLevel();
                correctBuyLimitOrder(expectedDcLevel);
            }

            if(runners[properRunnerIndex].getMode() == 1 && inventory > 0) {
                double exitpoint = runners[properRunnerIndex].exitpoint();
                correctSellLimitOrder(exitpoint);
            }
        }
    }


    private boolean checkBuyFilled(Price price){
        if (buyLimitOrder != null){
            if (price.getAsk() < buyLimitOrder.getLevel()){
                return true;
            }
        }
        return false;
    }


    private boolean checkSellFilled(Price price){
        if (sellLimitOrder != null){
            if (price.getBid() > sellLimitOrder.getLevel()){
                return true;
            }
        }
        return false;
    }


    /**
     * Once called the method will put two orders to the order book: limit order sell at the deltaUp from the current
     * price and limit orders sell at the deltaDown form the actual price.
     * Prior to putting the limit orders, the method updates init size and thresholds.
     * @param price is the current price
     */
    private void putOrders(Price price){

        float cascadeVol = (float) (uniteSizeFromInventory * computeLiqUniteCoef(localLiquidityIndicator.liq));
        int properIndex = findProperRunnerIndex();
        double expectedUpperIE = runners[properIndex].getExpectedUpperIE();
        double expectedLowerIE = runners[properIndex].getExpectedLowerIE();
        int runnerMode = runners[properIndex].getMode();
        double deltaUp = runners[properIndex].getDeltaUp();
        double deltaDown = runners[properIndex].getDeltaDown();
        double dStarUp = runners[properIndex].getdStarUp();
        double dStarDown = runners[properIndex].getdStarDown();
        int upperIEtype = runners[properIndex].getUpperIEtype();
        int lowerIEtype = runners[properIndex].getLowerIEtype();
        double expectedDcLevel = runners[properIndex].getExpectedDcLevel();
        int buyDcOROS, sellDcOrOS;
        double buyDelta, sellDelta;
        if (runnerMode == -1){
            buyDcOROS = 2;
            buyDelta = dStarDown;
            sellDcOrOS = 1;
            sellDelta = deltaUp;
        } else {
            buyDcOROS = 1;
            buyDelta = deltaDown;
            sellDcOrOS = 2;
            sellDelta = dStarUp;
        }

        if (longShort == 1){
        //    if (runnerMode ==){ // if there is no disbalanced orders then we just open a new position.
                sellLimitOrder = null;
                double OSV = runners[properRunnerIndex].getExpectedOsLevel();
                double gamma = 0.5;
                double modified_delta = deltaDown * gamma * (1 - OSV);
                double ExpectedModifiedDeltaDC = runners[properIndex].exitpoint();
                buyLimitOrder = new LimitOrder(1, price.clone(), expectedDcLevel, cascadeVol, 1, dStarDown);
                sellLimitOrder = new LimitOrder(-1, price.clone(), ExpectedModifiedDeltaDC, inventory, 1, modified_delta);
        }

        else {
            if (disbalancedOrders.size() == 0){ // if there is no disbalanced orders if there is no disbalanced orders then
                // we just open a new position.
                buyLimitOrder = null;
                sellLimitOrder = new LimitOrder(-1, price.clone(), expectedUpperIE, cascadeVol, upperIEtype, deltaUp);
                computeTargetRelatPnL();
            } else {
                LinkedList<LimitOrder> compensatedOrdersList = findCompensatedOrdersList(expectedLowerIE, originalDelta, 1);
                if (compensatedOrdersList.size() != 0){
                    buyLimitOrder = new LimitOrder(1, price.clone(), expectedLowerIE, 0, buyDcOROS, buyDelta); // volume is computed at the next step
                    buyLimitOrder.setCompensatedOrders(compensatedOrdersList);
                } else {
                    buyLimitOrder = null;
                }
                sellLimitOrder = new LimitOrder(-1, price.clone(), expectedUpperIE, cascadeVol, sellDcOrOS, sellDelta);
            }
        }

    }


    /**
     * The method finds the index of a runner which should be used at the current inventory.
     * @return runner
     */
    private int findProperRunnerIndex(){
        if (Math.abs(inventory) < 15){
            return 0;
        } else if (Math.abs(inventory) >= 15 && Math.abs(inventory) < 30){
            return 1;
        } else {
            return 2;
        }
    }


    /**
     * Should be called when we consider the buy limit order filled
     * @param price is the current market price
     */
    private void makeBuyFilled(Price price){
        inventory += buyLimitOrder.getVolume();
        correctThresholdsAndVolumes(inventory);
        
        // Tools.appendToFile(logFileName, "[TRADE EXECUTED] BUY " + buyLimitOrder.getVolume() + " @ " + buyLimitOrder.getLevel());
        
        if (longShort == 1){
            disbalancedOrders.add(buyLimitOrder.clone());
        } else { // the case if the order is de-cascading
            positionRealizedProfit += buyLimitOrder.getRelativePnL();
            disbalancedOrders.removeAll(buyLimitOrder.compensatedOrders);
        }
        if (disbalancedOrders.size() == 0){ // inventory can become equal to 0, so there is no
            // a position in reality. We should close in this case.
            closePosition(price);
        }
        buyLimitOrder = null;
    }


    /**
     * Should be called when we consider the sell limit order filled
     * @param price is the current market price
     */
    private void makeSellFilled(Price price){
        inventory -= sellLimitOrder.getVolume();
        correctThresholdsAndVolumes(inventory);
        
        // Tools.appendToFile(logFileName, "[TRADE EXECUTED] SELL " + sellLimitOrder.getVolume() + " @ " + sellLimitOrder.getLevel());
        
        if (longShort == -1) {
            disbalancedOrders.add(sellLimitOrder.clone());
        } else { // the case if the order is de-cascading
            positionRealizedProfit += sellLimitOrder.getRelativePnL();
            disbalancedOrders.removeAll(sellLimitOrder.compensatedOrders);
        }
        if (disbalancedOrders.size() == 0){ // inventory can become equal to 0, so there is no
            // a position in reality. We should close in this case.
            closePosition(price);
        }
        sellLimitOrder = null;
    }



    /**
     * The method moves the DC limit order if price goes further then size of the threshold
     */
    private void correctOrdersLevel(double expectedDcLevel){
        if (buyLimitOrder != null){
            correctBuyLimitOrder(expectedDcLevel);
        }
        if (sellLimitOrder != null){
            correctSellLimitOrder(expectedDcLevel);
        }
    }


    private LinkedList<LimitOrder> findCompensatedOrdersList(double levelOrder, double delta, int buySell){
        LinkedList<LimitOrder> compensatedOrders = new LinkedList<>();
        for (LimitOrder aDisbalancedOrder : disbalancedOrders){
            if ((aDisbalancedOrder.getLevel() - levelOrder) * buySell >= delta * aDisbalancedOrder.getLevel()){
                compensatedOrders.add(aDisbalancedOrder);
            }
        }
        return compensatedOrders;
    }


    private boolean correctBuyLimitOrder(double expectedDcLevel){
        if (longShort == 1 || disbalancedOrders.size() > 1){
            if (expectedDcLevel < buyLimitOrder.getLevel()) {
                buyLimitOrder.setLevel(expectedDcLevel);
                return true; 
            }
        }
        return false;
    }


    private boolean correctSellLimitOrder(double expectedDcLevel){
        if (longShort == 1 || disbalancedOrders.size() > 1){
            if (expectedDcLevel > sellLimitOrder.getLevel()) {
                sellLimitOrder.setLevel(expectedDcLevel);
                return true; 
            }
        }
        return false;
    }


    private void cancelSellLimitOrder(){
        sellLimitOrder = null;
    }


    private void cancelBuyLimitOrder(){
        buyLimitOrder = null;
    }


    /**
     * The method corrects thresholds.
     */
    private void correctThresholdsAndVolumes(float inventory){
        if (Math.abs(inventory) < 15){
            uniteSizeFromInventory = originalUnitSize;
        } else if (Math.abs(inventory) >= 15 && Math.abs(inventory) < 30){
            uniteSizeFromInventory = originalUnitSize / 2;
        } else {
            uniteSizeFromInventory = originalUnitSize / 4;
        }
    }


    /**
     * The method checks the level returned by the Liquidity indicator and changes unit size. Described on the page 15 of the
     * "Alpha..."
     */
    private double computeLiqUniteCoef(double liquidity){
        double liqUnitCoef;
        if (0.5 <= liquidity){
            liqUnitCoef = 1.0;
        } else if (0.1 <= liquidity && liquidity < 0.5){
            liqUnitCoef = 0.5;
        } else {
            liqUnitCoef = 0.1;
        }
        return liqUnitCoef;
    }


    /**
     * The method compares my current total PnL and the assigned target level.
     * @return true if the PnL is greater or equal then the target PnL
     */
    private boolean positionCrossedTargetPnL(Price price){
        // return (getPositionTotalPnL(price) >= targetAbsPnL);

        if(getPositionTotalPnL(price) >= takeProfit){
            goodTrades++;
            return true;
        }
        if(getPositionTotalPnL(price) <= stopLoss){
            badTrades++;
            return true;
        }
        return false;
    }


    /**
     * The method returns total PnL based on realized and unrealized ones.
     * @param price is current market price
     * @return total PnL
     */
    public double getPositionTotalPnL(Price price){
        return getPositionProfit(price);
    }

    /**
     * The method computes total profit of the opened posiition
     * @param price current market price
     * @return profit of the opened position
     */
    public double getPositionProfit(Price price){
        return positionRealizedProfit + getPositionUnrealizedProfit(price);
    }


    /**
     * The method computes unrealized profit of the opened position
     * @param price is current market price
     * @return unrealized profit
     */
    private double getPositionUnrealizedProfit(Price price){
        if (disbalancedOrders.size() != 0){
            double marketPrice = (longShort == 1 ? price.getBid() : price.getAsk());
            double unrealizedProfit = 0.0;
            for (LimitOrder aDisbalancedOrder : disbalancedOrders){
                double absPriceMove = (marketPrice - aDisbalancedOrder.getLevel()) * aDisbalancedOrder.getType();
                unrealizedProfit += absPriceMove / aDisbalancedOrder.getLevel()  * aDisbalancedOrder.getVolume();
            }
            return unrealizedProfit;
        }
        return 0.0;
    }



    /**
     * The method should be called as soon as the current PnL is bigger or equal then the targetPnL.
     */
    private void closePosition(Price price){

        // So basically what is happening is:
        // Once the target pnl is reached
        // All the open trades are closed at the same price
        double exitPrice = (longShort == 1 ? price.getBid() : price.getAsk());

        for (LimitOrder entryOrder : disbalancedOrders) {
            double entryPrice = entryOrder.getLevel();
            String tradeType = (longShort == 1) ? "LONG" : "SHORT";

            // Tools.logTrade(logFileName, entryPrice, exitPrice, tradeType);
        }

        marketOrderToClosePosition(price);
        realizedProfit += positionRealizedProfit;
        positionRealizedProfit = 0.0;
        inventory = 0;
        cancelBuyLimitOrder();
        cancelSellLimitOrder();
        correctThresholdsAndVolumes(inventory);
    }


    /**
     * The method creates a market order of volume of all disbalanced orders to close the current position
     * @param price is current market price
     * @return true if no problems
     */
    private boolean marketOrderToClosePosition(Price price){
        double marketPrice = (longShort == 1 ? price.getBid() : price.getAsk());

        // if(longShort == 1)
        //     Tools.appendToFile(logFileName, "[LONG POSITION CLOSED] Market Order @ " + marketPrice);
        // else
        //     Tools.appendToFile(logFileName, "[SHORT POSITION CLOSED] Market Order @ " + marketPrice);

        for (LimitOrder aDisbalencedOrder : disbalancedOrders){
            double absPriceMove = (marketPrice - aDisbalencedOrder.getLevel()) * aDisbalencedOrder.getType();
            positionRealizedProfit += absPriceMove / aDisbalencedOrder.getLevel() * aDisbalencedOrder.getVolume();
        }
        disbalancedOrders = new LinkedList<>();
        return true;
    }


    // Strategy A
    private void computeTargetRelatPnL(){
        // takeProfit = extreme * (1 + originalDelta) * (1 + originalDelta/2);
        // stopLoss = extreme * (1 + originalDelta) * (1 - originalDelta/2);

        int properRunnerIndex = findProperRunnerIndex();
        double P_DCC = runners[properRunnerIndex].getExpectedDcLevel();
        //double delta;

        double delta_down = runners[properRunnerIndex].getDeltaDown();
        double delta_up = runners[properRunnerIndex].getDeltaUp();
        // takeProfit = P_DCC * (1 + delta / 2.0);
        // stopLoss = P_DCC * (1 - delta / 2.0);

        // We need percentages
        double price_bought = buyLimitOrder.getLevel();

        double OSV = runners[properRunnerIndex].getExpectedOsLevel();
        double gamma = 0.5;
        double modified_delta = runners[properRunnerIndex].getDeltaDown() * gamma * (1 - OSV);

         

        takeProfit = 1000;
        stopLoss = runners[properRunnerIndex].getExtreme() - 1 - modified_delta;
    }


    public double getRealizedProfit(){
        return realizedProfit;
    }

    public int getGoodTrades(){
        return goodTrades;
    }

    public int getBadTrades(){
        return badTrades;
    }

}