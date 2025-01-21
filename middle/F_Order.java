package middle;

import catalogue.Basket;
import debug.DEBUG;
import remote.RemoteOrder_I;

import java.rmi.Naming;
import java.util.List;
import java.util.Map;
// HL604 Recall
import java.util.HashMap;
import java.util.Map;

// There can only be 1 ResultSet opened per statement
// so no simultaneous use of the statement object
// hence the synchronized methods

/**
 * Facade for the order processing handling which is implemented on the middle tier.
 * This code is incomplete
 * @author  Mike Smith University of Brighton
 * @version 2.0
 */

public class F_Order implements OrderProcessing
{
  private RemoteOrder_I aR_Order    = null;
  private String        theOrderURL = null;
  private Map<Integer, Basket> pendingOrders = new HashMap<>(); // HL604 Store unpaid orders

  public F_Order(String url)
  {
    theOrderURL = url;
  }
  
  private void connect() throws OrderException
  {
    try                                            // Setup
    {                                              //  connection
      aR_Order =                                   //  Connect to
       (RemoteOrder_I) Naming.lookup(theOrderURL); // Stub returned
    }
    catch ( Exception e )                          // Failure to
    {                                              //  attach to the
      aR_Order = null;
      throw new OrderException( "Com: " + 
                               e.getMessage()  );  //  object
      
    }
  }


  public void newOrder( Basket bought )
         throws OrderException
  {
    DEBUG.trace("F_Order:newOrder()" );
    try
    {
      if ( aR_Order == null ) connect();
      aR_Order.newOrder( bought );
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }

  public int uniqueNumber()
         throws OrderException
  {
    DEBUG.trace("F_Order:uniqueNumber()" );
    try
    {
      if ( aR_Order == null ) connect();
      return aR_Order.uniqueNumber();
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }
  
  
  private int generateUniqueOrderNumber() { // HL604 Generate unique order number
	    return pendingOrders.size() + 1000; // HL604 Start order numbers from 1000
	}
  
  
  public int newPendingOrder(Basket basket) throws OrderException { // HL604 Store unpaid order
	    int orderNum = generateUniqueOrderNumber(); // HL604 Generate unique order number
	    pendingOrders.put(orderNum, basket); // HL604 Save the order
	    return orderNum; // Return order number for recall
	}
  
  
  public Basket getPendingOrder(int orderNum) throws OrderException { // HL604 Retrieve unpaid order
	    return pendingOrders.getOrDefault(orderNum, null); // Fetch order, return null if not found
	}

  /**
   * Returns an order to pick from the warehouse
   * if no order then returns null.
   * @return An order to pick
   */

  public synchronized Basket getOrderToPack()
         throws OrderException
  {
    DEBUG.trace("F_Order:getOrderTioPack()" );
    try
    {
      if ( aR_Order == null ) connect();
      return aR_Order.getOrderToPack();
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }

  /**
   * Informs the order processing system that the order has been
   * picked and the products are now on the conveyor belt to
   * the shop floor.
   */

  public synchronized boolean informOrderPacked( int orderNum )
         throws OrderException
  {
    DEBUG.trace("F_Order:informOrderPacked()" );
    try
    {
      if ( aR_Order == null ) connect();
      return aR_Order.informOrderPacked(orderNum);
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }

  /**
   * Informs the order processing system that the order has been
   * collected by the customer
   */

  public synchronized boolean informOrderCollected( int orderNum )
         throws OrderException
  {
    DEBUG.trace("F_Order:informOrderCollected()" );
    try
    {
      if ( aR_Order == null ) connect();
      return aR_Order.informOrderCollected(orderNum);
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }

  /**
   * Returns information about all orders in the order processing system
   */

  public synchronized Map<String, List<Integer> > getOrderState()
         throws OrderException
  {
    DEBUG.trace("F_Order:getOrderState()" );
    try
    {
      if ( aR_Order == null ) connect();
      return aR_Order.getOrderState();
    } catch ( Exception e )
    {
      aR_Order = null;
      throw new OrderException( "Net: " + e.getMessage() );
    }
  }
}
