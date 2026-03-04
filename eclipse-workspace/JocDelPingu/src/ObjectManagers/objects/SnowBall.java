package ObjectManagers.objects;

import ObjectManagers.ObjectType;
import java.util.Random;
public class SnowBall extends Object {
		
	private ObjectType objectType = ObjectType.SNOWBALL;
	
}

/**
 * Classe que representa una bola de neu
 */
public class BolaDeNeu {
    
    private int casellesRetrocedir;
    private Random random;
    
    public BolaDeNeu() {
        this.random = new Random();
        this.casellesRetrocedir = calcularRetroces();
    }
    
    /**
     * Llança la bola de neu a un jugador objectiu
     */
    public void llançar(Jugador objectiu) {
        int posicioActual = objectiu.getPosicio();
        int novaPosicio = Math.max(0, posicioActual - casellesRetrocedir);
        
        objectiu.actualitzarPosicio(novaPosicio);
        
        System.out.println(objectiu.getNom() + " retrocedeix " + casellesRetrocedir + 
                          " caselles (posició: " + novaPosicio + ")");
    }
    
    /**
     * Calcula el retrocés aleatori (1-3 caselles)
     */
    private int calcularRetroces() {
        return random.nextInt(3) + 1; // 1, 2 o 3
    }
    
    public int getCasellesRetrocedir() {
        return casellesRetrocedir;
    }
}
