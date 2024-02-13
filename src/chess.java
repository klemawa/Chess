import java.util.Scanner;
import java.util.*;  
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class chess {
    static boolean playerTurn = true; //false-red, true-green
    static chessBoardField chessBoard = new chessBoardField();
    
    //Listy zawierajace pola
    static List<fieldCoordinates> listOfPossibleFields = new ArrayList<fieldCoordinates>();
    static List<fieldCoordinates> listOfGreenOccupiedFields = new ArrayList<fieldCoordinates>();
    static List<fieldCoordinates> listOfRedOccupiedFields = new ArrayList<fieldCoordinates>();
    static List<fieldCoordinates> listOfGreenDangerFields = new ArrayList<fieldCoordinates>();
    static List<fieldCoordinates> listOfRedDangerFields = new ArrayList<fieldCoordinates>();
    
    //Zmienne do notacji szachowej
    static boolean beating = false; //Bicie (:)
    static boolean castling = false; //Roszada (0-0 lub 0-0-0)
    static boolean check = false; //Szach (+)
    static boolean mat = false; //Mat (#)
    
    //Zmienna okreslajaca numer ruchu
    static int moveNumber = 0;
    
    public static void main(String[] args) {
        fieldCoordinates figureToMoveFieldCoordinates = new fieldCoordinates(-1,-1);
        fieldCoordinates figureDestinationFieldCoordinates = new fieldCoordinates(-1,-1);;
        
        chessBoard.addInitialFigures();
        chessBoard.showChessBoard();
        
        //Rozpoczecie zapisywania notacji szachowej do pliku
        LocalDateTime myDateObj = LocalDateTime.now();  //Pobieranie aktualnej daty i czasu
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy"); //Wybieranie formatu odczytu daty
        String currentDateTime = myDateObj.format(myFormatObj);     //Formatowanie aktualnej daty do wybranego formatu
        try{
            noteToFile("Partia szachowa z dnia " + currentDateTime + "\n", false);
        }catch(Exception ex) { 
            System.out.println("Blad pliku do zapisu notacji szachowej!"); 
        }
        
        while(checkMate()){
            if(moveNumber > 1){
                String dataToFile = gatInformationForChessWrite(figureToMoveFieldCoordinates, figureDestinationFieldCoordinates);
                try{
                    noteToFile(moveNumber + ". " + dataToFile, true);
                }catch(IOException ex) {
                    System.out.println("Blad pliku do zapisu notacji szachowej!");
                }
            }
            figureToMoveFieldCoordinates = getPlayerInput();
            checkPossibleDestinations(figureToMoveFieldCoordinates, playerTurn);
            while(listOfPossibleFields.isEmpty()){
                System.out.println("Nie ma mozliwych ruchow dla wybranej figury");
                figureToMoveFieldCoordinates = getPlayerInput();
                checkPossibleDestinations(figureToMoveFieldCoordinates, playerTurn);
            }
            figureDestinationFieldCoordinates = getDestinationFieldCoordinates();
            moveFigure(figureToMoveFieldCoordinates, figureDestinationFieldCoordinates);
            
            chessBoard.showChessBoard();

            //Zmiana gracza
            playerTurn=!playerTurn;
            moveNumber++;
        }
    }
    
    public static String gatInformationForChessWrite(fieldCoordinates figureToMoveFieldCoordinates, fieldCoordinates figureDestinationFieldCoordinates){
        String moveDone = "";
        char figureType = chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureType;
        String figureTypeAsString = "";
        
        switch(figureType){
            case 'P':
                figureTypeAsString = ""; //Brak oznaczenia piona w notacji szachowej
                break;
            case 'W':
                figureTypeAsString = "W"; 
                break;
            case 'S':
                figureTypeAsString = "S";
                break;
            case 'G':
                figureTypeAsString = "G";
                break;
            case 'H':
                figureTypeAsString = "H";
                break;
            case 'K':
                figureTypeAsString = "K";
                break;
        }
        
        //Krotkie roszady
        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == true 
                && figureDestinationFieldCoordinates.equals(new fieldCoordinates(7,7))){
            moveDone = "K0-0";
            return moveDone;
        }
        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == false 
                && figureDestinationFieldCoordinates.equals(new fieldCoordinates(0,7))){
            moveDone = "K0-0";
            return moveDone;
        }
        
        //Dlugie roszady
        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == true 
                && figureDestinationFieldCoordinates.equals(new fieldCoordinates(7,0))){
            moveDone = "K0-0-0";
            return moveDone;
        }
        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == false 
                && figureDestinationFieldCoordinates.equals(new fieldCoordinates(0,0))){
            moveDone = "K0-0-0";
            return moveDone;
        }
        
        char[] firstLetter = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
        char[] secondLetter = {'1', '2', '3', '4', '5', '6', '7', '8'};
        
        if(beating){
            moveDone = figureTypeAsString + firstLetter[figureToMoveFieldCoordinates.firstDim] 
                    +  secondLetter[figureToMoveFieldCoordinates.secondDim] + ":" + figureTypeAsString
                    +  firstLetter[figureDestinationFieldCoordinates.firstDim]
                    +  secondLetter[figureDestinationFieldCoordinates.secondDim];
        }
        else{
            moveDone = figureTypeAsString + firstLetter[figureToMoveFieldCoordinates.firstDim] 
                    +  secondLetter[figureToMoveFieldCoordinates.secondDim] + "-" + figureTypeAsString
                    +  firstLetter[figureDestinationFieldCoordinates.firstDim]
                    +  secondLetter[figureDestinationFieldCoordinates.secondDim];
        }
        
        if(check){
            moveDone = moveDone + "+";
        }
        
        if (mat){
            moveDone = moveDone + "#";
        }
        
        //Resetowanie zmiennych dla kolejnych flag
        beating = false; 
        castling = false; 
        check = false; 
        mat = false; 
        
        return moveDone + "\n";
    }
    
    public static void noteToFile(String textToWrite, boolean appendToFile) throws IOException
    {
        File file = new File("D:/notacja_szachowa.txt");
        try (FileWriter fr = new FileWriter(file, appendToFile)) {
            fr.write(textToWrite);
        }
    }
    
    public static boolean checkMate(){
        //Pozycje krolow 
        fieldCoordinates kingGreenFieldCoordinates = new fieldCoordinates(-1,-1);
        fieldCoordinates kingRedFieldCoordinates = new fieldCoordinates(-1,-1);
        
        //Listy mozliwych ruchow dla krolow
        List<fieldCoordinates> listOfGreenKingPossibleMoves = new ArrayList<fieldCoordinates>();
        List<fieldCoordinates> listOfRedKingPossibleMoves = new ArrayList<fieldCoordinates>();
        
        //Czyszczenie poprzednich wynikow i pozycji
        listOfGreenOccupiedFields.clear();
        listOfRedOccupiedFields.clear();
        listOfGreenDangerFields.clear();
        listOfRedDangerFields.clear();
        
        //Sprawdzanie pozycji dla kazdej figury, false - red    true - green
        for(int i = 0; i < 8; i++){
            for(int k = 0; k < 8; k++){
                if(chessBoard.chessBoard[i][k].isFigureOnThisField){
                    if(chessBoard.chessBoard[i][k].figureOnField.figureColor == true){ //green
                        //Tworzenie listy pozycji zielonych figur
                        listOfGreenOccupiedFields.add(new fieldCoordinates(k,i));
                        //Sprawdzanie mozliwosci ataku dla danej figury
                        checkPossibleDestinations(new fieldCoordinates(k,i), true);
                        listOfGreenDangerFields.addAll(listOfPossibleFields);
                        //Pozycja i mozliwe ruchy krola
                        if(chessBoard.chessBoard[i][k].figureOnField.figureType == 'K'){
                            kingGreenFieldCoordinates = new fieldCoordinates(k,i);
                            listOfGreenKingPossibleMoves.addAll(listOfPossibleFields);
                        }
                    }
                    if(chessBoard.chessBoard[i][k].figureOnField.figureColor == false){ //red
                        //Tworzenie listy pozycji zielonych figur
                        listOfRedOccupiedFields.add(new fieldCoordinates(k,i));
                        //Sprawdzanie mozliwosci ataku dla danej figury
                        checkPossibleDestinations(new fieldCoordinates(k,i), false);
                        listOfRedDangerFields.addAll(listOfPossibleFields);
                        //Pozycja i mozliwe ruchy krola
                        if(chessBoard.chessBoard[i][k].figureOnField.figureType == 'K'){
                            kingRedFieldCoordinates = new fieldCoordinates(k,i);
                            listOfRedKingPossibleMoves.addAll(listOfPossibleFields);
                        }
                    }
                }
            }
        }
        
        if(playerTurn){ //Ruch zielonego 
            if(listOfRedDangerFields.contains(kingGreenFieldCoordinates)){
                System.out.println("Szach!");
                check = true;
                if(listOfRedDangerFields.containsAll(listOfGreenKingPossibleMoves)){
                    System.out.println("Szach mat!");
                    mat = true;
                    return false;
                }
            }
            else{
                if(listOfRedDangerFields.containsAll(listOfGreenKingPossibleMoves)){
                    System.out.println("Pat!");
                    return false;
                }
            }
            //Brak ruchow
            if(listOfGreenDangerFields.isEmpty()){
                System.out.println("Pat!");
                return false;
            }
        }
        else{
            if(listOfGreenDangerFields.contains(kingRedFieldCoordinates)){
                System.out.println("Szach!");
                check = true;
                if(listOfGreenDangerFields.containsAll(listOfRedKingPossibleMoves)){
                    System.out.println("Szach mat!");
                    mat = true;
                    return false;
                }
            }
            else{
                if(listOfGreenDangerFields.containsAll(listOfRedKingPossibleMoves)){
                    System.out.println("Pat!");
                    return false;
                }
            }
            //Brak ruchow
            if(listOfRedDangerFields.isEmpty()){
                System.out.println("Pat!");
                return false;
            }
        }
        
        return true;
    }
    
    public static void moveFigure(fieldCoordinates figureToMoveFieldCoordinates, fieldCoordinates figureDestinationFieldCoordinates){
        boolean roszada = false;
        
        //Pobieranie figury do przenoszenia
        figure figureToBeMoved = chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField;
        //Pobieranie informacji o polu na ktore zostanie przeniesiona figura
        figure figureAtDestination = new figure('E',"Dummy"); 
        if(chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].isFigureOnThisField){
            figureAtDestination = chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].figureOnField;
            if(figureToBeMoved.figureType == 'K' && figureAtDestination.figureType == 'W' && figureToBeMoved.figureColor == figureAtDestination.figureColor){
                roszada = true;
            }
        }
        
        if(!roszada){
            //Zmiana informacji o pierwszym ruchy danej figury 
            figureToBeMoved.firstFigureMove = false;
            
            //Sprawdzanie czy ruch byl biciem
            if(chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].isFigureOnThisField 
                    && chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn){
                System.out.println("Bicie figury przeciwnika!");
                beating = true;
            }
            
            //Przenoszenie figury na wybrane pole
            chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].figureOnField = figureToBeMoved;
            //Zmiana zajetosci pola dla obu wybranych pol
            chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].isFigureOnThisField = true;
            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField = false;
        }
        else{
            //Zmiana informacji o pierwszym ruchu krola i wiezy
            figureToBeMoved.firstFigureMove = false;
            figureAtDestination.firstFigureMove = false;
            
            System.out.println("Roszada!");
            castling = true;
            
            //Przenoszenie obu figur
            chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].figureOnField = figureToBeMoved;
            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField = figureAtDestination;
            //Dodawanie zajetosci pola
            chessBoard.chessBoard[figureDestinationFieldCoordinates.secondDim][figureDestinationFieldCoordinates.firstDim].isFigureOnThisField = true;
            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField = true;
        } 
    }
    
    public static void checkPossibleDestinations(fieldCoordinates figureToMoveFieldCoordinates, boolean playerTurn){
        listOfPossibleFields.clear();
        
        switch(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureType){
            case 'P':
                if(playerTurn == true){ //Ruch zielonego
                    // Ruch
                    if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        // Ruch o 1
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - 1));
                            }
                        }catch (Exception ex) {}
                        // Ruch o 2
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-2][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - 2));
                            }
                        }catch (Exception ex) {}
                    }
                    else{
                        // Ruch o 1
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - 1));
                            }
                        }catch (Exception ex) {}
                    }
                    
                    // Bicie
                    try{
                        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim-1].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim-1].figureOnField.figureColor != playerTurn){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim - 1));
                        }
                    }catch (Exception ex) {}
                    
                    try{
                        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim-1][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim - 1));
                        }
                    }catch (Exception ex) {}
                    
                }
                else{                   //Ruch czerwonego
                    // Ruch
                    if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        // Ruch o 1
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + 1));
                            }
                        }catch (Exception ex) {}
                        // Ruch o 2
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + 2));
                            }
                        }catch (Exception ex) {}
                    }
                    else{
                        // Ruch o 1
                        try{
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                                listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + 1));
                            }
                        }catch (Exception ex) {}
                    }
                    
                    // Bicie
                    try{
                        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim + 1));
                        }
                    }catch (Exception ex) {}
                    
                    try{
                        if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim + 1));
                        }
                    }catch (Exception ex) {}
                }
                break;
            case 'G':
                // Skos 1
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 2
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 3
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 4
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                break;
            case 'S':
                //Pozycja 1
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim - 2));
                    }
                }catch (Exception ex) {}
                //Pozycja 2
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 2][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim - 2));
                    }
                }catch (Exception ex) {}
                //Pozycja 3
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 2].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 2].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 2].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 2, figureToMoveFieldCoordinates.secondDim - 1));
                    }
                }catch (Exception ex) {}
                //Pozycja 4
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 2].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 2].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 2].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 2, figureToMoveFieldCoordinates.secondDim - 1));
                    }
                }catch (Exception ex) {}
                //Pozycja 5
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 2].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 2].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 2].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 2, figureToMoveFieldCoordinates.secondDim + 1));
                    }
                }catch (Exception ex) {}
                //Pozycja 6
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 2].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 2].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 2].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 2, figureToMoveFieldCoordinates.secondDim + 1));
                    }
                }catch (Exception ex) {}
                //Pozycja 7
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim + 2));
                    }
                }catch (Exception ex) {}
                //Pozycja 8
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 2][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim + 2));
                    }
                }catch (Exception ex) {}
                break;
            case 'W':
                // Strona 1
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 2
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 3
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 4
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                break;
            case 'H':
                // Strona 1
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 2
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 3
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Strona 4
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                // Skos 1
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 2
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + i, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim + i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 3
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim + i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                
                //Skos 4
                for(int i = 1; i < 8; i++){
                    try{
                        if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn)
                                || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == false){
                            listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - i, figureToMoveFieldCoordinates.secondDim - i));
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor != playerTurn){
                                break;
                            }
                        }
                        else
                        {
                            if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].isFigureOnThisField == true &&
                                chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - i][figureToMoveFieldCoordinates.firstDim - i].figureOnField.figureColor == playerTurn){
                                break;
                            }
                        }
                    }catch (Exception ex) {}
                }
                break;
            case 'K':
                //Ruchy 1 do 8
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim - 1));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim + 1));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim + 1));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim - 1));
                    }
                }catch (Exception ex) {}
                
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim - 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim - 1));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim + 1][figureToMoveFieldCoordinates.firstDim].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim, figureToMoveFieldCoordinates.secondDim + 1));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim + 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim + 1, figureToMoveFieldCoordinates.secondDim));
                    }
                }catch (Exception ex) {}
                try{
                    if((chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - 1].figureOnField.figureColor != playerTurn)
                            || chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim - 1].isFigureOnThisField == false){
                        listOfPossibleFields.add(new fieldCoordinates(figureToMoveFieldCoordinates.firstDim - 1, figureToMoveFieldCoordinates.secondDim));
                    }
                }catch (Exception ex) {}
                
                //Roszada
                if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor){ //Ruch zielonego
                    if(chessBoard.chessBoard[7][0].figureOnField.figureType == 'W' &&
                            chessBoard.chessBoard[7][0].figureOnField.firstFigureMove == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        listOfPossibleFields.add(new fieldCoordinates(0, 7));
                    }
                    if(chessBoard.chessBoard[7][7].figureOnField.figureType == 'W' &&
                            chessBoard.chessBoard[7][7].figureOnField.firstFigureMove == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        listOfPossibleFields.add(new fieldCoordinates(7, 7));
                    }
                }
                else{ //Ruch czerwonego
                    if(chessBoard.chessBoard[0][0].figureOnField.figureType == 'W' &&
                            chessBoard.chessBoard[0][0].figureOnField.firstFigureMove == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        listOfPossibleFields.add(new fieldCoordinates(0, 0));
                    }
                    if(chessBoard.chessBoard[0][7].figureOnField.figureType == 'W' &&
                            chessBoard.chessBoard[0][7].figureOnField.firstFigureMove == true &&
                            chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.firstFigureMove == true){
                        listOfPossibleFields.add(new fieldCoordinates(7, 0));
                    }
                }
                
                //Usuwanie pol potencjalnie prowadzacych do szachu
                if(chessBoard.chessBoard[figureToMoveFieldCoordinates.secondDim][figureToMoveFieldCoordinates.firstDim].figureOnField.figureColor){ //Ruch zielonego
                    listOfPossibleFields.removeAll(listOfRedDangerFields);
                }
                else{
                    listOfPossibleFields.removeAll(listOfGreenDangerFields);
                }
                
                break;
        }
        
    }
    
    public static fieldCoordinates getDestinationFieldCoordinates()
    {
        Scanner myObj = new Scanner(System.in); 
        String playerInput;
        fieldCoordinates receivedFieldCoordinates;
        boolean fistTimeAsking = true;
        
        do{
            if(!fistTimeAsking){
                System.out.println("Bledne wspolzedne!");
            }
            fistTimeAsking=false;
            
            if(playerTurn == true){
                System.out.println("Gracz Zielony: Podaj numer pola do ktorego chcesz ruszyc figura");
                 
            }
            else{
                System.out.println("Gracz Czerwony: Podaj numer pola do ktorego chcesz ruszyc figura");
            }
            
            playerInput = myObj.nextLine();
            
            receivedFieldCoordinates = checkIfFieldIsCorrect(playerInput);
            
            if(receivedFieldCoordinates.isCorrect){
                if(listOfPossibleFields.contains(receivedFieldCoordinates)){
                    receivedFieldCoordinates.isCorrect = true;
                }
                else{
                    receivedFieldCoordinates.isCorrect = false;
                }
            }

        }while(!receivedFieldCoordinates.isCorrect);
        
        return receivedFieldCoordinates;
    }
    
    public static fieldCoordinates getPlayerInput(){
        Scanner myObj = new Scanner(System.in); 
        String playerInput;
        fieldCoordinates receivedFieldCoordinates;
        boolean fistTimeAsking = true;
        
        do{
            if(!fistTimeAsking){
                System.out.println("Bledne wspolzedne!");
            }
            fistTimeAsking=false;
            
            if(playerTurn == true){
                System.out.println("Gracz Zielony: Podaj numer pola z ktorego chcesz ruszyc figura");
                 
            }
            else{
                System.out.println("Gracz Czerwony: Podaj numer pola z ktorego chcesz ruszyc figura");
            }
            
            playerInput = myObj.nextLine();
            
            receivedFieldCoordinates = checkIfFieldIsCorrect(playerInput);
            
            if(receivedFieldCoordinates.isCorrect){
                receivedFieldCoordinates.isCorrect = false;
                
                try{
                    if(chessBoard.chessBoard[receivedFieldCoordinates.secondDim][receivedFieldCoordinates.firstDim].isFigureOnThisField){
                        if(chessBoard.chessBoard[receivedFieldCoordinates.secondDim][receivedFieldCoordinates.firstDim].figureOnField.figureColor == playerTurn){
                            receivedFieldCoordinates.isCorrect = true;
                        }
                    }
                }
                catch(Exception ex){
                    receivedFieldCoordinates.isCorrect = false;
                }
            }

        }while(!receivedFieldCoordinates.isCorrect);
        
        return receivedFieldCoordinates;
    }
    
    public static fieldCoordinates checkIfFieldIsCorrect(String playerInput){
        fieldCoordinates returnFieldCoordinates = new fieldCoordinates(-1, -1);
        
        char[] firstLetter = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
        int[] firstLetterDecoded = {0, 1, 2, 3, 4, 5, 6, 7};
        char[] secondLetter = {'1', '2', '3', '4', '5', '6', '7', '8'};
        int[] secondLetterDecoded = {0, 1, 2, 3, 4, 5, 6, 7};
        boolean isCorrect = false;
        
        int firstDim = -1;
        int secondDim = -1;
        
        if(playerInput.length() == 2){
            for(int i = 0; i < firstLetter.length; i++){
                if(playerInput.charAt(0)==firstLetter[i]){
                    firstDim = firstLetterDecoded[i];
                    for(int k = 0; k < secondLetter.length; k++){
                        if(playerInput.charAt(1)==secondLetter[k]){
                            secondDim = secondLetterDecoded[k];
                            isCorrect = true;
                        }
                    }
                }
            }
        }
        
        returnFieldCoordinates.isCorrect = isCorrect;
        returnFieldCoordinates.firstDim = firstDim;
        returnFieldCoordinates.secondDim = secondDim;
                
        return returnFieldCoordinates;
    }
}
    

