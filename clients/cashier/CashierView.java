package clients.cashier;

import catalogue.Basket;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockReadWriter;
import utils.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;


/**
 * View of the model 
 */
public class CashierView implements Observer
{
  private static final int H = 350;       // HL604 Increased height for total sales display
  private static final int W = 400;       

  private static final String CHECK  = "Check";
  private static final String BUY    = "Add";
  private static final String BOUGHT = "Clear";

  private final JLabel      pageTitle  = new JLabel();
  private final JLabel      theAction  = new JLabel();
  private final JTextField  theInput   = new JTextField();
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtCheck = new JButton( CHECK );
  private final JButton     theBtBuy   = new JButton( BUY );
  private final JButton     theBtBought= new JButton( BOUGHT );
  private final JButton 	theBtRefund = new JButton("Refund"); // HL604 Added refund button
  private final JTextField theRecallInput = new JTextField(); // HL604 Input for order number
  private final JButton theBtRecall = new JButton("Recall Order"); // HL604 Recall order button

  private JLabel totalSalesLabel; // HL604 Added label for total sales display
  
  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;
  private CashierController cont       = null;

  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-coordinate of position of window on screen 
   * @param y     y-coordinate of position of window on screen  
   */
  public CashierView(  RootPaneContainer rpc,  MiddleFactory mf, int x, int y  )
  {
    try                                           
    {      
      theStock = mf.makeStockReadWriter();        
      theOrder = mf.makeOrderProcessing();        
    } catch ( Exception e )
    {
      System.out.println("Exception: " + e.getMessage() );
    }
    Container cp         = rpc.getContentPane();    
    Container rootWindow = (Container) rpc;         
    cp.setLayout(null);                             
    rootWindow.setSize( W, H );                     
    rootWindow.setLocation( x, y );

    Font f = new Font("Monospaced",Font.PLAIN,12);  

    pageTitle.setBounds( 110, 0 , 270, 20 );       
    pageTitle.setText( "Thank You for Shopping at MiniStore" );                        
    cp.add( pageTitle );  

    theBtCheck.setBounds( 16, 25+60*0, 80, 40 );    
    theBtCheck.addActionListener(                   
      e -> { cont.doCheck( theInput.getText() ); 
    	 SoundPlayer.playSound("assets/button.wav"); // HL604 Plays sound
      });
    cp.add( theBtCheck );                           

    theBtBuy.setBounds( 16, 25+60*1, 80, 40 );      
    theBtBuy.addActionListener(                     
      e -> { cont.doBuy(); 
      SoundPlayer.playSound("assets/button.wav"); // HL604 Plays sound
      });
    cp.add( theBtBuy );                             

    theBtBought.setBounds( 16, 25+60*3, 80, 40 );   
    theBtBought.addActionListener(                  
      e -> { cont.doBought(); 
      SoundPlayer.playSound("assets/button.wav");
      });
    cp.add( theBtBought );                          

    theAction.setBounds( 110, 25 , 270, 20 );       
    theAction.setText( "" );                        
    cp.add( theAction );                            

    theInput.setBounds( 110, 50, 270, 40 );         
    theInput.setText("");                           
    cp.add( theInput );                             

    theSP.setBounds( 110, 100, 270, 160 );          
    theOutput.setText( "" );                        
    theOutput.setFont( f );                         
    cp.add( theSP );                                
    theSP.getViewport().add( theOutput );           

    // HL604 Added total sales label
    totalSalesLabel = new JLabel("Total Sales: £0.00");
    totalSalesLabel.setBounds(110, 285, 200, 20); 
    cp.add(totalSalesLabel);
    
    theBtRefund.setBounds(16, 25+60*4, 80, 40); // HL604 Positioned below "Bought"
    theBtRefund.addActionListener(e -> { 
        cont.doRefund(); // HL604 Calls refund function
        SoundPlayer.playSound("assets/button.wav");
    });
    cp.add(theBtRefund);
    
    theRecallInput.setBounds(110, 260, 100, 30);
    cp.add(theRecallInput);

    theBtRecall.setBounds(220, 260, 120, 30);
    theBtRecall.addActionListener(e -> { 
        cont.doRecallOrder(theRecallInput.getText()); // HL604 Calls recall function
        SoundPlayer.playSound("assets/button.wav");
    });
    cp.add(theBtRecall);

    rootWindow.setVisible( true );                  
    theInput.requestFocus();                        
  }

  /**
   * The controller object, used so that an interaction can be passed to the controller
   * @param c   The controller
   */
  public void setController( CashierController c )
  {
    cont = c;
  }

  /**
   * Update the view
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update(Observable modelC, Object arg) {
	    CashierModel model = (CashierModel) modelC;
	    String message = (String) arg;
	    theAction.setText(message);
	    
	    Basket basket = model.getBasket();
	    if (basket == null) {
	        theOutput.setText("Customers order");
	    } else {
	        theOutput.setText(basket.getDetails());
	    }

	    // HL604 Ensure total sales updates after refund
	    totalSalesLabel.setText("Total Sales: £" + String.format("%.2f", model.getSessionTotalSales()));
	}

}
