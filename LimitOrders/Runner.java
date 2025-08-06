public class Runner {

    private double deltaUp;
    private double deltaDown;
    private double dStarUp;
    private double dStarDown;
    private int mode; // -1 or +1
    private boolean initialized;
    private double extreme;
    private double reference;
    private double expectedDcLevel, expectedOsLevel;
    private double currentPrice;


    public Runner(double deltaUp, double deltaDown, double dStarUp, double dStarDown){
        this.deltaUp = deltaUp;
        this.deltaDown = deltaDown;
        this.dStarUp = dStarUp;
        this.dStarDown = dStarDown;
        this.initialized = false;
        this.mode = 1;
    }


    public int run(Price price){
        if(!initialized){
            initialized = true;
            extreme = reference = price.getMid();
            findExpectedDClevel();
            findExpectedOSlevel(price);
            return 0;
        }

        if( mode == -1 ){
            if( price.getBid() >= expectedDcLevel){
                mode = 1;
                extreme = reference = price.getBid();
                findExpectedDClevel();
                findExpectedOSlevel(price);
                return 1;
            }
            if( price.getAsk() < extreme ){
                extreme = price.getAsk();
                findExpectedDClevel();
                if( price.getAsk() < expectedOsLevel ){
                    reference = extreme;
                    findExpectedOSlevel(price);
                    return -2;
                }
            }
        }else if( mode == 1 ){
            if( price.getAsk() <= expectedDcLevel ){
                mode = -1;
                extreme = reference = price.getAsk();
                findExpectedDClevel();
                findExpectedOSlevel(price);
                return -1;
            }
            if( price.getBid() > extreme ){
                extreme = price.getBid();
                findExpectedDClevel();
                if( price.getBid() > expectedOsLevel ){
                    reference = extreme;
                    findExpectedOSlevel(price);
                    return 2;
                }
            }
            
        }
        return 0;
    }


    private void findExpectedDClevel(){
        if (mode == -1){
            // expectedDcLevel = Math.exp(Math.log(extreme) + deltaUp);
            expectedDcLevel = extreme * (1 + deltaUp);
        } else {
            // expectedDcLevel = Math.exp(Math.log(extreme) - deltaDown);
            expectedDcLevel = extreme * (1 - deltaDown);
        }
    }
    
    
    private void findExpectedOSlevel(Price price){

        currentPrice = getCurrentPrice(price);

        double commonPart = (currentPrice - expectedDcLevel) / expectedDcLevel ;

        if (mode == -1){
            // expectedOsLevel = Math.exp(Math.log(reference) - dStarDown);

            expectedOsLevel = commonPart / deltaDown;

        } else {
            // expectedOsLevel = Math.exp(Math.log(reference) + dStarUp);

            expectedOsLevel = commonPart / deltaUp;
        }



    }


    public double getExpectedDcLevel(){
        return expectedDcLevel;
    }


    public double getExpectedOsLevel(){
        return expectedOsLevel;
    }


    public double getExpectedUpperIE(){
        if (expectedDcLevel > expectedOsLevel){
            return expectedDcLevel;
        } else {
            return expectedOsLevel;
        }
    }


    public double getExpectedLowerIE(){
        if (expectedDcLevel < expectedOsLevel){
            return expectedDcLevel;
        } else {
            return expectedOsLevel;
        }
    }


    public int getMode(){
        return mode;
    }


    public double getDeltaUp() {
        return deltaUp;
    }


    public double getDeltaDown() {
        return deltaDown;
    }


    public double getdStarUp() {
        return dStarUp;
    }


    public double getdStarDown() {
        return dStarDown;
    }


    public int getUpperIEtype(){
        if (expectedDcLevel > expectedOsLevel){
            return 1;
        } else {
            return 2;
        }
    }


    public int getLowerIEtype(){
        if (expectedDcLevel < expectedOsLevel){
            return 1;
        } else {
            return 2;
        }
    }

    public double getCurrentPrice(Price price){
        double marketPrice = (getMode() == 1 ? price.getBid() : price.getAsk());
        return marketPrice;
    }


    public double getExtreme(){
        return extreme;
    }

    public double exitpoint(){
        double exitvalue;
        double OSV = getExpectedOsLevel();
        double gamma = 0.5;
        double modified_delta = deltaDown * gamma * (1 - OSV);
        exitvalue = extreme * (1 - modified_delta);
        return exitvalue;
    }


}

