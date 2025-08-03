import java.util.List;
import java.util.ArrayList;

public class AlphaEnginePublic {

    public static void main(String[] args) {


        List<Price> priceFeed = new ArrayList<>();

        // Enter File Name and pass the priceFeed
        // Please download csv file from https://www.truefx.com/truefx-historical-downloads/
        // Only enter the file name not the path 
        // Please do not enter the file extension
        new ReadData("USDTRY-2025-03", priceFeed);
        
        
        AlphaEngine alphaEngine = new AlphaEngine();
        for (Price price : priceFeed){
            alphaEngine.run(price);
        }

        System.out.println("Total good trades = " + alphaEngine.getGoodTrades());
        System.out.println("Total bad trades = " + alphaEngine.getBadTrades());
    }

}