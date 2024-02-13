public class chessBoardField{
    field[][] chessBoard;

    chessBoardField(){
        chessBoard = new field[8][8];

        boolean fieldType = true;

        for (int i = 0; i < 8; i++) {  
            for (int k = 0; k < 8; k++) {  
                if(fieldType == true){
                    chessBoard[i][k] = new field(' ');
                }
                else{
                    chessBoard[i][k] = new field(' ');
                }
                fieldType = !fieldType;
            }  
            fieldType = !fieldType;
        }       
    }
   
    
    public void addInitialFigures(){
        //Pierwszy gracz
        for(int i = 0; i < 2; i++)
            for(int k = 0; k < 8; k++){
                chessBoard[i][k].isFigureOnThisField = true;
            }
        
        chessBoard[0][0].figureOnField = new figure('W', "red");
        chessBoard[0][7].figureOnField = new figure('W', "red");
        chessBoard[0][1].figureOnField = new figure('S', "red");
        chessBoard[0][6].figureOnField = new figure('S', "red");
        chessBoard[0][2].figureOnField = new figure('G', "red");
        chessBoard[0][5].figureOnField = new figure('G', "red");
        chessBoard[0][3].figureOnField = new figure('H', "red");
        chessBoard[0][4].figureOnField = new figure('K', "red");
        
        for(int i = 0; i < 8; i++){
            chessBoard[1][i].figureOnField = new figure('P', "red");
        }
        
        //Drugi gracz
        for(int i = 6; i < 8; i++)
            for(int k = 0; k < 8; k++){
                chessBoard[i][k].isFigureOnThisField = true;
            }
        
        chessBoard[7][0].figureOnField = new figure('W', "green");
        chessBoard[7][7].figureOnField = new figure('W', "green");
        chessBoard[7][1].figureOnField = new figure('S', "green");
        chessBoard[7][6].figureOnField = new figure('S', "green");
        chessBoard[7][2].figureOnField = new figure('G', "green");
        chessBoard[7][5].figureOnField = new figure('G', "green");
        chessBoard[7][4].figureOnField = new figure('K', "green");
        chessBoard[7][3].figureOnField = new figure('H', "green");
        
        for(int i = 0; i < 8; i++){
            chessBoard[6][i].figureOnField = new figure('P', "green");
        }
        
    }

    public void showChessBoard(){
        System.out.print("\t" + "A" + "\t");
        System.out.print("B"+"\t");
        System.out.print("C"+"\t");
        System.out.print("D"+"\t");
        System.out.print("E"+"\t");
        System.out.print("F"+"\t");
        System.out.print("G"+"\t");
        System.out.print("H"+"\t");
        System.out.print("\n");
        System.out.println("    |-------|-------|-------|-------|-------|-------|-------|-------|");

        for (int i = 0; i < 8; i++) {  
            System.out.print((i+1)+"   |   ");
            for (int k = 0; k < 8; k++) {  
                if(chessBoard[i][k].isFigureOnThisField == true){
                    chessBoard[i][k].figureOnField.printFigure();
                }
                else{
                    System.out.print(chessBoard[i][k].initialSetup);
                }
                System.out.print("   |   ");
            }  
            System.out.print("\n");
            System.out.println("    |-------|-------|-------|-------|-------|-------|-------|-------|");
        }  
        System.out.print("\n");
    }

}