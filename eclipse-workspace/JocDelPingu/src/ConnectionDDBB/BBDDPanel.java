package ConnectionDDBB;

import java.sql.Connection;
import java.util.Scanner;

public class BBDDPanel {
	public static Connection con;

	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);
		con = BBDD.conectarBaseDatos(scan);
		
		if (con != null) {
			// Añadimos también COLOUR a la lista para imprimirlo y verlo
			String[] columnas = { "ENTITYID", "ENTITYTYPE", "PLAYERNAME", "PLAYERPASSWORD", "COLOUR" };
			
			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n--- 1. INSERT ---");
			// Le ponemos el color 'BLUE' para cumplir la regla CK_PLAYER_COLOUR
			BBDD.insert(con, "INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR) VALUES (999, 'PLAYER', 'PinguTest', 'SuperPingu1234', 'BLUE')");
			BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);
			
			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n--- 2. UPDATE ---");
			BBDD.update(con, "UPDATE ENTITY SET PLAYERPASSWORD = 'NuevaClave99' WHERE PLAYERNAME = 'PinguTest'");
			BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);
			
			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n--- 3. DELETE ---");
			BBDD.delete(con, "DELETE FROM ENTITY WHERE PLAYERNAME = 'PinguTest'");
			BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 999", columnas);
			
			////////////////////////////////////////////////////////////////////////////////////
			BBDD.cerrar(con);
			System.out.println("\n¡Pruebas terminadas y conexión cerrada! Todo funciona de 10.");
		}
	}
}