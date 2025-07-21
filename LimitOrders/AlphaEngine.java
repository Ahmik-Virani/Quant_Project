public class AlphaEngine {

    private CoastlineTrader[] longCoastlineTraders;
    private CoastlineTrader[] shortCoastlineTraders;

    public AlphaEngine(){
        initiateTraders();
    }


    public void run(Price price){
        for (CoastlineTrader coastlineTrader : longCoastlineTraders){
            coastlineTrader.run(price);
        }
        for (CoastlineTrader coastlineTrader: shortCoastlineTraders){
            coastlineTrader.run(price);
        }
    }


    private void initiateTraders(){
        longCoastlineTraders = new CoastlineTrader[4];
        longCoastlineTraders[0] = new CoastlineTrader(0.0025, 1, "../log_files/long_0.log");
        longCoastlineTraders[1] = new CoastlineTrader(0.005, 1, "../log_files/long_1.log");
        longCoastlineTraders[2] = new CoastlineTrader(0.01, 1, "../log_files/long_2.log");
        longCoastlineTraders[3] = new CoastlineTrader(0.015, 1, "../log_files/long_3.log");
        shortCoastlineTraders = new CoastlineTrader[4];
        shortCoastlineTraders[0] = new CoastlineTrader(0.0025, -1, "../log_files/short_0.log");
        shortCoastlineTraders[1] = new CoastlineTrader(0.005, -1, "../log_files/short_1.log");
        shortCoastlineTraders[2] = new CoastlineTrader(0.01, -1, "../log_files/short_2.log");
        shortCoastlineTraders[3] = new CoastlineTrader(0.015, -1, "../log_files/short_3.log");
    }

}
