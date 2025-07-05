package agenda.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import agenda.config.Config;
import agenda.excepciones.ContactoPersistenceException;
import agenda.modelo.Contacto;

public class ContactoDaoJDBC implements ContactoDao {

	private DataSource ds;
	
	public ContactoDaoJDBC() {
		ds = Config.getDataSource();
	}
	
	@Override
	public void insertar(Contacto c) {
		String sql = "insert into contactos values(null,?,?,?,?,?,?,?,?,?,?,?)";
		try(Connection cx = ds.getConnection()) {
			cx.setAutoCommit(false);
			PreparedStatement ps = cx.prepareStatement(sql);
			ps.setString(1, c.getNombre());
			ps.setString(2, c.getApellidos());
			ps.setString(3, c.getApodo());
			ps.setString(4, c.getDom().getTipoVia());
			ps.setString(5, c.getDom().getVia());
			ps.setInt(6, c.getDom().getNumero());
			ps.setInt(7, c.getDom().getPiso());
			ps.setString(8, c.getDom().getPuerta());
			ps.setString(9, c.getDom().getCodigoPostal());
			ps.setString(10, c.getDom().getCiudad());
			ps.setString(11, c.getDom().getProvincia());
			
			int filas = ps.executeUpdate();
			
			if(filas == 1) {
				PreparedStatement psId = cx.prepareStatement("select LAST_INSERT_ID()");
				ResultSet rs = psId.executeQuery();
				rs.next();
				int idGen = rs.getInt(1);
				
				sql = "insert into telefonos values(null,?,?)";
				PreparedStatement psTel = cx.prepareStatement(sql);
				
				int cantTel = 0;
				for (String tel : c.getTelefonos()) {
					psTel.setInt(1, idGen); //fk a contactos
					psTel.setString(2, tel); //nro de telefono
					cantTel += psTel.executeUpdate();
				}
				
				sql = "insert into correos values(null,?,?)";
				PreparedStatement psCor = cx.prepareStatement(sql);
				
				int cantCor = 0;
				for (String correo : c.getCorreos()) {
					psCor.setInt(1, idGen); //fk a contactos
					psCor.setString(2, correo); //direccion de corrre
					cantCor += psCor.executeUpdate();
				}
				
				if (cantTel == c.getTelefonos().size() && cantCor == c.getCorreos().size()) {
					cx.commit();
				} else {
					cx.rollback();
					throw new ContactoPersistenceException();
				}
					
			} else {
				cx.rollback();
				throw new ContactoPersistenceException();
			}
		} catch (SQLException e) {
			// Hacer un log fecha, hora, usuario, tipo de error
			e.printStackTrace();
			throw new ContactoPersistenceException();
		}
		
	}

	@Override
	public void actualizar(Contacto c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean eliminar(int idContacto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean eliminar(Contacto c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Contacto buscar(int idContacto) {
		Contacto c = null;
		Set<Contacto> resu = buscarCondicion("idcontactos = " + idContacto);
		for (Contacto contacto : resu) {
			c = contacto;
		}
		return c;
	}

	@Override
	public Set<Contacto> buscar(String cadena) {
		String cond = "nombre like '%" + cadena + "%' or " +
				"apellidos like '%" + cadena + "%' or " +
				"apodo like '%" + cadena + "%'";
		return buscarCondicion(cond);
	}

	@Override
	public Set<Contacto> buscarTodos() {
		return buscarCondicion("true");
	}
	
	private Set<Contacto> buscarCondicion(String condicion) {
		Set<Contacto> resu = new HashSet<>();
		String sql = """
			SELECT idcontactos, nombre, apellidos, apodo, 
				tipo_via, via, numero, piso, puerta, 
				codigo_postal, ciudad, provincia 
				FROM contactos WHERE 
				""" + condicion + ";";
		try (Connection cx = ds.getConnection()){
			PreparedStatement ps = cx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Contacto c = new Contacto();
				c.setIdContacto(rs.getInt("idcontactos"));
				c.setNombre(rs.getString("nombre"));
				c.setApellidos(rs.getString("apellidos"));
				c.setApodo(rs.getString("apodo"));
				
				c.getDom().setTipoVia(rs.getString("tipo_via"));
				c.getDom().setVia(rs.getString("via"));
				c.getDom().setNumero(rs.getInt("numero"));
				c.getDom().setPiso(rs.getInt("piso"));
				c.getDom().setPuerta(rs.getString("puerta"));
				c.getDom().setCodigoPostal(rs.getString("codigo_postal"));
				c.getDom().setCiudad(rs.getString("ciudad"));
				c.getDom().setProvincia(rs.getString("provincia"));
				
				sql = "select telefono from telefonos where fk_contacto = ?";
				PreparedStatement psT = cx.prepareStatement(sql);
				psT.setInt(1, c.getIdContacto());
				
				ResultSet rsT = psT.executeQuery();
				while(rsT.next()) {
					c.addTelefono(rsT.getString("telefono"));
				}
				
				sql = "select correo from correos where fk_contacto = ?";
				PreparedStatement psC = cx.prepareStatement(sql);
				psC.setInt(1, c.getIdContacto());
				
				ResultSet rsC = psC.executeQuery();
				while(rsC.next()) {
					c.addCorreo(rsC.getString("correo"));
				}
				
				resu.add(c);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ContactoPersistenceException();
		}
		
		return resu;
	}

}
