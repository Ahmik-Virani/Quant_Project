public class AlphaEngine {

    private long goodTrades = 0;
    private long badTrades = 0;
    private double realizedProfit = 0;

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
        longCoastlineTraders[0] = new CoastlineTrader(0.0025, 1, "../log_files/long_0.csv");
        longCoastlineTraders[1] = new CoastlineTrader(0.005, 1, "../log_files/long_1.csv");
        longCoastlineTraders[2] = new CoastlineTrader(0.01, 1, "../log_files/long_2.csv");
        longCoastlineTraders[3] = new CoastlineTrader(0.015, 1, "../log_files/long_3.csv");
        shortCoastlineTraders = new CoastlineTrader[4];
        shortCoastlineTraders[0] = new CoastlineTrader(0.0025, -1, "../log_files/short_0.csv");
        shortCoastlineTraders[1] = new CoastlineTrader(0.005, -1, "../log_files/short_1.csv");
        shortCoastlineTraders[2] = new CoastlineTrader(0.01, -1, "../log_files/short_2.csv");
        shortCoastlineTraders[3] = new CoastlineTrader(0.015, -1, "../log_files/short_3.csv");
    }

    public long getGoodTrades(){

        for (CoastlineTrader coastlineTrader : longCoastlineTraders){
            goodTrades += coastlineTrader.getGoodTrades();
        }
        for (CoastlineTrader coastlineTrader: shortCoastlineTraders){
            goodTrades += coastlineTrader.getGoodTrades();
        }

        return goodTrades;
    }

    public long getBadTrades(){

        for (CoastlineTrader coastlineTrader : longCoastlineTraders){
            badTrades += coastlineTrader.getBadTrades();
        }
        for (CoastlineTrader coastlineTrader: shortCoastlineTraders){
            badTrades += coastlineTrader.getBadTrades();
        }

        return badTrades;
    }

    public double getRealizedProfit(){

        for (CoastlineTrader coastlineTrader : longCoastlineTraders){
            realizedProfit += coastlineTrader.getRealizedProfit();
        }
        for (CoastlineTrader coastlineTrader: shortCoastlineTraders){
            realizedProfit += coastlineTrader.getRealizedProfit();
        }

        return realizedProfit;
    }
}
