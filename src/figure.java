public class figure {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";

    char figureType;
    boolean figureColor; //false-red, true-green
    boolean firstFigureMove;

    figure(char figureType, String color){
        this.figureType = figureType;
        if(color == "red") this.figureColor = false;
        else this.figureColor = true;
        this.firstFigureMove = true;
    }

    public void printFigure(){
        if(figureColor == false){
            System.out.print(ANSI_RED + this.figureType + ANSI_RESET);
        }
        else
        {
            System.out.print(ANSI_GREEN + this.figureType + ANSI_RESET);
        }
        
    }
}