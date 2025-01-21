package middle;

import catalogue.Basket;

import java.util.List;
import java.util.Map;
import middle.OrderException;

/**
  * Defines the interface for accessing the order processing system.
  * @author  Mike Smith University of Brighton
  * @version 2.0
  */

public interface OrderProcessing
{
                                                   // Used by
  public void newOrder(Basket bought)              // Cashier
         throws OrderException;

  public int  uniqueNumber()                       // Cashier
         throws OrderException;
   
  public Basket getOrderToPack()                   // Packer
         throws OrderException;
 
  public boolean informOrderPacked(int orderNum)   // Packer 
         throws OrderException;
         
  // not being used in this version
  public boolean informOrderCollected(int orderNum) // Collection
         throws OrderException;
   
  // not being used in this version
  public Map<String,List<Integer>> getOrderState() // Display
         throws OrderException;
  
  // HL604 - Added methods for recalling orders
  public int newPendingOrder(Basket basket) throws OrderException; // Stores unpaid orders  

  public Basket getPendingOrder(int orderNum) throws OrderException; // Retrieves unpaid orders  
}
