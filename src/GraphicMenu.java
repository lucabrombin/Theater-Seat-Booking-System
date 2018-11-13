import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class GraphicMenu extends JFrame implements ActionListener{
	//ARRAYLIST CON I POSTI 
    private static ArrayList<Integer> seatArrayList = new ArrayList<Integer>();

    public static void main(String [] argv) {
    }
    
    //METODO CHE DATI I POSTI CALCOLA CHE ALTEZZA DEVE AVERE IL PANNELLO PER CONTENERE TUTTI I POSTI (10 x riga)
    public int Getsize(ArrayList<Integer> i) {
        return (int)Math.ceil((double)(i.size()/10)+2)*40;
    }
    
    public void setInvisible() {
    	setVisible(false);
    }

    //COSTRUTTORE
    public GraphicMenu(ArrayList<Integer> i,String name)	// Constructor Method for GUI
    {
        this.seatArrayList=i;
        int posti= freeSeat(seatArrayList);					//calcola i posti liberi      
        setAlwaysOnTop(true);								//resta in primo piano	
        setLocationRelativeTo(null);        				//cancella i riferimenti spaziali
        setTitle("SEZIONE "+name+"   - Posti disponibili: "+ posti);							//setta il titolo
        setSize(500,Getsize(i));							//definisce la dimensione del jframe
        setResizable(false);								//fa in modo che le diemnsioni non varino a seconda della risoluzione dell'ultente
        setDefaultCloseOperation(EXIT_ON_CLOSE);			//definisce cosa succede quando clicchi sulla x per uscire
        setVisible(true);									//fa in modo che il jframe sia visibile
    }

    public void actionPerformed (ActionEvent action)	
    {}
    
    public static int freeSeat(ArrayList<Integer> list) {
		int number=0;
		for(int i=0;i<list.size();i++) {
			if(list.get(i)==0) {
				number++;
			}
		}
		
		return number;
		
	}

    public void paint(Graphics g) {
    	
        ArrayList<Integer> list = seatArrayList;
        
        
        super.paint(g);										// Clears the frame when method is called

        int width = 32;										//Definisco le dimensioni dei rettangoli
        int height = 32;									
        			
        int cordX = 15;										//definisco le posizioni di partenza del primo blocco
        int cordY = 40;		
        
        int cont=0;											//contatore per il numero di riga
        for(int i=0;i<list.size();i++) {
        	
            if(cont<10) {									//caso in cui non ho ancora 10 posti nella riga
                cordX=cordX+40;
                if(list.get(i)==0) {						//se il posto è libero lo coloro
                    g.setColor(Color.green);
                }
                else if(list.get(i)==1){					//se il posto è libero lo coloro
                    g.setColor(Color.red);
                }
                //creo i rettangoli 	
                g.fillRect(cordX, cordY, width, height);		
                //scrivo i numeri in nero
                g.setColor(Color.black);
                //definisco posizioni diverse in funzione al numero di cifre che ha il posto
                if(i<10) {
                    g.drawString(Integer.toString(i), cordX+11, cordY+19);
                }
                if(i>9&&i<100) {
                    g.drawString(Integer.toString(i), cordX+8, cordY+19);
                }
                if(i>99&&i<1000) {
                    g.drawString(Integer.toString(i), cordX+6, cordY+19);
                }
                
                //incremento il contatore dei posti
                cont++;
            	}
            
            if(cont==10) {									//caso in cui sia l'ultimo posto
                cont=0;										//resetto il contatore
                cordX = 15;							//riparto dalla posizione iniziale della colonna
                cordY= cordY+40;			//passo ad una nuova riga
            }
        }

    }
}

