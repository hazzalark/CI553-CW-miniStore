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


  public void doBuy()
  {
    String theAction = "";
    int    amount  = 1;                         
    try
    {
      if ( theState != State.checked )          
      {                                         
        theAction = "please check its availablity";
      } else {
        boolean stockBought =                   // Buy
          theStock.buyStock(                    // however
            theProduct.getProductNum(),         // may fail
            theProduct.getQuantity() );         
        if ( stockBought )                      
        {                                       
          makeBasketIfReq();                    // new basket
          theBasket.add( theProduct );          // add to bought
          sessionTotalSales += theProduct.getPrice() * theProduct.getQuantity(); // HL604 Update total sales
          theAction = "Purchased " +            
                  theProduct.getDescription();  
        } else {                                
          theAction = "!!! Not in stock";       
        }
      }
    } catch( StockException e )
    {
      DEBUG.error( "%s\n%s", 
            "CashierModel.doBuy", e.getMessage() );
      theAction = e.getMessage();
    }
    theState = State.process;                   
    setChanged(); notifyObservers(theAction);
  }
  
  public void doBought()
  {
    String theAction = "";
    try
    {
      if ( theBasket != null &&
           theBasket.size() >= 1 )            
      {                                       
        //sessionTotalSales += theBasket.getTotalPrice(); // HL604 Update total sales ** fixed issue.
        theOrder.newOrder( theBasket );       
        theBasket = null;                     
      }                                       
      theAction = "Start New Order";            
      theState = State.process;               
      theBasket = null;
    } catch( OrderException e )
    {
      DEBUG.error( "%s\n%s", 
            "CashierModel.doCancel", e.getMessage() );
      theAction = e.getMessage();
    }
    theBasket = null;
    setChanged(); notifyObservers(theAction); 
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
