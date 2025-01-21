package clients.cashier;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.*;

import java.util.Observable;

/**
 * Implements the Model of the cashier client
 */
public class CashierModel extends Observable
{
  private enum State { process, checked }

  private State       theState   = State.process;   // Current state
  private Product     theProduct = null;            // Current product
  private Basket lastPurchasedOrder = null; // HL604 Stores the last completed order
  private Basket      theBasket  = null;            // Bought items

  private String      pn = "";                      // Product being processed

  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;
  
  private double sessionTotalSales = 0.0;  // HL604 Track total sales

  /**
   * Construct the model of the Cashier
   * @param mf The factory to create the connection objects
   */
  public CashierModel(MiddleFactory mf)
  {
    try                                           // 
    {      
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      DEBUG.error("CashierModel.constructor\n%s", e.getMessage() );
    }
    theState   = State.process;                  // Current state
  }
  
  /**
   * Get the Basket of products
   * @return basket
   */
  public Basket getBasket()
  {
    return theBasket;
  }

  /**
   * Buy the product
   */
  public void doBuy() {
	    String theAction = "";
	    try {
	        if (theState != State.checked) {
	            theAction = "Please check availability first.";
	        } else {
	            boolean stockBought = theStock.buyStock(theProduct.getProductNum(), theProduct.getQuantity());
	            if (stockBought) {
	                makeBasketIfReq();
	                sessionTotalSales += theProduct.getPrice() * theProduct.getQuantity(); // HL604 Update total sales
	                theBasket.add(theProduct);
	               
	                
	                theAction = "Purchased " + theProduct.getDescription();
	            } else {
	                theAction = "Not enough stock available!";
	            }
	        }
	    } catch (StockException e) {
	        theAction = e.getMessage();
	    }

	    setChanged();
	    notifyObservers(theAction);
	}
  
  
  public void doRefund() { // HL604 Refund last completed order
	    String theAction = "";

	    if (lastPurchasedOrder != null && lastPurchasedOrder.size() > 0) { // HL604 Check if there is an order to refund
	        try {
	            for (Product product : lastPurchasedOrder) { // HL604 Loop through all products in last order
	                theStock.addStock(product.getProductNum(), product.getQuantity()); // HL604 Return each product to stock
	            }
	            
	            sessionTotalSales -= lastPurchasedOrder.getTotalPrice(); // HL604 Deduct entire order from total sales
	            lastPurchasedOrder = null; // HL604 Clear last order
	            
	            theAction = "Last order refunded successfully!";
	        } catch (StockException e) {
	            theAction = e.getMessage();
	        }
	    } else {
	        theAction = "No completed order to refund!";
	    }

	    setChanged();
	    notifyObservers(theAction);
	}

  
  public void doBought() {
	    String theAction = "";
	    try {
	        if (theBasket != null && theBasket.size() >= 1) { // Ensure there is an order to process
	            theOrder.newOrder(theBasket); // Process order
	            
	            lastPurchasedOrder = theBasket; // HL604 Save the last completed order
	            
	            theBasket = null; // Reset basket for next order
	        }
	        theAction = "Start New Order";
	    } catch (OrderException e) {
	        theAction = e.getMessage();
	    }

	    setChanged();
	    notifyObservers(theAction);
	}
  
  
  
  public void recallOrder(String orderNum) {
	    if (orderNum == null || orderNum.trim().isEmpty()) { // HL604 Validate input
	        setChanged();
	        notifyObservers("Please enter a valid order number.");
	        return;
	    }

	    try {
	        int orderID = Integer.parseInt(orderNum.trim()); // HL604 Parse order number safely
	        Basket recalledBasket = theOrder.getPendingOrder(orderID); // HL604 Fetch unpaid order
	        if (recalledBasket != null) {
	            theBasket = recalledBasket; // HL604 Load order into current basket
	            
	            // HL604 Correctly update total sales by iterating over all products
	            for (Product product : theBasket) {
	                sessionTotalSales += product.getPrice() * product.getQuantity();
	            }

	            setChanged();
	            notifyObservers("Order " + orderNum + " loaded.");
	        } else {
	            setChanged();
	            notifyObservers("Order not found.");
	        }
	    } catch (NumberFormatException e) {
	        setChanged();
	        notifyObservers("Invalid order number. Please enter a number.");
	    } catch (OrderException e) {
	        setChanged();
	        notifyObservers(e.getMessage());
	    }
	}

  /**
   * Get total sales for the session
   * @return Total sales amount
   */
  public double getSessionTotalSales() { // HL604 Method to retrieve total sales
      return sessionTotalSales;
  }
  
  public void askForUpdate() { // HL604 Added missing method
	    setChanged();
	    notifyObservers("Welcome");
	}
  
  public void doCheck(String productNum) {
	    String theAction = "";
	    theState = State.process;
	    pn = productNum.trim();
	    int amount = 1;
	    try {
	        if (theStock.exists(pn)) {
	            Product pr = theStock.getDetails(pn);
	            if (pr.getQuantity() >= amount) {
	                theAction = String.format("%s : %7.2f (%2d) ", pr.getDescription(), pr.getPrice(), pr.getQuantity());
	                theProduct = pr;
	                theProduct.setQuantity(amount);
	                theState = State.checked;
	            } else {
	                theAction = pr.getDescription() + " not in stock";
	            }
	        } else {
	            theAction = "Unknown product number " + pn;
	        }
	    } catch (StockException e) {
	        DEBUG.error("CashierModel.doCheck\n%s", e.getMessage());
	        theAction = e.getMessage();
	    }
	    setChanged();
	    notifyObservers(theAction);
	}
  



  
  private void makeBasketIfReq() {
	    if (theBasket == null) {
	        try {
	            int uon = theOrder.uniqueNumber();
	            theBasket = makeBasket();
	            theBasket.setOrderNum(uon);
	        } catch (OrderException e) {
	            DEBUG.error("Comms failure\nCashierModel.makeBasketIfReq()\n%s", e.getMessage());
	        }
	    }
	}
  protected Basket makeBasket() { // HL604 Added missing method
	    return new Basket();
	}
  
}
