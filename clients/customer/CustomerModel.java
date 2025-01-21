package clients.customer;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockException;
import middle.StockReader;

import javax.swing.*;
import java.util.Observable;
import middle.OrderException;

/**
 * Implements the Model of the customer client
 */
public class CustomerModel extends Observable
{
  private Product     theProduct = null;          // Current product
  private Basket      theBasket  = null;          // Bought items

  private String      pn = "";                    // Product being processed

  private StockReader     theStock     = null;
  private OrderProcessing theOrder     = null;
  private ImageIcon       thePic       = null;

  /*
   * Construct the model of the Customer
   * @param mf The factory to create the connection objects
   */
  public CustomerModel(MiddleFactory mf)
  {
    try                                          // 
    {  
      theStock = mf.makeStockReader();           // Database access
      theOrder = mf.makeOrderProcessing(); // HL604 Initialize order processing
    } catch ( Exception e )
    {
      DEBUG.error("CustomerModel.constructor\n" +
                  "Database not created?\n%s\n", e.getMessage() );
    }
    theBasket = makeBasket();                    // Initial Basket
  }
  
  /**
   * return the Basket of products
   * @return the basket of products
   */
  public Basket getBasket()
  {
    return theBasket;
  }

  /**
   * Check if the product is in Stock
   * @param productNum The product number
   */
  public void doCheck(String productNum )
  {
    theBasket.clear();                          // Clear s. list
    String theAction = "";
    pn  = productNum.trim();                    // Product no.
    int    amount  = 1;                         //  & quantity
    try
    {
      if ( theStock.exists( pn ) )              // Stock Exists?
      {                                         // T
        Product pr = theStock.getDetails( pn ); //  Product
        if ( pr.getQuantity() >= amount )       //  In stock?
        { 
          theAction =                           //   Display 
            String.format( "%s : %7.2f (%2d) ", //
              pr.getDescription(),              //    description
              pr.getPrice(),                    //    price
              pr.getQuantity() );               //    quantity
          pr.setQuantity( amount );             //   Require 1
          theBasket.add( pr );                  //   Add to basket
          thePic = theStock.getImage( pn );     //    product
        } else {                                //  F
          theAction =                           //   Inform
            pr.getDescription() +               //    product not
            " not in stock" ;                   //    in stock
        }
      } else {                                  // F
        theAction =                             //  Inform Unknown
          "Unknown product number " + pn;       //  product number
      }
    } catch( StockException e )
    {
      DEBUG.error("CustomerClient.doCheck()\n%s",
      e.getMessage() );
    }
    setChanged(); notifyObservers(theAction);
  }

  /**
   * Clear the products from the basket
   */
  public void doClear()
  {
    String theAction = "";
    theBasket.clear();                        // Clear s. list
    theAction = "Enter Product Number";       // Set display
    thePic = null;                            // No picture
    setChanged(); notifyObservers(theAction);
  }
  
  
  public void doPlaceOrder() {
	    if (theBasket != null && theBasket.size() > 0) { // HL604 Ensure basket has items
	        try {
	            int orderNum = theOrder.newPendingOrder(theBasket); // HL604 Store order as unpaid
	            setChanged();
	            notifyObservers("Order placed. Order Number: " + orderNum);
	            theBasket = makeBasket(); // HL604 Reset basket
	        } catch (OrderException e) {
	            setChanged();
	            notifyObservers(e.getMessage());
	        }
	    } else {
	        setChanged();
	        notifyObservers("Cannot place empty order.");
	    }
	}
  
  public void recallOrder(String orderNum) {
	    try {
	        Basket recalledBasket = theOrder.getPendingOrder(Integer.parseInt(orderNum)); // HL604 Fetch unpaid order
	        if (recalledBasket != null) {
	            theBasket = recalledBasket; // HL604 Load order into current basket
	            setChanged();
	            notifyObservers("Order " + orderNum + " loaded.");
	        } else {
	            setChanged();
	            notifyObservers("Order not found.");
	        }
	    } catch (OrderException e) {
	        setChanged();
	        notifyObservers(e.getMessage());
	    }
	}
  
  
  public void addOrder() {
	    try {
	        if (theBasket != null && !theBasket.isEmpty()) {
	            int orderNum = theOrder.newPendingOrder(theBasket); // HL604 Store unpaid order
	            theBasket = new Basket(); // HL604 Clear basket
	            setChanged();
	            notifyObservers("Order placed! Order Number: " + orderNum);
	        } else {
	            setChanged();
	            notifyObservers("Basket is empty. Add items first.");
	        }
	    } catch (OrderException e) {
	        setChanged();
	        notifyObservers(e.getMessage());
	    }
	}
  
  /**
   * Return a picture of the product
   * @return An instance of an ImageIcon
   */ 
  public ImageIcon getPicture()
  {
    return thePic;
  }
  
  /**
   * ask for update of view callled at start
   */
  private void askForUpdate()
  {
    setChanged(); notifyObservers("START only"); // Notify
  }

  /**
   * Make a new Basket
   * @return an instance of a new Basket
   */
  protected Basket makeBasket()
  {
    return new Basket();
  }
}

