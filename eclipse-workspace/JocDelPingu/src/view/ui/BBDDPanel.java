package view.ui;

import java.sql.Connection;
import java.util.Scanner;

public class BBDDPanel {
	public static Connection con;

	public static void main(String[] args) {

		System.out.println("==> [PAS 0] Iniciant BBDDPanel.main()");
		System.out.flush();

		Scanner scan = new Scanner(System.in);

		System.out.println("==> [PAS 1] Cridant BBDD.conectarBaseDatos()...");
		System.out.flush();

		con = model.db.BBDD.conectarBaseDatos(scan);

		System.out.println("==> [PAS 2] conectarBaseDatos() ha retornat. con == null? " + (con == null));
		System.out.flush();

		if (con != null) {
			String[] columnas = { "ENTITYID", "ENTITYTYPE", "PLAYERNAME", "PLAYERPASSWORD", "COLOUR" };

			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n==> [PAS 3] --- 1. INSERT ---");
			System.out.flush();
			int filesInsert = model.db.BBDD.insert(con,
				"INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR) " +
				"VALUES (222, 'PLAYER', 'PinguTest2', 'SuperPingu1234', 'BLUE')");
			System.out.println("==> INSERT ha afectat " + filesInsert + " fila(es).");
			System.out.flush();

			System.out.println("==> [PAS 4] SELECT per verificar INSERT:");
			System.out.flush();
			model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 222", columnas);

			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n==> [PAS 5] --- 2. UPDATE ---");
			System.out.flush();
			int filesUpdate = model.db.BBDD.update(con,
				"UPDATE ENTITY SET PLAYERPASSWORD = 'NuevaClave99' WHERE PLAYERNAME = 'PinguTest2'");
			System.out.println("==> UPDATE ha afectat " + filesUpdate + " fila(es).");
			System.out.flush();

			System.out.println("==> [PAS 6] SELECT per verificar UPDATE:");
			System.out.flush();
			model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 222", columnas);

			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n==> [PAS 7] --- 3. DELETE ---");
			System.out.flush();
			int filesDelete = model.db.BBDD.delete(con,
				"DELETE FROM ENTITY WHERE PLAYERNAME = 'PinguTest2'");
			System.out.println("==> DELETE ha afectat " + filesDelete + " fila(es).");
			System.out.flush();

			System.out.println("==> [PAS 8] SELECT per verificar DELETE (hauria de ser buit):");
			System.out.flush();
			model.db.BBDD.print(con, "SELECT * FROM ENTITY WHERE ENTITYID = 222", columnas);

			////////////////////////////////////////////////////////////////////////////////////
			System.out.println("\n==> [PAS 9] Tancant connexió...");
			System.out.flush();
			model.db.BBDD.cerrar(con);
			System.out.println("==> [PAS 10] Pruebas terminadas y conexión cerrada! Todo funciona de 10.");
			System.out.flush();
		} else {
			System.err.println("==> ERROR: La connexió és null. Abortant.");
		}
	}
}
