package entidades;

import java.time.LocalDate;
import java.util.*;

public class HomeSolution implements IHomeSolution{
	private HashMap<Integer, IEmpleado> empleados;
    private Stack<IEmpleado> empleadosLibres;
    private HashMap<String, Tarea> tareas;
    private HashMap<Integer, Proyecto> proyectos;

    public HomeSolution() {
        this.empleados = new HashMap<>();
        this.empleadosLibres = new Stack<>();
        this.tareas = new HashMap<>();
        this.proyectos = new HashMap<>();
    }

	@Override
	public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
        try {
            if (empleados.containsKey(nombre))
                throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if(valor < 0)
            throw new IllegalArgumentException("El valor no puede ser menor a 0: " + valor);
        EmpleadoPorHora empleado = new EmpleadoPorHora(nombre, valor);
        empleados.put(empleado.obtenerLegajo(), empleado);
        agregarEmpleadoLibre(empleado);
	}

	@Override
	public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
		if (empleados.containsKey(nombre))
            throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        EnpleadoDePlanta empleado = new EnpleadoDePlanta(nombre, valor, categoria);
        empleados.put(empleado.obtenerLegajo(), empleado);
        agregarEmpleadoLibre(empleado);
	}

	@Override
	public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias, String domicilio,
			String[] cliente, String inicio, String fin) throws IllegalArgumentException {
		try {
            for (String titulo : titulos) {
                if (tareas.containsKey(titulo)) {
                    throw new IllegalArgumentException("El titulo: " + titulo + " ya esta registrado.");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Advertencia: " + e.getMessage());
        }
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
        //for (Tarea tarea : tareasProyecto)
          //  proyecto.agregarTarea(titulos, descripcion, dias);
        for (int i = 0; i < tareasProyecto.toArray().length; i++) {
            proyecto.agregarTarea(titulos[i], descripcion[i], dias[i]);
        }
        proyectos.put(codProyecto, proyecto);
	}

	@Override
	public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        Tarea tarea = proyecto.obtenerTarea(titulo);
        if (tarea == null)
            throw new RuntimeException("La tarea no está registrada");

        IEmpleado empleadoAnterior = tarea.obtenerEmpleado();
        if (empleadoAnterior != null && !empleadoAnterior.estaLibre()) {
            empleadoAnterior.estaLibre();
            agregarEmpleadoLibre(empleadoAnterior);
        }

        IEmpleado empleadoAAsignar = null;
        if(!empleadosLibres.isEmpty()){
            empleadoAnterior = empleadosLibres.pop();
        }
        tarea.asignarEmpleado(empleadoAnterior);
        if (empleadoAnterior != null) {
            empleadoAnterior.marcarOcupado();
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

        Tarea tarea = proyecto.obtenerTarea(titulo);
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
            agregarEmpleadoLibre(mejorCandidato);
            // empleadosLibres.push(mejorCandidato);
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
        Tarea tarea = proyecto.obtenerTarea(titulo);
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
        proyecto.agregarTarea(titulo, descripcion, dias);
        tarea.actualizarFecha(dias);
        tareas.put(titulo, tarea);
	}

	@Override
	public void finalizarTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new Exception("El número de proyecto no está registrado");

        // Validar tarea
        Tarea tarea = proyecto.obtenerTarea(titulo);
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

        LocalDate fechaFin = LocalDate.parse(fin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(fechaFin.isBefore(proyecto.obtenerFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        proyecto.finalizar(fin);
        // Liberar empleados
        for (Tarea tarea : proyecto.obtenerTareas()) {
            IEmpleado emp = tarea.obtenerEmpleado();
            if (emp != null && !emp.estaLibre()) {
                emp.liberar();
                agregarEmpleadoLibre(emp);
                empleadosLibres.push(emp);
            }
        }
	}

	@Override
	public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        try {
            if(proyecto == null)
                throw new RuntimeException("El número de proyecto no está registrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        IEmpleado empleadoEnReemplazo = empleados.get(legajo);
        try {
            if(empleadoEnReemplazo == null)
                throw new RuntimeException("No existe ningun empleado con ese legajo");

            if(!empleadoEnReemplazo.estaLibre())
                throw new RuntimeException("Este empleado no esta libre");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        Tarea tarea = proyecto != null ? proyecto.obtenerTarea(titulo) : null;
        try {
            if (tarea == null)
                throw new Exception("La tarea no está registrada");

            if (!proyecto.perteneceAestaTarea(titulo))
                throw new Exception("La tarea no pertenece al proyecto");

            if (tarea.obtenerEmpleado() == null)
                throw new Exception("La tarea no tiene asignado ningun empleado previamente");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //Primero libero al empleado
        if (tarea != null) {
            tarea.liberarResponsable();
            tarea.asignarEmpleado(empleadoEnReemplazo);
        }
	}

	@Override
	public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = proyectos.get(numero);
		if(proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        //Primero valido que este libre, despues en una primer instacia asigno al primer empleado libre
        //despues voy viendo cual es el que tiene menos retrasos
        Empleado empleadoConMenosRetraso = null;
        for(IEmpleado empleado : empleados.values() ){
            if(empleado.estaLibre()){
                if(empleadoConMenosRetraso == null ||
                        (empleado.obtenerRetrasos() < empleadoConMenosRetraso.obtenerRetrasos()))
                    empleadoConMenosRetraso = (Empleado) empleado;
            }
        }

        if(empleadoConMenosRetraso == null)
            throw new RuntimeException("No existe ningun empleado libre");

        Tarea tarea = proyecto.obtenerTarea(titulo);
        if (tarea == null)
            throw new Exception("La tarea no está registrada");

        if (!proyecto.perteneceAestaTarea(titulo))
            throw new Exception("La tarea no pertenece al proyecto");

        if (tarea.obtenerEmpleado() == null)
            throw new Exception("La tarea no tiene asignado ningun empleado previamente");

        //Primero libero al empleado
        tarea.liberarResponsable();

        //Ahora le reasigno al empleado con menos retrasos
        tarea.asignarEmpleado(empleadoConMenosRetraso);

	}

	@Override
	public double costoProyecto(Integer numero) {
		Proyecto p = proyectos.get(numero);
        /*double total = 0.0;
        for (Tarea t : p.obtenerTareas()) {
            IEmpleado e = t.obtenerEmpleado();
            double dias = Math.ceil(t.obtenerDiasEstimados());
            double costo = e.calcularCosto(dias);
            if (e instanceof EnpleadoDePlanta && e.obtenerRetrasos() == 0)
                costo *= 1.02;
            total += costo;
        }
        return total;*/
        return p.calcularCostoTotal(empleados);
	}

	@Override
	public List<Tupla<Integer, String>> proyectosFinalizados() {
		List<Tupla<Integer, String>> finalizados = new ArrayList<>();
		for (Proyecto tupla : proyectos.values()) {
			if (tupla.verificarTodasTareasFinalizadas() && tupla.obtenerEstado().equals(Estado.finalizado)) {
				finalizados.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
			}
		}
		return finalizados;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosPendientes() {
		List<Tupla<Integer, String>> pendientes = new ArrayList<>();
        for (Proyecto tupla : proyectos.values()) {
            if (tupla.obtenerEstado().equals(Estado.pendiente)) {
                pendientes.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
            }
        }
        return pendientes;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosActivos() {
		List<Tupla<Integer, String>> activos = new ArrayList<>();
        for (Proyecto tupla : proyectos.values()) {
            if (tupla.obtenerEstado().equals(Estado.activo)) {
                activos.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
            }
        }
        return activos;
	}

	@Override
	public Object[] empleadosNoAsignados() {
		int cantEmpleadosLibres = 0;
        for(IEmpleado empleado : empleados.values()) {
            if(empleado.estaLibre()) {
                cantEmpleadosLibres++;
            }
        }
        Object[] empleadosNoAsignados = new Object[cantEmpleadosLibres];

        for(int i = 0; i < cantEmpleadosLibres; i++) {
            empleadosNoAsignados[i] = empleadosLibres.get(i).obtenerLegajo();
        }
        return empleadosNoAsignados;
	}

	@Override
	public boolean estaFinalizado(Integer numero) {
		return proyectos.get(numero).obtenerEstado().equals(Estado.finalizado);
	}

	@Override
	public int consultarCantidadRetrasosEmpleado(Integer legajo) {
        IEmpleado empleadoSeleccionado = null;

        for(IEmpleado empleado: empleados.values()) {
            Integer legajoBuscado = empleado.obtenerLegajo();
            if(legajoBuscado.equals(legajo)) {
                empleadoSeleccionado = empleado;
            }
        }

        return empleadoSeleccionado != null ? empleadoSeleccionado.obtenerRetrasos() : 0;
        // String nombre = obtenerEmpleadoPorLegajo(legajo);
		// return empleados.get(nombre).obtenerRetrasos();
	}

	@Override
	public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {
		Proyecto proyecto = proyectos.get(numero);
        List<Tupla<Integer, String>> lista = new ArrayList<>();

        for(IEmpleado empleado : proyecto.ObtenerHistorialDeEmpleados()){
            Tupla<Integer, String> datosEmpleado = new Tupla<Integer,String>(empleado.obtenerLegajo(), empleado.obtenerNombre());
            lista.add(datosEmpleado);
        }
		return lista;
	}

	@Override
	public Object[] tareasProyectoNoAsignadas(Integer numero) {
		Proyecto proyecto = proyectos.get(numero);
        if(proyecto.obtenerEstado() == "FINALIZADO") {
            throw new IllegalArgumentException("El proyecto ya esta finalizado");
        }
        List<Tarea> tareas = proyecto.obtenerTareas();
        List<Object> tareasDeProyecto = new ArrayList<>();

        for(Tarea tarea : tareas) {
            if(tarea.obtenerEmpleado() == null) {
                tareasDeProyecto.add(tarea);
            }
        }

        Object[] tareasArray = new Object[tareasDeProyecto.size()];

        for (int i = 0; i < tareasDeProyecto.size(); i++) {
            tareasArray[i] = tareasDeProyecto.get(i);
        }

		return tareasArray;
	}

	@Override
	public Object[] tareasDeUnProyecto(Integer numero) {
		Proyecto proyecto = proyectos.get(numero);
        List listaTareas = proyecto.obtenerTareas();
        Object[] tareasDeProyecto = new Object[listaTareas.size()];

        for(int i = 0; i < listaTareas.size(); i++) {
            tareasDeProyecto[i] = listaTareas.get(i);
        }

        return tareasDeProyecto;
	}

	@Override
	public String consultarDomicilioProyecto(Integer numero) {
		return proyectos.get(numero).obtenerDomicilio();
	}

	@Override
	public boolean tieneRestrasos(Integer legajo) {
		for (IEmpleado empleado : empleados.values()) {
            if (empleado.obtenerLegajo().equals(legajo)) {
                return empleado.obtenerRetrasos() > 0;
            }
        }
        throw new RuntimeException("Empleado con legajo " + legajo + " no encontrado");
	}

	@Override
	public List<Tupla<Integer, String>> empleados() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();

        for(IEmpleado empleado: empleados.values()) {
            Integer legajo = empleado.obtenerLegajo();
            String nombre = empleado.obtenerNombre();
            lista.add(new Tupla<>(legajo, nombre));
        }
        return lista;
	}

	@Override
	public String consultarProyecto(Integer numero) {
		return proyectos.get(numero).mostrarInfo();
	}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("empleados:\n");
        for (IEmpleado e : empleados.values()) {
            sb.append(" ").append(e).append("\n");
        }
        sb.append(", \nproyectos\n");
        for (Proyecto p : proyectos.values()) {
            sb.append(" ").append(p).append("\n");
        }
        return sb.toString();
    }

    private void agregarEmpleadoLibre(IEmpleado empleado) {
        if(empleado.estaLibre() && !empleadosLibres.contains(empleado)) {
            empleadosLibres.add(empleado);
        }
    }
}
