package Utils;

public class Tempo {
    public Tempo(int delai) {
        try {
            // Temporisation
            Thread.sleep(delai);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
