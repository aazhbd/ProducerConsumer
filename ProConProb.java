import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ProduceInteger extends Thread {
   
   private HoldIntegerSynchronized pHold;
   private JTextArea output;

   public ProduceInteger( HoldIntegerSynchronized h,
                          JTextArea o )
   {
      super( "Producer" );
      pHold = h;
      output = o;
   }

   public void run()
   {
      for ( int count = 1; count <= 10; count++ ) {
         try {
            Thread.sleep( (int) ( Math.random() * 500 ) );
         }
         catch( InterruptedException e ) {
            System.err.println( e.toString() );
         }

         pHold.setSharedInt( count );
      }

      output.append("\n"+getName()+" Production Finished "+"\nEnding "+getName()+"\n");
   }
}

class ConsumeInteger extends Thread {
   private HoldIntegerSynchronized cHold;
   private JTextArea output;

   public ConsumeInteger( HoldIntegerSynchronized h, JTextArea o )
   {
      super( "Consumer" );
      cHold = h;
      output = o;
   }

   public void run()
   {
      int val, sum = 0;

      do {
         try {
            Thread.sleep( (int) ( Math.random() * 3000 ) );
         }
         catch( InterruptedException e ) {
            System.err.println( e.toString() );
         }

         val = cHold.getSharedInt();
         sum += val;
      } while ( val != 10 );

      output.append( "\n" + getName() + " All values consumed is: " + sum + "\nEnding " + getName() + "\n" );
   }
}

class HoldIntegerSynchronized {
   private int sharedInt[] = { -1, -1, -1, -1, -1 };
   private boolean writeable = true;
   private boolean readable = false;
   private int readLoc = 0, writeLoc = 0;
   private JTextArea output;

   public HoldIntegerSynchronized( JTextArea o )
   {
      output = o;
   }

   public synchronized void setSharedInt( int val )
   {
      while ( !writeable ) {
         try {
            output.append( " Producer is waiting " + val );
            wait();
         }
         catch ( InterruptedException e ) {
            System.err.println( e.toString() );
         }
      }

      sharedInt[ writeLoc ] = val;
      readable = true;

      output.append( "\nProduced " + val + " into cell " + writeLoc );

      writeLoc = ( writeLoc + 1 ) % 5;

      output.append( "\twrite " + writeLoc + "\tread " + readLoc);
      
      displayBuffer( output, sharedInt );

      if ( writeLoc == readLoc ) {
         writeable = false;
         output.append( "\nBUFFER FULL" );
      }

      notify();
   }

   public synchronized int getSharedInt()
   {
      int val;

      while ( !readable ) {
         try {
            output.append( " Consumer is waiting" );
            wait();
         }
         catch ( InterruptedException e ) {
            System.err.println( e.toString() );
         }
      }

      writeable = true;
      val = sharedInt[ readLoc ];

      output.append( "\nConsumed " + val + " from cell " + readLoc );

      readLoc = ( readLoc + 1 ) % 5;

      output.append( "\twrite " + writeLoc +
                     "\tread " + readLoc );
      displayBuffer( output, sharedInt );

      if ( readLoc == writeLoc ) {
         readable = false;
         output.append( "\nThe Buffer is Empty" );
      }

      notify();
      return val;
   }

   public void displayBuffer( JTextArea out, int buf[] )
   {
      DecimalFormat formatNumber = new DecimalFormat( " #;-#" );
      output.append( "\tbuffer: " );

      for ( int i = 0; i < buf.length; i++ )
         out.append( " " + formatNumber.format( buf[ i ] ));
   }
}


public class ProConProb extends JFrame {
   public ProConProb()
   {
      super( "Producer Consumer Problem Description -by Zakir" );
      JTextArea jtf = new JTextArea( 20, 30 );

      getContentPane().add( new JScrollPane( jtf ) );
      setSize( 600, 500 );
      setVisible(true);

      HoldIntegerSynchronized h = new HoldIntegerSynchronized( jtf );
      
      ProduceInteger p = new ProduceInteger( h, jtf );
      ConsumeInteger c = new ConsumeInteger( h, jtf );

      p.start();
      c.start();
   }

   public static void main( String args[] )
   {
      ProConProb app = new ProConProb();
      app.addWindowListener(
         new WindowAdapter() {
            public void windowClosing( WindowEvent e )
            {
               System.exit( 0 );
            }
         }
      );
   }
}
