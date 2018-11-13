import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends Thread{
	private static BufferedReader in;
	private static PrintStream out;
	private static Socket socket;

	public Client() {
	}

	public void run() {
		try {
			ApriConnessione();
			apriCanaliComunicazione();
			Comunica();
		} catch (Exception e) {
		}
	}

	//APRO LA CONNESSIONE CON IL SERVER
	public static void ApriConnessione() {
		// open a socket connection
		try {
			socket = new Socket("localhost",4000);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//APRO I CANALI DI INPUT E OUTPUT PER COMUNICARE CON IL SERVER
	public static void apriCanaliComunicazione() {
		// Apre i canali I/O
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isNumeric(String s) {
		boolean numeric = true;

        try {
            Double num = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            numeric = false;
        }
        return numeric;
	}

	public static void Comunica() {
		try {
			boolean message=true;
			while (message) {
				Scanner input = new Scanner(System.in);
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - COSA VUOI FARE: \n 1) PRENOTAZIONE \n 2) FINE");
				String s = input.nextLine();
				//controllo l'input da tastiera, finchè non inserisce un valore accettato non può proseguire
				while(!(s.equals("1"))&&!(s.equals("2"))) {System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+"INPUT ERRATO, RITENTA");
					s = input.nextLine();
				}
				switch (s) {
					case "1":
						message = blockingMethod();
						break;

					case "2":
						message=false;
						out.close();
						in.close();
						break;

					default:
						message=false;
						out.close();
						in.close();
						break;
				}
			}

		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//STAMPO I POSTI DISPONIBILI
	public static void stampaPosti(String posti){
		String listaPosti[] = posti.split(";");
		int cont = 0;
		System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - ######### POSTI DISPONIBILI ##########");
		for(int i = 0; i < listaPosti.length; i++){
			if(cont < 10){
				System.out.print(listaPosti[i]+" ");
				cont++;
			}
			if(cont == 10){
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" ");
				cont = 0;
			}
		}
		System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+"\n - #####################################");
	}
	
	public static boolean contains(String[] array, String myString) {
		for(int i=0;i<array.length;i++) {
			if(array[i].equals(myString)) {
				return true;
			}
		}
		return false;
	}

	public static int freeSeat(ArrayList<Integer> list) {
		int number=0;
		for(int i=0;i<list.size();i++) {
			if(list.get(i)==0) {
				number++;
			}
		}
		
		return number;
		
	}
	
	//METODO CHE SI OCCUPA DI COMUNICARE CON IL SERVER, QUI AVVENGONO TUTTI GLI SCAMBI DI MESSAGGI
	public static boolean blockingMethod() {
		try {
			Scanner input = new Scanner(System.in);
			String message=null;

			//INVIO AL SERVER UN MESSAGGIO PER DIRGLI CHE VOGLIO LE SEZIONI DISPONIBILI
			out.println("Sezione");
			out.flush();

			//RICEVO DAL SERVER LE SEZIONI DISPONIBILI
			message = in.readLine();
			if (message.equals("tempo scaduto")){
			    throw new MyException();
			}

			System.out.println(+new Exception().getStackTrace()[0].getLineNumber() + " - SCEGLI LA SEZIONE: " + message);
			String sezioni[] = message.split(";");
						
			//SCELGO LA SEZIONE DESIDERATA
			String L = input.nextLine();
			//CONTROLLO GLI INPUT ( finchè non iseriche un valore contenuto nell'array non può proseguire)
			while(!contains(sezioni,L.toUpperCase())) {System.out.println(new Exception().getStackTrace()[0].getLineNumber()+"INPUT ERRATO, SEZIONE '"+L.toUpperCase()+"' NON TROVATA");
				L = input.nextLine();
			}
			out.println(L.toUpperCase());
			out.flush();

			System.out.println("VUOI SCEGLERE IL POSTO? (si/no)");
			String rand = input.nextLine();
			//CONTROLLO CHE GLI INPUT SIANO LIMITATI A SI O NO
			while(!(rand.toLowerCase().equals("si"))&&!(rand.toLowerCase().equals("no"))) {System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+"INPUT ERRATO, RITENTA");
			rand = input.nextLine();
			}

			switch (rand.toLowerCase()){
                case "si":
                    out.println(rand.toLowerCase());
                    out.flush();
                    boolean scegli=true;
                	while(scegli) {
	                    //RICEVO DAL SERVER I POSTI DELLA SEZIONE
	                    //VENGONO VISUALIZZATI A VIDEO TRAMITE UN'INTERFACCIA GRAFICA
	                    message = in.readLine();
	                    String posti[] = message.split(";");
	                    GraphicMenu m=new GraphicMenu(fromStringtoArrayList(posti),L);
	                    stampaPosti(message);
	
	                    //NUMERO DI POSTI CHE SI VOGLIONO PRENOTARE
	                    System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" QUANTI POSTI VUOI PRENOTARE? (DISPONIBLI "+freeSeat(fromStringtoArrayList(posti))+")");
	                    String nPosti = input.nextLine();
	                    //controllo gli input
	                    while(!isNumeric(nPosti)||!(Integer.parseInt(nPosti)<(freeSeat(fromStringtoArrayList(posti))+1))||!(Integer.parseInt(nPosti)<11)) {
		                    if(!isNumeric(nPosti)) {
		                    	//SE NON E' UN NUMERO
		                    	System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+"VALORE NON NUMERICO, RIPROVA");
		                    }else{
		                    	//SE IL NUMERO E' TROPPO GRANDE
		                    	System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+"NUMERO POSTI TROPPO GRANDE, RIPROVA");
		                    }	
		                    nPosti = input.nextLine();
	        			}
	                    int n = Integer.parseInt(nPosti);
	
	                    //DATO IL NUEMRO DI POSTI N, SCELGO I POSTI
	                    ArrayList<Integer> list = scegliposti(n, posti,fromStringtoArrayList(posti).size());
	
	                    //INVIO AL SERVER I POSTI SELEZIONATI CHE VOGLIO PRENOTARE
	                    String postiDaPrenotare = FromArrayListToString(list);
	                    System.out.println(+new Exception().getStackTrace()[0].getLineNumber() + " - " +postiDaPrenotare);
	                    m.setInvisible();
	                    out.println(postiDaPrenotare);
	                    out.flush();
	
	                    //CONFERMA DI PRENOTAZIONE DA PARTE DEL SERVER
	                    message = in.readLine();
	                    System.out.println(message);
	                    if (message.equals("ERRORE NELLA PRENOTAZIONE DEI POSTI")) {
	                        
	                    }else{
	                    	scegli=false;
	                    }
	                	
                	}
                    break;

                case "no":
                    out.println(rand.toLowerCase());
                    out.flush();

                    //RICEVO DAL SERVER IL POSTO CHE HA SCELTO A CASO
                    String posto = in.readLine();
                    System.out.println("POSTO SELEZIONATO: "+ posto);

                    break;
            }

			///////////////////////////REGISTRAZIONE DEL CLIENT //////////////////////////////
			//INIZIO DELLA REGISTRAZIONE DELL'UTENTE
			//RICEVO DAL SERVER LO START PER LA REGISTRAZIONE DELLA PRENOTAZIONE
			String start = in.readLine();
			if (start.equals("tempo scaduto")){
				//SE RICEVO TEMPO SCADUTO INVIO UN MESSAGGIO DI FINE AL SERVER
                //QUESTO MESSAGGIO ARRIVA IN CORRISPONDENZA DEL MESSAGGIO DEL NOME UTENTE
                out.println("fine");
                out.flush();

                //PER RICEVERE IL MESSAGGIO START CHE è STATO INVIATO DA TASK2
                System.out.println(in.readLine());

			    throw new MyException();
			}
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber() + " - " + start);

			//NOME UTENTE
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - INSERISCI IL TUO NOME");
			String nome = input.nextLine();
			out.println(nome);
			out.flush();

			//COGNOME UTENTE
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - INSERISCI IL TUO COGNOME");
			String cognome = input.nextLine();
			out.println(cognome);
			out.flush();

			//ETA UTENTE
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - INSERISCI LA TUA ETA");
			String eta = input.nextLine();
			out.println(eta);
			out.flush();

			//RICEVO CONFERMA DAL SERVER CHE LA REGISTRAZIONE E' COMPLETATA
			String ack = in.readLine();
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - FINE REGISTRAZIONE:" + ack);
			if (ack.equals("tempo scaduto")) {
                throw new MyException();
            }
			//LA REGISTRAZIONE è TERMINATA
			//////////////////////////////////////////////////////////////////////////////////

		}catch (MyException o) {
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - TEMPO SCADUTO 2");
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - VUOI CONTINUARE 2?");
			Scanner input = new Scanner(System.in);
			String s = input.nextLine();
			if(s.toLowerCase().equals("si")){
				return true;
			} else {
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - ARRIVEDERCI");
				return false;
			}
		} catch (IOException e) {
			out.flush();
			//SE CI METTO TROPPO TEMPO IL SERVER CHIUDE LA SOCKET, QUINDI VIENE CREATA UNA SOCKETEXCEPTION
			//DA QUI POSSO SCEGLIERE SE CONTINUARE (QUINDI APRIRE UNA NUOVA CONNESSIONE)
			//O TERMINARE L'ESECUZIONE
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - TEMPO SCADUTO");
			System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - VUOI CONTINUARE?");
			Scanner input = new Scanner(System.in);
			String s = input.nextLine();
			if(s.toLowerCase().equals("si")){
				ApriConnessione();
				apriCanaliComunicazione();
			} else {
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - ARRIVEDERCI");
				return false;
			}
		}

		return true;

	}

	//DALLA STRINGA CREO L'ARRAY LIST EQUIVALENTE
	public static ArrayList<Integer> fromStringtoArrayList(String[] stringa){
		ArrayList<Integer> array= new ArrayList<Integer>();
		for(int i=0;i<stringa.length;i++) {
			array.add(Integer.parseInt(stringa[i]));
		}
		return array;
	}

	//DALL'ARRAY LIST CREO LA LISTA EQUIVALENTE
	public static String FromArrayListToString(ArrayList<Integer> list) {
		String nPostiForServer = "";
		for (int j = 0; j < list.size(); j++) {
			nPostiForServer = nPostiForServer + list.get(j) + ";";
		}
		return nPostiForServer;
	}

	//DATO IL NUMERO DI POSTI E LA SEZIONE 
	//L'UTENTE SCEGLIE I POSTI
	public static ArrayList<Integer> scegliposti(int n, String[] posti, int k) {
		String posto = null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		Scanner input = new Scanner(System.in);
		boolean flag = false;
		for (int i = 0; i < n; i++) {
			do {
				flag = false;
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - PRENOTO IL POSTO NUMERO " + i);
				System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - QUALE POSTO VUOI PRENOTARE?");
				posto = input.nextLine();

				//CONTROLLO SE IL POSTO SELEZIONATO è PRESENTE TRA QUELLI DISPONIBILI
				if (isNumeric(posto) && Integer.parseInt(posto)<k) {
					try {
						if(posti[Integer.parseInt(posto)].equals("1")) {
							flag = true;
						} else {
							list.add(Integer.parseInt(posto));
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}else{
					System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - POSTO NON DISPONIBILE, RIPROVARE..");
					flag = true;
				}
			}while(flag);
		}
		return list;
	}

	//MAIN
	public static void main(String argv[]) {
		Client c=new Client();
		c.start();
	}
}
