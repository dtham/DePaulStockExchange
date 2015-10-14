package driver;


import client.User;
import client.UserImpl;
import client.exceptions.UserException;
import constants.GlobalConstants.MarketState;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductAlreadyExistsException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.ProductService;
import tradeprocessing.productservice.exceptions.InvalidMarketStateTransitionException;
import tradeprocessing.productservice.exceptions.ProductServiceException;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorFactoryException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;
import usercommand.exceptions.AlreadyConnectedException;
import usercommand.exceptions.InvalidConnectionIdException;
import usercommand.exceptions.UserCommandException;
import usercommand.exceptions.UserNotConnectedException;


public class MainManualTest {

    public static CountDownLatch countDownLatch;

    public static void main(String[] args) {
        setupTradingSystem();
        manualTestMode();
    }


    private static void setupTradingSystem() {
        try {
            ProductService.getInstance().createProduct("IBM");
            ProductService.getInstance().createProduct("CBOE");
            ProductService.getInstance().createProduct("GOOG");
            ProductService.getInstance().createProduct("AAPL");
            ProductService.getInstance().createProduct("GE");
            ProductService.getInstance().createProduct("T");
            ProductService.getInstance().setMarketState(MarketState.PREOPEN); // Replace PREOPEN with your preresenation of PREOPEN
            ProductService.getInstance().setMarketState(MarketState.OPEN);  // Replace OPEN with your preresenation of OPEN
        } catch (MessagePublisherException | TradeableException | DataValidationException | ProductAlreadyExistsException | ProductBookException | ProductBookSideException | InvalidProductBookSideValueException | TradeProcessorFactoryException | InvalidMarketStateTransitionException | InvalidMessageException | OrderNotFoundException | InvalidVolumeException | ProductServiceException | TradeProcessorPriceTimeImplException ex) {
            Logger.getLogger(MainManualTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void manualTestMode() {

        String name = "REX";
        boolean goodName = false;
        String error = "";
        int errorCount = 0;
        do {
            name = JOptionPane.showInputDialog(null, error + "Enter your First name", "Name", JOptionPane.INFORMATION_MESSAGE);

            if (name == null) {
                System.out.println("No Name provided - Defaulting to 'REX'");
                name = "REX";
                goodName = true;
                JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
            } else if (name.matches("^[a-zA-Z]+$")) {
                goodName = true;
            } else {
                errorCount++;
                if (errorCount >= 3) {
                    System.out.println("Too many tried - Defaulting to 'REX'");
                    name = "REX";
                    goodName = true;
                    JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    error = "Names must consist of letters only - please try again.\n\n";
                }
            }
        } while (!goodName);


        JOptionPane.showMessageDialog(null, "User '" + name.toUpperCase() + "' and 'ANN' are connected.", "Users", JOptionPane.INFORMATION_MESSAGE);

        try {
          User user1 = new UserImpl(name.toUpperCase());
          User user2 = new UserImpl("ANN");
          user1.connect();
          user1.showMarketDisplay();
          user2.connect();
          user2.showMarketDisplay();
        } catch (UserCommandException | UserException | AlreadyConnectedException | UserNotConnectedException | InvalidConnectionIdException ex) {
            Logger.getLogger(MainManualTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
