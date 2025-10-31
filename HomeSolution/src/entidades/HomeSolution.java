package entidades;

import java.time.LocalDate;
import java.util.*;

public class HomeSolution implements IHomeSolution{
	private HashMap<String, IEmpleado> empleados;
	private HashMap<String, EnpleadoDePlanta> empleadosDePlantas;
    private Stack<IEmpleado> empleadosLibres;
    private Stack<EnpleadoDePlanta> empleadosDePlantaLibres;
    private HashMap<String, Tarea> tareas;
    private HashMap<String, Cliente> clientes;
    private HashMap<Integer, Proyecto> proyectos;
    private List<Proyecto> listaProyectos;
    private Proyecto proyecto;
    private Map<Integer, Integer> atrasosPorEmpleado;
    private final Estado tipoDeEstado = new Estado();

    public HomeSolution() {
        this.empleados = new HashMap<>();
        this.empleadosDePlantas = new HashMap<>();
        this.empleadosLibres = new Stack<>();
        this.empleadosDePlantaLibres = new Stack<>();
        this.tareas = new HashMap<>();
        this.clientes = new HashMap<>();
        this.proyectos = new HashMap<>();
        this.listaProyectos = new ArrayList<>();
        this.proyecto = null;
        this.atrasosPorEmpleado = new HashMap<>();
    }
	
	@Override
	public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
		if (empleados.containsKey(nombre))
            throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        EmpleadoPorHora empleado = new EmpleadoPorHora(nombre, valor);
        empleados.put(nombre, empleado);
        empleadosLibres.push(empleado);
		
	}

	@Override
	public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
		if (empleados.containsKey(nombre))
            throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        EnpleadoDePlanta empleado = new EnpleadoDePlanta(nombre, valor, categoria);
        empleados.put(nombre, empleado);
        empleadosDePlantaLibres.push(empleado);
	}

	@Override
	public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias, String domicilio,
			String[] cliente, String inicio, String fin) throws IllegalArgumentException {
		for (String titulo : titulos)
            if (tareas.containsKey(titulo))
                throw new IllegalArgumentException("El titulo: " + titulo + " ya esta registrado.");

        String nombre = cliente[0];
        String telefono = cliente[1];
        String email = cliente[2];
        Cliente c = new Cliente(nombre, telefono, email);
        //clientes.put(email, c);

        int codProyecto = proyectos.size() + 1;

        List<Tarea> tareasProyecto = new ArrayList<>();
        for (int i = 0; i < titulos.length; i++) {
            Tarea tarea = new Tarea(titulos[i], descripcion[i], dias[i]);
            tareasProyecto.add(tarea);
            tareas.put(titulos[i], tarea);
        }

        Proyecto proyecto = new Proyecto(codProyecto, domicilio, inicio, fin);
        proyecto.agregarCliente(c);
        for (Tarea tarea : tareasProyecto)
            proyecto.agregarTarea(tarea);
        proyectos.put(codProyecto, proyecto);
	}

	@Override
	public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        Tarea tarea = tareas.get(titulo);
        if (tarea == null)
            throw new RuntimeException("La tarea no está registrada");

        IEmpleado empleadoAnterior = tarea.obtenerEmpleado();
        if (empleadoAnterior != null && !empleadoAnterior.estaLibre()) {
            empleadoAnterior.estaLibre();
            empleadosLibres.push(empleadoAnterior);
        }
        proyecto.asignarEmpleado(tarea, empleadoAnterior);
	}

	@Override
	public void asignarResponsableMenosRetraso(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");
        if (!tareas.containsKey(titulo))
            throw new RuntimeException("La tarea no está registrada");

        Tarea tarea = tareas.get(titulo);
        if (tarea.obtenerEstado().equals("FINALIZADO"))
            throw new RuntimeException("No se pueden asignar empleados a una tarea finalizada");

        Iterator<IEmpleado> este = empleadosLibres.iterator();
        if (!este.hasNext())
            throw new RuntimeException("No hay empleados libres disponibles");

        IEmpleado mejorCandidato = null;
        int menosAtrasos = Integer.MAX_VALUE;

        while (este.hasNext()) {
            IEmpleado empleado = este.next();
            int atrasos = empleado.obtenerRetrasos();
            if (atrasos < menosAtrasos) {
                menosAtrasos = atrasos;
                mejorCandidato = empleado;
            }
        }

        mejorCandidato = tarea.obtenerEmpleado();
        if (!mejorCandidato.estaLibre()) {
            mejorCandidato.estaLibre();
            empleadosLibres.push(mejorCandidato);
        }

        proyecto.asignarEmpleado(tarea, mejorCandidato);
        mejorCandidato.marcarOcupado();
	}

	@Override
	public void registrarRetrasoEnTarea(Integer numero, String titulo, double cantidadDias)
			throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");
        if (!tareas.containsKey(titulo))
            throw new RuntimeException("La tarea no está registrada");
        Tarea tarea = tareas.get(titulo);
        tarea.registrarRetraso(cantidadDias);
	}

	@Override
	public void agregarTareaEnProyecto(Integer numero, String titulo, String descripcion, double dias)
			throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new IllegalArgumentException("El número de proyecto no está registrado.");
        if (proyecto.obtenerEstado().equals("FINALIZADO"))
            throw new IllegalArgumentException("No se puede agregar tareas a un proyecto finalizado.");
        if (tareas.containsKey(titulo))
            throw new IllegalArgumentException("El titulo: " + titulo + " ya esta registrado.");

        Tarea tarea = new Tarea(titulo, descripcion, dias);
        proyecto.agregarTarea(tarea);
        tarea.actualizarFecha(dias);
        tareas.put(titulo, tarea);
	}

	@Override
	public void finalizarTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new Exception("El número de proyecto no está registrado");

        // Validar tarea
        Tarea tarea = tareas.get(titulo);
        if (tarea == null)
            throw new Exception("La tarea no está registrada");

        if (!proyecto.perteneceAestaTarea(titulo))
            throw new Exception("La tarea no pertenece al proyecto");

        // Marcar tarea como finalizada
        if (tarea.estaFinalizada())
            throw new Exception("La tarea ya está finalizada");
        tarea.marcarComoFinalizada();
        proyecto.liberarEmpleadoAsignado(titulo);
        proyecto.verificarTodasTareasFinalizadas();
	}

	@Override
	public void finalizarProyecto(Integer numero, String fin) throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");
        proyecto.finalizar(fin);
        // Liberar empleados
        for (Tarea tarea : proyecto.obtenerTareas()) {
            IEmpleado emp = tarea.obtenerEmpleado();
            if (emp != null && !emp.estaLibre()) {
                emp.estaLibre();
                empleadosLibres.push(emp);
            }
        }
	}

	@Override
	public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double costoProyecto(Integer numero) {
		Proyecto p = proyectos.get(numero);
        return p.calcularCostoTotal(empleados);
	}

	@Override
	public List<Tupla<Integer, String>> proyectosFinalizados() {
		List<Tupla<Integer, String>> finalizados = new ArrayList<>();
		for (Proyecto tupla : listaProyectos) {
			if (tupla.verificarTodasTareasFinalizadas() == true) {
				finalizados.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
			}
		}
		return finalizados;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosPendientes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosActivos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] empleadosNoAsignados() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean estaFinalizado(Integer numero) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int consultarCantidadRetrasosEmpleado(Integer legajo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] tareasProyectoNoAsignadas(Integer numero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] tareasDeUnProyecto(Integer numero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String consultarDomicilioProyecto(Integer numero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tieneRestrasos(Integer legajo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Tupla<Integer, String>> empleados() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String consultarProyecto(Integer numero) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
