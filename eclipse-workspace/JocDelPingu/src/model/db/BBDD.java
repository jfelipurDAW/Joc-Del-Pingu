package model.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Clase que proporciona métodos para interactuar con una base de datos Oracle.
 */
public class BBDD {

	/**
	 * Intenta establecer una conexión a la base de datos Oracle. NO HACE FALTA QUE
	 * ENTENDÁIS CÓMO FUNCIONA, SE HACE TODO DE MANERA AUTOMÁTICA.
	 *
	 * @param scan Scanner de main con el que vais a leer por consola
	 * @return Objeto Connection si la conexión es exitosa, null en caso contrario.
	 *         LA VARIABLE QUE DEVUELVE LA TENÉIS QUE GUARDAR PARA LAS DEMÁS
	 *         FUNCIONES
	 */
	public static Connection conectarBaseDatos(Scanner scan) {
		System.out.println("Intentando conectarse a la base de datos...");

		// Hardcoded environment
		String entorno = "fuera"; // change to "centro" if working at Ilerna

		String url = entorno.equals("centro") ? "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2"
				: "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";

		// 2) Pedir credenciales (con trim para evitar espacios raros)
		// PODEIS HARDCODEAR ESTAS CREDENCIALES SI VAIS A USAR SIEMPRE LAS MISMAS
		//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
//		System.out.println("¿Usuario?");
//		String user = scan.nextLine().trim();
		String user = "DW2526_GR06_PINGU";

//		System.out.println("¿Contraseña?");
//		String pwd = scan.nextLine(); // aquí NO hago trim por si la contraseña tuviera espacios
		String pwd = "ABDJFMV";

		// 3) Conectar
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, user, pwd);
			if (con.isValid(5)) {
				System.out.println("Database connection established successfully.");
			}
			return con;
		} catch (ClassNotFoundException e) {
			System.err.println("Oracle JDBC driver not found. Make sure ojdbc is in the classpath.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Failed to connect to database at: " + url);
			System.err.println("SQL Error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Cierra la conexión con la BBDD.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 */
	public static void cerrar(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ignored) {
			}
		}
	}

	/**
	 * Realiza una inserción en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de inserción que hayáis creado.
	 */
	public static int insert(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Insert");
	}

	/**
	 * Realiza una actualización en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de actualización que hayáis creado.
	 */
	public static int update(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Update");
	}

	/**
	 * Realiza una eliminación en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de eliminación que hayáis creado.
	 */
	public static int delete(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Delete");
	}

	/**
	 * Realiza una consulta en la base de datos y devuelve los resultados.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de consulta.
	 * @return Devuelve un ArrayList con todas las filas del SELECT. Cada fila es un
	 *         Map con sus columnas (columna -> valor).
	 */
	public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {

		ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();

		if (con == null) {
			return resultados;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

			ResultSetMetaData meta = rs.getMetaData();
			int numColumnas = meta.getColumnCount();

			while (rs.next()) {
				LinkedHashMap<String, String> fila = new LinkedHashMap<>();

				for (int i = 1; i <= numColumnas; i++) {
					String columna = meta.getColumnLabel(i);
					String valor = rs.getString(i);
					fila.put(columna, valor);
				}

				resultados.add(fila);
			}

		} catch (SQLException e) {
			System.err.println("SELECT error: " + e.getMessage());
		}

		return resultados;
	}

	/**
	 * Imprime los resultados de una consulta SELECT en la base de datos. EN ESTE
	 * CASO SÍ PODÉIS IMPRIMIR MÁS DE UNA FILA.
	 *
	 * @param con                         Objeto Connection que representa la
	 *                                    conexión a la base de datos.
	 * @param sql                         Sentencia SQL de consulta.
	 * @param listaElementosSeleccionados Array de Strings con los nombres de las
	 *                                    columnas seleccionadas.
	 */
	public static void print(Connection con, String sql, String[] listaElementosSeleccionados) {
		if (con != null) {
			try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

				int fila = 0;
				boolean hayResultados = false;

				while (rs.next()) {
					hayResultados = true;
					fila++;
					System.out.println("---- Fila " + fila + " ----");
					for (String col : listaElementosSeleccionados) {
						System.out.println(col + ": " + rs.getString(col));
					}
				}

			} catch (SQLException e) {
				// error in SELECT execution
			}
		}
	}

	/**
	 * Ejecuta las consultas Insert, Update o Delete.
	 *
	 * @param con      Objeto Connection que representa la conexión a la base de
	 *                 datos.
	 * @param sql      Sentencia SQL que se va a ejecutar.
	 * @param etiqueta Consulta a ejecutar -> Insert / Update / Delete
	 * @return Número de filas afectadas
	 */
	public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
		if (con == null) {
			return 0;
		}

		try (Statement st = con.createStatement()) {
			int filas = st.executeUpdate(sql);
			return filas;
		} catch (SQLException e) {
			System.err.println(etiqueta + " error: " + e.getMessage());
			return 0;
		}
	}
	
	public void registrarJugadorEnBD(Connection con, int idJugador, String nombre, String password, String color) {
	    String sql = "INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR) " +
	                 "VALUES (" + idJugador + ", 'PLAYER', '" + nombre + "', '" + password + "', '" + color + "')";
	    int filas = BBDD.insert(con, sql);
	}

    public static String encriptarTexto(String textoNormal) {
        return java.util.Base64.getEncoder().encodeToString(textoNormal.getBytes());
    }

    public static boolean guardarPartida(java.util.List<model.entity.Player> listaJugadores) {
        Connection con = conectarBaseDatos(null);
        if (con == null) return false;

        StringBuilder textoDatos = new StringBuilder();
        
        for (model.entity.Player p : listaJugadores) {
            textoDatos.append("Jugador:").append(p.getName())
                      .append(",Casilla:").append(p.getSquareIndex())
                      .append(",Bolas:").append(p.getInventory().getSnowballQuantity())
                      .append(",Peces:").append(p.getInventory().getFishQuantity())
                      .append("; ");
        }
        
        String datosFinales = textoDatos.toString();
        String datosEncriptados = encriptarTexto(datosFinales);
        String idUnico = "PARTIDA_" + System.currentTimeMillis();
        
        String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) " +
                     "VALUES ('" + idUnico + "', '" + datosEncriptados + "')";
        
        int filas = insert(con, sql);
     // Esto activa el trigger TRG_GAME_FINISHED que incrementa GAMES_WON
        if (filas > 0) {
            String sqlFinish = "UPDATE GAME SET GAMESTATE = 'FINISHED' " +
                               "WHERE GAMEID = (SELECT MAX(GAMEID) FROM GAME)";
            update(con, sqlFinish);
        }
        cerrar(con);
        return filas > 0;
    }
}