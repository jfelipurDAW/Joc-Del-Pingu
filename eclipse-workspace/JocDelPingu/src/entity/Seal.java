package entity;

import config.Lang;
import config.LangConfig;
import ObjectManagers.ObjectType;

public class Seal extends Entity {

    private boolean isBribed;
    private int blockedTurns; // Contador para saber cuánto tiempo está comiendo
    private String name;
    
    public Seal() {
        this.name = Lang.ENTITY_SEAL.getKey();
        this.type = EntityType.SEAL;
        
        this.isBribed = false;
        this.blockedTurns = 0;
    }
    
    @Override
    public String getName() {
        return Lang.ENTITY_SEAL.getKey();
    }
    
    public boolean hasBeenBribed() {
        return isBribed;
    }
    
    /**
     * MÈTODE PRINCIPAL: Se llama cuando un jugador cae en la casilla de la foca.
     * Le pasamos el jugador y el número de la casilla del agujero anterior.
     */
    public void interact(Player player, int previousHoleSquare) {
        
        // 1. Si la foca ya está comiendo, no ataca.
        if (this.blockedTurns > 0) {
            System.out.println("La foca està menjant un peix i no et fa cas. Et salves!");
            return; // Terminamos el método aquí, el jugador se salva.
        }
        
        // 2. Comprobamos si el jugador tiene algún pez en su inventario.
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            bribeSeal(player);
        } else {
            hitPlayer(player, previousHoleSquare);
        }
    }
    
    /**
     * MÈTODE SOBORNAR: El jugador tiene un pez y se lo da a la foca.
     */
    public void bribeSeal(Player player) {
        System.out.println(player.getName() + " li dóna un peix a la foca!");
        
        // Le quitamos 1 pez del inventario
        player.getInventory().useObject(ObjectType.FISH, 1);
        
        // Bloqueamos a la foca
        this.isBribed = true;
        this.blockedTurns = 2; // Queda bloqueada por 2 turnos
        
        System.out.println("La foca es menjarà el peix i quedarà bloquejada durant 2 torns.");
    }
    
    /**
     * MÈTODE COLPEJAR: El jugador no tiene peces y la foca lo castiga.
     */
    public void hitPlayer(Player player, int previousHoleSquare) {
        System.out.println("Oh no! " + player.getName() + " no té peixos!");
        System.out.println("La foca et colpeja i t'envia al forat anterior!");
        
        // Movemos al jugador a la casilla del agujero anterior
        player.setSquare(previousHoleSquare);
    }
    
   
    public void updateSealTurns() {
        if (this.blockedTurns > 0) {
            this.blockedTurns--; // Restamos 1 turno
            
            if (this.blockedTurns == 0) {
                this.isBribed = false; // La foca vuelve a tener hambre
                System.out.println("La foca ja ha acabat de menjar i torna a ser perillosa!");
            }
        }
    }
}