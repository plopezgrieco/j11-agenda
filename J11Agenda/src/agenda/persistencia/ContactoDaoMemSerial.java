package agenda.persistencia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import agenda.modelo.Contacto;

public class ContactoDaoMemSerial implements ContactoDao {

	private Map<Integer, Contacto> almacen;
	private Integer proximoId;
	private final String F_MAPA = "mapa.dat";
	private final String F_IDX = "indice.dat";
	
	public ContactoDaoMemSerial() {
		try (FileInputStream fis = new FileInputStream(F_MAPA);
			 FileInputStream fisIdx = new FileInputStream(F_IDX)){
			ObjectInputStream ois = new ObjectInputStream(fis);
			almacen = (Map<Integer, Contacto>) ois.readObject();
			ObjectInputStream oisIdx = new ObjectInputStream(fisIdx);
			proximoId = (Integer)oisIdx.readObject();
		} catch (FileNotFoundException e) {
			//Si no existe el fichero creamos una nueva almacen
			almacen = new HashMap<>();
			proximoId = 1;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		} 
	}
	
	private void grabar() {
		try (FileOutputStream fos = new FileOutputStream(F_MAPA);
			 FileOutputStream fosIdx = new FileOutputStream(F_IDX)){
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(almacen);
			oos = new ObjectOutputStream(fosIdx);
			oos.writeObject(proximoId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	//Generar el id
	//Añadir un elemento en el map
	@Override
	public void insertar(Contacto c) {
		c.setIdContacto(proximoId++);
		almacen.put(c.getIdContacto(), c);
		grabar();
	}

	@Override
	public void actualizar(Contacto c) {
		almacen.replace(c.getIdContacto(), c);
		grabar();
	}

	// retorna false si el contacto no existe
	@Override
	public boolean eliminar(int idContacto) {
		Contacto eliminado = almacen.remove(idContacto);
		grabar();
		return eliminado != null;
	}

	@Override
	public boolean eliminar(Contacto c) {
		return eliminar(c.getIdContacto());
	}

	@Override
	public Contacto buscar(int idContacto) {
		return almacen.get(idContacto);
	}

	@Override
	public Set<Contacto> buscar(String cadena) {
		cadena = cadena.toLowerCase();
		Set<Contacto> resu = new HashSet<>();
		for (Contacto con : almacen.values()) {
			if (con.getApellidos().toLowerCase().contains(cadena) ||
					con.getNombre().toLowerCase().contains(cadena) ||
					con.getApodo().toLowerCase().contains(cadena)) {
				resu.add(con);
			}
		}
		return resu;
	}

	@Override
	public Set<Contacto> buscarTodos() {
		//opcion 1 muy fea
//		Collection<Contacto> valores = almacen.values();
//		Set<Contacto> resu = new HashSet<Contacto>();
//		for (Contacto c : valores) {
//			resu.add(c);
//		}
//		return resu;
		
		//opcion 2 - menos fea
//		Collection<Contacto> valores = almacen.values();
//		Set<Contacto> resu = new HashSet<Contacto>();
//		resu.addAll(valores);
//		return resu;

		//opcion 3 - bastante menos fea
//		Collection<Contacto> valores = almacen.values();
//		Set<Contacto> resu = new HashSet<Contacto>(valores);
//		return resu;
		
		//opcion que mas me gusta
		return new HashSet<Contacto>(almacen.values());
	}

}
