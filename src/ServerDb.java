import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import java.io.File;

public class ServerDb extends Thread {

	private static ArrayList<ArrayList<ReentrantLock>> seatLock = new ArrayList<ArrayList<ReentrantLock>>();
	private static ArrayList<ReentrantLock> Locks = new ArrayList<ReentrantLock>();
	
    private static ArrayList<ArrayList<Integer>> table = new ArrayList<ArrayList<Integer>>();
    private static String[] file;
    private ServerSocket Server;
    private static ExecutorService executor;
    private static ExecutorService executor2;
    private static File m;

    //MAIN
    public static void main(String argv[]) throws Exception {
        executor = Executors.newFixedThreadPool(5);
        m= new File(".\\File");
        file=cercafile(m);
        //file = new String[] { "A", "B", "C", "D", "E", "F", "G" };
        carica(file);
        creaLock();

        // metodo facoltativo per resettare il Db
        // reset(table);
        //System.out.println(table.get(0));
        new ServerDb();
    }

    public static String[] cercafile(File cartella) {

        File[] listOfFiles = cartella.listFiles();
        int cont=0;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if(listOfFiles[i].getName().substring(listOfFiles[i].getName().length()-3).equals("txt")) {
                    cont++;
                }
                //  System.out.println("File " + listOfFiles[i].getName());
            }
        }

        String[] nomi=new String[cont];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if(listOfFiles[i].getName().substring(listOfFiles[i].getName().length()-3).equals("txt")) {
                    nomi[i]=listOfFiles[i].getName().substring(0,listOfFiles[i].getName().length()-4);
                }
                //   System.out.println("File " + nomi[i]);
            }
        }
        return nomi;
    }

    public static File getDirectory(){
        return m;
    }

    // COSTRUTTORE DEL THREAD
    public ServerDb() throws Exception {
        Server = new ServerSocket(4000);
        System.out.println("Il Server e in attesa sulla porta 4000.");
        this.start();
    }

    // METODO RUN DEL THREAD
    public void run() {
        while(true) {
            try {
                System.out.println("In attesa di Connessione.");
                Socket client = Server.accept();
                System.out.println("Connessione accettata da: " + client.getInetAddress());
                Runnable connect = new connectionThread(client);
                executor.execute(connect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // CREO UN LOCK PER OGNI SETTORE (file .txt)
    public static void creaLock() {
    	//INIZIALIZZO IL LOCK DELLE SEZIONI
        Locks.clear();
        Locks.add(new ReentrantLock());
        for (int i = 0; i < table.size(); i++) {
            Locks.add(new ReentrantLock());
        }
    	
    	//INIZIALIZZO IL LOCK DEI POSTI
    	//TABLE.SIZE MI DA IL NUMERO DI FILE (SEZIONI)
        ArrayList<ReentrantLock> temp = new ArrayList<ReentrantLock>();
        for (int i = 0; i < table.size(); i++) {
        	temp.clear();
        	//TABLE.GET.SIZE MI DA IL NUMERO DI POSTI DELLA SEZIONE I-ESIMA
        	if(table.get(i)!=null) {
	            for (int j = 0; j < table.get(i).size(); j++) {
	            	temp.add(new ReentrantLock());
	            }            
	            seatLock.add(temp);
        	}
        }
        
    }

    // LIBERO TUTTI I POSTI E AGGIORNO I FILE .txt
    public static void reset(ArrayList<ArrayList<Integer>> lista) throws IOException {
        for (int i = 0; i < lista.size(); i++) {
            for (int m = 0; m < lista.get(i).size(); m++) {
                lista.get(i).set(m, 0);
            }
            connectionThread.aggiornafile(i, lista.get(i));
        }
    }


    // VADO A CREARE LA TABELLA DEI POSTI
    // UNA LISTA PER OGNI SEZIONE CHE VERRANNO CARICATE DAL RELATIVO FILE
    // LE LISTE VENGONO MESSE ALL'INTERO DI UN'ALTRA LISTA (TABLE)
    public static void carica(String[] nome) {
        table.add(null);
        for (int i = 0; i < nome.length; i++) {
            try {
                BufferedReader br = new BufferedReader(
                        new FileReader(m+"\\"+ nome[i] + ".txt"), 10);
                try {
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    String line = null;
                    int foo = -10;
                    String[] total = null;
                    while ((line = br.readLine()) != null) {
                        String posti[] = line.split(";");
                        for (int m = 0; m < posti.length; m++) {
                            foo = Integer.parseInt(posti[m]);
                            list.add(foo);
                        }
                    }
                    table.add(list);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    

    public static String[] getFile() {
        return file;
    }

    // PRENDO LA LISTA DEI POSTI DI UNA SEZIONE INTERESSATA
    public static ArrayList<Integer> prendi(int i) {
        return table.get(i);
    }

    // PRENDO UN LOCK DI UNA SEZIONE INTERESSATA
    public static ReentrantLock prendilock(int i) {
        if (i > Locks.size()) {
            System.out.println("DIMENSIONE RICHIESTA ERRATA" + "NON ESISTE IL LOCK NUMERO " + i);
        }
        return Locks.get(i);
        //return Locks.get(i);
    }
    
    // PRENDO UN LOCK DI UNA SEZIONE INTERESSATA
    public static ArrayList<ReentrantLock> prendiSeatLock(int i) {
        if (i > Locks.size()) {
            System.out.println("DIMENSIONE RICHIESTA ERRATA" + "NON ESISTE IL LOCK NUMERO " + i);
        }
        return seatLock.get(i);
        //return Locks.get(i);
    }

}

class connectionThread implements Runnable {
    private static BufferedReader br = null;
    private List<String> list;
    private Socket client = null;
    BufferedReader in = null;
    PrintStream out = null;

    String section = "0";

    public connectionThread() {
    }

    // OGNI QUAL VOLTA VIENE ACCETTATA UNA CONNESSIONE DI UN CLIENT VIENE CREATO UN
    // THREAD
    public connectionThread(Socket clientSocket) {
        this.client = clientSocket;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintStream(client.getOutputStream(), true);
        } catch (Exception e1) {
            try {
                client.close();
            } catch (Exception e) {
                System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+e.getMessage());
            }
            return;
        }
    }

    public void run() {
        System.out.println("Connessione con " + Thread.currentThread().getName());
        try {
            String message;
            //out.println("BENVENUTO NEL SERVIZIO DI PRENOTAZIONE");
            //out.flush();
            while (true) {
                while (true) {
                    // ricevo dal client: TIPO DI MESSAGGIO
                    System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"IN ATTESA DEL MESSAGGIO DEL CLIENT");
                    message = in.readLine();
                    System.out.println(new Exception().getStackTrace()[0].getLineNumber()+" - "+message);
                    switch (caseSelection(message)) {
                        case "Spettacolo":
                            stampa("--SPETTACOLO--");
                            //out.println("Disponibilita_Sezione");
                            //out.flush();
                            break;

                        case "Sezione":
                            stampa("--SEZIONE--");
                            out.println(fromArrayToString(ServerDb.getFile()));
                            out.flush();
                            action();
                            break;

                        case "TEMPO SCADUTO":
                            System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"TEMPO SCADUTO");
                            break;

                        default:
                            System.out.println("MESSAGGIO RICEVUTO IN DEFAULT: " + message);
                            break;
                    }
                }
                // chiude gli stream e le connessioni
                // out.close();
                // in.close();
                // client.close();
            }
        } catch (Exception exc) {
        }
    }

    //DALL'ARRAY DI STRINGHE VADO A CREARE LA STRINGA EQUIVALENTE
    public String fromArrayToString(String[] stringa) {
        String sol = "";

        for (int i = 0; i < stringa.length; i++) {
            sol = sol + stringa[i] + ";";
        }

        return sol.substring(0, sol.length() - 2);
    }

    // CREA LA STRINGA PER LA SELEZIONE DEL CASE DELLO SWITCH
    // I LE SEZIONI AVRANNO UN SOLO CASE, VIENE UTILIZZATA LA STRINGA SEZIONE
    // GLI ALTRI CASE RIMANGONO INVARIATI
    public String caseSelection(String message) {
        String temp;
        if (message.equals("A") || message.equals("B") || message.equals("C") || message.equals("D")
                || message.equals("E") || message.equals("F") || message.equals("G")) {
            temp = "SEZIONE";
        } else {
            temp = message;
        }
        return temp;
    }

    // AGGIORNO IL FILE CONTENENTE I POSTI DI UNA SEZIONE, VADO A SETTARE A 1 I
    // POSTI PRENOTATI DA UN CLIENT
    public static void aggiornafile(int n, ArrayList<Integer> array) throws IOException {
        String posti = "";

        for (int i = 0; i < array.size(); i++) {
            posti = posti + array.get(i) + ";";
        }

        try{
            if(ServerDb.prendilock(n).isLocked()){
                System.out.println("Aspetto che un altro utente possa finire");
            }

            ServerDb.prendilock(n).lock();

            FileWriter w;
            String nome = riconverti(n);

            System.out.println("AGGIORNO IL FILE DELLA SEZIONE: " + nome);
            w = new FileWriter(".\\File\\" + nome + ".txt");
            w.write(posti);
            w.flush();
            w.close();
            System.out.println("FINITO");
        }finally {
            ServerDb.prendilock(n).unlock();
        }
    }

    //AGGIORNO LA LISTA DEI POSTI DI UNA SEZIONE, VADO A SETTARE A 1 I POSTI
    //PRENOTATI DA UN CLIENT
    public static boolean pre_commit(int n, String message) throws IOException {
        //POSTI DA PRENOTARE
        String posti[] = message.split(";");

        //PRENOTO I POSTI SELEZIONATI DALL'UTENTE
        //AGGIORNO I LA LISTA DEI POSTI DELLA RELATIVA SEZIONE
        boolean conferma = false;
        
        for (int m = 0; m < posti.length; m++) {
        	if(ServerDb.prendiSeatLock(n).get(Integer.parseInt(posti[m])).isLocked()){
                System.out.println("##########-POSTO NON DISPONIBILE-##########");
                return true;
            }
        	
        	ServerDb.prendiSeatLock(n).get(Integer.parseInt(posti[m])).lock();
        	ServerDb.prendi(n).set(Integer.parseInt(posti[m]), 1);
        }
      
        System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"POSTI PRENOTATI CON SUCCESSO NELLA SEZIONE - "+riconverti(n));

        return conferma;
    }

    //QUANDO UN UTENTE NON COMPLETA CORRETTAMENTE LA REGISTRAZIONE, VADO A LIBERARE I POSTI CHE AVEVA SELEZIONATO
    public static void libera(int n, String message) throws IOException {
        //POSTI DA LIBERARE
        String posti[] = message.split(";");

        try {
            if (ServerDb.prendilock(n).isLocked()) {
                System.out.println("Aspetto che un altro utente possa finire");
            }

            ServerDb.prendilock(n);

            //SETTO A 0 I POSTI CHE AVEVO OCCUPATO PRECEDENTEMENTE
            int foo = -100;
            for (int m = 0; m < posti.length; m++) {
                foo = Integer.parseInt(posti[m]);
                ServerDb.prendi(n).set(foo, 0);
            }

            //CREO LA NUOVA STRINGA DEI POSTI
            /*
            String temp = "";
            for (int i = 0; i < ServerDb.prendi(n).size(); i++) {
                temp = temp + ServerDb.prendi(n).get(i) + ";";
            }*/

            //SCRIVO SUL FILE I POSTI AGGIORNATI
            /*FileWriter w;
            String nome = riconverti(n);
            System.out.println("Sto aggiornando il file della sezione");
            w = new FileWriter(".\\File\\" + nome + ".txt");
            w.write(temp);
            w.flush();
            w.close();*/

        } finally {
            ServerDb.prendilock(n).unlock();
        }
    }

    //QUESTO METODO NON VIENE UTILIZZATO, E' STATO MESSO DENTRO LA CLASSE TASK
    public  String blockingMethod() throws IOException{
        try {
        	boolean section_enter= true;
            String message;
            message = in.readLine();
            section = message;
            System.out.println(Thread.currentThread().getName().toString() + ": SEZIONE DEL UTENTE" + section);
            int n = converti(section);
            while(section_enter){
            //CONTROLLO SE LA SEZIONE CHE HA SELEZIONATO IL CLIENT NON E' STATA GIA SELEZIONATA DA ALTRI
            if (ServerDb.prendilock(n).isLocked()) {
                // GESTIRE COSA FARE NEL MOMENTO IN CUI LA SESIONE E STATA GIA OCCUPATA
                System.out.println("In attesa che un altro client possa finire..");
            }

            //BLOCCO LA SEZIONE CHE STO UTILIZZANDO
            ServerDb.prendilock(n).lock();

            //INVIO AL CLIENT I POSTI DISPONIBILE PER LA SEZIONE CHE HA SCELTO
            out.println(estraiPosti(ServerDb.prendi(n)));
            stampa("--SEZIONE INVIATA--");
            message = in.readLine();

            //SERVER RICEVE I POSTI DA PRENOTARE SELEZIONATI DAL CLIENT
            stampa("--PRENOTO_I_POSTI :" + message);
            if(!pre_commit(n, message)) {
            	System.out.println("QUESTO");
            }
            }
            out.println("FATTO");

        } finally {
            // SBLOCCO IL LOCK
            System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"SBLOCCO SEZIONE");
            ServerDb.prendilock(converti(section)).unlock();
        }

        return "fatto";
    }

    // CONVERTO LA SEZIONE NEL CORISPETTIVO NUMERO: DA LETTERA A NUMERO
    public static int converti(String str) {
        int n = -100;
        if (str.equals("A")) { n = 1; }
        if (str.equals("B")) { n = 2;}
        if (str.equals("C")) { n = 3; }
        if (str.equals("D")) { n = 4; }
        if (str.equals("E")) { n = 5; }
        if (str.equals("F")) { n = 6; }
        if (str.equals("G")) { n = 7; }
        return n;
    }

    // CONVERTO IL NUMERO DI SEZIONE NELLA CORISPETTIVA LETTERA: DA NUMERO A LETTERA
    public static String riconverti(int str) {
        String n = null;
        if (str == 1) { n = "A"; }
        if (str == 2) { n = "B"; }
        if (str == 3) { n = "C"; }
        if (str == 4) { n = "D"; }
        if (str == 5) { n = "E"; }
        if (str == 6) { n = "F"; }
        if (str == 7) { n = "G"; }
        return n;
    }

    // FUNZIONE PER STAMPARE UNA STRINGA
    public static void stampa(String s) {
        System.out.println(s);
    }

    // DATA LA RELATIVA SEZIONE, RESTITUISCO I POSTI LIBERI PER QUELLA SEZIONE
    public String estraiPosti(ArrayList<Integer> data) {
        String dati = "";
        for (int i = 0; i < data.size(); i++) {
            dati = dati + data.get(i) + ";";
        }
        //System.out.println(dati);
        return dati;
    }

    // INUTILE PER ORA
    // IL CONTROLLO DEI POSTI LO FACCIAMO LATO CLIENT
    public int postiDisponibili(ArrayList<Integer> data) {
        int n = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(0)) {
                n++;
            }
        }
        return n;
    }

    //RESTITUISCE L'ID DELLA PRENOTAZIONE
    public String getID() throws IOException{
        FileReader fr = new FileReader(".\\File\\Reg\\Reg.txt");
        BufferedReader br = new BufferedReader(fr);
        String s;
        String temp = null;
        while((s = br.readLine()) != null) {
            temp = s;
        }
        fr.close();

        String temp2[] = temp.split("-");
        String ID = temp2[0];

        int foo = Integer.parseInt(ID);
        foo=foo+1;
        ID = String.valueOf(foo);

        return ID;
    }

    //SI OCCUPA DI REGISTRARE LA PRENOTAZIONE DELL'UTENTE
    //LA SEZIONE REG VIENE BLOCCATA IN QUESTO METODO
    public void registra(int n, String utente) throws Exception{
        try{
            ///////////////////////////REGISTRAZIONE DEL CLIENT //////////////////////////////
            if(ServerDb.prendilock(0).isLocked()) {
                // GESTIRE COSA FARE NEL MOMENTO IN CUI LA SESIONE E STATA GIA OCCUPATA
                System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"In attesa che un altro client possa finire..");
            }

            //BLOCCO LA REGISTRAZIONE
            ServerDb.prendilock(0).lock();

            //INDICE INCREMENTALE
            String id = getID();
            utente = id+"-"+utente;

            FileWriter w;
            w = new FileWriter(".\\File\\Reg\\Reg.txt", true);
            w.write(utente);
            w.flush();
            w.close();
            //////////////////////////////////////////////////////////////////////////////////
        }finally {
            System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"SBLOCCO REG");
            ServerDb.prendilock(0).unlock();
        }
    }

    //QUESTA CLASSE NON VIENE PIU UTILIZZATA
    //VIENE TUTTO ESEGUITO ALL'INTERNO DELLA CLASSE TASK1
    class Task2 implements Callable<String> {
        private int n;
        BufferedReader in;
        PrintStream out;

        Task2(int n, BufferedReader in,PrintStream out) {
            this.n=n;
            this.in=in;
            this.out=out;
        }
        
        public String call() throws Exception {
        	
	        ///////////////////////////REGISTRAZIONE DEL CLIENT //////////////////////////////         
			out.println("start");
			out.flush();
			
			//RICEVO LE CREDENZIALI DAL CLIENT
			String nome = in.readLine();    
			String cognome = in.readLine();
			String eta = in.readLine();
			
			//CREO LA STRINGA CHE VERRA SCRITTA SUL FILE
			String utente = "Nome:"+nome+"Cognome:"+cognome+"Eta:"+eta+"Posti:";
			//////////////////////////////////////////////////////////////////////////////////

            return utente;
        }
    }

    //GESTISCO COSA FARE DATA UNA SEZIONE SELEZIONATA DAL CLIENT
    public void action() throws Exception {

        //RICEVO LA SEZIONE SELEZIONATA DALL'UTENTE
        String sezione = in.readLine();
        int n = converti(sezione);

        //SI SE L'UTENTE VUOLE SCEGLIERE IL POSTO
        //NO SE L'UTENTE NON VUOLE SCEGLIERE UN POSTO IN PARTICOLARE
        String rand = in.readLine();
        int fase = 0;
        if (rand.equals("si")) fase = 2;
        if (rand.equals("no")) fase = 1;

        ExecutorService executor = Executors.newSingleThreadExecutor();

        switch (fase){
            case 1:
                //CASE PER LA SELEZIONE CASUALE DEL POSTO

                try {
                    //CONTROLLO SE LA SEZIONE CHE HA SELEZIONATO IL CLIENT NON E' STATA GIA SELEZIONATA DA ALTRI
                    if (ServerDb.prendilock(n).isLocked()) {
                        // GESTIRE COSA FARE NEL MOMENTO IN CUI LA SESIONE E STATA GIA OCCUPATA
                        System.out.println("In attesa che un altro client possa finire..");
                    }

                    //BLOCCO LA SEZIONE
                    ServerDb.prendilock(n).lock();

                    //PER ORA IL POSTO SCELTO A CASO è FISSO POI FAREMO UN METODO PER ESTRARLO
                    String postoACaso = "2;";

                    //INVIO AL CLIENT IL POSTO CHE è STATO SCELTO A CASO
                    out.println(postoACaso);
                    out.flush();

                    ///////////////////////////REGISTRAZIONE DEL CLIENT //////////////////////////////
                    //RICEVO IL MESSAGGIO PER INIZIALE LA REGISTRAZIONE: "Registrazione"
                    //message = in.readLine();
                    //System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+message);

                    out.println("start");
                    out.flush();

                    //RICEVO LE CREDENZIALI DAL CLIENT
                    String nome = in.readLine();
                    String cognome = in.readLine();
                    String eta = in.readLine();

                    //CREO LA STRINGA CHE VERRA SCRITTA SUL FILE
                    String utente = "Nome:"+nome+"Cognome:"+cognome+"Eta:"+eta+"Posti:"+postoACaso+'\n';

                    //PRENOTO IL POSTO SCELTO A CASO
                    //TODO sistemare la prenotazione del posto casuale 
                    pre_commit(n,postoACaso);

                    //REGISTRO LA PRENOTAZIONE DELL'UTENTE
                    try {
                        registra(n, utente);
                    }catch (Exception e){

                    }

                    //INVIO CONFERMA AL CLIENT
                    out.println("REGISTRAZIONE COMPLETATA");
                    out.flush();
                    //////////////////////////////////////////////////////////////////////////////////
                }finally {
                    System.out.println("SBLOCCO LA SEZIONE");
                    ServerDb.prendilock(n).unlock();
                }

                break;
            case 2:
                //CASE PER LA SELEZIONE DEL POSTO DA PARTE DELL'UTENTE
            	String posti = null;
            	String utente = null;
                Future<String> future = executor.submit(new Task(in,out, sezione));
                try{
                	//TASK CHE SI OCCUPA DI RICEVERE I POSTI, A ESSO VIENE ASSEGNATO IL TEMPO MASSIMO CON CUI L'UTENTE PUOI SELEZIONARE I POSTI
                    posti = future.get(1000, TimeUnit.SECONDS);
                    	
                    Future<String> future2 = executor.submit(new Task2(n,in,out));
                    
                    try {
                    	//TASK CHE SI OCCUPA DI RICEVERE I DATI UTENTE, A ESSE VIENE ASSEGNTO IL TEMPO MASSIMO CON CUI L'UTENTE PUO INSERIRE I DATI
                    	utente = future2.get(1000, TimeUnit.SECONDS);
                    }catch (InterruptedException e) {
                        System.out.println(e);
                        System.out.println("TEMPO SCADUTO 2, LIBERO I POSTI:" + posti);
                        libera(n,posti); 
                    }catch (ExecutionException e) {
                        System.out.println(e);
                        System.out.println("TEMPO SCADUTO 2, LIBERO I POSTI:" + posti);
                        libera(n,posti); 
                    }catch (TimeoutException e) {
                    	//QUANDO SCADE LA CONNESIONE CON IL CLIENT CHIUDO LA SOCKET 
                    	//E LIBERO I POSTI CHE STAVA CERCANDO DI PRENOTARE
                        future2.cancel(true);
                        client.close();                 

                        System.out.println("TEMPO SCADUTO 2, LIBERO I POSTI:" + posti);
                        libera(n,posti);                  

                        //SE IL TEMPO E' SCADUTO MA ERA STATA SELEZIONATA UNA SEZIONE, VIENE SBLOCCATA
                        if(!section.equals("0")){
                            System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"SBLOCCO LA SEZIONE - " + section);
                            ServerDb.prendilock(converti(section)).unlock();
                        }
                    }
                    
					
					//REGISTRAZIONE DELLA PRENOTAZIONE DELL'UTENTE
					registra(n,utente);
					
					//COMMIT FINALE DELLA PRENOTAZIONE DELL'UTENTE
					aggiornafile(n,ServerDb.prendi(n));
					
					//INVIO AL CLIENT DEI MESSAGGI DI CONFERMA DELLA PRENOTAZIONE
					out.println("REGISTRAZIONE COMPLETATA");
					out.flush();
                }catch (SocketException e){
                	//LA SOCKET è STATA CHIUSA PERTANTO LA CONNESSIONE è STATA INTERROTTA, VENGONO LIBERATI I POSTI 
                	System.out.println(e);
                    System.out.println("TEMPO SCADUTO 2, LIBERO I POSTI:" + posti);
                    libera(n,posti); 
                }catch (InterruptedException e) {
                    System.out.println(e);
                }catch (ExecutionException e) {
                    System.out.println(e);
                }catch (TimeoutException e) {
                	//QUANDO SCADE LA CONNESIONE CON IL CLIENT CHIUDO LA SOCKET 
                	//E LIBERO I POSTI CHE STAVA CERCANDO DI PRENOTARE
                    future.cancel(true);
                    client.close();                 

                    System.out.println("TEMPO SCADUTO, LIBERO I POSTI:" + posti);
                    libera(n,posti);

                    //SE IL TEMPO E' SCADUTO MA ERA STATA SELEZIONATA UNA SEZIONE, VIENE SBLOCCATA
                    if(!section.equals("0")){
                        System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - "+"SBLOCCO LA SEZIONE - " + section);
                        ServerDb.prendilock(converti(section)).unlock();
                    }

                }finally {
                    future.cancel(true);
                }
                executor.shutdownNow();
                break;
        }

    }
    
    //TASK CHE DEVE ESEGUIRE IL THREAD
    //QUESTA PARTE E' SOGGETTA AD UN TIMEOUT
    class Task implements Callable<String> {
        BufferedReader in;
        PrintStream out;
        String s;

        Task(BufferedReader in,PrintStream out, String sezione) {
            this.in=in;
            this.out=out;
            this.s = sezione;
        }

        @Override
        public String call() throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            String posti = "";
            String message;
            //RICEVO LA SEZIONE DAL CLIENT
            //message = in.readLine();

            section = s;
            int n = converti(section);     
                
            //INVIO AL CLIENT I POSTI DELLA SEZIONE 
            try{
                //CONTROLLO SE LA SEZIONE CHE HA SELEZIONATO IL CLIENT NON E' STATA GIA SELEZIONATA DA ALTRI
                if (ServerDb.prendilock(n).isLocked()) {
                    // GESTIRE COSA FARE NEL MOMENTO IN CUI LA SESIONE E STATA GIA OCCUPATA
                    System.out.println("In attesa che un altro client possa finire..");
                }

                //BLOCCO LA SEZIONE CHE STO UTILIZZANDO
                ServerDb.prendilock(n).lock();

                //INVIO AL CLIENT I POSTI DELLA SEZIONE SCELTA
                out.println(estraiPosti(ServerDb.prendi(n)));
                out.flush();

            }finally {
                //SBLOCCO SEZIONE
                System.out.println(+new Exception().getStackTrace()[0].getLineNumber()+" - SBLOCCO SEZIONE");
                ServerDb.prendilock(n).unlock();
            }
            
            //RICEVO DAL CLIENT I POSTI DA PRENOTARE
            //CONTROLLO CHE ESSI SIANO ANCORA DISPONIBILI
            boolean conferma;
            do{
                //SERVER RICEVE I POSTI DA PRENOTARE SELEZIONATI DAL CLIENT
                message = in.readLine();
                posti = message;
                stampa("--PRENOTO_I_POSTI :" + posti);

                //PRENOTO I POSTI
                conferma = pre_commit(n, posti);

                if (conferma) {
                    out.println("ERRORE NELLA PRENOTAZIONE DEI POSTI");
                    out.flush();
                    out.println(estraiPosti(ServerDb.prendi(n)));
                    out.flush();
                }else{
                    out.println("POSTO PRENOTATO CON SUCCESSO");
                    out.flush();
                }
            }while (conferma);         
            
            return posti;
        }
    }
}
