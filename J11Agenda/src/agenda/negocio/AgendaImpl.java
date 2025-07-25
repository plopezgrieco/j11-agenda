package agenda.negocio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import agenda.modelo.Contacto;
import agenda.persistencia.ContactoDao;
import agenda.persistencia.ContactoDaoJDBC;
import agenda.persistencia.ContactoDaoMemSerial;

public class AgendaImpl implements Agenda {
	
	private ContactoDao cDao;
	
	public AgendaImpl(ContactoDao cDao) {
		this.cDao = cDao;
	}
	
	public AgendaImpl() {
//		cDao = new ContactoDaoMemDinamica();
//		cDao = new ContactoDaoMemSerial();
		cDao = new ContactoDaoJDBC();
	}

	@Override
	public void insertarContacto(Contacto c) {
		cDao.insertar(c);
	}

	@Override
	public Contacto eliminar(int idContacto) {
		Contacto eli = cDao.buscar(idContacto);
		cDao.eliminar(eli);
		return eli;
	}

	@Override
	public boolean eliminar(Contacto c) {
		return cDao.eliminar(c);
	}

	@Override
	public void modificar(Contacto c) {
		cDao.actualizar(c);
	}

	@Override
	public Set<Contacto> buscarTodos() {
		Set<Contacto> resp = new TreeSet<>(getCompApodo());
		resp.addAll(cDao.buscarTodos());
		return resp;
	}
	
	private Comparator<Contacto> getCompApodo(){
		Collator col = Collator.getInstance(new Locale("es"));
		return new Comparator<Contacto>() {
			@Override
			public int compare(Contacto o1, Contacto o2) {
				return col.compare(o1.getApodo() + o1.getIdContacto(), o2.getApodo() + o2.getApodo());
			}
		};
	}

	private Comparator<Contacto> getCompApodoLambda(){
		Collator col = Collator.getInstance(new Locale("es"));
		return (c1, c2) -> col.compare(c1.getApodo() + c1.getIdContacto(), c2.getApodo() + c2.getIdContacto());
	}
	
	@Override
	public Set<Contacto> buscarContactoPorNombre(String nombre) {
		return cDao.buscar(nombre);
	}

	@Override
	public Contacto buscar(int idContacto) {
		return cDao.buscar(idContacto);
	}

	@Override
	public int importarCSV(String fichero) throws IOException {
		List<String[]> datos = importar(fichero);
		int cant = 0;
		for (String[] array : datos) {
			Contacto con = new Contacto();
			con.setNombre(array[0]);
			con.setApellidos(array[1]);
			con.setApodo(array[2]);
			con.getDom().setTipoVia(array[3]);
			con.getDom().setVia(array[4]);
			//controlar la excepcion Numberformat
			try {
				con.getDom().setNumero(Integer.parseInt(array[5]));
			} catch(NumberFormatException e) {
				//guardamos igual el contacto con numero = 0
				//hacer algun tipo de log
			}
			try {
				con.getDom().setPiso(Integer.parseInt(array[6]));
			} catch(NumberFormatException e) {
				//guardamos igual el contacto con piso = 0
				//hacer algun tipo de log
			}
			con.getDom().setPuerta(array[7]);
			con.getDom().setCodigoPostal(array[8]);
			con.getDom().setCiudad(array[9]);
			con.getDom().setProvincia(array[10]);
			String[] telefonos = array[11].split("/");
			for (String telefono : telefonos) {
				con.addTelefono(telefono);
			}
			for (String correo : array[12].split("/"))
				con.addCorreo(correo);
			//...
			cDao.insertar(con);
			cant++;
		}
		return cant;
	}

	private List<String[]> importar(String fichero){
		List<String[]> datos = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fichero))){
			String linea;
			while((linea = br.readLine()) != null){
				String[] datosLinea = linea.split(";");
				datos.add(datosLinea);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return datos;
	}

	
}
