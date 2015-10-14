
package client;

import constants.GlobalConstants.BookSide;
import driver.MainAutomatedTest;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import price.Price;
import price.PriceFactory;
import price.exceptions.InvalidPriceOperation;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.NoSuchProductException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.exceptions.InvalidMarketStateException;
import tradeprocessing.productservice.exceptions.ProductServiceException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;
import usercommand.exceptions.AlreadyConnectedException;
import usercommand.exceptions.InvalidConnectionIdException;
import usercommand.exceptions.UserCommandException;
import usercommand.exceptions.UserNotConnectedException;

public class UserSim implements Runnable {

    private User user;
    private boolean running = true;
    private boolean showDisplay = false;
    private int waitBase = 1500; // ms
    private long runDuration = 60000; // 1 min
    private int orderCount = 0;
    private int orderCxlCount = 0;
    private int quoteCount = 0;
    private int quoteCxlCount = 0;
    private int bookDepthCount = 0;

    public UserSim(long time, User u, boolean show) {
        user = u;
        runDuration = time;
        showDisplay = show;
    }

    @Override
    public void run() {
        System.out.println("Simulated user '" + user.getUserName() + "' starting trading activity - " + runDuration/1000 + " second duration.");
        long start = System.currentTimeMillis();
        try {
            user.connect();
            if (showDisplay) {
                user.showMarketDisplay();
            }

            while (running) {
                try {
                    doRandomEvent();
                } catch (Exception ex) {
                    Logger.getLogger(UserSim.class.getName()).log(Level.SEVERE, null, ex);
                }

                waitRandomTime();

                if ((System.currentTimeMillis() - start) > runDuration) {
                    running = false;
                }
            }

            MainAutomatedTest.simDone();


        } catch (UserCommandException | AlreadyConnectedException | UserNotConnectedException | InvalidConnectionIdException ex) {
            Logger.getLogger(UserSim.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        System.out.println("-----------------------");
        System.out.println(user.getUserName());
        System.out.println("Order Count: " + orderCount);
        System.out.println("Order CXL Count: " + orderCxlCount);
        System.out.println("Quote Count: " + quoteCount);
        System.out.println("Quote CXL Count: " + quoteCxlCount);
        System.out.println("Book Depth Count: " + bookDepthCount);
        System.out.println("-----------------------");
    }

    private void doRandomEvent() throws Exception {

        double num = Math.random();

        if (num < 0.70) // Quote
        {
            double next = Math.random();
            if (next < 0.85) {
                makeQuote();
            } else {
                makeQuoteCancel();
            }
        } else if (num < 0.9) // Order
        {
            double next = Math.random();
            if (next < 0.55) {
                makeOrder();

            } else {
                makeOrderCancel();
            }
        } else {
            makeBookDepth();
        }
    }

    private void makeBookDepth() throws Exception {
        ArrayList<String> list = user.getProductList();
        String product = list.get((int) (Math.random() * list.size()));
        String[][] bd = user.getBookDepth(product);
        //printBookDepth(bd);
        bookDepthCount++;
    }

    private void makeOrderCancel() throws Exception {
        ArrayList<TradeableUserData> list = user.getOrderIds();
        if (list.isEmpty()) {
            return;
        }
        TradeableUserData order = list.get((int) (Math.random() * list.size()));

        user.submitOrderCancel(order.getProduct(), order.getSide(), order
                .getID());
        orderCxlCount++;
    }

    private void makeOrder() throws Exception {
        ArrayList<String> list = user.getProductList();
        String product = list.get((int) (Math.random() * list.size()));
        BookSide side = makeRandomSide();
        Price p = makeRandomOrderPrice(side, product);
        int v = makeRandomVolume(product);

        user.submitOrder(product, p, v, side);
        orderCount++;
    }

    private void makeQuoteCancel() throws Exception {
        ArrayList<String> list = user.getProductList();
        String product = list.get((int) (Math.random() * list.size()));
        user.submitQuoteCancel(product);
        quoteCxlCount++;
    }

    private void makeQuote() throws Exception {
        ArrayList<String> list = user.getProductList();
        String product = list.get((int) (Math.random() * list.size()));
        Price bp = makeRandomPrice(BookSide.BUY, product);
        int bv = makeRandomVolume(product);

        Price sp = makeRandomPrice(BookSide.SELL, product);
        int sv = makeRandomVolume(product);



        try {
            if (bp.greaterOrEqual(sp)) {
                bp = sp.subtract(PriceFactory.makeLimitPrice("0.01"));
            }
            user.submitQuote(product, bp, bv, sp, sv);
            quoteCount++;
        } catch (IllegalArgumentException | InvalidPriceOperation | UserNotConnectedException | InvalidConnectionIdException | InvalidVolumeException | TradeableException | InvalidMarketStateException | NoSuchProductException | DataValidationException | InvalidMessageException | ProductBookSideException | ProductBookException | ProductServiceException | TradeProcessorPriceTimeImplException ex) {
            Logger.getLogger(UserSim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BookSide makeRandomSide() {
        if (Math.random() < 0.5) {
            return BookSide.BUY;
        } else {
            return BookSide.SELL;
        }
    }

    private Price makeRandomOrderPrice(BookSide side, String product) {

        if (Math.random() < 0.1) {
            return PriceFactory.makeMarketPrice();
        } else {
            return makeRandomPrice(side, product);
        }
    }

    private Price makeRandomPrice(BookSide side, String product) {

        double priceBase = (side == BookSide.BUY ? UserSimSettings.getBuyPriceBase(product) : UserSimSettings.getSellPriceBase(product));

        double price = priceBase * (1 - UserSimSettings.priceVariance);
        price += priceBase * (UserSimSettings.priceVariance * 2) * Math.random();

        return PriceFactory.makeLimitPrice(String.format("%.2f", price));
    }

    private int makeRandomVolume(String product) {
        int vol = (int) (UserSimSettings.getVolumeBase(product) * (1 - UserSimSettings.volumeVariance));
        vol += UserSimSettings.getVolumeBase(product) * (UserSimSettings.volumeVariance * 2) * Math.random();
        return vol;
    }

    private void waitRandomTime() {
        int waitTime = (int) ((0.75 * waitBase) + (0.5 * waitBase * Math.random()));

        synchronized (this) {
            try {
                wait(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserSim.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printBookDepth(String[][] bd) {
        int max = Math.max(bd[0].length, bd[1].length);

        System.out.println("----------------------------");
        System.out.print(String.format("%-16s%-16s%n", "BUY", "SELL"));
        for (int i = 0; i < max; i++) {
            System.out.print(String.format("%-16s", bd[0].length > i ? bd[0][i] : ""));
            System.out.println(String.format("%-16s", bd[1].length > i ? bd[1][i] : ""));
        }
        System.out.println("----------------------------");
    }
}
