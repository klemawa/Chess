public class fieldCoordinates {
    boolean isCorrect;
    int firstDim;
    int secondDim;
    
    fieldCoordinates(int firstDim, int secondDim){
        this.firstDim = firstDim;
        this.secondDim = secondDim;
    }
    
    //Nadpisana funkcja porownawcza dwoch elementow klasy fieldCoordinates
    @Override public boolean equals(Object o) {
    //Sprawdzanie czy takie same
    if (o == this) {
        return true;
    }

    //Sprawdzanie zgodnosci typu typu 
    if (!(o instanceof fieldCoordinates)) {
        return false;
    }

    //Rzutowanie w celu umozliwienia porownania
    fieldCoordinates c = (fieldCoordinates) o;

    //Porownanie kolejnych skladowych
    return Double.compare(firstDim, c.firstDim) == 0
            && Double.compare(secondDim, c.secondDim) == 0;
    }
}
